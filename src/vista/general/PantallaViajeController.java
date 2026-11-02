package vista.general;

import estaciones.Estacion;
import excepciones.ReglaNegocioException;
import excepciones.UsuarioNoEncontradoException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
import javafx.util.StringConverter;
import usuarios.Usuario;
import vehiculos.Electrico;
import vehiculos.Vehiculo;
import vista.Main;
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
    @FXML
    private TableView<Vehiculo> tablaVehiculos;
    @FXML
    private TableColumn<Vehiculo, String> colCodigo;
    @FXML
    private TableColumn<Vehiculo, String> colTipo;
    @FXML
    private TableColumn<Vehiculo, String> colEstado;
    @FXML
    private TableColumn<Vehiculo, String> colBateria;
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
        configurarTabla();
        cargarDatos();
    }

    // Como se muestra cada estacion en los desplegables: "E01 - Av. Corrientes 1234"
    private void configurarCombos() {
        StringConverter<Estacion> conversor = new StringConverter<Estacion>() {
            @Override
            public String toString(Estacion estacion) {
                return estacion == null ? "" : estacion.getCodigo() + " - " + estacion.getDireccion();
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

    private void configurarTabla() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(nombreTipo(c.getValue())));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().toString()));
        colBateria.setCellValueFactory(c -> new SimpleStringProperty(bateria(c.getValue())));
        tablaVehiculos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) -> actualizarResumen());
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

    private void mostrarVehiculos(Estacion estacion) {
        if (estacion == null) {
            tablaVehiculos.getItems().clear();
            return;
        }
        tablaVehiculos.setItems(FXCollections.observableArrayList(estacion.getVehiculos()));
        lblSinVehiculos.setText("Esta estación no tiene vehículos");
        actualizarResumen();
    }

    // Texto con lo elegido y los lugares libres en el destino para ese tipo de vehiculo
    private void actualizarResumen() {
        Vehiculo vehiculo = tablaVehiculos.getSelectionModel().getSelectedItem();
        Estacion destino = cmbDestino.getValue();
        lblLugaresDestino.setText(textoLugares(destino, vehiculo));
        if (vehiculo == null) {
            lblResumen.setText("Seleccioná un vehículo de la tabla");
        } else {
            lblResumen.setText("Vehículo elegido: " + vehiculo.getCodigo() + " (" + nombreTipo(vehiculo) + ")");
        }
    }

    static String textoLugares(Estacion destino, Vehiculo vehiculo) {
        if (destino == null || vehiculo == null) {
            return "";
        }
        int libres = destino.lugaresLibres(vehiculo.getTipo());
        if (libres <= 0) {
            return "No hay lugares libres para " + nombreTipo(vehiculo) + " en esta estación";
        }
        return "Lugares libres para " + nombreTipo(vehiculo) + ": " + libres;
    }

    @FXML
    void iniciarViaje(ActionEvent event) {
        Estacion salida = cmbSalida.getValue();
        Vehiculo vehiculo = tablaVehiculos.getSelectionModel().getSelectedItem();
        Estacion destino = cmbDestino.getValue();
        if (salida == null) {
            mostrarMensaje(lblMensaje, "Elegí la estación de salida", false);
            return;
        }
        if (vehiculo == null) {
            mostrarMensaje(lblMensaje, "Elegí un vehículo de la tabla", false);
            return;
        }
        if (!vehiculo.estaDisponible()) {
            mostrarMensaje(lblMensaje, "Ese vehículo no está disponible", false);
            return;
        }
        if (destino == null) {
            mostrarMensaje(lblMensaje, "Elegí la estación de destino", false);
            return;
        }
        try {
            getControladorViajes().retirarVehiculo(Sesion.getDocumento(), salida.getCodigo(), vehiculo.getCodigo());
            DetalleViajeController.setDestinoElegido(destino.getCodigo());
            Main.mostrarPantalla("general/DetalleViaje.fxml");
        } catch (ReglaNegocioException e) {
            mostrarMensaje(lblMensaje, e.getMessage(), false);
        } catch (IOException e) {
            System.out.println("No se pudo abrir la pantalla: " + e.getMessage());
        }
    }

    static String nombreTipo(Vehiculo vehiculo) {
        switch (vehiculo.getTipo()) {
            case BICICLETA_COMUN:
                return "Bicicleta";
            case BICICLETA_ELECTRICA:
                return "Bicicleta eléctrica";
            case MONOPATIN_ELECTRICO:
                return "Monopatín eléctrico";
            default:
                return vehiculo.getTipo().toString();
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
