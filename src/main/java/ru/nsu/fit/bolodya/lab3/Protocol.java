package ru.nsu.fit.bolodya.lab3;

public interface Protocol {

    byte SUCCESS = 0x00;
    byte AGAIN   = 0x01;
    byte ERROR   = 0x02;

    int MESSAGE_SIZE = 1024 * 1024;

    int MD5_BYTES = 16;
}
