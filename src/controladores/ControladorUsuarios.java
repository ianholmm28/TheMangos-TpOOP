package controladores;
import usuarios.Usuario;
import excepciones.UsuarioNoEncontradoException;
import excepciones.SaldoInsuficienteException;
import java.time.YearMonth;
import excepciones.ReglaNegocioException;
import enums.TipoMedioPago;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import usuarios.Movimiento;

public class ControladorUsuarios {

    private Map<String, Usuario> usuarios;

    public ControladorUsuarios() {
        this.usuarios = new HashMap<>();
    }      
    
    public Usuario registrarUsuario(String nombre, String documento, String email) throws ReglaNegocioException{
        if (nombre == null || nombre.trim().isEmpty() ||
            documento == null || documento.trim().isEmpty() ||
            email == null || email.trim().isEmpty()) {
            throw new ReglaNegocioException("Los datos del usuario (nombre, documento, email) no pueden ser vacíos.");
        }
        if (usuarios.containsKey(documento)) {
            throw new ReglaNegocioException("Ya existe un usuario con ese documento");
        }
        Usuario usuarioNuevo = new Usuario(nombre, documento, email);
        usuarios.put(documento, usuarioNuevo);
        return usuarioNuevo;
    }
    
    public void agregarMedioPago(String documento, TipoMedioPago medio, String identificador) throws ReglaNegocioException {
        if (medio == null || identificador == null || identificador.trim().isEmpty()) {
            throw new ReglaNegocioException("Debe ingresar un tipo y un identificador válido para el medio de pago.");
        }
        Usuario usuario = buscarUsuario(documento);
        usuario.agregarMediosPago(medio, identificador);
    }

    public void quitarMedioPago(String documento, TipoMedioPago medio, String identificador) throws ReglaNegocioException {
        Usuario usuario = buscarUsuario(documento);
        usuario.quitarMedioPago(medio, identificador);
    }
    
    public void recargarSaldo(String documento, TipoMedioPago medio, String identificador, double monto) throws ReglaNegocioException {
        if (monto <= 0) {
            throw new ReglaNegocioException("El monto a recargar debe ser mayor a cero.");
        }
        Usuario usuario = buscarUsuario(documento);
        usuario.recargarSaldo(monto, medio, identificador);
    }
        
    public void suscribirPlan(String documento) throws ReglaNegocioException {
        Usuario usuario = buscarUsuario(documento);
        usuario.suscribirPlan(YearMonth.now());
    }

    public void darDeBajaPlan(String documento) throws ReglaNegocioException {
        Usuario usuario = buscarUsuario(documento);
        usuario.darDeBajaPlan();
    }

    public double consultarSaldo(String documento) throws UsuarioNoEncontradoException {
        Usuario usuario = buscarUsuario(documento);
        return usuario.getCuenta().getSaldo();
    }

    public List<Movimiento> consultarMovimientos(String documento) throws UsuarioNoEncontradoException {
        Usuario usuario = buscarUsuario(documento);
        List<Movimiento> lista = new ArrayList<>(usuario.getCuenta().getMovimientos());
        Collections.sort(lista); // Ordenados del más reciente al más antiguo
        return lista;
    }

    public Usuario buscarUsuario(String documento) throws UsuarioNoEncontradoException {
        Usuario usuario = usuarios.get(documento);
        if (usuario == null) {
            throw new UsuarioNoEncontradoException("No existe un usuario con el documento: " + documento);
        }
        return usuario;
    }

    public Map<String, Usuario> getUsuarios() {
        return usuarios;
    }
}
