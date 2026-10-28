package vehiculos;

import enums.EstadoVehiculo;
import enums.TipoVehiculo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Vehiculo {
    protected String codigo;
    protected LocalDate fechaAlta;
    protected double kmRecorridos;
    protected EstadoVehiculo estado;
    protected List<ReporteFalla> fallas;

    public Vehiculo(String codigo, LocalDate fechaAlta){
        this.codigo = codigo;
        this.fechaAlta = fechaAlta;
        this.kmRecorridos = 0.0;
        this.estado = EstadoVehiculo.DISPONIBLE;
        this.fallas = new ArrayList<>();
    }

    public void registrarFalla(String descripcion, LocalDateTime fecha){
        ReporteFalla reporte = new ReporteFalla(fecha, descripcion);
        this.fallas.add(reporte);
    }

    public int fallasUltimos30Dias(LocalDateTime fecha){
        LocalDateTime hace30Dias = fecha.minusDays(30);
        int contador = 0;
        for (ReporteFalla f : this.fallas){
            if(!f.getFecha().isBefore(hace30Dias)){
                contador++;
            }
        }
        return contador;
    }

    public void enviarAReparacion(){
        this.estado = EstadoVehiculo.EN_REPARACION;
    }

    public boolean estaDisponible(){
        return this.estado == EstadoVehiculo.DISPONIBLE;
    }
    public abstract TipoVehiculo getTipo();
    public String getCodigo(){return codigo;}
    public LocalDate getFechaAlta(){return fechaAlta;}
    public double getKmRecorridos() {return kmRecorridos;}
    public EstadoVehiculo getEstado(){return estado;}

    public void setEstado(EstadoVehiculo estado){
        this.estado = estado;
    }
}