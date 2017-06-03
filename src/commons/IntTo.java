package commons;

import java.nio.ByteBuffer;

public class IntTo {
    public static byte[] bytes(int i) {
        byte b[] = new byte[4];
        ByteBuffer.wrap(b).putInt(i);
        return b;
    }
}
