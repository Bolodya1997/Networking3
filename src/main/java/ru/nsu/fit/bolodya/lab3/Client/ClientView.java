package ru.nsu.fit.bolodya.lab3.Client;

import ru.nsu.fit.bolodya.lab3.FileData;
import ru.nsu.fit.bolodya.lab3.Speed;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/*
 *       _____________________________________
 *      |                             | - | X |
 *      |-------------------------------------|
 *      |                                     |
 *      |         serverIp:serverPort         |
 *      |          __________________         |
 *      |         | selectFileButton |        |
 *      |          ------------------         |
 *      |                                     |
 *       -------------------------------------
 *       _____________________________________
 *      |                             | - | X |
 *      |-------------------------------------|
 *      |                                     |
 *      |         serverIp:serverPort         |
 *      |              file name              |
 *      |   ----------progressBar----------   |
 *      |                speed                |
 *      |                                     |
 *       -------------------------------------
 */
public class ClientView extends JFrame {

    private Client client;
    private FileData fileData;
    private Speed speed;

    private JPanel selectPane = new JPanel(new GridLayout(2, 1));

    private JPanel transferPane = new JPanel(new GridLayout(4, 1));
    private JLabel fileNameLabel;
    private JProgressBar fileProgress = new JProgressBar(0, 100);
    private JLabel speedLabel;

    private Timer timer;
    private int timeout;

    private void initSelectPane() {
        JLabel addressLabel = new JLabel(client.getAddress());
        addressLabel.setHorizontalAlignment(SwingConstants.CENTER);
        selectPane.add(addressLabel);

        JButton selectButton = new JButton("Select file");
        selectButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                toTransferPane();
                new Thread(() -> {
                    try {
                        client.sendFile(chooser.getSelectedFile());
                    }
                    catch (IOException e1) {
                        timer.stop();
                        toSelectPane();
                    }
                }).start();

                while (fileData == client.getFileData());
                fileData = client.getFileData();
                fileNameLabel.setText(fileData.getName());

                while (speed == client.getSpeed());
                speed = client.getSpeed();

                /* 4 checks per second */
                timeout = 4 * 5;
                timer.start();
            }
        });
        selectPane.add(selectButton);
    }

    private void initTransferPane() {
        JLabel addressLabel = new JLabel(client.getAddress());
        addressLabel.setHorizontalAlignment(SwingConstants.CENTER);
        transferPane.add(addressLabel);

        fileNameLabel = new JLabel();
        fileNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        transferPane.add(fileNameLabel);

        fileProgress = new JProgressBar();
        transferPane.add(fileProgress);

        speedLabel = new JLabel();
        speedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        transferPane.add(speedLabel);
    }

    public ClientView(Client client) {
        super("Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(150, 150, 200, 125);
//        setResizable(false);

        this.client = client;

        timer = new Timer(250, e -> {
            speedLabel.setText(speed.getSpeed());
            fileProgress.setValue(percentage());

            if (fileData.isFinished() && fileData.getRemain() != 0)
                speedLabel.setText("ERROR IN TRANSFER");

            if (fileData.isFinished() && --timeout <= 0) {
                timer.stop();
                toSelectPane();
            }
        });

        initSelectPane();

        initTransferPane();

        toSelectPane();
        setVisible(true);
    }

    private void toSelectPane() {
        transferPane.setVisible(false);
        selectPane.setVisible(true);
        setContentPane(selectPane);
    }

    private void toTransferPane() {
        selectPane.setVisible(false);
        transferPane.setVisible(true);
        setContentPane(transferPane);
    }

    private int percentage() {
        return (int) (100 - fileData.getRemain() * 100 / fileData.getLength());
    }
}
