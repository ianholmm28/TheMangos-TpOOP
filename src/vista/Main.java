package vista;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage stage;
    private static Contexto contexto;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        contexto = new Contexto();
        stage.setTitle("TheMangos");
        mostrarPantalla("usuarios/RegistrarUsuario.fxml");
        stage.setMaximized(true);
        stage.show();
    }

    /**
     * Cambia la pantalla de la ventana. La ruta es relativa al package vista,
     * por ejemplo "usuarios/RegistrarUsuario.fxml".
     */
    public static void mostrarPantalla(String rutaFxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(rutaFxml));
        Parent root = loader.load();

        Object controlador = loader.getController();
        if (controlador instanceof Pantalla) {
            ((Pantalla) controlador).setContexto(contexto);
        }

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Main.class.getResource("estilos.css").toExternalForm());
        stage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
