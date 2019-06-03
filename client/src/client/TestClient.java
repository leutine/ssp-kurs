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
    private String filename; // имя клиента
    private Date time;
    private String dtime;
    private SimpleDateFormat dt1;

    private final String path = "C:\\ssp6\\client\\";

    /**
     * для создания необходимо принять адрес и номер порта
     */

    public ClientThread(String addr, int port) {
        this.addr = addr;
        this.port = port;
        try {
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
            new FileOperations().send();
//            new ReadMsg().start(); // нить читающая сообщения из сокета в бесконечном цикле
//            new WriteMsg().start(); // нить пишущая сообщения в сокет приходящие с консоли в бесконечном цикле
        } catch (IOException e) {
            // Сокет должен быть закрыт при любой
            // ошибке, кроме ошибки конструктора сокета:
            ClientThread.this.downService();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // В противном случае сокет будет закрыт
        // в методе run() нити.
    }

    public class FileOperations extends Thread {
        @Override
        public void run() {
//            String str;
            try {
                while (true) {
//                    str = in.readLine(); // ждем сообщения с сервера
//                    if (str.equals("stop")) {
//                        client.ClientThread.this.downService(); // харакири
//                        break; // выходим из цикла если пришло "stop"
//                    }
                    send(); // пишем сообщение с сервера на консоль
                }
            } catch (IOException e) {
                ClientThread.this.downService();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void send() throws IOException, InterruptedException {
            filename = inputFilename();

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            FileInputStream fis = new FileInputStream(path + filename);
            byte[] buffer = new byte[4096];

            while (fis.read(buffer) > 0) {
                dos.write(buffer);
            }

            fis.close();
            dos.close();
        }
    }

    /**
     * просьба ввести имя,
     * и отсылка эхо с приветсвием на сервер
     */

    private String inputFilename() throws InterruptedException {
        System.out.print("Input filename: ");
        try {
            filename = inputUser.readLine();

            File file = new File(path + filename);
            int size = (int) file.length();

            out.write(filename + "\n");
            out.write(size + "\n");
            out.flush();
//            Thread.sleep(1000);
        } catch (IOException ignored) {
        }
        return filename;

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

    // нить чтения сообщений с сервера
    private class ReadMsg extends Thread {
        @Override
        public void run() {

            String str;
            try {
                while (true) {
                    str = in.readLine(); // ждем сообщения с сервера
                    if (str.equals("stop")) {
                        ClientThread.this.downService(); // харакири
                        break; // выходим из цикла если пришло "stop"
                    }
                    System.out.println(str); // пишем сообщение с сервера на консоль
                }
            } catch (IOException e) {
                ClientThread.this.downService();
            }
        }
    }

    // нить отправляющая сообщения приходящие с консоли на сервер
    public class WriteMsg extends Thread {

        @Override
        public void run() {
            while (true) {
                String userWord;
                try {
                    time = new Date(); // текущая дата
                    dt1 = new SimpleDateFormat("HH:mm:ss"); // берем только время до секунд
                    dtime = dt1.format(time); // время
                    userWord = inputUser.readLine(); // сообщения с консоли
                    if (userWord.equals("stop")) {
                        out.write("stop" + "\n");
                        ClientThread.this.downService(); // харакири
                        break; // выходим из цикла если пришло "stop"
                    } else {
                        out.write("(" + dtime + ") " + filename + ": " + userWord + "\n"); // отправляем на сервер
                    }
                    out.flush(); // чистим
                } catch (IOException e) {
                    ClientThread.this.downService(); // в случае исключения тоже харакири

                }

            }
        }
    }
}

public class TestClient {

    public static String ipAddr = "192.168.1.6";
    public static int port = 8080;

    /**
     * создание клиент-соединения с узананными адресом и номером порта
     */

    public static void main(String[] args) {
        new ClientThread(ipAddr, port);
    }
}