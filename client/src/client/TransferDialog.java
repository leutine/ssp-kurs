package client;

import com.sun.org.apache.xml.internal.security.utils.JDKXPathAPI;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class TransferDialog extends JFrame {

    private JTextField editFileName;
    private JLabel labelFile;
    private JLabel labelFilename;
    private JButton btnGenerateKey;
    private JButton btnFileSelector;
    private JButton btnListOfFiles;
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
        setPreferredSize(new Dimension(800, 200));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel sendPanel = new JPanel(new GridBagLayout());
        JPanel recievePanel = new JPanel(new GridBagLayout());
        JPanel encryptionPanel = new JPanel(new GridBagLayout());
        JPanel bottomPanel = new JPanel(new GridBagLayout());

        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        labelFile = new JLabel("Путь к файлу: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        sendPanel.add(labelFile, cs);

        labelFilename = new JLabel("");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 3;
        sendPanel.add(labelFilename, cs);

        JComboBox fileList = new JComboBox();
        fileList.setVisible(false);

        cs.gridx = 0;
        cs.gridy = 1;
        recievePanel.add(fileList, cs);

//        sendPanel.setBorder(new LineBorder(Color.GRAY));

        chbxEncrypt = new JCheckBox("Использовать шифрование");
        chbxEncrypt.setToolTipText("Выберите чекбокс, если необходимо отправить или принять зашифрованное изображение");
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

        cs.gridx = 0;
        cs.gridy = 0;
        encryptionPanel.add(chbxEncrypt, cs);

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

        cs.gridx = 0;
        cs.gridy = 1;
        encryptionPanel.add(btnGenerateKey, cs);

        btnFileSelector = new JButton("Выбрать файл");
        btnFileSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filepath = FileSelector();

                labelFilename.setText(filepath);
                labelFilename.setToolTipText(filepath);
                file = new File(filepath);

                System.out.println(filepath + " chosen.");
            }
        });

        cs.gridx = 1;
        cs.gridy = 2;
        cs.gridwidth = 1;
        sendPanel.add(btnFileSelector, cs);

        btnListOfFiles = new JButton("Получить список файлов");
        btnListOfFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileList.setVisible(true);
                System.out.println("Getting list of files from server");
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

        cs.gridx = 0;
        cs.gridy = 0;
        recievePanel.add(btnListOfFiles, cs);

        btnSend = new JButton("Отправить файл");
        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("upload!");
                try {
                    connection.sendFile(labelFilename.getText(), encrypt);
                } catch (IOException | EncryptionException e1) {
                    JOptionPane.showMessageDialog(parent,
                            "Возникла ошибка при отправке файла! Выберите существующий файл.",
                            "IOException",
                            JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }
            }
        });

        cs.gridx = 1;
        cs.gridy = 3;
//        cs.gridwidth = 2;
        sendPanel.add(btnSend, cs);

        btnRecieve = new JButton("Скачать файл");
        btnRecieve.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("download!");
                try {
                    connection.recieveFile(fileList.getItemAt(fileList.getSelectedIndex()).toString(), encrypt);
                } catch (IOException | EncryptionException e1) {
                    e1.printStackTrace();
                }
            }
        });

        cs.gridx = 0;
        cs.gridy = 2;
        recievePanel.add(btnRecieve, cs);

        btnClose = new JButton("Выход");
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        bottomPanel.add(btnClose);

        getContentPane().add(sendPanel, BorderLayout.WEST);
        getContentPane().add(recievePanel, BorderLayout.CENTER);
        getContentPane().add(encryptionPanel, BorderLayout.EAST);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

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
