package server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {

    private final ServerSocket serverSocket;
    public static ArrayList<ServerHandler> clientsList = new ArrayList<>();

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public static void main(String[] args) throws IOException {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter port:");
            ServerSocket serverSocket = new ServerSocket(scanner.nextInt());
            Server server = new Server(serverSocket);
            server.startServer();
        } catch (BindException e) {
            System.out.println("This port is already in use, try another one");
        }
    }

    private void startServer() {
        try {
            System.out.println("Server is ready");
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Someone has connected");
                ServerHandler handler = new ServerHandler(socket);
                Thread t = new Thread(handler);
                t.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    private void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
