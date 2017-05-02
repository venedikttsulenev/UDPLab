import java.io.IOException;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            final String hw = "Hello world!";
            DatagramPacket packet = new DatagramPacket(hw.getBytes(), hw.length(), new InetSocketAddress("localhost", 5000));
            socket.send(packet);
            System.out.println("Packet sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
