package commons;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class PartOfFilePackage {
    public static final int MIN_PACKAGE_SIZE = 16; /* 16 bytes */
    public static final int MAX_PACKAGE_SIZE = 16384; /* 16 KB */
    private final int packageNumber;
    private transient byte[] serialized = null;
    private transient volatile boolean sent = false;
    private transient volatile boolean delivered = false;

    private PartOfFilePackage(byte[] data) {
        this.serialized = data;
        this.packageNumber = ByteBuffer.wrap(data, 0, 4).getInt();
    }

    public PartOfFilePackage(int packageNumber, byte[] data) throws IOException {
        this.packageNumber = packageNumber;
        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
        buffer.putInt(packageNumber);
        buffer.put(data);
        this.serialized = buffer.array();
    }

    public void onSend() {
        sent = true;
    }

    public boolean isSent() {
        return sent;
    }

    public void onDeliver() {
        delivered = true;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public int getPackageNumber() {
        return packageNumber;
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

    @Override
    public boolean equals(Object obj) {
        if (null == obj)
            return false;
        if (this == obj)
            return true;
        if (!(obj instanceof PartOfFilePackage))
            return false;
        PartOfFilePackage pack = (PartOfFilePackage) obj;
        return pack.packageNumber == packageNumber;
    }

    @Override
    public int hashCode() {
        return packageNumber;
    }
}
