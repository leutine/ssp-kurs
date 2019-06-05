package server;

import java.io.*;
import java.net.*;


class ServerThread extends Thread {

    private Socket socket; // сокет, через который сервер общается с клиентом,
    // кроме него - клиент и сервер никак не связаны
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток завписи в сокет
    private int filesize;

    private final String path = "images" + File.separator;
    private String clientpath;
    private String filename;

    /**
     * для общения с клиентом необходим сокет (адресные данные)
     */

    public ServerThread(Socket socket) throws IOException {
        this.socket = socket;
        // если потоку ввода/вывода приведут к генерированию исключения, оно проброситься дальше
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.clientpath = path + socket.getInetAddress().toString().replace("/", "") + File.separator;
        run();
    }

    @Override
    public void run() {
        try {
            try {
                while (true) {
                    String command = in.readLine();

                    if (command.equalsIgnoreCase("uploading")) {
                        System.out.println("Downloading file from client");
                        getFileInfoFromClient();
                        recieveFile();
                    }

                    if (command.equalsIgnoreCase("downloading")) {
                        System.out.println("Uploading file to client");
                        prepareClient();
                        sendFile();
                    }
                }
            } catch (NullPointerException ignored) {}
        } catch (IOException e) {
            this.downService();
        }
    }

    private void sendFile() throws IOException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        FileInputStream fis = new FileInputStream(clientpath + filename);

        byte[] buffer = new byte[4096];

        while (fis.read(buffer) > 0) {
            dos.write(buffer);
        }
        System.out.println("Sent file: " + filename);
        fis.close();
        dos.close();
    }

    private void recieveFile() throws IOException {
        File directory = new File(clientpath);
        if (!directory.exists()){
            directory.mkdirs();
        }

        DataInputStream dis = new DataInputStream(socket.getInputStream());
        FileOutputStream fos = new FileOutputStream(clientpath + filename);
        byte[] buffer = new byte[4096];

        // Send file size in separate msg
        int read = 0;
        int remaining = filesize;
        while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
            remaining -= read;
            fos.write(buffer, 0, read);
        }
        System.out.println("Created file: " + clientpath + filename);
        fos.close();
        dis.close();
    }

    private void getFileInfoFromClient() throws IOException {
        this.filename = in.readLine();
        this.filesize = Integer.parseInt(in.readLine());
    }

    private void prepareClient() throws IOException {
        this.filename = in.readLine();
        int size = getFilesize(new File(clientpath + filename));
        out.write(size + "\n");
        out.flush();
    }

    private int getFilesize(File file) {
        return (int) file.length();
    }

    /**
     * закрытие сервера
     * прерывание себя как нити и удаление из списка нитей
     */
    private void downService() {
        try {
            if(!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
                for (ServerThread vr : Server.serverList) {
                    if(vr.equals(this)) vr.interrupt();
                    Server.serverList.remove(this);
                }
            }
        } catch (IOException ignored) {}
    }
}