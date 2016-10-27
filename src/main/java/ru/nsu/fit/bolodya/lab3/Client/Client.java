package ru.nsu.fit.bolodya.lab3.Client;

import ru.nsu.fit.bolodya.lab3.FileData;
import ru.nsu.fit.bolodya.lab3.Protocol;
import ru.nsu.fit.bolodya.lab3.Speed;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static ru.nsu.fit.bolodya.lab3.Protocol.*;

public class Client implements Closeable {

    private SocketChannel socket;

    private ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_SIZE);
    private ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
    private ByteBuffer digestBuffer = ByteBuffer.allocate(Protocol.MD5_BYTES);

    private FileData fileData;
    private Speed speed;

    public Client(String hostName, int port) throws IOException {
        socket = SocketChannel.open(new InetSocketAddress(InetAddress.getByName(hostName), port));
    }

    void sendFile(File file) throws IOException {
        FileChannel fileChannel = new RandomAccessFile(file, "r").getChannel();

        fileData = new FileData(file.getName(), fileChannel.size(), fileChannel.size());
        speed = new Speed();

        buffer.putInt(file.getName().length());
        buffer.put(file.getName().getBytes("UTF-8"));
        buffer.putLong(fileChannel.size());
        send();

        ByteBuffer answerBuffer = ByteBuffer.allocate(1);
loop:   while (true) {
            answerBuffer.clear();
            if (socket.read(answerBuffer) < 0)
                break;

            answerBuffer.flip();
            switch (answerBuffer.get()) {
                case SUCCESS:
                    break;
                case AGAIN:
                    send();
                    continue;
                case ERROR:
                    break loop;
            }

            if (fileData.getRemain() == 0)
                break;

            buffer.clear();
            lengthBuffer.clear();

            fileChannel.read(buffer);
            send();

            fileData.setRemain(fileData.getRemain() - MESSAGE_SIZE);
        }

        buffer.clear();
        digestBuffer.clear();
        lengthBuffer.clear();

        fileChannel.close();
        fileData.setFinished(true);
    }

    private void send() throws IOException {
        int length = buffer.position() + digestBuffer.position();

        countDigest();
        socket.write((ByteBuffer) lengthBuffer.putInt(length).flip());
        socket.write((ByteBuffer) digestBuffer.flip());
        socket.write((ByteBuffer) buffer.flip());

        speed.put(length);
    }

    private void countDigest() {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update((ByteBuffer) buffer.flip());

            digestBuffer.clear();
            digestBuffer.put(digest.digest());
        } catch (NoSuchAlgorithmException ignored) {}
    }

    String getAddress() {
        try {
            return socket.getRemoteAddress().toString();
        }
        catch (IOException ignored) {}

        return null;
    }

    FileData getFileData() {
        return fileData;
    }

    Speed getSpeed() {
        return speed;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
