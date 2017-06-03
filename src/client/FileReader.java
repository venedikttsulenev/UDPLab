package client;

import commons.CyclicBuffer;
import commons.PartOfFilePackage;

import java.io.*;
import java.util.Arrays;

public class FileReader {
    public FileReader(FileInputStream input, CyclicBuffer<PartOfFilePackage> buffer, int blockSize, FileConsumer fileConsumer) {
        new Thread(() -> {
            try {
                int packNum = -1;
                byte bytes[] = new byte[blockSize];
                int bytesRead;
                while (-1 != (bytesRead = input.read(bytes))) {
                    PartOfFilePackage pack = new PartOfFilePackage(
                            ++packNum,
                            bytesRead == blockSize ? bytes : Arrays.copyOfRange(bytes, 0, bytesRead)
                    );
                    buffer.put(pack);
                }
                if (fileConsumer != null)
                    fileConsumer.onEOFReached();
                System.out.println("FileReader finished");
            } catch (IOException e) {
                System.out.println("I/O error happened while reading file '" + e.getMessage() + '\'');
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
