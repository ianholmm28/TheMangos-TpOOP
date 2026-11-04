package vista.general;

import estaciones.Estacion;
import excepciones.ReglaNegocioException;
import excepciones.UsuarioNoEncontradoException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import usuarios.Usuario;
import vehiculos.Vehiculo;
import viajes.PuntoRecorrido;
import viajes.Viaje;
import vista.Formato;
import vista.Pantalla;
import vista.Sesion;

/**
 * FXML Controller class
 *
 * @author holmian
 */
public class DetalleViajeController extends Pantalla implements Initializable {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Estacion de destino elegida al iniciar el viaje, para tenerla preseleccionada
    private static String codigoDestinoElegido;

    private Viaje viaje;

    @FXML
    private Label lblBienvenida;
    @FXML
    private Label lblVehiculo;
    @FXML
    private Label lblDetalle;
    @FXML
    private ComboBox<Estacion> cmbDestino;
    @FXML
    private Label lblLugares;
    @FXML
    private Label lblMensaje;
    @FXML
    private Button btnDevolver;
    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnAgregarPunto;

    @FXML
    private Label lblDistancia;
    @FXML
    private ListView<String> lstPuntos;
    @FXML
    private TextField txtLatitud;
    @FXML
    private TextField txtLongitud;
    @FXML
    private Label lblMensajePunto;

    @FXML
    private TextField txtFalla;
    @FXML
    private Button btnReportarFalla;
    @FXML
    private Label lblMensajeFalla;

    public static void setDestinoElegido(String codigoEstacion) {
        codigoDestinoElegido = codigoEstacion;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbDestino.setConverter(new StringConverter<Estacion>() {
            @Override
            public String toString(Estacion estacion) {
                return Formato.estacion(estacion);
            }

            @Override
            public Estacion fromString(String texto) {
                return null;
            }
        });
        cmbDestino.valueProperty().addListener((obs, anterior, nueva) -> actualizarLugares());

        cargarDatos();
    }

    private void cargarDatos() {
        if (!Sesion.hayUsuario()) {
            lblBienvenida.setText("Sin sesión iniciada");
            return;
        }
        try {
            Usuario usuario = getControladorUsuarios().buscarUsuario(Sesion.getDocumento());
            lblBienvenida.setText("Hola, " + usuario.getNombre());
            viaje = usuario.getViajeEnCurso();
            if (viaje == null) {
                lblVehiculo.setText("No tenés un viaje en curso");
                deshabilitarAcciones();
                return;
            }

            Vehiculo vehiculo = viaje.getVehiculo();
            lblVehiculo.setText(Formato.nombreTipo(vehiculo.getTipo()) + " " + vehiculo.getCodigo());
            lblDetalle.setText("Saliste de " + viaje.getOrigen().getCodigo() + " - " + viaje.getOrigen().getDireccion()
                    + "\nInicio: " + viaje.getInicio().format(FORMATO_FECHA));

            List<Estacion> estaciones = new ArrayList<>(getControladorEstaciones().getEstaciones().values());
            estaciones.sort(Comparator.comparing(Estacion::getCodigo));
            cmbDestino.setItems(FXCollections.observableArrayList(estaciones));
            for (Estacion estacion : estaciones) {
                if (estacion.getCodigo().equals(codigoDestinoElegido)) {
                    cmbDestino.setValue(estacion);
                }
            }
            actualizarLugares();
            actualizarPuntos();
        } catch (UsuarioNoEncontradoException e) {
            lblBienvenida.setText("Usuario no encontrado");
        }
    }

    private void actualizarLugares() {
        if (viaje != null) {
            lblLugares.setText(Formato.lugaresLibres(cmbDestino.getValue(), viaje.getVehiculo()));
        }
    }

    private void actualizarPuntos() {
        // Un renglon por punto: "1.  14:10:37  -  Lat -34.6037, Lon -58.3816"
        lstPuntos.getItems().clear();
        int numero = 1;
        for (PuntoRecorrido punto : viaje.getPuntos()) {
            lstPuntos.getItems().add(numero + ".  " + punto.getHora().format(FORMATO_HORA)
                    + "  -  Lat " + punto.getLatitud() + ", Lon " + punto.getLongitud());
            numero++;
        }
        lblDistancia.setText(String.format("Distancia recorrida: %.2f km", viaje.getDistancia()));
    }

    // Agrega un punto del recorrido (una ubicacion cualquiera, no una estacion)
    @FXML
    void agregarPunto(ActionEvent event) {
        try {
            double latitud = leerCoordenada(txtLatitud, "latitud", 90);
            double longitud = leerCoordenada(txtLongitud, "longitud", 180);
            getControladorViajes().registrarPunto(Sesion.getDocumento(), latitud, longitud);
            txtLatitud.clear();
            txtLongitud.clear();
            actualizarPuntos();
            mostrarExito(lblMensajePunto, "Punto agregado");
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensajePunto, e.getMessage());
        }
    }

    private double leerCoordenada(TextField campo, String nombre, double maximo) throws ReglaNegocioException {
        double valor;
        try {
            valor = Double.parseDouble(campo.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("La " + nombre + " tiene que ser un número");
        }
        if (valor < -maximo || valor > maximo) {
            throw new ReglaNegocioException("La " + nombre + " tiene que estar entre -" + (int) maximo + " y " + (int) maximo);
        }
        return valor;
    }

    // CU13: el usuario reporta una falla del vehiculo que esta usando
    @FXML
    void reportarFalla(ActionEvent event) {
        try {
            getControladorMantenimiento().reportarFalla(viaje.getVehiculo().getCodigo(), txtFalla.getText().trim());
            mostrarExito(lblMensajeFalla, "Gracias, la falla quedó reportada");
            txtFalla.clear();
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensajeFalla, e.getMessage());
        }
    }

    @FXML
    void finalizarViaje(ActionEvent event) {
        Estacion destino = cmbDestino.getValue();
        if (destino == null) {
            mostrarError(lblMensaje, "Elegí la estación donde dejás el vehículo");
            return;
        }
        try {
            Viaje cerrado = getControladorViajes().devolverVehiculo(Sesion.getDocumento(), destino.getCodigo());
            codigoDestinoElegido = null;
            mostrarExito(lblMensaje, "Viaje finalizado en " + destino.getCodigo()
                    + ". Importe: " + String.format("$ %,.2f", cerrado.getImporte()));
            deshabilitarAcciones();
        } catch (ReglaNegocioException e) {
            // Si la estacion esta llena el usuario puede elegir otra
            mostrarError(lblMensaje, e.getMessage());
            actualizarLugares();
        }
    }

    @FXML
    void cancelarViaje(ActionEvent event) {
        try {
            getControladorViajes().cancelarRetiro(Sesion.getDocumento());
            codigoDestinoElegido = null;
            mostrarExito(lblMensaje, "Viaje cancelado, el vehículo volvió a la estación");
            deshabilitarAcciones();
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensaje, e.getMessage());
        }
    }

    // Una vez cerrado o cancelado el viaje solo queda volver al inicio
    private void deshabilitarAcciones() {
        cmbDestino.setDisable(true);
        txtLatitud.setDisable(true);
        txtLongitud.setDisable(true);
        btnDevolver.setDisable(true);
        btnCancelar.setDisable(true);
        btnAgregarPunto.setDisable(true);
        txtFalla.setDisable(true);
        btnReportarFalla.setDisable(true);
    }
}
