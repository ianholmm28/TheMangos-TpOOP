package vista.general;

import estaciones.Estacion;
import excepciones.ReglaNegocioException;
import excepciones.UsuarioNoEncontradoException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.util.StringConverter;
import usuarios.Usuario;
import vehiculos.Vehiculo;
import vista.Formato;
import vista.Pantalla;
import vista.Sesion;

/**
 * FXML Controller class
 *
 * @author holmian
 */
public class PantallaViajeController extends Pantalla implements Initializable {

    @FXML
    private Label lblBienvenida;

    @FXML
    private ComboBox<Estacion> cmbSalida;
    // La lista muestra un texto por vehiculo; en vehiculosMostrados estan los vehiculos en el mismo orden
    @FXML
    private ListView<String> lstVehiculos;
    private List<Vehiculo> vehiculosMostrados = new ArrayList<>();
    @FXML
    private Label lblSinVehiculos;
    @FXML
    private ComboBox<Estacion> cmbDestino;
    @FXML
    private Label lblLugaresDestino;
    @FXML
    private Label lblResumen;
    @FXML
    private Label lblMensaje;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarCombos();
        // Al elegir un vehiculo de la lista se actualiza el resumen
        lstVehiculos.getSelectionModel().selectedIndexProperty().addListener((obs, anterior, nuevo) -> actualizarResumen());
        cargarDatos();
    }

    // Como se muestra cada estacion en los desplegables: "E01 - Av. Corrientes 1234"
    private void configurarCombos() {
        StringConverter<Estacion> conversor = new StringConverter<Estacion>() {
            @Override
            public String toString(Estacion estacion) {
                return Formato.estacion(estacion);
            }

            @Override
            public Estacion fromString(String texto) {
                return null;
            }
        };
        cmbSalida.setConverter(conversor);
        cmbDestino.setConverter(conversor);

        // Al cambiar la salida se muestran los vehiculos de esa estacion
        cmbSalida.valueProperty().addListener((obs, anterior, nueva) -> mostrarVehiculos(nueva));
        // Al cambiar el destino o el vehiculo se actualizan los lugares libres
        cmbDestino.valueProperty().addListener((obs, anterior, nueva) -> actualizarResumen());
    }

    private void cargarDatos() {
        if (!Sesion.hayUsuario()) {
            lblBienvenida.setText("Sin sesión iniciada");
            return;
        }
        try {
            Usuario usuario = getControladorUsuarios().buscarUsuario(Sesion.getDocumento());
            lblBienvenida.setText("Hola, " + usuario.getNombre());

            List<Estacion> estaciones = new ArrayList<>(getControladorEstaciones().getEstaciones().values());
            estaciones.sort(Comparator.comparing(Estacion::getCodigo));
            cmbSalida.setItems(FXCollections.observableArrayList(estaciones));
            cmbDestino.setItems(FXCollections.observableArrayList(estaciones));
            if (estaciones.isEmpty()) {
                lblSinVehiculos.setText("Todavía no hay estaciones cargadas");
            }
        } catch (UsuarioNoEncontradoException e) {
            lblBienvenida.setText("Usuario no encontrado");
        }
    }

    // Un renglon por vehiculo: "M001  -  Monopatín eléctrico  -  DISPONIBLE  -  Batería 80%"
    private void mostrarVehiculos(Estacion estacion) {
        lstVehiculos.getItems().clear();
        vehiculosMostrados.clear();
        if (estacion == null) {
            return;
        }
        for (Vehiculo vehiculo : estacion.getVehiculos()) {
            vehiculosMostrados.add(vehiculo);
            lstVehiculos.getItems().add(Formato.vehiculo(vehiculo));
        }
        lblSinVehiculos.setText("Esta estación no tiene vehículos");
        actualizarResumen();
    }

    // Devuelve el vehiculo del renglon seleccionado, o null si no hay ninguno elegido
    private Vehiculo vehiculoElegido() {
        int indice = lstVehiculos.getSelectionModel().getSelectedIndex();
        if (indice < 0) {
            return null;
        }
        return vehiculosMostrados.get(indice);
    }

    // Texto con lo elegido y los lugares libres en el destino para ese tipo de vehiculo
    private void actualizarResumen() {
        Vehiculo vehiculo = vehiculoElegido();
        Estacion destino = cmbDestino.getValue();
        lblLugaresDestino.setText(Formato.lugaresLibres(destino, vehiculo));
        if (vehiculo == null) {
            lblResumen.setText("Seleccioná un vehículo de la lista");
        } else {
            lblResumen.setText("Vehículo elegido: " + vehiculo.getCodigo() + " (" + Formato.nombreTipo(vehiculo.getTipo()) + ")");
        }
    }

    @FXML
    void iniciarViaje(ActionEvent event) {
        Estacion salida = cmbSalida.getValue();
        Vehiculo vehiculo = vehiculoElegido();
        Estacion destino = cmbDestino.getValue();
        if (salida == null) {
            mostrarError(lblMensaje, "Elegí la estación de salida");
            return;
        }
        if (vehiculo == null) {
            mostrarError(lblMensaje, "Elegí un vehículo de la lista");
            return;
        }
        if (!vehiculo.estaDisponible()) {
            mostrarError(lblMensaje, "Ese vehículo no está disponible");
            return;
        }
        if (destino == null) {
            mostrarError(lblMensaje, "Elegí la estación de destino");
            return;
        }
        try {
            getControladorViajes().retirarVehiculo(Sesion.getDocumento(), salida.getCodigo(), vehiculo.getCodigo());
            DetalleViajeController.setDestinoElegido(destino.getCodigo());
            irA("general/DetalleViaje.fxml");
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensaje, e.getMessage());
        }
    }
}
