package vista.usuarios;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import vista.Main;
import vista.Pantalla;

/**
 * FXML Controller class
 *
 * @author holmian
 */
public class RegistrarUsuarioController extends Pantalla implements Initializable {

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtDocumento;
    @FXML
    private TextField txtEmail;
    @FXML
    private Label lblMensaje;

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
        // Por ahora pasa directo a la pantalla principal, sin validar datos
        try {
            Main.mostrarPantalla("principal/PantallaPrincipal.fxml");
        } catch (IOException e) {
            System.out.println("No se pudo abrir la pantalla: " + e.getMessage());
        }
    }

    @FXML
    void irAIniciarSesion(ActionEvent event) {
        try {
            Main.mostrarPantalla("usuarios/IniciarSesion.fxml");
        } catch (IOException e) {
            System.out.println("No se pudo abrir la pantalla: " + e.getMessage());
        }
    }

}
