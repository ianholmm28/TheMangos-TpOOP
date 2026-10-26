package excepciones;

public class EstacionLlenaException extends ReglaNegocioException {

    public EstacionLlenaException(String mensaje) {
        super(mensaje);
    }
}