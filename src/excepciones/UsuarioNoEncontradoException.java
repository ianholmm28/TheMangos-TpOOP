package excepciones;

public class UsuarioNoEncontradoException extends ReglaNegocioException {

    public UsuarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
