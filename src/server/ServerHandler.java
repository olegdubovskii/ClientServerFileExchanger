package server;

import exeptions.SuchUserExistsException;
import network.FileInfo;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ServerHandler implements Runnable {

    private Socket connectionSocket;
    private DataInputStream in;
    private DataOutputStream out;
    public static ArrayList<FileInfo> files = new ArrayList<>();
    private String userName;

    public ServerHandler(Socket socket) throws IOException {
        try {
            this.connectionSocket = socket;
            this.in = new DataInputStream(connectionSocket.getInputStream());
            this.out = new DataOutputStream(connectionSocket.getOutputStream());

            int userNameLength = in.readInt();
            byte[] userNameBytes = new byte[userNameLength];
            in.readFully(userNameBytes);
            this.userName = new String(userNameBytes, StandardCharsets.UTF_8);
            for (ServerHandler tmp : Server.clientsList) {
                if (tmp.userName.equals(this.userName)) {
                    throw new SuchUserExistsException("Error! Such user already exists");
                }
            }
            Server.clientsList.add(this);

            String connect = userName + " connected";
            broadcastFiles(connect, "connect", userName);

            for (FileInfo fileInfo : files) {
                sendInfo("getFile", userName, fileInfo.getName(), null);
            }
            for (ServerHandler tempHandler : Server.clientsList) {
                if (tempHandler != this) {
                    sendInfo("getClient", tempHandler.userName, userName, null);
                }
            }
        } catch (SuchUserExistsException e) {
            closeEverything(connectionSocket, in, out);
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }


    @Override
    public void run() {
        while (connectionSocket.isConnected()) {
            try {
                int operationTypeLength = in.readInt();
                byte[] operationTypeBytes = new byte[operationTypeLength];
                in.readFully(operationTypeBytes);
                String opType = new String(operationTypeBytes, StandardCharsets.UTF_8);

                int userNameLength = in.readInt();
                byte[] userNameBytes = new byte[userNameLength];
                in.readFully(userNameBytes);
                String userName = new String(userNameBytes, StandardCharsets.UTF_8);

                int fileNameLength = in.readInt();
                byte[] fileNameBytes = new byte[fileNameLength];
                in.readFully(fileNameBytes);
                String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

                if (opType.equals("put")) {
                    int fileContentLength = in.readInt();
                    byte[] fileContentBytes = new byte[fileContentLength];
                    in.readFully(fileContentBytes);
                    FileInfo fileInfo = new FileInfo(fileName, fileContentBytes);
                    files.add(fileInfo);
                    broadcastFiles(fileInfo.getName(), "listUpdate", userName);
                } else if (opType.equals("get")) {
                    FileInfo fileToSend = files.stream().filter(fileInfo -> fileInfo.getName()
                            .equals(fileName))
                            .findFirst()
                            .orElseThrow(IOException::new);
                    sendInfo("getRequest", userName, null, fileToSend);
                    broadcastFiles(fileToSend.getName(), "remove", userName);
                    files.remove(fileToSend);
                }
            } catch (IOException e) {
                try {
                    closeEverything(connectionSocket, in, out);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                break;
            }
        }
    }

    private void broadcastFiles(String message, String type, String name) throws IOException {
        for (ServerHandler tmp : Server.clientsList) {
            if (!type.equals("remove")) {
                tmp.sendInfo(type, name, message, null);
            } else if (tmp != this) {
                tmp.sendInfo(type, name, message, null);
            }
        }
    }



    private void sendInfo(String operation, String name, String msg, FileInfo fileInfo) throws IOException {

        out.writeInt(operation.length());
        out.write(operation.getBytes(StandardCharsets.UTF_8));

        out.writeInt(name.length());
        out.write(name.getBytes(StandardCharsets.UTF_8));

        if (msg != null) {
            out.writeInt(msg.length());
            out.write(msg.getBytes(StandardCharsets.UTF_8));
        }

        if (fileInfo != null) {
            out.writeInt(fileInfo.getName().length());
            out.write(fileInfo.getName().getBytes(StandardCharsets.UTF_8));

            out.writeInt(fileInfo.getData().length);
            out.write(fileInfo.getData());
        }

        out.flush();
    }

    private void closeEverything(Socket socket, DataInputStream in, DataOutputStream out) throws IOException {
        removeServerHandler();
        try {
            if (socket != null) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeServerHandler() throws IOException {
        Server.clientsList.remove(this);
        broadcastFiles(userName + " disconnected", "disconnect", userName);
    }


}
