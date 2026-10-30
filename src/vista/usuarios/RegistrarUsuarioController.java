package vista.usuarios;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import vista.Pantalla;

/**
 * FXML Controller class
 *
 * @author holmian
 */
public class RegistrarUsuarioController extends Pantalla implements Initializable {

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    void registrar(ActionEvent event) {
        System.out.println("Hola, cinti le gusta la berenjena");
    }

}
