package estaciones;

import personal.Operador;
import vehiculos.Vehiculo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Traslado implements Serializable {

    private Estacion origen;
    private Estacion destino;
    private List<Vehiculo> vehiculos;
    private Operador operador;
    private LocalDateTime fecha;

    public Traslado(Estacion origen, Estacion destino, List<Vehiculo> vehiculos, Operador operador, LocalDateTime fecha){
        this.origen = origen;
        this.destino = destino;
        this.vehiculos = new ArrayList<>(vehiculos);
        this.operador = operador;
        this.fecha = fecha;
    }

    public Estacion getOrigen() { return origen; }
    public Estacion getDestino() { return destino; }
    public List<Vehiculo> getVehiculos() { return vehiculos; }
    public Operador getOperador() { return operador; }
    public LocalDateTime getFecha() { return fecha; }
}
