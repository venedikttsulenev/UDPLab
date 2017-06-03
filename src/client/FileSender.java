package client;

import commons.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

public class FileSender implements FileConsumer {
    private volatile boolean eofReached = false;
    private final DatagramSocket socket;
    private final InetSocketAddress receiverAddress;
    private final Timer timer;

    private void sendPartOfFile(PartOfFilePackage pack) throws IOException {
        byte[] packBytes = pack.getSerialized();
        DatagramPacket packet = new DatagramPacket(packBytes, packBytes.length, receiverAddress);
        socket.send(packet);
    }

    private class ResendPacketTask extends TimerTask {
        private final PartOfFilePackage pack;

        public ResendPacketTask(PartOfFilePackage pack) {
            this.pack = pack;
        }

        @Override
        public void run() {
            if (!pack.isDelivered()) {
                try {
                    sendPartOfFile(pack);
                    System.out.println("> Timeout for package #" + pack.getPackageNumber() + ", resending");
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            } else
                this.cancel();
        }
    }

    public FileSender(File file, long bufferSize, int windowSize, int packSize, DatagramSocket socket, InetSocketAddress receiverAddress, long timeout) throws IOException {
        this.socket = socket;
        this.receiverAddress = receiverAddress;
        CyclicBuffer<PartOfFilePackage> slidingWindow = new CyclicBuffer<>(new PartOfFilePackage[windowSize + 1]);
        this.timer = new Timer();
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput oo = new ObjectOutputStream(bos);
            InitialPackage initialPackage = new InitialPackage(file.getName(), file.length(), packSize);
            int totalPackages = (int) (file.length() - 1 / packSize + 1);
            oo.writeObject(initialPackage);
            oo.flush();
            DatagramPacket response = new DatagramPacket(new byte[4], 4);
            socket.send(new DatagramPacket(bos.toByteArray(), bos.toByteArray().length, receiverAddress));
            socket.receive(response);
            boolean initialPackDelivered = (InitialPackage.ID == BytesTo.integer(response.getData()));
            while (!initialPackDelivered) {
                socket.receive(response);
                int id = BytesTo.integer(response.getData());
                initialPackDelivered = (id == InitialPackage.ID);
                System.out.println("> Initial package delivered");
            }

            new Thread(() -> { /* Acknowledgement receiving thread */
                int packagesDelivered = 0;
                while (packagesDelivered < totalPackages) {
                    try {
                        socket.receive(response);
                        int id = BytesTo.integer(response.getData());
                        if (!slidingWindow.isEmpty())
                            for (PartOfFilePackage p : slidingWindow) {
                                if (p.getPackageNumber() == id) {
                                    ++packagesDelivered;
                                    p.onDeliver();
                                    System.out.println("> Package #" + id + " delivered");
                                    break;
                                }
                            }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("File successfully transferred");
            }).start();

            new Thread(() -> { /* Packet sending thread */
                try {
                    while (!eofReached || !slidingWindow.isEmpty()) { /* When EOF is reached, buffer may be not empty */
                        PartOfFilePackage pack = slidingWindow.first();
                        while (pack.isDelivered()) {  /* Remove delivered packages from head of buffer */
                            slidingWindow.take();
                            pack = slidingWindow.first();
                        }
                        for (PartOfFilePackage p : slidingWindow) {
                            if (!p.isSent()) {
                                try {
                                    sendPartOfFile(p);
                                    p.onSend();
                                    System.out.println("> Package #" + p.getPackageNumber() + " sent");
                                    timer.schedule(new ResendPacketTask(p), timeout, timeout);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    System.out.println("Packet sending thread finished");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            CyclicBuffer<PartOfFilePackage> fileBuffer = new CyclicBuffer<>(
                    new PartOfFilePackage[(int) ((bufferSize - 1) / packSize + 1)]
            );

            new Thread(() -> { /* From buffer to window */
                try {
                    while (!eofReached || !fileBuffer.isEmpty())
                        slidingWindow.put(fileBuffer.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            new FileReader(new FileInputStream(file), fileBuffer, packSize, this); /* Starts implicitly */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEOFReached() {
        if (eofReached)
            throw new IllegalStateException("Wut?! EOF reached 2 times.");
        eofReached = true;
    }
}
