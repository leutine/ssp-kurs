package client;

import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * создание клиента со всеми необходимыми утилитами, точка входа в программу в классе Client
 */

class ClientThread {

    private Socket socket;
    private DataInputStream in; // поток чтения из сокета
    private DataOutputStream out; // поток чтения в сокет
    private BufferedReader inputUser; // поток чтения с консоли
    private String filename;
    private int filesize;
    private int getting;

    private static final String path = "images_client\\";
    private static final String keyfile = "crypto.key";
    private static String key;

    String[] toppings = new String[20];

    private static String ipAddr;
    private static int port;

    /**
     * для создания необходимо принять адрес и номер порта
     */

    public ClientThread(String addr, int port) {
        try {
            this.ipAddr = addr;
            this.port = port;
            this.socket = new Socket(addr, port);
            System.out.println("Connected to " + socket);
        } catch (IOException e) {
            System.err.println("Socket failed");
        }

        try {
            // потоки чтения из сокета / записи в сокет, и чтения с консоли
            inputUser = new BufferedReader(new InputStreamReader(System.in));
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            // Сокет должен быть закрыт при любой
            // ошибке, кроме ошибки конструктора сокета:
            ClientThread.this.downService();
        }
        // В противном случае сокет будет закрыт
        // в методе run() нити.
    }

    public static void generateKey() throws IOException {
        key = Encryption.readKey(Encryption.generateKey(keyfile, 16));
        System.out.println(key);
    }

    public static void readKey() {
        try {
            key = Encryption.readKey(new File(keyfile));
            System.out.println(key);
        } catch (IOException e) {
            System.err.println("Reading keyfile failed");
        }
    }

    public void sendFile(String filename, boolean encrypt) throws IOException, EncryptionException {
        out.writeUTF("uploading");
        out.flush();

        String file = filename;
        if (encrypt) {
            System.out.println("Encrypting file " + file);

            readKey();
            String encrypted_file = filename + ".encrypted";
            Encryption.encrypt(key, new File(file), new File(encrypted_file));
            file = encrypted_file;
        }

        prepareServer(file);
        
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        
        int read = 0;
        while((read = fis.read(buffer)) > 0) {
            out.write(buffer, 0, read);
        }

        fis.close();
        
        out.flush();
    }

    public void recieveFile(String filename, boolean decrypt) throws IOException, EncryptionException {
        out.writeUTF("downloading");
        out.flush();

        this.filename = filename;

        getFileInfoFromServer();
        
        FileOutputStream fos = new FileOutputStream(filename);
        byte[] buffer = new byte[4096];

        // Send file size in separate msg
        int read = 0;
        int remaining = filesize;
        while((read = in.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
            remaining -= read;
            fos.write(buffer, 0, read);
        }
        System.out.println("Created file: " + filename);
        fos.close();

        if (decrypt) {
            System.out.println("Decrypting file " + filename);
            readKey();
            String decrypted_file = filename.replace(".encrypted", "");
            Encryption.decrypt(key, new File(filename), new File(decrypted_file));
        }
    }

    private void prepareServer(String filepath) throws IOException {
        File file = new File(filepath);
        filename = file.getName();

        int size = getFilesize(file);
        out.writeUTF(filename);
        out.writeInt(size);
        out.flush();
    }

    private void getFileInfoFromServer() throws IOException {
        out.writeUTF(filename);
        out.flush();

        this.filesize = in.readInt();
    }

    public void getListFromServer() throws IOException {
        out.writeUTF("listing");
        out.flush();

        getting = in.readInt();

        for (int i = 0; i < getting; i++) {
            toppings[i]=in.readUTF();
            System.out.println(toppings[i]);
        }
    }

    private int getFilesize(File file) {
        return (int) file.length();
    }

    /**
     * закрытие сокета
     */
    public void downService() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch (IOException ignored) {}
    }

    public String getKeyfile() {
        return keyfile;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public int getPort() {
        return port;
    }

    public int getListSize() {
        return getting;
    }
}

public class Client {

    public static String ipAddr = "localhost";
    public static int port = 8080;

    /**
     * создание клиент-соединения с узананными адресом и номером порта
     */

    public static void main(String[] args) {
    }

    public static ClientThread connect(String ipAddr, int port) {
        return new ClientThread(ipAddr, port);
    }

    public static void generateKey() throws IOException {
        ClientThread.generateKey();
    }
}