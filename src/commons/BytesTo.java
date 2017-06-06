package commons;

import java.nio.ByteBuffer;

public class BytesTo {
    public static int integer(byte b[]) {
        return ByteBuffer.wrap(b, 0, 4).getInt();
    }
}
