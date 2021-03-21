package ba.unsa.etf.rpr;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PretragaController implements Initializable {

    public ListView<String> listView;
    public TextField textField;
    private ObservableList<String> apsPutanje;
    public ArrayList<String> putanje;
    private Thread thread;

    public void pretragaDirektorija(ActionEvent actionEvent) {
        putanje.clear();
        apsPutanje.setAll(putanje);
        thread = new Thread(this::trazi);
        thread.start();
    }

    private void trazi() {
        if(textField.getText().isEmpty()) {
            textField.setText("");
        }
        pronadjiDirektorij(textField.getText(), new File(System.getProperty("user.home")));
    }

    private void pronadjiDirektorij(String text, File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    pronadjiDirektorij(text, f);
                } else if (f.getAbsolutePath().toLowerCase().contains(text.toLowerCase())) {
                    Platform.runLater(() -> apsPutanje.add(f.getAbsolutePath()));
                }
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        thread = new Thread();
        putanje = new ArrayList<>();
        apsPutanje = FXCollections.observableArrayList(putanje);
        listView.setItems(apsPutanje);
    }


}
