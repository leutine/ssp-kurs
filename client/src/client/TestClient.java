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
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток чтения в сокет
    private BufferedReader inputUser; // поток чтения с консоли
    private String addr; // ip адрес клиента
    private int port; // порт соединения
    private String filename;
    private int filesize;
    private Date time;
    private String dtime;
    private SimpleDateFormat dt1;

    private final String path = "C:\\ssp6\\client\\";
    private final String keyfile = "crypto.key";
    private String key;

    /**
     * для создания необходимо принять адрес и номер порта
     */

    public ClientThread(String addr, int port) {
        this.addr = addr;
        this.port = port;
        try {
//            this.key = Encryption.readKey(Encryption.generateKey(keyfile, 16));
            this.key = Encryption.readKey(new File(keyfile));
            System.out.println(key);

//            encryptionTest();

            this.socket = new Socket(addr, port);
            System.out.println("Connected to " + socket);
        } catch (IOException e) {
            System.err.println("Socket failed");
        }
        try {
            // потоки чтения из сокета / записи в сокет, и чтения с консоли
            inputUser = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//            filename = this.inputFilename(); // перед началом необходимо спросит имя
            new FileOperations().run();
        } catch (IOException e) {
            // Сокет должен быть закрыт при любой
            // ошибке, кроме ошибки конструктора сокета:
            ClientThread.this.downService();
        }
        // В противном случае сокет будет закрыт
        // в методе run() нити.
    }

    public class FileOperations extends Thread {
        @Override
        public void run() {
//            while (true) {
                inputOperation();
//            }
        }
    }

    /**
     * просьба ввести имя,
     * и отсылка эхо с приветсвием на сервер
     */

    private void inputOperation() {
        try {
            System.out.print("Input operation.\n(d)ownload file from server or (u)pload to server?\n");
            String operation = inputUser.readLine();
            process(operation);
        } catch (IOException ignored) {
        } catch (EncryptionException e) {
            e.printStackTrace();
        }
    }

    private void process(String operation) throws IOException, EncryptionException {
        if (operation.equalsIgnoreCase("u")) {
            out.write("uploading" + "\n");
            out.flush();

            System.out.print("Input filename to upload: ");
            filename = inputUser.readLine();
//            prepareServer();
            sendFile(true);
        } else if (operation.equalsIgnoreCase("d")) {
            out.write("downloading" + "\n");
            out.flush();

            System.out.print("Input filename to download: ");
            filename = inputUser.readLine();
            getFileInfoFromServer();
            recieveFile(true);
        }
    }

    private void sendFile(boolean encrypt) throws IOException, EncryptionException {
        String file = path + filename;
        if (encrypt) {
            System.out.println("Encrypting file " + file);
            String encrypted_file = path + filename + ".encrypted";
            Encryption.encrypt(key, new File(file), new File(encrypted_file));
            file = encrypted_file;
        }

        prepareServer(file);

        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];

        while (fis.read(buffer) > 0) {
            dos.write(buffer);
        }

        fis.close();
        dos.close();
    }

    private void recieveFile(boolean decrypt) throws IOException, EncryptionException {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        FileOutputStream fos = new FileOutputStream(filename);
        byte[] buffer = new byte[4096];

        // Send file size in separate msg
        int read = 0;
        int remaining = filesize;
        while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
            remaining -= read;
            fos.write(buffer, 0, read);
        }
        System.out.println("Created file: " + filename);
        fos.close();
        dis.close();

        if (decrypt) {
            System.out.println("Decrypting file " + filename);
            String decrypted_file = path + filename + ".decrypted";
            Encryption.decrypt(key, new File(filename), new File(decrypted_file));
        }
    }

    private void prepareServer(String file) throws IOException {
        int size = getFilesize(new File(file));
        out.write(filename + "\n");
        out.write(size + "\n");
        out.flush();
    }

    private void getFileInfoFromServer() throws IOException {
        out.write(filename + "\n");
        out.flush();

        this.filesize = Integer.parseInt(in.readLine());
    }

    private int getFilesize(File file) {
        return (int) file.length();
    }

    /**
     * закрытие сокета
     */
    private void downService() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch (IOException ignored) {}
    }

    public void encryptionTest() throws EncryptionException {
//        Encryption.encrypt(key, new File(path + "boat.png"), new File(path + "boat_encrypted.png"));
        Encryption.decrypt(key, new File(path + "lena.png.encrypted"), new File(path + "lena_decrypted.png"));
        System.out.println("Test Done!");
    }
}

public class TestClient {

    public static String ipAddr = "localhost";
    public static int port = 8080;

    /**
     * создание клиент-соединения с узананными адресом и номером порта
     */

    public static void main(String[] args) {
        new ClientThread(ipAddr, port);
    }
}