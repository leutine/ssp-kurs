package server;

import java.io.*;
import java.net.*;


class ServerThread extends Thread {

    private Socket socket; // сокет, через который сервер общается с клиентом,
    // кроме него - клиент и сервер никак не связаны
    private DataInputStream in; // поток чтения из сокета
    private DataOutputStream out; // поток завписи в сокет
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
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        this.clientpath = path + socket.getInetAddress().toString().replace("/", "") + File.separator;
    }

    @Override
    public void run() {
        try {
            try {
                String command;
                while (true) {
                    command = in.readUTF();
                    
                    if (command.equalsIgnoreCase("uploading")) {
                        System.out.println("Downloading file from client");
                        getFileInfoFromClient();
                        recieveFile();
                    } else if (command.equalsIgnoreCase("downloading")) {
                        System.out.println("Uploading file to client");
                        prepareClient();
                        sendFile();
                    } else if (command.equalsIgnoreCase("listing")) {
                        System.out.println("Uploading list to client");
                        sendList();
                    }
                }
            } catch (NullPointerException ignored) {}
        } catch (IOException e) {
            System.out.println("Down Service!");
            this.downService();
        }
    }

    private void sendFile() throws IOException {
        FileInputStream fis = new FileInputStream(clientpath + filename);

        byte[] buffer = new byte[4096];

        int read = 0;
        while ((read = fis.read(buffer)) > 0) {
            out.write(buffer, 0, read);
        }

        System.out.println("Sent file: " + filename);
        fis.close();
        
        out.flush();
    }

    private void recieveFile() throws IOException {
        File directory = new File(clientpath);
        if (!directory.exists()){
            directory.mkdirs();
        }
        
        FileOutputStream fos = new FileOutputStream(clientpath + filename);
        byte[] buffer = new byte[4096];

        // Send file size in separate msg
        int read = 0;
        int remaining = filesize;
        while((read = in.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
            remaining -= read;
            fos.write(buffer, 0, read);
        }
        System.out.println("Created file: " + clientpath + filename);
        fos.close();
    }

    private void getFileInfoFromClient() throws IOException {
        this.filename = in.readUTF();
        this.filesize = in.readInt();
    }

    private void prepareClient() throws IOException {
        this.filename = in.readUTF();
        int size = getFilesize(new File(clientpath + filename));
        out.writeInt(size);
        out.flush();
    }

    private void sendList() throws IOException {
        File folder = new File(clientpath);
        File[] listOfFiles = folder.listFiles();

        out.writeInt(listOfFiles.length);
        out.writeChars("\n");

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                //System.out.println(listOfFiles[i].getName());
                out.writeChars(listOfFiles[i].getName() + "\n");
            }
        }
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