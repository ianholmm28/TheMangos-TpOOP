package controladores;

import enums.EstadoVehiculo;
import enums.EstadoViaje;
import estaciones.Estacion;
import excepciones.*;
import reloj.Reloj;
import tarifas.CalculadorTarifa;
import usuarios.Usuario;
import vehiculos.Electrico;
import vehiculos.Vehiculo;
import viajes.Viaje;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControladorViajes {
    private Map<String, Usuario> usuarios;
    private Map<String, Estacion> estaciones;
    private List<Viaje> viajes;
    private CalculadorTarifa calculador;
    private Reloj reloj;

    public ControladorViajes(CalculadorTarifa calculador, Reloj reloj) {
        this.usuarios = new HashMap<>();
        this.estaciones = new HashMap<>();
        this.viajes = new ArrayList<>();
        this.calculador = calculador;
        this.reloj = reloj;
    }

    public Viaje retirarVehiculo(String documento, String codEstacion, String codVehiculo) throws ReglaNegocioException {
        Usuario usuario = buscarUsuario(documento);
        Estacion estacion = buscarEstacion(codEstacion);
        Vehiculo vehiculo = estacion.buscarVehiculo(codVehiculo);

        if (vehiculo == null || !vehiculo.estaDisponible()) {
            throw new VehiculoNoDisponibleException("El vehículo no está disponible en la estación: " +codVehiculo);
        }
        double saldoRequerido = this.calculador.saldoMinimo(vehiculo.getTipo());
        usuario.verificarHabilitacion(saldoRequerido);

        estacion.entregar(codVehiculo);
        vehiculo.setEstado(EstadoVehiculo.EN_USO);

        Viaje nuevoViaje = new Viaje(usuario, vehiculo, estacion, this.reloj.ahora());
        usuario.iniciarViaje(nuevoViaje);
        this.viajes.add(nuevoViaje);

        return nuevoViaje;
    }

    public void cancelarRetiro(String documento) throws ReglaNegocioException {
        Usuario usuario = buscarUsuario(documento);
        Viaje viaje = usuario.getViajeEnCurso();
        if (viaje == null) {
            throw new ReglaNegocioException("El usuario no tiene ningún viaje en curso para cancelar.");
        }

        long segundosTranscurridos = Duration.between(viaje.getInicio(), this.reloj.ahora()).getSeconds();
        if (segundosTranscurridos > 120) {
            throw new ReglaNegocioException("Pasaron más de 2 minutos. No se puede cancelar, debe devolver el vehículo.");
        }

        viaje.setEstado(EstadoViaje.CANCELADO);
        Vehiculo vehiculo = viaje.getVehiculo();
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        viaje.getOrigen().recibir(vehiculo);
        usuario.cancelarViaje();
    }

    public void registrarPunto(String documento, double lat, double lon) throws ReglaNegocioException {
        Usuario usuario = buscarUsuario(documento);
        Viaje viaje = usuario.getViajeEnCurso();
        if (viaje == null) {
            throw new ReglaNegocioException("El usuario no tiene un viaje en curso.");
        }
        viaje.agregarPunto(lat, lon, this.reloj.ahora());
    }

    public Viaje devolverVehiculo(String documento, String codEstacion) throws ReglaNegocioException {
        return devolverVehiculo(documento, codEstacion, -1);
    }

    public Viaje devolverVehiculo(String documento, String codEstacion, int nivelBateria) throws ReglaNegocioException {
        Usuario usuario = buscarUsuario(documento);
        Viaje viaje = usuario.getViajeEnCurso();
        if (viaje == null) {
            throw new ReglaNegocioException("El usuario no tiene un viaje en curso para devolver.");
        }

        Estacion destino = buscarEstacion(codEstacion);
        Vehiculo vehiculo = viaje.getVehiculo();

        if (destino.estaLlenaPara(vehiculo.getTipo())) {
            viaje.registrarIntentoEstacionLlena(this.reloj.ahora());
            throw new EstacionLlenaException("La estación de destino está llena. Dispone de 10 minutos sin cargo para llegar a otra.");
        }

        viaje.cerrar(destino, this.reloj.ahora());
        destino.recibir(vehiculo);

        if (nivelBateria >= 0 && vehiculo instanceof Electrico) {
            ((Electrico) vehiculo).actualizarBateria(nivelBateria);
        }

        if (vehiculo.getEstado() != EstadoVehiculo.EN_REPARACION) {
            if (vehiculo instanceof Electrico && ((Electrico) vehiculo).getNivelBateria() < 15) {
                vehiculo.setEstado(EstadoVehiculo.BATERIA_BAJA);
            } else {
                vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
            }
        }

        vehiculo.agregarKm(viaje.getDistancia());

        double importe = this.calculador.calcular(viaje);
        viaje.setImporte(importe);
        usuario.getCuenta().debitar(importe, "Cierre de viaje #" + this.viajes.indexOf(viaje));

        if (usuario.getCuenta().getSaldo() < 0) {
            usuario.inhabilitar();
        }
        usuario.finalizarViaje();
        return viaje;
    }

    public void registrarNotaAjuste(int idViaje, String motivo, double importe) throws ReglaNegocioException {
        if (idViaje < 0 || idViaje >= this.viajes.size()) {
            throw new ReglaNegocioException("No existe el viaje indicado.");
        }
        Viaje viaje = this.viajes.get(idViaje);
        if (viaje.getEstado() != EstadoViaje.CERRADO) {
            throw new ReglaNegocioException("Solo se pueden aplicar notas de ajuste a viajes cerrados.");
        }

        viaje.agregarNotaAjuste(this.reloj.ahora(), motivo, importe);
        viaje.getUsuario().getCuenta().ajustar(importe, "Nota de ajuste: " + motivo);

        if (viaje.getUsuario().getCuenta().getSaldo() >= 0) {
            viaje.getUsuario().rehabilitar();
        } else {
            viaje.getUsuario().inhabilitar();
        }
    }

    public List<Viaje> controlarViajesVencidos() {
        List<Viaje> vencidos = new ArrayList<>();
        for (Viaje v : this.viajes) {
            if (v.getEstado() == EstadoViaje.EN_CURSO) {
                long horas = Duration.between(v.getInicio(), this.reloj.ahora()).toHours();
                if (horas >= 24) {
                    v.setEstado(EstadoViaje.PERDIDO);
                    v.setImporte(150000.0);
                    v.getVehiculo().setEstado(EstadoVehiculo.FUERA_DE_SERVICIO);
                    v.getUsuario().getCuenta().debitar(150000.0, "Cargo fijo por vehículo perdido");
                    if (v.getUsuario().getCuenta().getSaldo() < 0) {
                        v.getUsuario().inhabilitar();
                    }
                    v.getUsuario().finalizarViaje();
                    vencidos.add(v);
                }
            }
        }
        return vencidos;
    }

    public Usuario buscarUsuario(String documento) throws UsuarioNoEncontradoException {
        Usuario u = this.usuarios.get(documento);
        if (u == null) {
            throw new UsuarioNoEncontradoException("No existe el usuario con documento: " + documento);
        }
        return u;
    }

    public Estacion buscarEstacion(String codigo) throws ReglaNegocioException {
        Estacion e = this.estaciones.get(codigo);
        if (e == null) {
            throw new ReglaNegocioException("No existe la estación: " + codigo);
        }
        return e;
    }

    public void agregarUsuario(Usuario u) { this.usuarios.put(u.getDocumento(), u); }
    public void agregarEstacion(Estacion e) { this.estaciones.put(e.getCodigo(), e); }
    public List<Viaje> getViajes() { return this.viajes; }
}