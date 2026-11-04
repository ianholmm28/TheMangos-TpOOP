package vista.usuarios;

import excepciones.ReglaNegocioException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import vista.Pantalla;
import vista.Sesion;

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
        String documento = txtDocumento.getText().trim();
        String nombre = txtNombre.getText().trim();
        String email = txtEmail.getText().trim();
        try {
            getControladorUsuarios().registrarUsuario(nombre, documento, email);
            Sesion.iniciar(documento);
            irA("general/PantallaPrincipal.fxml");
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensaje, e.getMessage());
        }
    }

    @FXML
    void irAIniciarSesion(ActionEvent event) {
        irA("usuarios/IniciarSesion.fxml");
    }

}
