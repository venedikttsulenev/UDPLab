package client;

import commons.PartOfFilePackage;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class Client implements Runnable {
    private static final int DEFAULT_PACKAGE_SIZE = 2048; /* 2KB */
    private static final int DEFAULT_CHANNEL_SIZE = 6; /* Sliding window width of 6 packets */
    private static final long DEFAULT_TIMEOUT = 5000; /* 5 seconds */

    private final File file;
    private final int port;
    private final int packageSize;
    private final int channelSize;
    private final long timeout;
    private final InetSocketAddress serverAddress;

    public Client(int port, String serverHostname, int serverPort, File file) {
        this(port, serverHostname, serverPort, file, DEFAULT_PACKAGE_SIZE, DEFAULT_CHANNEL_SIZE, DEFAULT_TIMEOUT);
    }

    public Client(int port, String serverHostname, int serverPort, File file, int packageSize) {
        this(port, serverHostname, serverPort, file, packageSize, DEFAULT_CHANNEL_SIZE, DEFAULT_TIMEOUT);
    }

    public Client(int port, String serverHostname, int serverPort, File file, int packageSize, int channelSize) {
        this(port, serverHostname, serverPort, file, packageSize, channelSize, DEFAULT_TIMEOUT);
    }

    public Client(int port, String serverHostname, int serverPort, File file, int packageSize, int channelSize, long timeout) {
        this.port = port;
        this.file = file;
        this.packageSize = packageSize;
        this.serverAddress = new InetSocketAddress(serverHostname, serverPort);
        this.channelSize = channelSize;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        System.out.println("Sending '" + file.getName() + "' to " + serverAddress.getHostName() + ':' + serverAddress.getPort());
        System.out.println("Package size: " + packageSize);
        System.out.println("Channel breadth: " + channelSize);
        System.out.println("Timeout: " + timeout + " ms");
        try {
            new FileSender(file, 33554432 /* 32 MB */, channelSize, packageSize, new DatagramSocket(port), serverAddress, timeout); /* Starts implicitly */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        /* Usage: java Client <port> <receiverAddress> <fileName> <packageSize> <channelSize> <timeout> */
        if (args.length < 3) {
            System.out.println("At least 3 arguments expected");
            System.out.println("    Usage: 'java Client <port> <receiver address> <file path> [package size (2048 by default)] [channel size (6 by default)] [timeout (5000 ms by default)]");
        } else {
            int port = Integer.parseInt(args[0]);
            String address = args[1];
            String addrSplit[] = address.split(":");
            String rHostname = addrSplit[0];
            int rPort = Integer.parseInt(addrSplit[1]);
            String fileName = args[2];
            File file = new File(fileName);
            Client client;
            if (file.exists()) {
                if (args.length > 3) {
                    int packageSize = Integer.parseInt(args[3]);
                    if (packageSize < PartOfFilePackage.MIN_PACKAGE_SIZE)
                        packageSize = PartOfFilePackage.MIN_PACKAGE_SIZE;
                    else if (packageSize > PartOfFilePackage.MAX_PACKAGE_SIZE)
                        packageSize = PartOfFilePackage.MAX_PACKAGE_SIZE;
                    if (args.length > 4) {
                        int channelSize = Integer.parseInt(args[4]);
                        if (args.length > 5)
                            client = new Client(port, rHostname, rPort, file, packageSize, channelSize, Long.parseLong(args[5]));
                        else
                            client = new Client(port, rHostname, rPort, file, packageSize, channelSize);
                    } else
                        client = new Client(port, rHostname, rPort, file, packageSize);
                } else
                    client = new Client(port, rHostname, rPort, file);
                client.run();
            } else
                System.out.println("There is no such file: '" + fileName + '\'');
        }
    }
}
