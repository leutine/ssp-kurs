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
    private String filename;

    /**
     * для общения с клиентом необходим сокет (адресные данные)
     */

    public ServerThread(Socket socket) throws IOException {
        this.socket = socket;
        // если потоку ввода/вывода приведут к генерированию искдючения, оно проброситься дальше
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        start(); // вызываем run()
    }
    @Override
    public void run() {
        try {
            // первое сообщение отправленное сюда - это никнейм
            this.filename = in.readLine();
            this.filesize = Integer.parseInt(in.readLine());
            System.out.println("Filename: " + filename);
            System.out.println("Size: " + filesize);
//            try {
//                out.write(filename + "\n");
//                out.flush(); // flush() нужен для выталкивания оставшихся данных
//                // если такие есть, и очистки потока для дьнейших нужд
//            } catch (IOException ignored)
            try {
                while (true) {
//                    word = in.readLine();
//                    if(word.equals("stop")) {
//                        this.downService(); // харакири
//                        break; // если пришла пустая строка - выходим из цикла прослушки
//                    }
//                    System.out.println("Echoing: " + word);
                    saveFile(socket);
//                    for (ServerThread vr : Server.serverList) {
//                        vr.send(filename); // отослать принятое сообщение с привязанного клиента всем остальным влючая его
//                    }
                }
            } catch (NullPointerException ignored) {}
        } catch (IOException e) {
            this.downService();
        }
    }

    private void saveFile(Socket clientSocket) throws IOException {
        String newPath = path + socket.getInetAddress().toString().replace("/", "") + File.separator;

        File directory = new File(newPath);
        if (! directory.exists()){
            directory.mkdirs();
        }

        DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
        FileOutputStream fos = new FileOutputStream(newPath + filename);
        byte[] buffer = new byte[4096];

        // Send file size in separate msg
        int read = 0;
        int totalRead = 0;
        int remaining = filesize;
        while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
//            totalRead += read;
            remaining -= read;
//            System.out.println("read " + totalRead + " bytes.");
            fos.write(buffer, 0, read);
        }
        System.out.println("Created file: " + newPath + filename);
        fos.close();
        dis.close();
    }

    /**
     * отсылка одного сообщения клиенту по указанному потоку
     * @param msg
     */
    private void send(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {}

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