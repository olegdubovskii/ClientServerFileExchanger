package client;

import javafx.application.Platform;
import sample.singnIn.SignInController;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    private static final String relativePath = "C:\\Users\\Oleg\\Desktop\\files\\";

    private final Socket clientSocket;
    private final DataOutputStream out;
    private final DataInputStream in;
    public final String nickName;
    private final SignInController fxmlCntllr;

    public Client(String host, int port, String nickName, SignInController fxmlCntllr) throws IOException {
        this.clientSocket = new Socket(host, port);
        this.nickName = nickName;
        this.out = new DataOutputStream(clientSocket.getOutputStream());
        this.in = new DataInputStream(clientSocket.getInputStream());
        this.fxmlCntllr = fxmlCntllr;
    }

    public void sendFile(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
        byte[] fileNameBytes = file.getName().getBytes(StandardCharsets.UTF_8);
        byte[] fileContentBytes = new byte[(int) file.length()];
        fileInputStream.read(fileContentBytes);

        String typeOfOperation = "put";
        out.writeInt(typeOfOperation.length());
        out.write(typeOfOperation.getBytes(StandardCharsets.UTF_8));

        out.writeInt(nickName.length());
        out.write(nickName.getBytes(StandardCharsets.UTF_8));

        out.writeInt(fileNameBytes.length);
        out.write(fileNameBytes);

        out.writeInt(fileContentBytes.length);
        out.write(fileContentBytes);

        out.flush();
    }

    public void getFile(String fileName) throws IOException {

        String typeOfOperation = "get";
        out.writeInt(typeOfOperation.length());
        out.write(typeOfOperation.getBytes(StandardCharsets.UTF_8));

        out.writeInt(nickName.length());
        out.write(nickName.getBytes(StandardCharsets.UTF_8));

        out.writeInt(fileName.length());
        out.write(fileName.getBytes(StandardCharsets.UTF_8));

        out.flush();
    }


    public void listenMessgage() {
        new Thread(() -> {
            try {
                out.writeInt(nickName.getBytes(StandardCharsets.UTF_8).length);
                out.write(nickName.getBytes(StandardCharsets.UTF_8));
                out.flush();
            } catch (IOException e) {
                closeEverythingForClient();
            }
            while (clientSocket.isConnected()) {
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

                    switch (opType) {
                        case "listUpdate": {
                            Platform.runLater(() -> {
                                fxmlCntllr.lvFiles.getItems().add(fileName);
                                fxmlCntllr.taLogs.appendText(userName + " loaded " + fileName + "\n");
                            });
                            break;
                        }
                        case "getRequest": {
                            int fileContentLength = in.readInt();
                            byte[] fileContentBytes = new byte[fileContentLength];
                            in.readFully(fileContentBytes);
                            File downloadedFile = new File(relativePath + fileName);
                            FileOutputStream fileOutputStream = new FileOutputStream(downloadedFile);
                            fileOutputStream.write(fileContentBytes);
                            fileOutputStream.flush();
                            Platform.runLater(() -> {
                                fxmlCntllr.lvFiles.getItems().remove(fileName);
                                fxmlCntllr.taLogs.appendText(userName + " downloaded " + fileName + "\n");
                            });
                            break;
                        }
                        case "remove": {
                            Platform.runLater(() -> {
                                fxmlCntllr.lvFiles.getItems().remove(fileName);
                                fxmlCntllr.taLogs.appendText(userName + " downloaded " + fileName + "\n");
                            });
                            break;
                        }
                        case "connect": {
                            Platform.runLater(() -> {
                                fxmlCntllr.taLogs.appendText(fileName + "\n");
                                fxmlCntllr.lvClients.getItems().add(userName);
                            });
                            break;
                        }
                        case "disconnect": {
                            Platform.runLater(() -> {
                                fxmlCntllr.taLogs.appendText(fileName + "\n");
                                fxmlCntllr.lvClients.getItems().remove(userName);
                            });
                            break;
                        }
                        case "getFile": {
                            Platform.runLater(() -> {
                                fxmlCntllr.lvFiles.getItems().add(fileName);
                            });
                            break;
                        }
                        case "getClient": {
                            Platform.runLater(() -> {
                                fxmlCntllr.lvClients.getItems().add(userName);
                            });
                            break;
                        }
                    }
                } catch (IOException e) {
                    closeEverythingForClient();
                }
            }
        }).start();
    }

    public void closeEverythingForClient() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
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
}
