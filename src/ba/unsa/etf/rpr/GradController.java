package ba.unsa.etf.rpr;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class GradController {
    public TextField fieldNaziv;
    public TextField fieldBrojStanovnika;
    public TextField fldPostanskiBroj;
    public ChoiceBox<Drzava> choiceDrzava;
    public ObservableList<Drzava> listDrzave;
    private Grad grad;
    public ImageView imgView;

    public GradController(Grad grad, ArrayList<Drzava> drzave) {
        this.grad = grad;
        listDrzave = FXCollections.observableArrayList(drzave);
    }

    @FXML
    public void initialize() {
        choiceDrzava.setItems(listDrzave);
        if (grad != null) {
            fieldNaziv.setText(grad.getNaziv());
            fieldBrojStanovnika.setText(Integer.toString(grad.getBrojStanovnika()));
            // choiceDrzava.getSelectionModel().select(grad.getDrzava());
            // ovo ne radi jer grad.getDrzava() nije identički jednak objekat kao član listDrzave
            for (Drzava drzava : listDrzave)
                if (drzava.getId() == grad.getDrzava().getId())
                    choiceDrzava.getSelectionModel().select(drzava);
            fldPostanskiBroj.setText(Integer.toString(grad.getPostanskiBroj()));
            imgView.setImage(new Image("img/slika.jpg"));
        } else {
            choiceDrzava.getSelectionModel().selectFirst();
        }
    }

    public Grad getGrad() {
        return grad;
    }

    public void clickCancel(ActionEvent actionEvent) {
        grad = null;
        Stage stage = (Stage) fieldNaziv.getScene().getWindow();
        stage.close();
    }

    public void clickOk(ActionEvent actionEvent) {
        boolean sveOk = true;

        if (fieldNaziv.getText().trim().isEmpty()) {
            fieldNaziv.getStyleClass().removeAll("poljeIspravno");
            fieldNaziv.getStyleClass().add("poljeNijeIspravno");
            sveOk = false;
        } else {
            fieldNaziv.getStyleClass().removeAll("poljeNijeIspravno");
            fieldNaziv.getStyleClass().add("poljeIspravno");
        }


        int brojStanovnika = 0;
        try {
            brojStanovnika = Integer.parseInt(fieldBrojStanovnika.getText());
        } catch (NumberFormatException e) {
            // ...
        }
        if (brojStanovnika <= 0) {
            fieldBrojStanovnika.getStyleClass().removeAll("poljeIspravno");
            fieldBrojStanovnika.getStyleClass().add("poljeNijeIspravno");
            sveOk = false;
        } else {
            fieldBrojStanovnika.getStyleClass().removeAll("poljeNijeIspravno");
            fieldBrojStanovnika.getStyleClass().add("poljeIspravno");
        }

        if(!sveOk && fldPostanskiBroj.getText().isEmpty()){
                return;
            }

        if (fldPostanskiBroj.getText().isEmpty()) {
            if (grad == null) grad = new Grad();
            grad.setNaziv(fieldNaziv.getText());
            grad.setBrojStanovnika(Integer.parseInt(fieldBrojStanovnika.getText()));
            grad.setDrzava(choiceDrzava.getValue());
            Stage stage = (Stage) fieldNaziv.getScene().getWindow();
            stage.close();
        } else if (fldPostanskiBroj.getText().matches("(?=.*[a-zA-Z])[a-zA-Z0-9]+")) {
            fldPostanskiBroj.getStyleClass().removeAll("poljeIspravno");
            fldPostanskiBroj.getStyleClass().add("poljeNijeIspravno");
        } else {

            try {
                URL lokacija = new URL("http://c9.etf.unsa.ba/proba/postanskiBroj.php?postanskiBroj=" + fldPostanskiBroj.getText());
                new Thread(() -> {
                    String json = "";
                    String line = null;
                    BufferedReader ulaz = null;
                    try {
                        ulaz = new BufferedReader(new InputStreamReader(lokacija.openStream(), StandardCharsets.UTF_8));
                        while ((line = ulaz.readLine()) != null) {
                            json = json + line;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (json.equals("NOT OK")) {
                        fldPostanskiBroj.getStyleClass().removeAll("poljeIspravno");
                        fldPostanskiBroj.getStyleClass().add("poljeNijeIspravno");
                    } else {
                        Platform.runLater(() -> {
                            fldPostanskiBroj.getStyleClass().removeAll("poljeNijeIspravno");
                            fldPostanskiBroj.getStyleClass().add("poljeIspravno");
                            if(!fieldNaziv.getText().trim().isEmpty() && !fieldBrojStanovnika.getText().trim().isEmpty()) {
                                if (grad == null) grad = new Grad();
                                grad.setNaziv(fieldNaziv.getText());
                                grad.setBrojStanovnika(Integer.parseInt(fieldBrojStanovnika.getText()));
                                grad.setDrzava(choiceDrzava.getValue());
                                grad.setPostanskiBroj(Integer.parseInt(fldPostanskiBroj.getText()));
                                Stage stage = (Stage) fieldNaziv.getScene().getWindow();
                                stage.close();
                            }
                        });
                    }

                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void dodajSliku(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("PUTANJA");
        dialog.setContentText("Unesite putanju do Vase fotografije");
        dialog.showAndWait();
        if(!dialog.getEditor().getText().isEmpty()) {
            String putanja = dialog.getEditor().getText();
            Image image = new Image(putanja);
            imgView.setImage(image);
        }
    }

    public void odaberiSliku(ActionEvent actionEvent) throws IOException {
        Stage stage=new Stage();
        ResourceBundle bundle = ResourceBundle.getBundle("Translation");
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/pretraga.fxml"), bundle);
        stage.setTitle("Pretraga datoteke");
        stage.setScene(new Scene(root, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE));
        stage.show();
    }
}
