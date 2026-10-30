package controladores;

import enums.EstadoVehiculo;
import enums.TipoVehiculo;
import estaciones.Estacion;
import estaciones.Traslado;
import excepciones.EstacionLlenaException;
import excepciones.ReglaNegocioException;
import excepciones.VehiculoNoDisponibleException;
import personal.Operador;
import reloj.Reloj;
import vehiculos.Vehiculo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControladorEstaciones {
    private Map<String, Estacion> estaciones;
    private Map<String, Operador> operadores;
    private List<Traslado> traslados;
    private Reloj reloj;

    public ControladorEstaciones(Reloj reloj){
        this.estaciones = new HashMap<>();
        this.operadores = new HashMap<>();
        this.traslados = new ArrayList<>();
        this.reloj = reloj;
    }

    public Estacion registrarEstacion(String codigo, String direccion, double latitud, double longitud, Map<TipoVehiculo, Integer> capacidades) throws ReglaNegocioException{
        if (this.estaciones.containsKey(codigo)){
            throw new ReglaNegocioException("Ya existe una estación con el código: "+codigo);
        }
        for (Integer capacidad : capacidades.values()){
            if (capacidad <= 0){
                throw new ReglaNegocioException("La capacidad para cada vehículo debe ser mayor a cero.");
            }
        }

        Estacion nuevaEstacion = new Estacion(codigo, direccion, latitud, longitud, capacidades);
        this.estaciones.put(codigo, nuevaEstacion);
        return nuevaEstacion;
    }

    public void modificarEstacion(String codigo, String nuevaDireccion, double nuevaLatitud, double nuevaLongitud, Map<TipoVehiculo, Integer> nuevasCapacidades) throws ReglaNegocioException {
        Estacion estacion = buscarEstacion(codigo);
        for (Map.Entry<TipoVehiculo, Integer> entrada : nuevasCapacidades.entrySet()) {
            TipoVehiculo tipo = entrada.getKey();
            int capacidad = entrada.getValue();
            if (capacidad <= 0) {
                throw new ReglaNegocioException("La capacidad para cada vehículo debe ser mayor a cero.");
            }
            if (capacidad < estacion.cantidadVehiculosPorTipo(tipo)) {
                throw new ReglaNegocioException("La nueva capacidad no puede ser menor a los vehículos presentes para " + tipo);
            }
        }
        estacion.modificar(nuevaDireccion, nuevaLatitud, nuevaLongitud, nuevasCapacidades);
    }

    public Traslado registrarTraslado(String codOrigen, String codDestino, List<String> codVehiculos, String legajoOperador) throws ReglaNegocioException, VehiculoNoDisponibleException, EstacionLlenaException{
        if (codVehiculos == null || codVehiculos.isEmpty()){
            throw new ReglaNegocioException("Debe seleccionar al menos un vehículo para trasladar.");
        }
        Estacion origen = buscarEstacion(codOrigen);
        Estacion destino = buscarEstacion(codDestino);
        Operador operador = buscarOperador(legajoOperador);

        if (origen.getCodigo().equals(destino.getCodigo())){
            throw new ReglaNegocioException("La estación de origen y destino no pueden ser la misma.");
        }

        //Veo si todos los vehiculos que quieren trasladar estan libres
        List<Vehiculo> aTrasladar = new ArrayList<>();
        for (String codigo : codVehiculos){
            Vehiculo vehiculo = origen.buscarVehiculo(codigo);
            if (vehiculo == null || vehiculo.getEstado() == EstadoVehiculo.EN_USO){
                throw new VehiculoNoDisponibleException("El vehículo " + codigo + " no está en origen o se encuentra en uso");
            }
            aTrasladar.add(vehiculo);
        }

        //Veo si hay lugar para cada tipo de vehiculo en la estacion destino
        Map<TipoVehiculo, Integer> cantidadPorTipoATrasladar = new HashMap<>();
        for (Vehiculo vehiculo : aTrasladar){
            cantidadPorTipoATrasladar.put(vehiculo.getTipo(), cantidadPorTipoATrasladar.getOrDefault(vehiculo.getTipo(), 0) + 1);
        }
        for (Map.Entry<TipoVehiculo, Integer> entrada : cantidadPorTipoATrasladar.entrySet()){
            TipoVehiculo tipo = entrada.getKey();
            int cantidad = entrada.getValue();
            if (destino.lugaresLibres(tipo) < cantidad){
                throw new EstacionLlenaException("La estación destino no tiene lugar suficiente para "+tipo);
            }
        }

        //Muevo los vehiculso
        for (Vehiculo vehiculo : aTrasladar){
            origen.entregar(vehiculo.getCodigo());
            destino.recibir(vehiculo);
        }

        Traslado nuevoTraslado = new Traslado(origen, destino, aTrasladar, operador, this.reloj.ahora());
        this.traslados.add(nuevoTraslado);

        if (origen.necesitaRedistribucion()){
            generarAlerta(origen);
        }
        if (destino.necesitaRedistribucion()){
            generarAlerta(destino);
        }

        return nuevoTraslado;
    }

    public List<Estacion> getAlertasRedistribucion(){
        List<Estacion> enAlerta = new ArrayList<>();
        for (Estacion estacion : this.estaciones.values()){
            if (estacion.necesitaRedistribucion()){
                enAlerta.add(estacion);
            }
        }
        return enAlerta;
    }

    private void generarAlerta(Estacion estacion){
        double porcentaje = estacion.getOcupacion() * 100;
        System.out.println("Alerta. La estación "+estacion.getCodigo()+" se encuentra al "+porcentaje+"% de ocupación.");
    }

    public Estacion buscarEstacion(String codigo) throws ReglaNegocioException{
        Estacion estacion = this.estaciones.get(codigo);
        if (estacion == null){
            throw new ReglaNegocioException("No se encontró la estación: "+codigo);
        }
        return estacion;
    }

    public Operador buscarOperador(String legajo) throws ReglaNegocioException{
        Operador operador = this.operadores.get(legajo);
        if (operador == null) {
            throw new ReglaNegocioException("No se encontró el operador con legajo: " + legajo);
        }
        return operador;
    }

    public Map<String, Estacion> getEstaciones(){return estaciones;}
    public List<Traslado> getTraslados(){ return traslados;}
}
