package vista.general;

import enums.TipoMedioPago;
import excepciones.ReglaNegocioException;
import excepciones.UsuarioNoEncontradoException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import usuarios.MedioPago;
import usuarios.Usuario;
import vista.Pantalla;
import vista.Sesion;

/**
 * FXML Controller class
 *
 * @author holmian
 */
public class AjustesAplicacionController extends Pantalla implements Initializable {

    @FXML
    private Label lblBienvenida;
    @FXML
    private Label lblDatos;
    @FXML
    private TextField txtNombre;
    @FXML
    private Label lblMensajeNombre;
    @FXML
    private ListView<MedioPago> lstMedios;
    @FXML
    private ComboBox<TipoMedioPago> cmbTipo;
    @FXML
    private TextField txtIdentificador;
    @FXML
    private Label lblMensajeMedios;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Como se muestra cada medio de pago en la lista: "TARJETA 1234"
        lstMedios.setCellFactory(lista -> new ListCell<MedioPago>() {
            @Override
            protected void updateItem(MedioPago medio, boolean vacio) {
                super.updateItem(medio, vacio);
                setText(vacio || medio == null ? null : medio.getTipo() + " " + medio.getIdentificador());
            }
        });
        lstMedios.setPlaceholder(new Label("Todavía no agregaste medios de pago"));
        cmbTipo.setItems(FXCollections.observableArrayList(TipoMedioPago.values()));
        cargarDatos();
    }

    // Muestra los datos del usuario y su lista de medios de pago
    private void cargarDatos() {
        if (!Sesion.hayUsuario()) {
            lblBienvenida.setText("Sin sesión iniciada");
            return;
        }
        try {
            Usuario usuario = getControladorUsuarios().buscarUsuario(Sesion.getDocumento());
            lblBienvenida.setText("Hola, " + usuario.getNombre());
            lblDatos.setText("Documento " + usuario.getDocumento() + " · " + usuario.getEmail());
            txtNombre.setText(usuario.getNombre());
            lstMedios.setItems(FXCollections.observableArrayList(usuario.getMediosPago()));
        } catch (UsuarioNoEncontradoException e) {
            lblBienvenida.setText("Usuario no encontrado");
        }
    }

    @FXML
    void guardarNombre(ActionEvent event) {
        try {
            getControladorUsuarios().cambiarNombre(Sesion.getDocumento(), txtNombre.getText());
            mostrarExito(lblMensajeNombre, "Nombre actualizado");
            cargarDatos();
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensajeNombre, e.getMessage());
        }
    }

    @FXML
    void agregarMedioPago(ActionEvent event) {
        try {
            getControladorUsuarios().agregarMedioPago(Sesion.getDocumento(), cmbTipo.getValue(), txtIdentificador.getText().trim());
            mostrarExito(lblMensajeMedios, "Medio de pago agregado");
            cmbTipo.setValue(null);
            txtIdentificador.clear();
            cargarDatos();
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensajeMedios, e.getMessage());
        }
    }

    @FXML
    void quitarMedioPago(ActionEvent event) {
        MedioPago medio = lstMedios.getSelectionModel().getSelectedItem();
        if (medio == null) {
            mostrarError(lblMensajeMedios, "Elegí de la lista el medio que querés quitar");
            return;
        }
        try {
            getControladorUsuarios().quitarMedioPago(Sesion.getDocumento(), medio.getTipo(), medio.getIdentificador());
            mostrarExito(lblMensajeMedios, "Medio de pago quitado");
            cargarDatos();
        } catch (ReglaNegocioException e) {
            mostrarError(lblMensajeMedios, e.getMessage());
        }
    }
}
