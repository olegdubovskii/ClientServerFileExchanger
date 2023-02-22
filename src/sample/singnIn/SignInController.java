package sample.singnIn;

import client.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;


import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class SignInController {
    @FXML
    private AnchorPane apMain;

    @FXML
    private TextField tbAdress;

    @FXML
    private TextField tbName;

    @FXML
    private TextField tbPort;

    @FXML
    private Button btnGet;

    @FXML
    public TextArea taLogs;

    @FXML
    public ListView<String> lvClients;

    @FXML
    private Button btnDiscon;

    @FXML
    private Button btnStart;

    @FXML
    private Button btnSend;

    @FXML
    public ListView<String> lvFiles;

    private Client client;
    public FileChooser fileChooser;

    @FXML
    private void initialize() {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
    }

    public void btnStartOnClickMetod() throws IOException {
        if (!(tbAdress.getText().equals("") || tbName.getText().equals("") || tbPort.getText().equals(""))) {
            if (client == null) {
                try {
                    client = new Client(tbAdress.getText(), Integer.parseInt(tbPort.getText()), tbName.getText(), this);
                    client.listenMessgage();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error! Connection refused");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Error! Client already exists");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Error! Wrong input");
        }
    }

    public void btnSendOnClickMetod() throws IOException {
        if (client != null) {
            File file = fileChooser.showOpenDialog(null);
            client.sendFile(file);
        } else {
            JOptionPane.showMessageDialog(null, "Error! Client doesn't exist");
        }
    }

    public void btnGetOnClickMetod() throws IOException {
        if (client != null) {
            String fileName = lvFiles.getSelectionModel().getSelectedItem();
            client.getFile(fileName);
        } else {
            JOptionPane.showMessageDialog(null, "Error! Client doesn't exist");
        }
    }

    public void btnDisconOnClickMetod() {
        if(client != null) {
            taLogs.appendText(client.nickName + " disconnected" + "\n");
            lvFiles.getItems().clear();
            lvClients.getItems().clear();
            client.closeEverythingForClient();
            client = null;
        } else {
            JOptionPane.showMessageDialog(null, "Error! Client doesn't exist");
        }
    }
}
