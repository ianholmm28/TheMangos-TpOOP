package usuarios;

import enums.TipoMedioPago;
import excepciones.SaldoInsuficienteException;
import excepciones.ReglaNegocioException;
import excepciones.UsuarioInhabilitadoException;
import excepciones.ViajeAbiertoException;
import viajes.Viaje;

import java.time.LocalDate;
import java.time.YearMonth;

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
    private Viaje viajeEnCurso;
    private boolean yaViajo;

    public Usuario(String nombre, String documento, String email) {
        this.nombre = nombre;
        this.documento = documento;
        this.email = email;
        this.fechaAlta = LocalDate.now();
        this.habilitado = true;
        this.cuenta = new Cuenta(0, new ArrayList<Movimiento>());
        this.plan = null;
        this.mediosPago = new ArrayList<>();
        this.yaViajo = false;
    }
    
    public void verificarHabilitacion(double montoMinimo)
            throws UsuarioInhabilitadoException, SaldoInsuficienteException, ViajeAbiertoException {
        if (!this.habilitado) {
            throw new UsuarioInhabilitadoException("El usuario está inhabilitado");
        }
        if (!this.cuenta.tieneSaldo(montoMinimo)) {
            throw new SaldoInsuficienteException("Saldo insuficiente para retirar el vehículo");
        }
        if (this.viajeEnCurso != null) {
            throw new ViajeAbiertoException("El usuario ya tiene un viaje en curso");
        }
    }
    
    public void recargarSaldo(double monto, TipoMedioPago tipo, String identificador) throws ReglaNegocioException {
        if (monto <= 0) {
            throw new ReglaNegocioException("El monto a recargar debe ser mayor a cero.");
        }
        MedioPago medio = null;
        for (MedioPago m : this.mediosPago) {
            if (m.getTipo() == tipo && m.getIdentificador().equals(identificador)) {
                medio = m;
                break;
            }
        }
        if (medio == null) {
            throw new ReglaNegocioException("El usuario no tiene ese medio de pago asociado");
        }
        this.cuenta.recargar(monto, "Recarga con " + tipo + " " + identificador);
        this.rehabilitar();
    }

    public void rehabilitar() {
        if (!this.habilitado && this.cuenta.getSaldo() >= 0) {
            this.habilitado = true;
        }
    }

    public void suscribirPlan(YearMonth mes) throws ReglaNegocioException {
        if (this.plan != null && this.plan.getMes().equals(mes)) {
            throw new ReglaNegocioException("El usuario ya cuenta con un plan mensual suscrito para el mes actual.");
        }
        if (!this.cuenta.tieneSaldo(PlanMensual.COSTO_PLAN)) {
            throw new SaldoInsuficienteException("Saldo insuficiente para el plan mensual");
        }
        this.cuenta.debitar(PlanMensual.COSTO_PLAN, "Plan mensual " + mes);
        this.plan = new PlanMensual(mes);
    }

    public void darDeBajaPlan() throws ReglaNegocioException {
        if (this.plan == null) {
            throw new ReglaNegocioException("El usuario no tiene un plan activo para dar de baja.");
        }
        this.plan.cancelarRenovacion();
    }
    
    public void agregarMediosPago(TipoMedioPago tipo, String identificador) {
        MedioPago medioPago = new MedioPago(tipo, identificador);
        this.mediosPago.add(medioPago);
    }

    public void quitarMedioPago(TipoMedioPago tipo, String identificador) throws ReglaNegocioException {
        MedioPago aEliminar = null;
        for (MedioPago m : this.mediosPago) {
            if (m.getTipo() == tipo && m.getIdentificador().equals(identificador)) {
                aEliminar = m;
                break;
            }
        }
        if (aEliminar == null) {
            throw new ReglaNegocioException("El usuario no tiene asociado ese medio de pago.");
        }
        this.mediosPago.remove(aEliminar);
    }
    
    public void inhabilitar(){
        this.habilitado = false;
    }

    //Hay que agregarla al diagrama porque no existe
    public void cambiarNombre(String nombre){
        this.nombre = nombre;
    }

    public void iniciarViaje(Viaje viaje){
        this.viajeEnCurso = viaje;
    }

    public void finalizarViaje(){
        this.viajeEnCurso = null;
        this.yaViajo = true;
    }

    public void cancelarViaje(){
        this.viajeEnCurso = null;
        // No se marca yaViajo para no perder la promoción de primer viaje
    }

    public Viaje getViajeEnCurso(){
        return viajeEnCurso;
    }

    public boolean esPrimerViaje(){
        return !yaViajo;
    }

    public PlanMensual getPlan(){return plan;}
    public Cuenta getCuenta() {return cuenta;}
    public String getNombre() {return nombre;}
    public String getDocumento(){return documento;}
    public String getEmail() {return email;}
    public LocalDate getFechaAlta() {return fechaAlta;}
    public boolean isHabilitado() {return habilitado;}
    public List<MedioPago> getMediosPago() {return mediosPago;}
}