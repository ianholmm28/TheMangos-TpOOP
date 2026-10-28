package controladores;
import usuarios.Usuario;
import excepciones.UsuarioNoEncontradoException;
import excepciones.SaldoInsuficienteException;
import java.time.YearMonth;
import excepciones.ReglaNegocioException;
import enums.TipoMedioPago;
import java.util.HashMap;
import java.util.Map;

public class ControladorUsuarios {

    private Map<String, Usuario> usuarios;

    public ControladorUsuarios() {
        this.usuarios = new HashMap<>();
    }      
    
    public Usuario registrarUsuario(String nombre, String documento, String email) throws ReglaNegocioException{
        if (usuarios.containsKey(documento)) {
            throw new ReglaNegocioException("Ya existe un usuario con ese documento");
        }
        Usuario usuarioNuevo = new Usuario(nombre, documento, email);
        usuarios.put(documento, usuarioNuevo);
        return usuarioNuevo;
    }
    
    public void agregarMedioPago(String documento, TipoMedioPago medio, String identificador)throws UsuarioNoEncontradoException{
        Usuario usuario = usuarios.get(documento);
        if (usuario == null) {
            throw new UsuarioNoEncontradoException("no existe un usuario con ese documento");
        }
        usuario.agregarMediosPago(medio, identificador);
    }
    
    public void recargarSaldo(String documento, TipoMedioPago medio, String identificador, double monto)throws ReglaNegocioException{
        Usuario usuario = usuarios.get(documento);
        if (usuario == null) {
            throw new UsuarioNoEncontradoException("no existe un usuario con ese documento");
        }
        usuario.recargarSaldo(monto, medio, identificador);
    }
        
    public void suscribirPlan(String documento)throws UsuarioNoEncontradoException, SaldoInsuficienteException{
        Usuario usuario = usuarios.get(documento);
        if (usuario == null) {
            throw new UsuarioNoEncontradoException("no existe un usuario con ese documento");
        }
        usuario.suscribirPlan(YearMonth.now());
    }
}
