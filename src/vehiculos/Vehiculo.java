package vehiculos;

import enums.EstadoVehiculo;
import enums.TipoVehiculo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public abstract class Vehiculo {
    private String codigo;
    private LocalDate fechaAlta;
    private double kmRecorridos;
    private EstadoVehiculo estado;
    private List<ReporteFalla> fallas;

    public boolean estaDisponible(){
        return this.estado == EstadoVehiculo.DISPONIBLE;
    }

    abstract TipoVehiculo getTipo();

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
}