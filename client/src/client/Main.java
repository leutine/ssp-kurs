package client;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        ClientWindow();
    }

    public static void ClientWindow() {
        final JFrame frame = new JFrame("Курсач");

        ConnectionDialog connectionDialog = new ConnectionDialog(frame);
        connectionDialog.setVisible(true);
    }
}
