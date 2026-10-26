package usuarios;

import excepciones.SaldoInsuficienteException;
import excepciones.UsuarioInhabilitadoException;
import excepciones.ViajeAbiertoException;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private String nombre;
    private String documento;
    private String email;
    private LocalDate fechaAlta;
    private boolean habilitado;
    private Cuenta cuenta;
    private PlanMensual plan;
    private List<MedioPago> mediosPago;
    // private Viaje viajeEnCurso; // agregar cuando exista la clase Viaje

    public Usuario(String nombre, String documento, String email) {
        this.nombre = nombre;
        this.documento = documento;
        this.email = email;
        this.fechaAlta = LocalDate.now();
        this.habilitado = true;
        this.cuenta = new Cuenta(0, new ArrayList<Movimiento>());
        this.plan = null;
        this.mediosPago = new ArrayList<>();
    }
    
    
    public void verificarHabilitacion(double montoMinimo)
            throws UsuarioInhabilitadoException, SaldoInsuficienteException, ViajeAbiertoException {
        if (!this.habilitado) {
            throw new UsuarioInhabilitadoException("El usuario está inhabilitado");
        }
        if (!this.cuenta.tieneSaldo(montoMinimo)) {
            throw new SaldoInsuficienteException("Saldo insuficiente para retirar el vehículo");
        }
        // Viaje no existe todavia, descomentar cuando este
        // if (this.viajeEnCurso != null) {
        //     throw new ViajeAbiertoException("El usuario ya tiene un viaje en curso");
        // }
    }
    
    //Hay que agregarla al diagrama porque no existe
    public void rehabilitar() {
        if (!this.habilitado && this.cuenta.getSaldo() >= 0) {
            this.habilitado = true;
        }
    }
    
    public void inhabilitar(){
        this.habilitado = false;
    }
    
}
