package vista.usuarios;

import excepciones.ReglaNegocioException;
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
        if (documento.isEmpty()) {
            mostrarMensaje("Ingresá tu documento");
            return;
        }
        if (nombre.isEmpty()) {
            mostrarMensaje("Ingresá tu nombre");
            return;
        }
        if (email.isEmpty()) {
            mostrarMensaje("Ingresá tu email");
            return;
        }
        try {
            getControladorUsuarios().registrarUsuario(nombre, documento, email);
            Sesion.iniciar(documento);
            Main.mostrarPantalla("general/PantallaPrincipal.fxml");
        } catch (ReglaNegocioException e) {
            mostrarMensaje(e.getMessage());
        } catch (IOException e) {
            System.out.println("No se pudo abrir la pantalla: " + e.getMessage());
        }
    }

    private void mostrarMensaje(String texto) {
        lblMensaje.setText(texto);
        lblMensaje.getStyleClass().add("mensaje-error");
        lblMensaje.setManaged(true);
        lblMensaje.setVisible(true);
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
