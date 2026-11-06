package vista;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        stage.setTitle("TheMangos");
        mostrarPantalla("usuarios/RegistrarUsuario.fxml");
        stage.setMaximized(true);
        stage.show();
    }

    public static void mostrarPantalla(String rutaFxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(rutaFxml));
        Parent root = loader.load();
        if (stage.getScene() == null) {
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Main.class.getResource("estilos.css").toExternalForm());
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(root);
        }
    }

    @Override
    public void stop() {
        Pantalla.getSistema().getControladorPersistencia().guardarDatos();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
