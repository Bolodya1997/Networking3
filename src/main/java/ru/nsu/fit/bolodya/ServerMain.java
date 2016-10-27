package ru.nsu.fit.bolodya;

import ru.nsu.fit.bolodya.lab3.Server.Server;

import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) {
        try {
            new Server(2049);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
