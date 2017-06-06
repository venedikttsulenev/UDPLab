package client;

import commons.*;
import commons.Package;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

public class FileSender implements AcknowledgementReceiver.Listener, Runnable {
    private final Object slidingWindowLock = new Object();
    private final DatagramSocket socket;
    private final InetSocketAddress receiverAddress;
    private final Timer timer;
    private final CyclicBuffer<PartOfFilePackage> slidingWindow;
    private final int packSize;
    private final long timeout;
    private final File file;
    private final int totalPackages;
    private final AcknowledgementReceiver acknowledgementReceiver;
    private final FileReader fileReader;
    private final Channel<byte[]> inputChannel;
    private volatile int packagesDelivered;

    public FileSender(File file, int bufferSize, int windowSize, int packSize, DatagramSocket socket, InetSocketAddress receiverAddress, long timeout) throws IOException {
        this.socket = socket;
        this.receiverAddress = receiverAddress;
        this.slidingWindow = new CyclicBuffer<>(new PartOfFilePackage[windowSize + 1]);
        this.timer = new Timer();
        this.packSize = packSize;
        this.timeout = timeout;
        this.file = file;
        this.totalPackages = (int) (file.length() - 1) / packSize + 1;
        this.acknowledgementReceiver = new AcknowledgementReceiver(socket, this);
        this.inputChannel = new Channel<>(bufferSize / packSize);
        this.fileReader = new FileReader(new FileInputStream(file), packSize, inputChannel);
    }

    private class PurgeTimerTask extends TimerTask {
        private final Timer timer;

        private PurgeTimerTask(Timer timer) {
            this.timer = timer;
        }

        @Override
        public void run() {
            timer.purge();
        }
    }

    private class ResendPacketTask extends TimerTask {
        private final Package pack;
        private final DatagramPacket packet;

        private ResendPacketTask(Package pack, DatagramPacket packet) {
            this.pack = pack;
            this.packet = packet;
        }

        @Override
        public void run() {
            if (pack.isDelivered())
                this.cancel();
            else {
                Logger.getInstance().packageTimedOut(pack.getPackageNumber());
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput oo = new ObjectOutputStream(bos);
            InitialPackage initialPackage = new InitialPackage(file.getName(), file.length(), packSize);
            oo.writeObject(initialPackage);
            oo.flush();
            DatagramPacket initialPacket = new DatagramPacket(bos.toByteArray(), bos.toByteArray().length, receiverAddress);
            DatagramPacket response = new DatagramPacket(new byte[4], 4);
            socket.send(initialPacket);
            timer.schedule(new ResendPacketTask(initialPackage, initialPacket), timeout, timeout);
            boolean initialPackDelivered = false;
            while (!initialPackDelivered) {
                socket.receive(response);
                int id = BytesTo.integer(response.getData());
                initialPackDelivered = (id == InitialPackage.ID);
            }
            initialPackage.onDeliver();
            Logger.getInstance().packageDelivered(InitialPackage.ID);
        } catch (IOException e) {
            e.printStackTrace();
        }

        timer.schedule(new PurgeTimerTask(timer), 300, 300); /* Remove cancelled tasks every 300 ms */
        packagesDelivered = 0;
        new Thread(acknowledgementReceiver).start();
        new Thread(fileReader).start();

        int packagesSent = 0;
        try {
            int packNum = -1;
            while (packagesSent < totalPackages) {
                synchronized (slidingWindowLock) {
                    for (int i = slidingWindow.size(); packagesSent < totalPackages && i < slidingWindow.capacity(); ++i) {
                        try {
                            PartOfFilePackage p = new PartOfFilePackage(++packNum, inputChannel.take());
                            slidingWindow.add(p);
                            byte[] packBytes = p.getSerialized();
                            DatagramPacket packet = new DatagramPacket(packBytes, packBytes.length, receiverAddress);
                            socket.send(packet);
                            timer.schedule(new ResendPacketTask(p, packet), timeout, timeout);
                            ++packagesSent;
                            Logger.getInstance().packageSent(packNum);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            Logger.getInstance().allPackagesSent();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Logger.getInstance().fileSenderFinished();
    }

    @Override
    public void onAcknowledgementReceived(int packageNumber) {
        synchronized (slidingWindowLock) {
            for (PartOfFilePackage p : slidingWindow)
                if (packageNumber == p.getPackageNumber()) {
                    p.onDeliver();
                    ++packagesDelivered;
                    Logger.getInstance().packageDelivered(packageNumber);

                    /* Remove delivered packages from head of buffer */
                    PartOfFilePackage pack = slidingWindow.peek();
                    while (pack != null && pack.isDelivered()) {
                        slidingWindow.poll();
                        pack = slidingWindow.peek();
                    }
                    if (packagesDelivered == totalPackages) {
                        timer.cancel();
                        timer.purge();
                        acknowledgementReceiver.stop();
                        Logger.getInstance().allPackagesDelivered();
                    }
                    return;
                }
        }
    }
}
