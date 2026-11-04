package vista.tecnico;

import excepciones.ReglaNegocioException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import personal.Personal;
import vehiculos.Electrico;
import vehiculos.OrdenMantenimiento;
import vehiculos.Vehiculo;
import vista.Formato;
import vista.Pantalla;
import vista.Sesion;

/**
 * Panel del tecnico de mantenimiento: una pestaña por cada caso de uso
 * (CU18 Cerrar orden de mantenimiento, CU19 Registrar carga de bateria).
 *
 * @author holmian
 */
public class PanelTecnicoController extends Pantalla implements Initializable {

    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private Label lblBienvenida;

    // Cada lista muestra textos; al lado se guardan los objetos en el mismo orden para saber cual se eligio
    // CU18 Cerrar orden de mantenimiento
    @FXML
    private ListView<String> lstOrdenes;
    private List<OrdenMantenimiento> ordenesMostradas = new ArrayList<>();
    @FXML
    private TextField txtTareas;
    @FXML
    private TextField txtRepuestos;
    @FXML
    private Label lblMensajeOrden;

    // CU19 Registrar carga de bateria
    @FXML
    private ListView<String> lstVehiculos;
    private List<Vehiculo> vehiculosMostrados = new ArrayList<>();
    @FXML
    private TextField txtBateria;
    @FXML
    private Label lblMensajeCarga;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Personal tecnico = getControladorAdministracion().getPersonal().get(Sesion.getLegajo());
        if (tecnico != null) {
            lblBienvenida.setText("Técnico: " + tecnico.getNombre() + " (legajo " + tecnico.getLegajo() + ")");
        }
        cargarOrdenes();
        cargarVehiculos();
    }

    // Solo las ordenes abiertas asignadas a este tecnico: "Orden N° 1  -  B001 Bicicleta  -  abierta el 04/11/2026"
    private void cargarOrdenes() {
        ordenesMostradas.clear();
        lstOrdenes.getItems().clear();
        for (OrdenMantenimiento orden : getControladorMantenimiento().getOrdenes()) {
            if (orden.estaAbierta() && orden.getTecnico().getLegajo().equals(Sesion.getLegajo())) {
                ordenesMostradas.add(orden);
                lstOrdenes.getItems().add("Orden N° " + orden.getNumero() + "  -  " + orden.getVehiculo().getCodigo()
                        + " " + Formato.nombreTipo(orden.getVehiculo().getTipo())
                        + "  -  abierta el " + orden.getFechaApertura().format(FORMATO_DIA));
            }
        }
    }

    // Solo los vehiculos electricos, que son los que tienen bateria
    private void cargarVehiculos() {
        // Se recuerda cual estaba elegido para dejarlo seleccionado despues de actualizar
        int indiceElegido = lstVehiculos.getSelectionModel().getSelectedIndex();
        vehiculosMostrados.clear();
        for (Vehiculo vehiculo : getControladorMantenimiento().getVehiculos().values()) {
            if (vehiculo instanceof Electrico) {
                vehiculosMostrados.add(vehiculo);
            }
        }
        vehiculosMostrados.sort(Comparator.comparing(Vehiculo::getCodigo));
        lstVehiculos.getItems().clear();
        for (Vehiculo vehiculo : vehiculosMostrados) {
            lstVehiculos.getItems().add(Formato.vehiculo(vehiculo));
        }
        lstVehiculos.getSelectionModel().select(indiceElegido);
    }

    // CU18
    @FXML
    void cerrarOrden(ActionEvent event) {
        int indice = lstOrdenes.getSelectionModel().getSelectedIndex();
        if (indice < 0) {
            mostrarError(lblMensajeOrden, "Seleccioná una orden de la lista");
            return;
        }
        String tareas = txtTareas.getText().trim();
        if (tareas.isEmpty()) {
            mostrarError(lblMensajeOrden, "Contá qué tareas hiciste");
            return;
        }
        OrdenMantenimiento orden = ordenesMostradas.get(indice);
        try {
            getControladorMantenimiento().cerrarOrden(orden.getNumero(), tareas, txtRepuestos.getText().trim());
            mostrarExito(lblMensajeOrden, "Orden N° " + orden.getNumero() + " cerrada. "
                    + orden.getVehiculo().getCodigo() + " quedó " + orden.getVehiculo().getEstado());
            txtTareas.clear();
            txtRepuestos.clear();
            cargarOrdenes();
            cargarVehiculos();
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensajeOrden, e.getMessage());
        }
    }

    // CU19
    @FXML
    void registrarCarga(ActionEvent event) {
        int indice = lstVehiculos.getSelectionModel().getSelectedIndex();
        if (indice < 0) {
            mostrarError(lblMensajeCarga, "Seleccioná un vehículo de la lista");
            return;
        }
        int nivel;
        try {
            nivel = Integer.parseInt(txtBateria.getText().trim());
        } catch (NumberFormatException e) {
            mostrarError(lblMensajeCarga, "El nivel de batería tiene que ser un número entero");
            return;
        }
        Vehiculo vehiculo = vehiculosMostrados.get(indice);
        try {
            // El controlador valida que el nivel este entre 0 y 100
            getControladorMantenimiento().registrarCarga(vehiculo.getCodigo(), nivel);
            mostrarExito(lblMensajeCarga, "Batería de " + vehiculo.getCodigo() + " cargada al " + nivel + "%");
            txtBateria.clear();
            cargarVehiculos();
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensajeCarga, e.getMessage());
        }
    }
}
