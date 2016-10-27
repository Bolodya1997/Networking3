package ru.nsu.fit.bolodya.lab3;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ByteBufferImproved {

    private ByteBuffer byteBuffer;

    public ByteBufferImproved(int size) {
        byteBuffer = ByteBuffer.allocate(size);
    }

    public ByteBuffer asByteBuffer() {
        return byteBuffer;
    }

    public byte[] getByteArray(int length) {
        byte [] buffer = Arrays.copyOfRange(
                byteBuffer.array(),
                byteBuffer.position(),
                byteBuffer.position() + length);
        byteBuffer.position(byteBuffer.position() + length);

        return buffer;
    }
}
