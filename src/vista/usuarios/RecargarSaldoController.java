package vista.usuarios;

import excepciones.ReglaNegocioException;
import excepciones.UsuarioNoEncontradoException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import usuarios.MedioPago;
import usuarios.Usuario;
import vista.Formato;
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
            lblSaldo.setText(Formato.plata(usuario.getCuenta().getSaldo()));

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
            mostrarError(lblMensaje, "Elegí un medio de pago");
            return;
        }
        try {
            double monto = Double.parseDouble(txtMonto.getText().trim().replace(",", "."));
            getControladorUsuarios().recargarSaldo(Sesion.getDocumento(), medio.getTipo(), medio.getIdentificador(), monto);
            mostrarExito(lblMensaje, "Se cargaron " + Formato.plata(monto) + " con " + medio.getTipo() + " " + medio.getIdentificador());
            txtMonto.clear();
            cargarDatos();
        } catch (NumberFormatException e) {
            mostrarError(lblMensaje, "Ingresá un monto válido, por ejemplo 5000");
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensaje, e.getMessage());
        }
    }
}
