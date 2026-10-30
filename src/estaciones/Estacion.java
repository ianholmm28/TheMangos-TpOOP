package estaciones;

import enums.TipoVehiculo;
import vehiculos.Vehiculo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Estacion {
    private String codigo;
    private String direccion;
    private double latitud;
    private double longitud;
    private Map<TipoVehiculo, Integer> capacidadPorTipo;
    private List<Vehiculo> vehiculos;

    public Estacion(String codigo, String direccion, double latitud, double longitud, Map<TipoVehiculo, Integer> capacidadPorTipo){
        this.codigo = codigo;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.capacidadPorTipo = new HashMap<>(capacidadPorTipo);
        this.vehiculos = new ArrayList<>();
    }

    public Vehiculo buscarVehiculo(String codigo){
        for (Vehiculo vehiculo : this.vehiculos){
            if (vehiculo.getCodigo().equals(codigo)){
                return vehiculo;
            }
        }
        return null;
    }

    public Vehiculo entregar(String codigo){
        Vehiculo vehiculo = buscarVehiculo(codigo);
        if (vehiculo != null){
            this.vehiculos.remove(vehiculo);
        }
        return vehiculo;
    }

    public void recibir(Vehiculo vehiculo){
        if (vehiculo != null){
            this.vehiculos.add(vehiculo);
        }
    }

    public int cantidadVehiculosPorTipo(TipoVehiculo tipo){
        int contador = 0;
        for (Vehiculo vehiculo : this.vehiculos){
            if (vehiculo.getTipo() == tipo){
                contador++;
            }
        }
        return contador;
    }

    public int cantidadVehiculosDisponiblesPorTipo(TipoVehiculo tipo){
        int contador = 0;
        for (Vehiculo vehiculo : this.vehiculos){
            if (vehiculo.getTipo() == tipo && vehiculo.estaDisponible()){
                contador++;
            }
        }
        return contador;
    }

    public void modificar(String direccion, double latitud, double longitud, Map<TipoVehiculo, Integer> capacidades){
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.capacidadPorTipo = new HashMap<>(capacidades);
    }

    public int lugaresLibres(TipoVehiculo tipo){
        int capacidad = this.capacidadPorTipo.getOrDefault(tipo, 0);
        int presentes = cantidadVehiculosPorTipo(tipo);
        return capacidad - presentes;
    }

    public boolean estaLlenaPara(TipoVehiculo tipo){
        return lugaresLibres(tipo) <= 0;
    }

    public int getCapacidadTotal(){
        int total = 0;
        for (int capacidad : this.capacidadPorTipo.values()){
            total += capacidad;
        }
        return total;
    }

    public double getOcupacion(){
        int capacidadTotal = getCapacidadTotal();
        if (capacidadTotal == 0){
            return 0;
        }
        double ocupacion = (double) this.vehiculos.size() / capacidadTotal;
        return ocupacion;
    }

    public boolean necesitaRedistribucion(){
        if (getCapacidadTotal() == 0){
            return false;
        }
        double ocupacion = getOcupacion();
        return ocupacion < 0.2 || ocupacion > 0.9;
    }

    public String getCodigo(){return codigo;}
    public String getDireccion(){return direccion;}
    public double getLatitud(){return latitud;}
    public double getLongitud(){return longitud;}
    public Map<TipoVehiculo, Integer> getCapacidadPorTipo(){return capacidadPorTipo;}
    public List<Vehiculo> getVehiculos(){return vehiculos;}
}
