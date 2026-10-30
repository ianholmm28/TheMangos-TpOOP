package vista.principal;

import excepciones.UsuarioNoEncontradoException;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import usuarios.Movimiento;
import usuarios.Usuario;
import viajes.Viaje;
import vista.Main;
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
    private TableView<Viaje> tablaViajes;
    @FXML
    private TableColumn<Viaje, String> colViajeFecha;
    @FXML
    private TableColumn<Viaje, String> colViajeVehiculo;
    @FXML
    private TableColumn<Viaje, String> colViajeMinutos;
    @FXML
    private TableColumn<Viaje, String> colViajeImporte;

    @FXML
    private TableView<Movimiento> tablaMovimientos;
    @FXML
    private TableColumn<Movimiento, String> colMovFecha;
    @FXML
    private TableColumn<Movimiento, String> colMovConcepto;
    @FXML
    private TableColumn<Movimiento, String> colMovTipo;
    @FXML
    private TableColumn<Movimiento, String> colMovImporte;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTablas();
        if (Sesion.hayUsuario()) {
            cargarDatos(Sesion.getDocumento());
        }
    }

    private void configurarTablas() {
        colViajeFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInicio().format(FORMATO_FECHA)));
        colViajeVehiculo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVehiculo().getCodigo()));
        colViajeMinutos.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getMinutosCobrables())));
        colViajeImporte.setCellValueFactory(c -> new SimpleStringProperty(formatearPlata(c.getValue().getImporte())));

        colMovFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFecha().format(FORMATO_FECHA)));
        colMovConcepto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getConcepto()));
        colMovTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo().toString()));
        colMovImporte.setCellValueFactory(c -> new SimpleStringProperty(formatearPlata(c.getValue().getImporte())));
    }


    private void cargarDatos(String documento) {
        try {
            Usuario usuario = getControladorUsuarios().buscarUsuario(documento);
            lblBienvenida.setText("Hola, " + usuario.getNombre());
            lblSaldo.setText(formatearPlata(usuario.getCuenta().getSaldo()));

            tablaMovimientos.setItems(FXCollections.observableArrayList(
                    getControladorUsuarios().consultarMovimientos(documento)));

            List<Viaje> viajesDelUsuario = new ArrayList<>();
            for (Viaje v : getControladorViajes().getViajes()) {
                if (v.getUsuario().getDocumento().equals(documento)) {
                    viajesDelUsuario.add(v);
                }
            }
            tablaViajes.setItems(FXCollections.observableArrayList(viajesDelUsuario));
        } catch (UsuarioNoEncontradoException e) {
            lblBienvenida.setText("Usuario no encontrado");
        }
    }

    private String formatearPlata(double monto) {
        return String.format("$ %,.2f", monto);
    }

    @FXML
    void recargarSaldo(ActionEvent event) {
        try {
            Main.mostrarPantalla("usuarios/RecargarSaldo.fxml");
        } catch (IOException e) {
            System.out.println("No se pudo abrir la pantalla: " + e.getMessage());
        }
    }

    @FXML
    void registrarVehiculo(ActionEvent event) {
        // TODO
    }

    @FXML
    void irAAjustes(ActionEvent event) {
        try {
            Main.mostrarPantalla("principal/AjustesAplicacion.fxml");
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
