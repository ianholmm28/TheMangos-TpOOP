package vista.operador;

import enums.EstadoVehiculo;
import estaciones.Estacion;
import excepciones.ReglaNegocioException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import personal.Personal;
import personal.Tecnico;
import vehiculos.OrdenMantenimiento;
import vehiculos.Vehiculo;
import vista.Formato;
import vista.Pantalla;
import vista.Sesion;

/**
 * Panel del operador del sistema: una pestaña por cada caso de uso
 * (CU13 Reportar falla, CU14 Generar orden de mantenimiento, CU15 Registrar traslado).
 *
 * @author holmian
 */
public class PanelOperadorController extends Pantalla implements Initializable {

    @FXML
    private Label lblBienvenida;

    // Cada lista muestra textos; al lado se guardan los objetos en el mismo orden para saber cual se eligio
    // CU13 Reportar falla
    @FXML
    private ListView<String> lstVehiculosFalla;
    private List<Vehiculo> vehiculosFalla = new ArrayList<>();
    @FXML
    private TextField txtDescripcionFalla;
    @FXML
    private Label lblMensajeFalla;

    // CU14 Generar orden de mantenimiento
    @FXML
    private ListView<String> lstVehiculosOrden;
    private List<Vehiculo> vehiculosOrden = new ArrayList<>();
    @FXML
    private ComboBox<Tecnico> cmbTecnico;
    @FXML
    private Label lblMensajeOrden;

    // CU15 Registrar traslado
    @FXML
    private ComboBox<Estacion> cmbOrigen;
    @FXML
    private ComboBox<Estacion> cmbDestino;
    @FXML
    private ListView<String> lstVehiculosTraslado;
    private List<Vehiculo> vehiculosTraslado = new ArrayList<>();
    @FXML
    private Label lblMensajeTraslado;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Personal operador = getControladorAdministracion().getPersonal().get(Sesion.getLegajo());
        if (operador != null) {
            lblBienvenida.setText("Operador: " + operador.getNombre() + " (legajo " + operador.getLegajo() + ")");
        }

        cmbTecnico.setConverter(Formato.conversor(tecnico -> tecnico.getLegajo() + " - " + tecnico.getNombre()));
        List<Tecnico> tecnicos = new ArrayList<>(getControladorMantenimiento().getTecnicos().values());
        tecnicos.sort(Comparator.comparing(Tecnico::getLegajo));
        cmbTecnico.setItems(FXCollections.observableArrayList(tecnicos));
        if (!tecnicos.isEmpty()) {
            cmbTecnico.setValue(tecnicos.get(0));
        }

        cmbOrigen.setConverter(Formato.conversor(Formato::estacion));
        cmbDestino.setConverter(Formato.conversor(Formato::estacion));
        List<Estacion> estaciones = new ArrayList<>(getControladorEstaciones().getEstaciones().values());
        estaciones.sort(Comparator.comparing(Estacion::getCodigo));
        cmbOrigen.setItems(FXCollections.observableArrayList(estaciones));
        cmbDestino.setItems(FXCollections.observableArrayList(estaciones));
        // Al cambiar el origen se muestran los vehiculos de esa estacion
        cmbOrigen.valueProperty().addListener((obs, anterior, nueva) -> cargarVehiculosTraslado());
        // En el traslado se pueden elegir varios vehiculos a la vez
        lstVehiculosTraslado.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        cargarVehiculos();
    }

    // Llena una lista con un renglon por vehiculo y guarda los vehiculos en el mismo orden
    private void llenarLista(ListView<String> lista, List<Vehiculo> mostrados, Collection<Vehiculo> vehiculos) {
        mostrados.clear();
        mostrados.addAll(vehiculos);
        mostrados.sort(Comparator.comparing(Vehiculo::getCodigo));
        lista.getItems().clear();
        for (Vehiculo vehiculo : mostrados) {
            lista.getItems().add(Formato.vehiculo(vehiculo));
        }
    }

    // Devuelve el vehiculo del renglon seleccionado, o null si no hay ninguno elegido
    private Vehiculo elegido(ListView<String> lista, List<Vehiculo> mostrados) {
        int indice = lista.getSelectionModel().getSelectedIndex();
        if (indice < 0) {
            return null;
        }
        return mostrados.get(indice);
    }

    private void cargarVehiculos() {
        Collection<Vehiculo> todos = getControladorMantenimiento().getVehiculos().values();
        llenarLista(lstVehiculosFalla, vehiculosFalla, todos);
        llenarLista(lstVehiculosOrden, vehiculosOrden, todos);
        cargarVehiculosTraslado();
    }

    private void cargarVehiculosTraslado() {
        Estacion origen = cmbOrigen.getValue();
        if (origen == null) {
            llenarLista(lstVehiculosTraslado, vehiculosTraslado, new ArrayList<>());
        } else {
            llenarLista(lstVehiculosTraslado, vehiculosTraslado, origen.getVehiculos());
        }
    }

    // CU13
    @FXML
    void reportarFalla(ActionEvent event) {
        Vehiculo vehiculo = elegido(lstVehiculosFalla, vehiculosFalla);
        if (vehiculo == null) {
            mostrarError(lblMensajeFalla, "Seleccioná un vehículo de la lista");
            return;
        }
        try {
            getControladorMantenimiento().reportarFalla(vehiculo.getCodigo(), txtDescripcionFalla.getText().trim());
            String texto = "Falla reportada para " + vehiculo.getCodigo();
            if (vehiculo.getEstado() == EstadoVehiculo.EN_REPARACION) {
                texto += ". El vehículo está en reparación";
            }
            mostrarExito(lblMensajeFalla, texto);
            txtDescripcionFalla.clear();
            cargarVehiculos();
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensajeFalla, e.getMessage());
        }
    }

    // CU14
    @FXML
    void generarOrden(ActionEvent event) {
        Vehiculo vehiculo = elegido(lstVehiculosOrden, vehiculosOrden);
        Tecnico tecnico = cmbTecnico.getValue();
        if (vehiculo == null) {
            mostrarError(lblMensajeOrden, "Seleccioná un vehículo de la lista");
            return;
        }
        if (tecnico == null) {
            mostrarError(lblMensajeOrden, "Elegí el técnico");
            return;
        }
        try {
            OrdenMantenimiento orden = getControladorMantenimiento().generarOrden(vehiculo.getCodigo(), tecnico.getLegajo());
            mostrarExito(lblMensajeOrden, "Orden N° " + orden.getNumero() + " generada: " + vehiculo.getCodigo()
                    + " queda en reparación con " + tecnico.getNombre());
            cargarVehiculos();
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensajeOrden, e.getMessage());
        }
    }

    // CU15
    @FXML
    void registrarTraslado(ActionEvent event) {
        Estacion origen = cmbOrigen.getValue();
        Estacion destino = cmbDestino.getValue();
        if (origen == null || destino == null) {
            mostrarError(lblMensajeTraslado, "Elegí la estación de origen y la de destino");
            return;
        }
        List<String> codigos = new ArrayList<>();
        for (int indice : lstVehiculosTraslado.getSelectionModel().getSelectedIndices()) {
            codigos.add(vehiculosTraslado.get(indice).getCodigo());
        }
        try {
            getControladorEstaciones().registrarTraslado(origen.getCodigo(), destino.getCodigo(), codigos, Sesion.getLegajo());
            mostrarExito(lblMensajeTraslado, "Traslado registrado: " + String.join(", ", codigos)
                    + " de " + origen.getCodigo() + " a " + destino.getCodigo());
            cargarVehiculos();
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensajeTraslado, e.getMessage());
        }
    }
}
