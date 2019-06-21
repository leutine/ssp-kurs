package client;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class TransferDialog extends JFrame {

    private JTextField editFileName;
    private JLabel labelFIleName;
    private JButton btnGenerateKey;
    private JButton btnFileSelector;
    private JButton btnListOfFiles;
    private JButton btnServerFileSelect;
    private JButton btnSend;
    private JButton btnRecieve;
    private JButton btnClose;
    private JCheckBox chbxEncrypt;

    private File file;

    private ClientThread connection;
    private boolean encrypt;


    public TransferDialog(Frame parent, ClientThread conn) {
        this.connection = conn;
        String ipAddr = conn.getIpAddr();
        int port = conn.getPort();
        setPreferredSize(new Dimension(800, 300));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        labelFIleName = new JLabel("Файл: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(labelFIleName, cs);

        editFileName = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(editFileName, cs);

        JComboBox fileList = new JComboBox();
        fileList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        JPanel dropdown = new JPanel();

        dropdown.add(fileList);

        panel.setBorder(new LineBorder(Color.GRAY));

        chbxEncrypt = new JCheckBox("Использовать шифрование");
        chbxEncrypt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chbxEncrypt.isSelected()) {
                    encrypt = true;
                    if (!IsKeyExist(new File(connection.getKeyfile()))) {
                        JOptionPane.showMessageDialog(parent,
                                "Для использования шифрования необходимо сгенерировать ключ!",
                                "No key",
                                JOptionPane.INFORMATION_MESSAGE);
                        chbxEncrypt.setSelected(false);
                    }
                } else {
                    encrypt = false;
                }
            }
        });

        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 3;
        panel.add(chbxEncrypt, cs);

        btnGenerateKey = new JButton("Сгенерировать ключ");
        btnGenerateKey.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Client.generateKey();
                    JOptionPane.showMessageDialog(parent,
                            "Ключ сгенерирован!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    System.out.println("Key generated!");
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(parent,
                            "Не удалось сгенерировать ключ!",
                            "generateKey Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnFileSelector = new JButton("Выбрать файл для загрузки на сервер");
        btnFileSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filepath = FileSelector();

                editFileName.setText(filepath);
                file = new File(filepath);

                System.out.println(filepath + " chosen.");
            }
        });

        btnServerFileSelect = new JButton("Выбрать файл для скачивания");
        btnServerFileSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editFileName.setText(fileList.getItemAt(fileList.getSelectedIndex()).toString());
            }
        });

        btnListOfFiles = new JButton("Получить список файлов");
        btnListOfFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Got list of files");
                try {
                    fileList.removeAllItems();
                    connection.getListFromServer();

                    for(int i = 0; i < conn.getListSize(); i++){
                        fileList.addItem(conn.toppings[i]);
                    }

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        btnSend = new JButton("Отправить файл");
        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("upload!");
                try {
                    connection.sendFile(editFileName.getText(), encrypt);
                } catch (IOException | EncryptionException e1) {
                    e1.printStackTrace();
                }
            }
        });

        btnRecieve = new JButton("Скачать файл");
        btnRecieve.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("download!");
                try {
                    connection.recieveFile(editFileName.getText(), encrypt);
                } catch (IOException | EncryptionException e1) {
                    e1.printStackTrace();
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
        bp.add(btnGenerateKey);
        panel.add(btnFileSelector);

        bp.add(btnListOfFiles);
        bp.add(btnSend);
        bp.add(btnRecieve);
        bp.add(btnClose);

        dropdown.add(btnServerFileSelect);

        getContentPane().add(panel, BorderLayout.NORTH);
        getContentPane().add(bp, BorderLayout.SOUTH);
        getContentPane().add(dropdown, BorderLayout.CENTER);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private static String FileSelector() {
        FileDialog dialog = new FileDialog((Frame)null, "Выбрать файл для отправки");
        dialog.setMode(FileDialog.LOAD);
        dialog.setVisible(true);
        return dialog.getDirectory() + dialog.getFile();
    }

    private static boolean IsKeyExist(File keyfile) {
        return keyfile.exists();
    }
}
