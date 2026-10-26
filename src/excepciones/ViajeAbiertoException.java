package excepciones;

public class ViajeAbiertoException extends ReglaNegocioException {

    public ViajeAbiertoException(String mensaje) {
        super(mensaje);
    }
}