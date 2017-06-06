package server;

import commons.InitialPackage;
import commons.IntTo;
import commons.PartOfFilePackage;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.PriorityQueue;

public class FileReceiver implements Runnable {
    private final DatagramSocket socket;
    private final String path;
    private final InetSocketAddress address;

    public FileReceiver(DatagramSocket socket, String path, InetSocketAddress address) {
        this.socket = socket;
        this.path = path;
        this.address = address;
    }

    @Override
    public void run() {
        byte receivedBytes[] = new byte[4 + PartOfFilePackage.MAX_PACKAGE_SIZE];
        DatagramPacket packet = new DatagramPacket(receivedBytes, receivedBytes.length);
        try {
            socket.receive(packet);
            ByteArrayInputStream bis = new ByteArrayInputStream(receivedBytes);
            ObjectInputStream oi = new ObjectInputStream(bis);
            InitialPackage iPack = (InitialPackage) oi.readObject();
            System.out.println("Receiving file: " + iPack);
            socket.send(new DatagramPacket(IntTo.bytes(InitialPackage.ID), 4, address)); /* Send confirmation that iPack received */
            long startTime = System.currentTimeMillis();
            String filePath = path + '/' + iPack.getFileName();
            final long totalPacks = (iPack.getFileSize() - 1) / iPack.getPackageSize() + 1;
            PriorityQueue<PartOfFilePackage> packsToWrite = new PriorityQueue<>(1024, Comparator.comparing(PartOfFilePackage::getPackageNumber));
            FileOutputStream fOutput = new FileOutputStream(filePath);
            int receivedPacks = 0;
            int lastWrittenPackID = -1;
            receivedBytes = new byte[4 + iPack.getPackageSize()];
            packet = new DatagramPacket(receivedBytes, receivedBytes.length);
            while (receivedPacks < totalPacks) {
                try {
                    socket.receive(packet);
                    PartOfFilePackage pack = PartOfFilePackage.getDeserialized(packet.getData(), 0, packet.getLength());
                    if (pack.getPackageNumber() > lastWrittenPackID) {
                        packsToWrite.add(pack);
                        ++receivedPacks;
                        socket.send(new DatagramPacket(IntTo.bytes(pack.getPackageNumber()), 4, address)); /* Send confirmation */
                        while (!packsToWrite.isEmpty() && packsToWrite.peek().getPackageNumber() == lastWrittenPackID + 1) {
                            fOutput.write(packsToWrite.remove().getData());
                            ++lastWrittenPackID;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            double totalTime = (System.currentTimeMillis() - startTime) / 1000;
            System.out.format("File successfully received [%.1fs]%n", totalTime);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
