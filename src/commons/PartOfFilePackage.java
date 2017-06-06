package commons;

import java.io.*;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class PartOfFilePackage extends Package {
    public static final int MIN_PACKAGE_SIZE = 16; /* 16 bytes */
    public static final int MAX_PACKAGE_SIZE = 16384; /* 16 KB */
    private final transient byte[] serialized;

    private PartOfFilePackage(byte[] serialized) {
        super(BytesTo.integer(serialized));
        this.serialized = serialized;
    }

    public PartOfFilePackage(int packageNumber, byte[] data) throws IOException {
        super(packageNumber);
        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
        buffer.putInt(packageNumber);
        buffer.put(data);
        this.serialized = buffer.array();
    }

    public byte[] getData() {
        return Arrays.copyOfRange(serialized, 4, serialized.length);
    }

    public byte[] getSerialized() throws IOException {
        return serialized;
    }

    public static PartOfFilePackage getDeserialized(byte[] bytes, int from, int to) throws IOException {
        return new PartOfFilePackage(Arrays.copyOfRange(bytes, from, to));
    }
}
