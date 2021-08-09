package sample;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Controller implements Initializable {
    final String dir = System.getProperty("user.dir");
    File fileObject;


    @FXML
    private Button Choose;

    @FXML
    private Button Create;


    @FXML
    private TextField fileName;

    @FXML
    private Label fileMessage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private void CreateFile(ActionEvent event) throws IOException {
        if (fileName.getText().isEmpty()){
            fileMessage.setText("Write file name");
        }
        else {
            File file = new File(dir + "/data/" + fileName.getText() + ".txt");
            if (!file.createNewFile()){
                fileMessage.setText("File already exists");
                return;
            }
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "position_switch.fxml"
                    )
            );
            Parent root = loader.load();
            Position_Chart_Controller controller = loader.getController();
            controller.setParent_controller(this);
            controller.setFileObject(file);
            Stage stage = new Stage();
            stage.setTitle("Line_chart");
            stage.setScene(new Scene(root));
            stage.show();

        }


    }


    @FXML
    private void chooseFile(ActionEvent event) throws IOException {
        System.out.println("current dir = " + dir);
        Node source = (Node)event.getSource();
        Stage primaryStage = (Stage) source.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter txtFilter= new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(txtFilter);
        fileChooser.getExtensionFilters().addAll(txtFilter);
        fileChooser.setTitle("Выбор файла");

        fileChooser.setInitialDirectory(new File(dir + "/data"));
        fileObject = fileChooser.showOpenDialog(primaryStage);
        if (fileObject == null)
            return;
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "save.fxml"
                )
        );
        Parent root = loader.load();
        Saved_Chart_Controller controller = loader.getController();
        controller.setParent_controller(this);
        controller.setFileObject(fileObject);
        Stage stage = new Stage();
        stage.setTitle("Line_chart");
        stage.setScene(new Scene(root));
        stage.show();


    }



    public void setFileMessage(String fileMessage) {
        this.fileMessage.setText(fileMessage);
    }
}
