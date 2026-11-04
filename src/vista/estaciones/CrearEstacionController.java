package vista.estaciones;

import enums.TipoVehiculo;
import estaciones.Estacion;
import excepciones.ReglaNegocioException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import vehiculos.Vehiculo;
import vista.Formato;
import vista.Pantalla;

/**
 * FXML Controller class
 *
 * @author holmian
 */
public class CrearEstacionController extends Pantalla implements Initializable {

    // Formulario de estacion
    @FXML
    private TextField txtCodigo;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtLatitud;
    @FXML
    private TextField txtLongitud;
    @FXML
    private TextField txtCapBicicleta;
    @FXML
    private TextField txtCapBiciElectrica;
    @FXML
    private TextField txtCapMonopatin;
    @FXML
    private Label lblMensajeEstacion;

    // Lista de estaciones: un texto por estacion; en estacionesMostradas estan las estaciones en el mismo orden
    @FXML
    private ListView<String> lstEstaciones;
    private List<Estacion> estacionesMostradas = new ArrayList<>();

    // Vehiculos de la estacion seleccionada
    @FXML
    private Label lblVehiculosDe;
    @FXML
    private ListView<String> lstVehiculos;
    @FXML
    private ComboBox<TipoVehiculo> cmbTipoVehiculo;
    @FXML
    private TextField txtCodigoVehiculo;
    @FXML
    private Label lblMensajeVehiculo;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbTipoVehiculo.setItems(FXCollections.observableArrayList(TipoVehiculo.values()));
        cmbTipoVehiculo.setConverter(new StringConverter<TipoVehiculo>() {
            @Override
            public String toString(TipoVehiculo tipo) {
                return tipo == null ? "" : Formato.nombreTipo(tipo);
            }

            @Override
            public TipoVehiculo fromString(String texto) {
                return null;
            }
        });

        // Al seleccionar una estacion se muestran sus vehiculos
        lstEstaciones.getSelectionModel().selectedIndexProperty().addListener((obs, anterior, nuevo) -> mostrarVehiculos(estacionElegida()));

