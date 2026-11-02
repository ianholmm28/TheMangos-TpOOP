package vista.general;

import estaciones.Estacion;
import excepciones.ReglaNegocioException;
import excepciones.UsuarioNoEncontradoException;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import usuarios.Usuario;
import vehiculos.Vehiculo;
import viajes.PuntoRecorrido;
import viajes.Viaje;
import vista.Main;
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
    private TableView<PuntoRecorrido> tablaPuntos;
    @FXML
    private TableColumn<PuntoRecorrido, String> colNumero;
    @FXML
    private TableColumn<PuntoRecorrido, String> colHora;
    @FXML
    private TableColumn<PuntoRecorrido, String> colLatitud;
    @FXML
    private TableColumn<PuntoRecorrido, String> colLongitud;
    @FXML
    private TextField txtLatitud;
    @FXML
    private TextField txtLongitud;
    @FXML
    private Label lblMensajePunto;

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
                return estacion == null ? "" : estacion.getCodigo() + " - " + estacion.getDireccion();
            }

            @Override
            public Estacion fromString(String texto) {
                return null;
            }
        });
        cmbDestino.valueProperty().addListener((obs, anterior, nueva) -> actualizarLugares());

        colNumero.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(tablaPuntos.getItems().indexOf(c.getValue()) + 1)));
        colHora.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHora().format(FORMATO_HORA)));
        colLatitud.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getLatitud())));
        colLongitud.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getLongitud())));

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
            lblVehiculo.setText(PantallaViajeController.nombreTipo(vehiculo) + " " + vehiculo.getCodigo());
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
            lblLugares.setText(PantallaViajeController.textoLugares(cmbDestino.getValue(), viaje.getVehiculo()));
        }
    }

    private void actualizarPuntos() {
        tablaPuntos.setItems(FXCollections.observableArrayList(viaje.getPuntos()));
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
            mostrarMensaje(lblMensajePunto, "Punto agregado", true);
        } catch (ReglaNegocioException e) {
            mostrarMensaje(lblMensajePunto, e.getMessage(), false);
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

    @FXML
    void finalizarViaje(ActionEvent event) {
        Estacion destino = cmbDestino.getValue();
        if (destino == null) {
            mostrarMensaje(lblMensaje, "Elegí la estación donde dejás el vehículo", false);
            return;
        }
        try {
            Viaje cerrado = getControladorViajes().devolverVehiculo(Sesion.getDocumento(), destino.getCodigo());
            codigoDestinoElegido = null;
            mostrarMensaje(lblMensaje, "Viaje finalizado en " + destino.getCodigo()
                    + ". Importe: " + String.format("$ %,.2f", cerrado.getImporte()), true);
            deshabilitarAcciones();
        } catch (ReglaNegocioException e) {
            // Si la estacion esta llena el usuario puede elegir otra
            mostrarMensaje(lblMensaje, e.getMessage(), false);
            actualizarLugares();
        }
    }

    @FXML
    void cancelarViaje(ActionEvent event) {
        try {
            getControladorViajes().cancelarRetiro(Sesion.getDocumento());
            codigoDestinoElegido = null;
            mostrarMensaje(lblMensaje, "Viaje cancelado, el vehículo volvió a la estación", true);
            deshabilitarAcciones();
        } catch (ReglaNegocioException e) {
            mostrarMensaje(lblMensaje, e.getMessage(), false);
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
    }

    private void mostrarMensaje(Label label, String texto, boolean exito) {
        label.setText(texto);
        label.getStyleClass().removeAll("mensaje-error", "mensaje-exito");
        label.getStyleClass().add(exito ? "mensaje-exito" : "mensaje-error");
        label.setManaged(true);
        label.setVisible(true);
    }

    @FXML
    void volver(ActionEvent event) {
        try {
            Main.mostrarPantalla("general/PantallaPrincipal.fxml");
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
}
