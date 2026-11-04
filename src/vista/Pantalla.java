package vista;

import controladores.ControladorAdministracion;
import controladores.ControladorEstaciones;
import controladores.ControladorMantenimiento;
import controladores.ControladorPersistencia;
import controladores.ControladorUsuarios;
import controladores.ControladorViajes;
import controladores.Sistema;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import reportes.GeneradorReportes;

// Clase padre de todas las pantallas: acceso al sistema, navegacion y mensajes
public abstract class Pantalla {
    private static final Sistema sistema = new Sistema();

    public static Sistema getSistema() {return sistema;}
    public ControladorUsuarios getControladorUsuarios() {return sistema.getControladorUsuarios();}
    public ControladorViajes getControladorViajes() {return sistema.getControladorViajes();}
    public ControladorEstaciones getControladorEstaciones() {return sistema.getControladorEstaciones();}
    public ControladorMantenimiento getControladorMantenimiento() {return sistema.getControladorMantenimiento();}
    public ControladorAdministracion getControladorAdministracion() {return sistema.getControladorAdministracion();}
    public ControladorPersistencia getControladorPersistencia() {return sistema.getControladorPersistencia();}
    public GeneradorReportes getGeneradorReportes() {return sistema.getGeneradorReportes();}

    // Cambia a otra pantalla, por ejemplo "general/PantallaPrincipal.fxml"
    protected void irA(String rutaFxml) {
        try {
            Main.mostrarPantalla(rutaFxml);
        } catch (IOException e) {
            System.out.println("No se pudo abrir la pantalla: " + e.getMessage());
        }
    }

    // Boton "Volver": por defecto vuelve a la pantalla principal (cada pantalla lo puede cambiar)
    @FXML
    public void volver(ActionEvent event) {
        irA("general/PantallaPrincipal.fxml");
    }

    @FXML
    public void cerrarSesion(ActionEvent event) {
        Sesion.cerrar();
        irA("usuarios/IniciarSesion.fxml");
    }

    protected void mostrarError(Label label, String texto) {
        mostrarMensaje(label, texto, "mensaje-error");
    }

    protected void mostrarExito(Label label, String texto) {
        mostrarMensaje(label, texto, "mensaje-exito");
    }

    private void mostrarMensaje(Label label, String texto, String estilo) {
        label.setText(texto);
        label.getStyleClass().removeAll("mensaje-error", "mensaje-exito");
        label.getStyleClass().add(estilo);
        label.setManaged(true);
        label.setVisible(true);
    }
}
