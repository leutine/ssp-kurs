package client;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectionDialog extends JFrame {

    private JTextField editIpAddr;
    private JTextField editPort;
    private JLabel labelIpAddr;
    private JLabel labelPort;
    private JButton btnConnect;
    private JButton btnClose;

    private ClientThread connection;
    private boolean connected = false;


    public ConnectionDialog(Frame parent) {
        setTitle(parent.getTitle());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        labelIpAddr = new JLabel("Адрес: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(labelIpAddr, cs);

        editIpAddr = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(editIpAddr, cs);
        editIpAddr.setText("localhost");

        labelPort = new JLabel("Порт: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(labelPort, cs);

        editPort = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(editPort, cs);
        editPort.setText("8080");

        panel.setBorder(new LineBorder(Color.GRAY));

        btnConnect = new JButton("Подключиться");
        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (IsEmpty(editIpAddr) || IsEmpty(editIpAddr)) {
                    JOptionPane.showMessageDialog(parent,
                            "Введите адрес и порт сервера!",
                            "Connection Failed",
                            JOptionPane.ERROR_MESSAGE);
                }

                String ipAddr = editIpAddr.getText();
                int port = Integer.parseInt(editPort.getText());

                try {
                    connection = Client.connect(ipAddr, port);
                    connected = true;
                }
                catch (NullPointerException npe) {
                    JOptionPane.showMessageDialog(parent,
                            "Не удалось установить соединение!",
                            "Socket Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
                if (connected) {
                    setVisible(false);
                    TransferDialog transferDialog = new TransferDialog(new Frame(), connection);
                    transferDialog.setTitle(parent.getTitle());
                    transferDialog.setVisible(true);
                }
            }
        });

        btnClose = new JButton("Выход");
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JPanel bp = new JPanel();
        bp.add(btnConnect);
        bp.add(btnClose);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private static boolean IsEmpty(JTextField tf) {
        return tf.getText().matches("[/s]*");
    }
}
