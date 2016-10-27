package ru.nsu.fit.bolodya.lab3.Server;

import ru.nsu.fit.bolodya.lab3.ByteBufferImproved;
import ru.nsu.fit.bolodya.lab3.FileData;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static ru.nsu.fit.bolodya.lab3.Protocol.*;

class Connection implements Closeable {

    private enum States {
        INIT,
        READ,
        WRITE_SUCCESS,
        WRITE_AGAIN,
        ERROR
    }

    private Server server;
    private SelectionKey key;

    private SocketChannel socket;
    private ByteBufferImproved buffer = new ByteBufferImproved(
            MESSAGE_SIZE +
            Integer.BYTES +
            MD5_BYTES);

    private States state = States.INIT;
    private boolean paused = false;

    private FileChannel file;
    private FileData fileData;

    Connection(SelectionKey key, Server server){
        this.key = key;
        this.server = server;

        socket = (SocketChannel) key.channel();
    }

    private boolean isMessageReady() {
        int count = buffer.asByteBuffer().position() - Integer.BYTES;
        if (count < 0)
            return false;

        buffer.asByteBuffer().flip();
        int length = buffer.asByteBuffer().getInt();
        if (count < length) {
            buffer.asByteBuffer().position(0);
            buffer.asByteBuffer().compact();
            return false;
        }

        buffer.asByteBuffer().compact().flip();
        return true;
    }

    private boolean isMessageCorrect() {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ignored) {}

        byte digestReceived[] = buffer.getByteArray(MD5_BYTES);
        digest.update(buffer.asByteBuffer());

        if (!Arrays.equals(digestReceived, digest.digest()))
            return false;

        buffer.asByteBuffer().position(MD5_BYTES);
        buffer.asByteBuffer().compact().flip();
        return true;
    }

    /*
     *  Head structure:
     *      int fileNameLength;
     *      String fileName;
     *      long fileLength;
     */
    private void readHead() {
        int fileNameLength = buffer.asByteBuffer().getInt();

        state = States.ERROR;
        file = null;
        String preName = new String(buffer.getByteArray(fileNameLength), Charset.forName("UTF-8"));
        if (preName.contains("/"))
            return;

        String fileName = "uploads/" + preName;
        long fileLength = buffer.asByteBuffer().getLong();

        /* if file exists, return ERROR to client */
        boolean flag = true;
        try (FileChannel file = new RandomAccessFile(new File(fileName), "r").getChannel()) {
        } catch (FileNotFoundException e) {
            flag = false;
        }
        catch (Exception ignored) {}
        if (flag)
            return;

        try {
            file = new RandomAccessFile(new File(fileName), "rw").getChannel();
        } catch (Exception e) {
            return;
        }
        fileData = new FileData(fileName, fileLength, fileLength);
        state = States.INIT;
    }

    private void readMessage() {
        try {
            file.write(buffer.asByteBuffer());
        } catch (IOException ignored) {}
        fileData.setRemain(fileData.getRemain() - buffer.asByteBuffer().position());

        if (fileData.getRemain() == 0) {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            file = null;
        }
    }

    /*
     *  Message structure:
     *      int messageLength;
     *      byte[MD5_BYTES] digest;
     *      byte[] message;
     */
    void read() {
        try {
            if (socket.read(buffer.asByteBuffer()) < 0) {
                close();
                return;
            }
        } catch (IOException e) {
            close();
            return;
        }

        if (!isMessageReady())
            return;

        if (!isMessageCorrect()) {
            buffer.asByteBuffer().clear();

            state = States.WRITE_AGAIN;
            key.interestOps(SelectionKey.OP_WRITE);

            return;
        }

        if (state == States.INIT)
            readHead();
        else
            readMessage();

        buffer.asByteBuffer().clear();
        if (state != States.ERROR)
            state = States.WRITE_SUCCESS;
        key.interestOps(SelectionKey.OP_WRITE);
    }

    void write() {
        ByteBuffer answerBuffer = ByteBuffer.allocate(1);
        try {
            switch (state) {
                case ERROR:
                    socket.write((ByteBuffer) answerBuffer.put(ERROR).flip());
                    break;
                case WRITE_SUCCESS:
                    socket.write((ByteBuffer) answerBuffer.put(SUCCESS).flip());
                    break;
                case WRITE_AGAIN:
                    socket.write((ByteBuffer) answerBuffer.put(AGAIN).flip());
            }
        } catch (IOException e) {
            close();
            return;
        }

        if (file == null)
            state = States.INIT;
        else
            state = States.READ;

        key.interestOps(SelectionKey.OP_READ);
    }

    @Override
    public void close() {
        server.deleteConnection(socket);

        key.interestOps(0);
        key.cancel();

        try {
            socket.close();

            if (file != null) {
                file.close();
                new File(fileData.getName()).delete();
            }
        } catch (IOException ignored) {}
    }
}