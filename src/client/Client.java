package client;

import commons.InitialPackage;
import commons.Responses;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class Client implements Runnable {
    private final String address;
    private final File file;
    private final int packageSize;
    private final InetSocketAddress serverAddress;

    private static final int DEFAULT_PACKAGE_SIZE = 2000;
    public Client(String address, int port, File file) {
        this(address, port, file, DEFAULT_PACKAGE_SIZE);
    }
    public Client(String address, int port, File file, int packageSize) {
        this.address = address;
        this.file = file;
        this.packageSize = packageSize;
        this.serverAddress = new InetSocketAddress("localhost", port);
    }

    @Override
    public void run() {
        InitialPackage initialPackage = new InitialPackage(file.length(), file.getName(), packageSize);

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput o = new ObjectOutputStream(bos);
             DatagramSocket socket = new DatagramSocket())
        {
            o.writeObject(initialPackage);
            o.flush();
            byte bytes[] = bos.toByteArray();
            socket.send(new DatagramPacket(bytes, bytes.length, serverAddress));
            DatagramPacket responsePacket = new DatagramPacket(new byte[1], 1);
            socket.receive(responsePacket);
            if (responsePacket.getData()[0] == Responses.INITIAL_PACKAGE_RECEIVED)
                System.out.println("Sent");
            else
                System.out.println("Something went wrong. Server may not have received packet");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        /* Usage: java Client <address> <port> <fileName> <packageSize> */
        String address = args[0];
        int port = Integer.parseInt(args[1]);
        String fileName = args[2];
        File file = new File(fileName);
        if (file.exists()) {
            int packageSize = args.length > 3 ? Integer.parseInt(args[3]) : DEFAULT_PACKAGE_SIZE;

            Client client = new Client(address, port, file);
            client.run();
        }
        else
            System.out.println("There is no such file: '" + fileName + '\'');

    }
}
