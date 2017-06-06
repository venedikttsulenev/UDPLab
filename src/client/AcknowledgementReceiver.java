package client;

import commons.BytesTo;
import commons.Stoppable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.EventListener;

public class AcknowledgementReceiver implements Stoppable {
    public interface Listener extends EventListener {
        void onAcknowledgementReceived(int packageNumber);
    }

    private final DatagramSocket socket;
    private final Listener listener;
    private volatile boolean active = true;

    public AcknowledgementReceiver(DatagramSocket socket, Listener listener) {
        this.socket = socket;
        this.listener = listener;
    }

    @Override
    public void run() {
        DatagramPacket receivedPacket = new DatagramPacket(new byte[4], 4);
        while (active) {
            try {
                socket.receive(receivedPacket);
                int id = BytesTo.integer(receivedPacket.getData());
                listener.onAcknowledgementReceived(id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Logger.getInstance().acknowledgementReceiverFinished();
    }

    @Override
    public void stop() {
        active = false;
    }
}
