package client;

import commons.Channel;

import java.io.*;
import java.util.Arrays;

public class FileReader implements Runnable {
    private final FileInputStream input;
    private final int chunkSize;
    private final Channel<byte[]> channel;

    public FileReader(FileInputStream input, int chunkSize, Channel<byte[]> channel) {
        this.input = input;
        this.chunkSize = chunkSize;
        this.channel = channel;
    }

    @Override
    public void run() {
        try {
            byte bytes[] = new byte[chunkSize];
            int bytesRead;
            while (-1 != (bytesRead = input.read(bytes)))
                channel.put(Arrays.copyOf(bytes, bytesRead));
            Logger.getInstance().fileReaderFinished();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
