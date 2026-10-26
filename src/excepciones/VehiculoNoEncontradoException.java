package excepciones;

public class VehiculoNoEncontradoException extends ReglaNegocioException {

    public VehiculoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}