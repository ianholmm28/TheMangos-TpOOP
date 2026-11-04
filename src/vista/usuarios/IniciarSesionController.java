package vista.usuarios;

import excepciones.UsuarioNoEncontradoException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import personal.Administrador;
import personal.Operador;
import personal.Personal;
import personal.Tecnico;
import vista.Pantalla;
import vista.Sesion;

/**
 * FXML Controller class
 *
 * @author holmian
 */
public class IniciarSesionController extends Pantalla implements Initializable {

    // Tipo de ingreso elegido en el primer paso
    private String rol;

    @FXML
    private VBox panelRoles;
    @FXML
    private VBox panelIngreso;
    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblSubtitulo;
    @FXML
    private Label lblCampo;
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
    void elegirUsuario(ActionEvent event) {
        mostrarIngreso("Usuario", "Ingresá con tu documento para retirar un vehículo", "Documento", "Ej: 40123456");
    }

    @FXML
    void elegirOperador(ActionEvent event) {
        mostrarIngreso("Operador", "Ingresá con tu legajo de operador", "Legajo", "Ej: 1001");
    }

    @FXML
    void elegirTecnico(ActionEvent event) {
        mostrarIngreso("Técnico", "Ingresá con tu legajo de técnico", "Legajo", "Ej: 2001");
    }

    @FXML
    void elegirAdministrador(ActionEvent event) {
        mostrarIngreso("Administrador", "Ingresá con tu legajo de administrador", "Legajo", "Ej: 1");
    }

    // Pasa del paso 1 (elegir rol) al paso 2 (ingresar documento o legajo)
    private void mostrarIngreso(String rolElegido, String subtitulo, String campo, String ejemplo) {
        rol = rolElegido;
        lblTitulo.setText("Ingresar como " + rolElegido.toLowerCase());
        lblSubtitulo.setText(subtitulo);
        lblCampo.setText(campo);
        txtDocumento.clear();
        txtDocumento.setPromptText(ejemplo);
        lblMensaje.setManaged(false);
        lblMensaje.setVisible(false);
        cambiarPanel(true);
        txtDocumento.requestFocus();
    }

    @FXML
    void volverARoles(ActionEvent event) {
        cambiarPanel(false);
    }

    private void cambiarPanel(boolean ingreso) {
        panelRoles.setVisible(!ingreso);
        panelRoles.setManaged(!ingreso);
        panelIngreso.setVisible(ingreso);
        panelIngreso.setManaged(ingreso);
    }

    @FXML
    void ingresar(ActionEvent event) {
        String dato = txtDocumento.getText().trim();
        if (dato.isEmpty()) {
            mostrarError(lblMensaje, "Ingresá tu " + lblCampo.getText().toLowerCase());
            return;
        }
        if (rol.equals("Usuario")) {
            ingresarUsuario(dato);
        } else {
            ingresarPersonal(dato);
        }
    }

    private void ingresarUsuario(String documento) {
        try {
            getControladorUsuarios().buscarUsuario(documento);
            Sesion.iniciar(documento);
            irA("general/PantallaPrincipal.fxml");
        } catch (UsuarioNoEncontradoException e) {
            mostrarError(lblMensaje, "No existe un usuario con ese documento");
        }
    }

    // Busca el legajo en el personal y lo lleva al panel de su rol
    private void ingresarPersonal(String legajo) {
        Personal personal = getControladorAdministracion().getPersonal().get(legajo);
        String panel = null;
        if (rol.equals("Operador") && personal instanceof Operador) {
            panel = "operador/PanelOperador.fxml";
        } else if (rol.equals("Técnico") && personal instanceof Tecnico) {
            panel = "tecnico/PanelTecnico.fxml";
        } else if (rol.equals("Administrador") && personal instanceof Administrador) {
            panel = "administracion/PanelAdministrador.fxml";
        }
        if (panel == null) {
            mostrarError(lblMensaje, "No existe un " + rol.toLowerCase() + " con ese legajo");
            return;
        }
        Sesion.iniciarPersonal(legajo);
        irA(panel);
    }

    @FXML
    void irARegistro(ActionEvent event) {
        irA("usuarios/RegistrarUsuario.fxml");
    }
}
