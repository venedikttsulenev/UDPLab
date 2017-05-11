package server;

import commons.InitialPackage;
import commons.Responses;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class Server implements Runnable {
    private final int port;
    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            byte bytes[] = new byte[512];
            DatagramSocket socket = new DatagramSocket(port);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
            socket.receive(packet);
            SocketAddress senderAddress = packet.getSocketAddress();
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInput o = new ObjectInputStream(bis);
            InitialPackage initialPackage;
            try {
                initialPackage = (InitialPackage) o.readObject();
                System.out.print(initialPackage);
                long packages = (initialPackage.getFileSize() - 1) / initialPackage.getPackageSize() + 1;
                System.out.println(" (" + packages + " packages)");
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            byte responseBytes[] = new byte[1];
            responseBytes[0] = Responses.INITIAL_PACKAGE_RECEIVED;
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, senderAddress);
            socket.send(responsePacket);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        /* Usage: java Server <port> */
        int port = Integer.parseInt(args[0]);
        Server server = new Server(port);
        server.run();
    }
}
