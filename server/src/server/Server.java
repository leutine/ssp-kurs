package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
    public static LinkedList<ServerThread> serverList = new LinkedList<>(); // список всех нитей - экземпляров
    // сервера, слушающих каждый своего клиента

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getProperty("port", "8080"));

        try {
            ServerSocket server = new ServerSocket(port);
            System.out.println("Server started on port " + server.getLocalPort());
            while (true) {
                // Блокируется до возникновения нового соединения:
                Socket socket = server.accept();
                System.out.println("Connection established with " + socket.getLocalSocketAddress());
                try {
                    // UPD: Для того, чтобы код работал в отдельном потоке, ОБЯЗАТЕЛЬНО нужно вызывать
                    // метод start() класса Thread
                    // Вызов метода run() из конструктора ServerThread вводил сервер в ожидание завершения функции run
                    ServerThread serverThread = new ServerThread(socket);
                    serverList.add(serverThread);
                    serverThread.start();
                } catch (IOException e) {
                    // Если завершится неудачей, закрывается сокет,
                    // в противном случае, нить закроет его:
                    socket.close();
                }
            }
        } catch (NullPointerException npe) {
            System.out.println("NPE!");
        }
    }
}