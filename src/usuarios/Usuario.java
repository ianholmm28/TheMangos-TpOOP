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
    
    public void recargarSaldo(double monto,TipoMedioPago tipo, String identificador) throws ReglaNegocioException {  
        MedioPago medio = null;
        for (MedioPago m : this.mediosPago) {
            if (m.getTipo() == tipo && m.getIdentificador().equals(identificador)) {
                medio = m;
            }
        }
        if (medio == null) {
            throw new ReglaNegocioException("El usuario no tiene ese medio de pago asociado");
        }
        this.cuenta.recargar(monto, "Recarga con " + tipo + " " + identificador);
        this.rehabilitar();
    }

    //Hay que agregarla al diagrama porque no existe
    public void rehabilitar() {
        if (!this.habilitado && this.cuenta.getSaldo() >= 0) {
            this.habilitado = true;
        }
    }

    public void suscribirPlan(YearMonth mes) throws SaldoInsuficienteException {
        if (!this.cuenta.tieneSaldo(PlanMensual.costoPlan)) {
            throw new SaldoInsuficienteException("Saldo insuficiente para el plan mensual");
        }
        this.cuenta.debitar(PlanMensual.costoPlan, "Plan mensual " + mes);
        this.plan = new PlanMensual(mes);
    }
    
    public void agregarMediosPago(TipoMedioPago tipo, String identificador) {
        MedioPago medioPago = new MedioPago(tipo, identificador);
        this.mediosPago.add(medioPago);
    }
    
    public void inhabilitar(){
        this.habilitado = false;
    }

    public void iniciarViaje(Viaje viaje){
        this.viajeEnCurso = viaje;
    }
    public void finalizarViaje(){
        this.viajeEnCurso = null;
        this.yaViajo = true;
    }
    public Viaje getViajeEnCurso(){
        return viajeEnCurso;
    }
    public boolean esPrimerViaje(){
        return !yaViajo;
    }
    public PlanMensual getPlan(){return plan;}

    public Cuenta getCuenta() {return cuenta;}

    public String getDocumento(){return documento;}
}