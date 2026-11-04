package vista.general;

import excepciones.UsuarioNoEncontradoException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import usuarios.Movimiento;
import usuarios.Usuario;
import viajes.Viaje;
import vista.Formato;
import vista.Pantalla;
import vista.Sesion;

/**
 * FXML Controller class
 *
 * @author holmian
 */
public class PantallaPrincipalController extends Pantalla implements Initializable {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private Label lblBienvenida;
    @FXML
    private Label lblSaldo;
    @FXML
    private Label lblEstadoViaje;
    @FXML
    private Button btnDetalleViaje;
    @FXML
    private Button btnIniciarViaje;

    // Cada lista muestra un texto por renglon
    @FXML
    private ListView<String> lstViajes;
    @FXML
    private ListView<String> lstMovimientos;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (Sesion.hayUsuario()) {
            cargarDatos(Sesion.getDocumento());
        }
    }

    private void cargarDatos(String documento) {
        try {
            Usuario usuario = getControladorUsuarios().buscarUsuario(documento);
            lblBienvenida.setText("Hola, " + usuario.getNombre());
            lblSaldo.setText(Formato.plata(usuario.getCuenta().getSaldo()));

            // Si hay un viaje en curso se puede ver su detalle, si no se puede iniciar uno nuevo
            Viaje enCurso = usuario.getViajeEnCurso();
            btnDetalleViaje.setDisable(enCurso == null);
            btnIniciarViaje.setDisable(enCurso != null);
            if (enCurso == null) {
                lblEstadoViaje.setText("No tenés ningún viaje en curso");
            } else {
                lblEstadoViaje.setText("Estás viajando con el vehículo " + enCurso.getVehiculo().getCodigo());
            }

            // Un renglon por movimiento: "01/11/2026 14:10 - Recarga con TARJETA - $ 5.000,00"
            lstMovimientos.getItems().clear();
            for (Movimiento movimiento : getControladorUsuarios().consultarMovimientos(documento)) {
                lstMovimientos.getItems().add(movimiento.getFecha().format(FORMATO_FECHA) + "  -  "
                        + movimiento.getConcepto() + "  -  " + Formato.plata(movimiento.getImporte()));
            }

            // Un renglon por viaje: "01/11/2026 14:10 - Vehículo M001 - 12 min - $ 1.460,00"
            lstViajes.getItems().clear();
            for (Viaje viaje : getControladorViajes().getViajesUsuario(documento)) {
                lstViajes.getItems().add(viaje.getInicio().format(FORMATO_FECHA) + "  -  Vehículo "
                        + viaje.getVehiculo().getCodigo() + "  -  " + viaje.getMinutosCobrables() + " min  -  "
                        + Formato.plata(viaje.getImporte()));
            }
        } catch (UsuarioNoEncontradoException e) {
            lblBienvenida.setText("Usuario no encontrado");
        }
    }

    @FXML
    void recargarSaldo(ActionEvent event) {
        irA("usuarios/RecargarSaldo.fxml");
    }

    @FXML
    void registrarVehiculo(ActionEvent event) {
        irA("general/PantallaViaje.fxml");
    }

    @FXML
    void verDetalleViaje(ActionEvent event) {
        irA("general/DetalleViaje.fxml");
    }

    @FXML
    void irAAjustes(ActionEvent event) {
        irA("general/AjustesAplicacion.fxml");
    }
}
