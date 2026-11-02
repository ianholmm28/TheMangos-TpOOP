package vista.estaciones;

import enums.TipoVehiculo;
import estaciones.Estacion;
import excepciones.ReglaNegocioException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import vehiculos.Electrico;
import vehiculos.Vehiculo;
import vista.Main;
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

    // Tabla de estaciones
    @FXML
    private TableView<Estacion> tablaEstaciones;
    @FXML
    private TableColumn<Estacion, String> colEstCodigo;
    @FXML
    private TableColumn<Estacion, String> colEstDireccion;
    @FXML
    private TableColumn<Estacion, String> colEstBicis;
    @FXML
    private TableColumn<Estacion, String> colEstBiciElec;
    @FXML
    private TableColumn<Estacion, String> colEstMonopatines;
    @FXML
    private TableColumn<Estacion, String> colEstOcupacion;

    // Vehiculos de la estacion seleccionada
    @FXML
    private Label lblVehiculosDe;
    @FXML
    private TableView<Vehiculo> tablaVehiculos;
    @FXML
    private TableColumn<Vehiculo, String> colVehCodigo;
    @FXML
    private TableColumn<Vehiculo, String> colVehTipo;
    @FXML
    private TableColumn<Vehiculo, String> colVehEstado;
    @FXML
    private TableColumn<Vehiculo, String> colVehBateria;
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
        // Columnas de estaciones: "presentes / capacidad" por tipo
        colEstCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colEstDireccion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDireccion()));
        colEstBicis.setCellValueFactory(c -> new SimpleStringProperty(ocupacionTipo(c.getValue(), TipoVehiculo.BICICLETA_COMUN)));
        colEstBiciElec.setCellValueFactory(c -> new SimpleStringProperty(ocupacionTipo(c.getValue(), TipoVehiculo.BICICLETA_ELECTRICA)));
        colEstMonopatines.setCellValueFactory(c -> new SimpleStringProperty(ocupacionTipo(c.getValue(), TipoVehiculo.MONOPATIN_ELECTRICO)));
        colEstOcupacion.setCellValueFactory(c -> new SimpleStringProperty(Math.round(c.getValue().getOcupacion() * 100) + "%"));

        colVehCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colVehTipo.setCellValueFactory(c -> new SimpleStringProperty(nombreTipo(c.getValue().getTipo())));
        colVehEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().toString()));
        colVehBateria.setCellValueFactory(c -> new SimpleStringProperty(bateria(c.getValue())));

        cmbTipoVehiculo.setItems(FXCollections.observableArrayList(TipoVehiculo.values()));
        cmbTipoVehiculo.setConverter(new StringConverter<TipoVehiculo>() {
            @Override
            public String toString(TipoVehiculo tipo) {
                return tipo == null ? "" : nombreTipo(tipo);
            }

            @Override
            public TipoVehiculo fromString(String texto) {
                return null;
            }
        });

        // Al seleccionar una estacion se muestran sus vehiculos
        tablaEstaciones.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nueva) -> mostrarVehiculos(nueva));

        cargarEstaciones();
    }

    private void cargarEstaciones() {
        Estacion seleccionada = tablaEstaciones.getSelectionModel().getSelectedItem();
        List<Estacion> estaciones = new ArrayList<>(getControladorEstaciones().getEstaciones().values());
        estaciones.sort(Comparator.comparing(Estacion::getCodigo));
        tablaEstaciones.setItems(FXCollections.observableArrayList(estaciones));
        if (seleccionada != null) {
            tablaEstaciones.getSelectionModel().select(seleccionada);
        }
        tablaEstaciones.refresh();
    }

    private void mostrarVehiculos(Estacion estacion) {
        if (estacion == null) {
            lblVehiculosDe.setText("Vehículos de la estación");
            tablaVehiculos.getItems().clear();
            return;
        }
        lblVehiculosDe.setText("Vehículos de " + estacion.getCodigo() + " - " + estacion.getDireccion());
        tablaVehiculos.setItems(FXCollections.observableArrayList(estacion.getVehiculos()));
    }

    @FXML
    void crearEstacion(ActionEvent event) {
        String codigo = txtCodigo.getText().trim();
        String direccion = txtDireccion.getText().trim();
        if (codigo.isEmpty() || direccion.isEmpty()) {
            mostrarMensaje(lblMensajeEstacion, "Completá el código y la dirección", false);
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
            mostrarMensaje(lblMensajeEstacion, "Estación " + codigo + " creada", true);
            limpiarFormulario();
            cargarEstaciones();
            tablaEstaciones.getSelectionModel().select(nueva);
        } catch (ReglaNegocioException e) {
            mostrarMensaje(lblMensajeEstacion, e.getMessage(), false);
        }
    }

    @FXML
    void agregarVehiculo(ActionEvent event) {
        Estacion estacion = tablaEstaciones.getSelectionModel().getSelectedItem();
        TipoVehiculo tipo = cmbTipoVehiculo.getValue();
        String codigo = txtCodigoVehiculo.getText().trim();
        if (estacion == null) {
            mostrarMensaje(lblMensajeVehiculo, "Seleccioná una estación de la tabla", false);
            return;
        }
        if (tipo == null) {
            mostrarMensaje(lblMensajeVehiculo, "Elegí el tipo de vehículo", false);
            return;
        }
        if (codigo.isEmpty()) {
            mostrarMensaje(lblMensajeVehiculo, "Ingresá el código del vehículo", false);
            return;
        }
        try {
            // Sin datos extra: el controlador usa los valores por defecto (rodado, bateria, etc.)
            getControladorAdministracion().altaVehiculo(tipo, codigo, null, estacion.getCodigo());
            mostrarMensaje(lblMensajeVehiculo, nombreTipo(tipo) + " " + codigo + " agregado a " + estacion.getCodigo(), true);
            txtCodigoVehiculo.clear();
            cargarEstaciones();
            mostrarVehiculos(estacion);
        } catch (ReglaNegocioException e) {
            mostrarMensaje(lblMensajeVehiculo, e.getMessage(), false);
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

    private String nombreTipo(TipoVehiculo tipo) {
        switch (tipo) {
            case BICICLETA_COMUN:
                return "Bicicleta";
            case BICICLETA_ELECTRICA:
                return "Bicicleta eléctrica";
            case MONOPATIN_ELECTRICO:
                return "Monopatín eléctrico";
            default:
                return tipo.toString();
        }
    }

    private String bateria(Vehiculo vehiculo) {
        if (vehiculo instanceof Electrico) {
            return ((Electrico) vehiculo).getNivelBateria() + "%";
        }
        return "-";
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
            Main.mostrarPantalla("usuarios/IniciarSesion.fxml");
        } catch (IOException e) {
            System.out.println("No se pudo abrir la pantalla: " + e.getMessage());
        }
    }
}
