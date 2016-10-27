package ru.nsu.fit.bolodya.lab3.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

public class Server {
    private Selector selector;
    private HashMap<SocketChannel, Connection> clients = new HashMap<>();

    public Server(int port) throws IOException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);

        selector = Selector.open();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable())
                    acceptRoutine(key);
                else if (key.isReadable())
                        readRoutine(key);
                else if (key.isWritable())
                        writeRoutine(key);

                iterator.remove();
            }
        }
    }

    private void acceptRoutine(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel socket = serverSocket.accept();
        socket.configureBlocking(false);

        clients.put(socket, new Connection(socket.register(selector, SelectionKey.OP_READ), this));
    }

    private void readRoutine(SelectionKey key) throws IOException {
        SocketChannel socket = (SocketChannel) key.channel();
        clients.get(socket).read();
    }

    private void writeRoutine(SelectionKey key) {
        SocketChannel socket = (SocketChannel) key.channel();
        clients.get(socket).write();
    }

    void deleteConnection(SocketChannel socket) {
        clients.remove(socket);
    }
}