package viajes;

import enums.EstadoViaje;
import estaciones.Estacion;
import usuarios.Usuario;
import vehiculos.Vehiculo;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Viaje implements Serializable {
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private double distancia;
    private EstadoViaje estado;
    private double importe;
    private int minutosBonificados;
    private Usuario usuario;
    private Vehiculo vehiculo;
    private Estacion origen;
    private Estacion destino;
    private List<PuntoRecorrido> puntos;
    private List<NotaAjuste> notasAjuste;

    public Viaje(Usuario usuario, Vehiculo vehiculo, Estacion origen, LocalDateTime inicio){
        this.usuario = usuario;
        this.vehiculo = vehiculo;
        this.origen = origen;
        this.inicio = inicio;
        this.fin = null;
        this.distancia = 0;
        this.estado = EstadoViaje.EN_CURSO;
        this.importe = 0;
        this.minutosBonificados = 0;
        this.puntos = new ArrayList<>();
        this.notasAjuste = new ArrayList<>();
    }

    public void agregarPunto(double latitud, double longitud, LocalDateTime hora){
        PuntoRecorrido nuevoPunto = new PuntoRecorrido(hora, longitud, latitud);

        if(!this.puntos.isEmpty()){
            PuntoRecorrido ultimoPunto = this.puntos.get(this.puntos.size() - 1);
            this.distancia += calcularDistanciaKm(ultimoPunto, nuevoPunto);
        }
        this.puntos.add(nuevoPunto);
    }

    public void agregarPunto(LocalDateTime hora, double longitud, double latitud){
        agregarPunto(latitud, longitud, hora);
    }

    public void cerrar(Estacion destino, LocalDateTime fin){
        this.destino = destino;
        this.fin = fin;
        this.estado = EstadoViaje.CERRADO;
    }

    public void registrarIntentoEstacionLlena(LocalDateTime hora){
        this.minutosBonificados = 10;
    }

    public void registrarIntentoEstacionLlena(){
        this.minutosBonificados = 10;
    }

    public long getMinutosCobrables(){
        LocalDateTime momentoFin = this.fin != null ? this.fin : this.inicio;
        long segundos = Duration.between(this.inicio, momentoFin).getSeconds();
        long minutosTotales = (long) Math.ceil(segundos / 60.0);
        long minutosCobrables = minutosTotales - this.minutosBonificados;
        return Math.max(0, minutosCobrables);
    }

    public void agregarNotaAjuste(LocalDateTime fecha, String motivo, double importe){
        NotaAjuste nota = new NotaAjuste(fecha, motivo, importe);
        this.notasAjuste.add(nota);
    }

    private double calcularDistanciaKm(PuntoRecorrido p1, PuntoRecorrido p2){
        double distanciaLatitud = p2.getLatitud() - p1.getLatitud();
        double distanciaLongitud = p2.getLongitud() - p1.getLongitud();
        double distanciaEnGrados = Math.sqrt(distanciaLatitud * distanciaLatitud + distanciaLongitud * distanciaLongitud);
        return distanciaEnGrados * 111;
    }

    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFin() { return fin; }
    public double getDistancia() { return distancia; }
    public EstadoViaje getEstado() { return estado; }
    public void setEstado(EstadoViaje estado) { this.estado = estado; }
    public double getImporte() { return importe; }
    public void setImporte(double importe) { this.importe = importe; }
    public int getMinutosBonificados() { return minutosBonificados; }
    public Usuario getUsuario() { return usuario; }
    public Vehiculo getVehiculo() { return vehiculo; }
    public Estacion getOrigen() { return origen; }
    public Estacion getDestino() { return destino; }
    public List<PuntoRecorrido> getPuntos() { return puntos; }
    public List<NotaAjuste> getNotasAjuste() { return notasAjuste; }
}