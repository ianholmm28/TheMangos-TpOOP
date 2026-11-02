package vista;

import enums.TipoVehiculo;
import java.io.IOException;
import java.time.LocalDate;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tarifas.CalculadorTarifa;
import tarifas.Tarifa;

public class Main extends Application {

    private static Stage stage;

    // Tarifa fija, igual para todos los vehiculos
    public static final double CARGO_DESBLOQUEO = 500;
    public static final double PRECIO_MINUTO = 80;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        stage.setTitle("TheMangos");
        cargarTarifaFija();
        mostrarPantalla("usuarios/RegistrarUsuario.fxml");
        stage.setMaximized(true);
        stage.show();
    }

    // Carga la tarifa fija para cada tipo de vehiculo si todavia no tiene una
    private void cargarTarifaFija() {
        CalculadorTarifa calculador = Pantalla.getSistema().getControladorAdministracion().getCalculador();
        for (TipoVehiculo tipo : TipoVehiculo.values()) {
            boolean tieneTarifa = false;
            for (Tarifa tarifa : calculador.getTarifas()) {
                if (tarifa.getTipo() == tipo) {
                    tieneTarifa = true;
                }
            }
            if (!tieneTarifa) {
                calculador.agregarTarifa(new Tarifa(tipo, LocalDate.of(2000, 1, 1), CARGO_DESBLOQUEO, PRECIO_MINUTO));
            }
        }
    }

    public static void mostrarPantalla(String rutaFxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(rutaFxml));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Main.class.getResource("estilos.css").toExternalForm());
        stage.setScene(scene);
    }

    @Override
    public void stop() {
        Pantalla.getSistema().getControladorPersistencia().guardarDatos();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
