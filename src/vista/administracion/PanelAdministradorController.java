package vista.administracion;

import excepciones.ReglaNegocioException;
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
import javafx.scene.control.TextField;
import personal.Operador;
import personal.Personal;
import personal.Tecnico;
import vehiculos.Vehiculo;
import viajes.Viaje;
import vista.Formato;
import vista.Pantalla;

/**
 * Panel del administrador: una pestaña por cada caso de uso
 * (CU21 Gestionar vehiculos, CU22 Gestionar personal, CU24 Emitir reportes).
 * CU20 Gestionar estaciones es el boton que lleva a la pantalla de estaciones.
 *
 * @author holmian
 */
public class PanelAdministradorController extends Pantalla implements Initializable {

    // CU21 Gestionar vehiculos: la lista muestra textos; en vehiculosMostrados estan los vehiculos en el mismo orden
    @FXML
    private ListView<String> lstVehiculos;
    private List<Vehiculo> vehiculosMostrados = new ArrayList<>();
    @FXML
    private Label lblMensajeVehiculos;

    // CU22 Gestionar personal
    @FXML
    private ListView<String> lstPersonal;
    @FXML
    private ComboBox<String> cmbRol;
    @FXML
    private TextField txtLegajo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtTelefono;
    @FXML
    private Label lblMensajePersonal;

    // CU24 Emitir reportes
    @FXML
    private Label lblTituloReporte;
    @FXML
    private ListView<String> lstReporte;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Ya hay un administrador y el sistema no permite otro, asi que solo se dan de alta operadores y tecnicos
        cmbRol.setItems(FXCollections.observableArrayList("Operador", "Técnico"));
        cargarVehiculos();
        cargarPersonal();
    }

    // Un renglon por vehiculo: "M001  -  Monopatín eléctrico  -  DISPONIBLE  -  Batería 80%"
    private void cargarVehiculos() {
        vehiculosMostrados = new ArrayList<>(getControladorAdministracion().getVehiculos().values());
        vehiculosMostrados.sort(Comparator.comparing(Vehiculo::getCodigo));
        lstVehiculos.getItems().clear();
        for (Vehiculo vehiculo : vehiculosMostrados) {
            lstVehiculos.getItems().add(Formato.vehiculo(vehiculo));
        }
    }

    // Un renglon por persona: "1001  -  Ana López  -  Operador  -  Tel. 1155554444"
    private void cargarPersonal() {
        List<Personal> personal = new ArrayList<>(getControladorAdministracion().getPersonal().values());
        personal.sort(Comparator.comparing(Personal::getLegajo));
        lstPersonal.getItems().clear();
        for (Personal persona : personal) {
            lstPersonal.getItems().add(persona.getLegajo() + "  -  " + persona.getNombre() + "  -  "
                    + persona.getRol() + "  -  Tel. " + persona.getTelefono());
        }
    }

    // CU21
    @FXML
    void darDeBaja(ActionEvent event) {
        int indice = lstVehiculos.getSelectionModel().getSelectedIndex();
        if (indice < 0) {
            mostrarError(lblMensajeVehiculos, "Seleccioná un vehículo de la lista");
            return;
        }
        Vehiculo vehiculo = vehiculosMostrados.get(indice);
        try {
            getControladorAdministracion().bajaVehiculo(vehiculo.getCodigo());
            mostrarExito(lblMensajeVehiculos, vehiculo.getCodigo() + " dado de baja: quedó fuera de servicio y salió de su estación");
            cargarVehiculos();
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensajeVehiculos, e.getMessage());
        }
    }

    // CU22
    @FXML
    void registrarPersonal(ActionEvent event) {
        String rol = cmbRol.getValue();
        if (rol == null) {
            mostrarError(lblMensajePersonal, "Elegí el rol");
            return;
        }
        String legajo = txtLegajo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String telefono = txtTelefono.getText().trim();
        Personal nuevo;
        if (rol.equals("Operador")) {
            nuevo = new Operador(legajo, nombre, telefono);
        } else {
            nuevo = new Tecnico(legajo, nombre, telefono);
        }
        try {
            // El controlador valida legajo y nombre obligatorios y que el legajo no se repita
            getControladorAdministracion().registrarPersonal(nuevo);
            mostrarExito(lblMensajePersonal, rol + " " + nombre + " registrado con legajo " + legajo);
            txtLegajo.clear();
            txtNombre.clear();
            txtTelefono.clear();
            cargarPersonal();
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensajePersonal, e.getMessage());
        }
    }

    // CU24: viajes ordenados por usuario
    @FXML
    void reporteViajes(ActionEvent event) {
        lblTituloReporte.setText("Viajes por usuario");
        lstReporte.getItems().clear();
        for (Viaje viaje : getGeneradorReportes().viajesPorUsuario(null, null)) {
            lstReporte.getItems().add(Formato.viaje(viaje));
        }
    }

    // CU24: el generador ya devuelve un texto por estacion
    @FXML
    void reporteEstaciones(ActionEvent event) {
        lblTituloReporte.setText("Uso por estación");
        lstReporte.getItems().setAll(getGeneradorReportes().usoPorEstacion(null, null));
    }

    // CU24: de mas a menos fallas, "M001  -  Monopatín eléctrico  -  3 fallas"
    @FXML
    void reporteFallas(ActionEvent event) {
        lblTituloReporte.setText("Vehículos con más fallas");
        lstReporte.getItems().clear();
        for (Vehiculo vehiculo : getGeneradorReportes().vehiculosConMasFallas()) {
            int fallas = vehiculo.getCantidadFallas();
            lstReporte.getItems().add(vehiculo.getCodigo() + "  -  " + Formato.nombreTipo(vehiculo.getTipo())
                    + "  -  " + fallas + (fallas == 1 ? " falla" : " fallas"));
        }
    }

    // CU20
    @FXML
    void irAEstaciones(ActionEvent event) {
        irA("estaciones/CrearEstacion.fxml");
    }
}
