package vista;

public class Sesion {
    private static String documento;
    // Legajo del personal que ingreso (operador, tecnico o administrador), null si entro un usuario
    private static String legajo;

    public static void iniciar(String documentoUsuario) {
        documento = documentoUsuario;
        legajo = null;
    }

    public static void iniciarPersonal(String legajoPersonal) {
        documento = null;
        legajo = legajoPersonal;
    }

    public static void cerrar() {
        documento = null;
        legajo = null;
    }

    public static String getDocumento() {
        return documento;
    }

    public static String getLegajo() {
        return legajo;
    }

    public static boolean hayUsuario() {
        return documento != null;
    }
}
