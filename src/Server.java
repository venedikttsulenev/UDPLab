import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {
    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(5000);
            DatagramPacket packet = new DatagramPacket(new byte[256], 256);
            socket.receive(packet);
            System.out.println("Received:" + new String(packet.getData()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
