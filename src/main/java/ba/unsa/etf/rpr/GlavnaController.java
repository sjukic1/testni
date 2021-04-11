package ba.unsa.etf.rpr;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
//import net.sf.jasperreports.engine.JRException;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

public class GlavnaController {

    public TableView<Grad> tableViewGradovi;
    public TableColumn colGradId;
    public TableColumn colGradNaziv;
    public TableColumn colGradStanovnika;
    public TableColumn colGradPostanskiBroj;
    public TableColumn<Grad,String> colGradDrzava;
    private GeografijaDAO dao;
    private ObservableList<Grad> listGradovi;
    public Button btnJezik;

    public GlavnaController() {
        dao = GeografijaDAO.getInstance();
        listGradovi = FXCollections.observableArrayList(dao.gradovi());
    }

    @FXML
    public void initialize() {
        tableViewGradovi.setItems(listGradovi);
        colGradId.setCellValueFactory(new PropertyValueFactory("id"));
        colGradNaziv.setCellValueFactory(new PropertyValueFactory("naziv"));
        colGradStanovnika.setCellValueFactory(new PropertyValueFactory("brojStanovnika"));
        colGradDrzava.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDrzava().getNaziv()));
        colGradPostanskiBroj.setCellValueFactory(new PropertyValueFactory("postanskiBroj"));
    }

    public void actionDodajGrad(ActionEvent actionEvent) {
        Stage stage = new Stage();
        Parent root = null;
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("Translation");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/grad.fxml"), bundle);
            GradController gradController = new GradController(null, dao.drzave());
            loader.setController(gradController);
            root = loader.load();
            stage.setTitle("Grad");
            stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
            stage.setResizable(true);
            stage.show();

            stage.setOnHiding( event -> {
                Grad grad = gradController.getGrad();
                if (grad != null) {
                    // Ovdje ne smije doći do izuzetka jer se prozor neće zatvoriti
                    try {
                        dao.dodajGrad(grad);
                        listGradovi.setAll(dao.gradovi());
                    } catch(Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            } );
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void actionDodajDrzavu(ActionEvent actionEvent) {
        Stage stage = new Stage();
        Parent root = null;
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("Translation");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/drzava.fxml"), bundle);
            DrzavaController drzavaController = new DrzavaController(null, dao.gradovi());
            loader.setController(drzavaController);
            root = loader.load();
            stage.setTitle("Država");
            stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
            stage.setResizable(true);
            stage.show();

            stage.setOnHiding( event -> {
                Drzava drzava = drzavaController.getDrzava();
                if (drzava != null) {
                    // Ovdje ne smije doći do izuzetka, jer se prozor neće zatvoriti
                    try {
                        dao.dodajDrzavu(drzava);
                        listGradovi.setAll(dao.gradovi());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actionIzmijeniGrad(ActionEvent actionEvent) {
        Grad grad = tableViewGradovi.getSelectionModel().getSelectedItem();
        if (grad == null) return;

        Stage stage = new Stage();
        Parent root = null;
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("Translation");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/grad.fxml"), bundle);
            GradController gradController = new GradController(grad, dao.drzave());
            loader.setController(gradController);
            root = loader.load();
            stage.setTitle("Grad");
            stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
            stage.setResizable(true);
            stage.show();

            stage.setOnHiding( event -> {
                Grad noviGrad = gradController.getGrad();
                if (noviGrad != null) {
                    // Ovdje ne smije doći do izuzetka jer se prozor neće zatvoriti
                    try {
                        dao.izmijeniGrad(noviGrad);
                        listGradovi.setAll(dao.gradovi());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actionObrisiGrad(ActionEvent actionEvent) {
        Grad grad = tableViewGradovi.getSelectionModel().getSelectedItem();
        if (grad == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potvrda brisanja");
        alert.setHeaderText("Brisanje grada "+grad.getNaziv());
        alert.setContentText("Da li ste sigurni da želite obrisati grad " +grad.getNaziv()+"?");
        alert.setResizable(true);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            dao.obrisiGrad(grad);
            listGradovi.setAll(dao.gradovi());
        }
    }

    // Metoda za potrebe testova, vraća bazu u polazno stanje
    public void resetujBazu() {
        GeografijaDAO.removeInstance();
        File dbfile = new File("baza.db");
        dbfile.delete();
        dao = GeografijaDAO.getInstance();
    }

    public void actionStampanje(ActionEvent actionEvent){
       /* try {
            new GradoviReport().showReport(GeografijaDAO.getInstance().conn);
        } catch (JRException e1) {
            e1.printStackTrace();
        }*/
    }

    public void odabirJezika (ActionEvent actionEvent){
        List<String> choices = new ArrayList<>();
        choices.add("BS");
        choices.add("US");
        ChoiceDialog<String> dialog;
        dialog = new ChoiceDialog<>("BS",choices);
        dialog.setTitle("Jezik");
        dialog.setHeaderText("Odaberite jezik");
        dialog.setContentText("Odaberite jedan od ponudenih jezika");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            if (result.get().equals("BS"))
                Locale.setDefault(new Locale("bs", "BA"));
            else if (result.get().equals("US")) {
                Locale.setDefault(new Locale("en", "US"));
            }
        }

        Stage thisStage = (Stage) btnJezik.getScene().getWindow();
        thisStage.close();

        Stage stage = new Stage();
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("Translation");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/glavna.fxml"), bundle);
            GlavnaController ctrl = new GlavnaController();
            loader.setController(ctrl);
            Parent root = loader.load();
            stage.setTitle("Gradovi svijeta");
            stage.setScene(new Scene(root, 600, 400));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
