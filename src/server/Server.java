package server;

import java.net.*;

public class Server implements Runnable {
    private final DatagramSocket socket;
    private final String path;
    private final InetSocketAddress senderAddress;

    public Server(int port, String path, InetSocketAddress senderAddress) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.path = path;
        this.senderAddress = senderAddress;
    }

    @Override
    public void run() {
        new FileReceiver(socket, path, senderAddress).run();
    }

    public static void main(String[] args) {
        /* Usage: java Server <port> <hostname:port> <directory> */
        int port = Integer.parseInt(args[0]);
        String address = args[1];
        String addrSplit[] = address.split(":");
        String senderHostname = addrSplit[0];
        int senderPort = Integer.parseInt(addrSplit[1]);
        String path = args[2];
        try {
            Server server = new Server(port, path, new InetSocketAddress(senderHostname, senderPort));
            server.run();
        } catch (SocketException e) {
            System.out.println(e.getMessage());
        }
    }
}
