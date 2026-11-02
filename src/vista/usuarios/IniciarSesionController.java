package vista.usuarios;

import excepciones.UsuarioNoEncontradoException;
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
public class IniciarSesionController extends Pantalla implements Initializable {

    @FXML
    private TextField txtDocumento;
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
    void ingresar(ActionEvent event) {
        String documento = txtDocumento.getText().trim();
        if (documento.isEmpty()) {
            mostrarMensaje("Ingresá tu documento");
            return;
        }
        try {
            getControladorUsuarios().buscarUsuario(documento); 
            Sesion.iniciar(documento);
            Main.mostrarPantalla("general/PantallaPrincipal.fxml");
        } catch (UsuarioNoEncontradoException e) {
            mostrarMensaje("No existe un usuario con ese documento");
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
    void irARegistro(ActionEvent event) {
        try {
            Main.mostrarPantalla("usuarios/RegistrarUsuario.fxml");
        } catch (IOException e) {
            System.out.println("No se pudo abrir la pantalla: " + e.getMessage());
        }
    }

    @FXML
    void irAEstaciones(ActionEvent event) {
        try {
            Main.mostrarPantalla("estaciones/CrearEstacion.fxml");
        } catch (IOException e) {
            System.out.println("No se pudo abrir la pantalla: " + e.getMessage());
        }
    }
}
