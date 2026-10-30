package vista.usuarios;

import excepciones.ReglaNegocioException;
import excepciones.UsuarioNoEncontradoException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import usuarios.MedioPago;
import usuarios.Usuario;
import vista.Main;
import vista.Pantalla;
import vista.Sesion;

/**
 * FXML Controller class
 *
 * @author holmian
 */
public class RecargarSaldoController extends Pantalla implements Initializable {

    @FXML
    private Label lblBienvenida;
    @FXML
    private Label lblSaldo;
    @FXML
    private ComboBox<MedioPago> cmbMedioPago;
    @FXML
    private TextField txtMonto;
    @FXML
    private Label lblMensaje;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Como se muestra cada medio de pago en el desplegable: "TARJETA 1234"
        cmbMedioPago.setConverter(new StringConverter<MedioPago>() {
            @Override
            public String toString(MedioPago medio) {
                return medio == null ? "" : medio.getTipo() + " " + medio.getIdentificador();
            }

            @Override
            public MedioPago fromString(String texto) {
                return null;
            }
        });
        cargarDatos();
    }

    // Muestra el saludo, el saldo y los medios de pago del usuario
    private void cargarDatos() {
        if (!Sesion.hayUsuario()) {
            lblBienvenida.setText("Sin sesión iniciada");
            return;
        }
        try {
            Usuario usuario = getControladorUsuarios().buscarUsuario(Sesion.getDocumento());
            lblBienvenida.setText("Hola, " + usuario.getNombre());
            lblSaldo.setText(formatearPlata(usuario.getCuenta().getSaldo()));

            MedioPago seleccionado = cmbMedioPago.getValue();
            cmbMedioPago.setItems(FXCollections.observableArrayList(usuario.getMediosPago()));
            if (seleccionado != null && usuario.getMediosPago().contains(seleccionado)) {
                cmbMedioPago.setValue(seleccionado);
            } else if (!usuario.getMediosPago().isEmpty()) {
                cmbMedioPago.setValue(usuario.getMediosPago().get(0));
            }
        } catch (UsuarioNoEncontradoException e) {
            lblBienvenida.setText("Usuario no encontrado");
        }
    }

    @FXML
    void recargarSaldo(ActionEvent event) {
        MedioPago medio = cmbMedioPago.getValue();
        if (medio == null) {
            mostrarMensaje("Elegí un medio de pago", true);
            return;
        }
        try {
            double monto = Double.parseDouble(txtMonto.getText().trim().replace(",", "."));
            getControladorUsuarios().recargarSaldo(Sesion.getDocumento(), medio.getTipo(), medio.getIdentificador(), monto);
            mostrarMensaje("Se cargaron " + formatearPlata(monto) + " con " + medio.getTipo() + " " + medio.getIdentificador(), false);
            txtMonto.clear();
            cargarDatos();
        } catch (NumberFormatException e) {
            mostrarMensaje("Ingresá un monto válido, por ejemplo 5000", true);
        } catch (ReglaNegocioException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    // Botones de monto rapido: el valor esta en el userData del boton
    @FXML
    void montoRapido(ActionEvent event) {
        txtMonto.setText(((Node) event.getSource()).getUserData().toString());
    }

    @FXML
    void volver(ActionEvent event) {
        try {
            Main.mostrarPantalla("principal/PantallaPrincipal.fxml");
        } catch (IOException e) {
            System.out.println("No se pudo abrir la pantalla: " + e.getMessage());
        }
    }

    @FXML
    void cerrarSesion(ActionEvent event) {
        Sesion.cerrar();
        try {
            Main.mostrarPantalla("usuarios/IniciarSesion.fxml");
        } catch (IOException e) {
            System.out.println("No se pudo abrir la pantalla: " + e.getMessage());
        }
    }

    private void mostrarMensaje(String texto, boolean esError) {
        lblMensaje.setText(texto);
        lblMensaje.getStyleClass().removeAll("mensaje-error", "mensaje-exito");
        lblMensaje.getStyleClass().add(esError ? "mensaje-error" : "mensaje-exito");
        lblMensaje.setManaged(true);
        lblMensaje.setVisible(true);
    }

    private String formatearPlata(double monto) {
        return String.format("$ %,.2f", monto);
    }
}
