package vista;

/**
 * Guarda el documento del usuario que inicio sesion, para que las pantallas
 * sepan de quien mostrar los datos.
 */
public class Sesion {
    private static String documento;

    public static void iniciar(String documentoUsuario) {
        documento = documentoUsuario;
    }

    public static void cerrar() {
        documento = null;
    }

    public static String getDocumento() {
        return documento;
    }

    public static boolean hayUsuario() {
        return documento != null;
    }
}
