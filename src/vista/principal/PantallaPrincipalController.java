package vista.principal;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import usuarios.Movimiento;
import viajes.Viaje;
import vista.Main;
import vista.Pantalla;

/**
 * FXML Controller class
 *
 * @author holmian
 */
public class PantallaPrincipalController extends Pantalla implements Initializable {

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
        // TODO
    }

    @FXML
    void recargarSaldo(ActionEvent event) {
        // TODO
    }

    @FXML
    void registrarVehiculo(ActionEvent event) {
        // TODO
    }

    @FXML
    void cerrarSesion(ActionEvent event) {
        try {
            Main.mostrarPantalla("usuarios/IniciarSesion.fxml");
        } catch (IOException e) {
            System.out.println("No se pudo abrir la pantalla: " + e.getMessage());
        }
    }
}