        cargarEstaciones();
    }

    // Un renglon por estacion: "E01 - Av. Corrientes 1234  |  Bicis 3/10  ·  Bici eléc. 1/5  ·  Monopatines 0/5  |  Ocupación 20%"
    private void cargarEstaciones() {
        lstEstaciones.getItems().clear();
        estacionesMostradas = new ArrayList<>(getControladorEstaciones().getEstaciones().values());
        estacionesMostradas.sort(Comparator.comparing(Estacion::getCodigo));
        for (Estacion estacion : estacionesMostradas) {
            lstEstaciones.getItems().add(estacion.getCodigo() + " - " + estacion.getDireccion()
                    + "  |  Bicis " + ocupacionTipo(estacion, TipoVehiculo.BICICLETA_COMUN)
                    + "  ·  Bici eléc. " + ocupacionTipo(estacion, TipoVehiculo.BICICLETA_ELECTRICA)
                    + "  ·  Monopatines " + ocupacionTipo(estacion, TipoVehiculo.MONOPATIN_ELECTRICO)
                    + "  |  Ocupación " + Math.round(estacion.getOcupacion() * 100) + "%");
        }
    }

    // Vuelve a armar la lista y deja seleccionada la estacion indicada
    private void cargarEstacionesYSeleccionar(Estacion estacion) {
        cargarEstaciones();
        lstEstaciones.getSelectionModel().select(estacionesMostradas.indexOf(estacion));
    }

    // Devuelve la estacion del renglon seleccionado, o null si no hay ninguna elegida
    private Estacion estacionElegida() {
        int indice = lstEstaciones.getSelectionModel().getSelectedIndex();
        if (indice < 0) {
            return null;
        }
        return estacionesMostradas.get(indice);
    }

    // Un renglon por vehiculo: "M001  -  Monopatín eléctrico  -  DISPONIBLE  -  Batería 100%"
    private void mostrarVehiculos(Estacion estacion) {
        lstVehiculos.getItems().clear();
        if (estacion == null) {
            lblVehiculosDe.setText("Vehículos de la estación");
            return;
        }
        lblVehiculosDe.setText("Vehículos de " + estacion.getCodigo() + " - " + estacion.getDireccion());
        for (Vehiculo vehiculo : estacion.getVehiculos()) {
            lstVehiculos.getItems().add(Formato.vehiculo(vehiculo));
        }
    }

    @FXML
    void crearEstacion(ActionEvent event) {
        String codigo = txtCodigo.getText().trim();
        String direccion = txtDireccion.getText().trim();
        if (codigo.isEmpty() || direccion.isEmpty()) {
            mostrarError(lblMensajeEstacion, "Completá el código y la dirección");
            return;
        }
        try {
            double latitud = leerDecimal(txtLatitud, "latitud");
            double longitud = leerDecimal(txtLongitud, "longitud");

            Map<TipoVehiculo, Integer> capacidades = new HashMap<>();
            capacidades.put(TipoVehiculo.BICICLETA_COMUN, leerEntero(txtCapBicicleta, "bicicletas"));
            capacidades.put(TipoVehiculo.BICICLETA_ELECTRICA, leerEntero(txtCapBiciElectrica, "bici eléctricas"));
            capacidades.put(TipoVehiculo.MONOPATIN_ELECTRICO, leerEntero(txtCapMonopatin, "monopatines"));

            Estacion nueva = getControladorEstaciones().registrarEstacion(codigo, direccion, latitud, longitud, capacidades);
            mostrarExito(lblMensajeEstacion, "Estación " + codigo + " creada");
            limpiarFormulario();
            cargarEstacionesYSeleccionar(nueva);
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensajeEstacion, e.getMessage());
        }
    }

    @FXML
    void agregarVehiculo(ActionEvent event) {
        Estacion estacion = estacionElegida();
        TipoVehiculo tipo = cmbTipoVehiculo.getValue();
        String codigo = txtCodigoVehiculo.getText().trim();
        if (estacion == null) {
            mostrarError(lblMensajeVehiculo, "Seleccioná una estación de la lista");
            return;
        }
        if (tipo == null) {
            mostrarError(lblMensajeVehiculo, "Elegí el tipo de vehículo");
            return;
        }
        if (codigo.isEmpty()) {
            mostrarError(lblMensajeVehiculo, "Ingresá el código del vehículo");
            return;
        }
        try {
            // Sin datos extra: el controlador usa los valores por defecto (rodado, bateria, etc.)
            getControladorAdministracion().altaVehiculo(tipo, codigo, null, estacion.getCodigo());
            mostrarExito(lblMensajeVehiculo, Formato.nombreTipo(tipo) + " " + codigo + " agregado a " + estacion.getCodigo());
            txtCodigoVehiculo.clear();
            cargarEstacionesYSeleccionar(estacion);
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensajeVehiculo, e.getMessage());
        }
    }

    private double leerDecimal(TextField campo, String nombre) throws ReglaNegocioException {
        try {
            return Double.parseDouble(campo.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("La " + nombre + " tiene que ser un número");
        }
    }

    private int leerEntero(TextField campo, String nombre) throws ReglaNegocioException {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("La capacidad de " + nombre + " tiene que ser un número entero");
        }
    }

    private void limpiarFormulario() {
        txtCodigo.clear();
        txtDireccion.clear();
        txtLatitud.clear();
        txtLongitud.clear();
        txtCapBicicleta.clear();
        txtCapBiciElectrica.clear();
        txtCapMonopatin.clear();
    }

    private String ocupacionTipo(Estacion estacion, TipoVehiculo tipo) {
        return estacion.cantidadVehiculosPorTipo(tipo) + " / " + estacion.getCapacidadPorTipo().getOrDefault(tipo, 0);
    }

    // Solo el administrador entra a estaciones: volver lo lleva a su panel
    @Override
    public void volver(ActionEvent event) {
        irA("administracion/PanelAdministrador.fxml");
    }
}
