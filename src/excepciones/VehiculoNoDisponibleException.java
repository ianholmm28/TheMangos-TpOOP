package excepciones;

public class VehiculoNoDisponibleException extends ReglaNegocioException {

    public VehiculoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}