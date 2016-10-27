package ru.nsu.fit.bolodya;

import ru.nsu.fit.bolodya.lab3.Client.Client;
import ru.nsu.fit.bolodya.lab3.Client.ClientView;

import java.io.IOException;

public class ClientMain {

    public static void main(String[] args) {
        try {
            new ClientView(new Client("172.16.13.142", 2049));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
