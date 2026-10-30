package controladores;

import enums.EstadoVehiculo;
import enums.TipoVehiculo;
import estaciones.Estacion;
import excepciones.EstacionLlenaException;
import excepciones.ReglaNegocioException;
import excepciones.VehiculoNoEncontradoException;
import personal.Operador;
import personal.Personal;
import personal.Tecnico;
import tarifas.CalculadorTarifa;
import tarifas.Promocion;
import tarifas.Tarifa;
import vehiculos.BicicletaComun;
import vehiculos.BicicletaElectrica;
import vehiculos.Electrico;
import vehiculos.MonopatinElectrico;
import vehiculos.Vehiculo;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ControladorAdministracion {
    private Map<String, Vehiculo> vehiculos;
    private Map<String, Estacion> estaciones;
    private Map<String, Personal> personal;
    private Map<String, Tecnico> tecnicos;
    private Map<String, Operador> operadores;
    private CalculadorTarifa calculador;

    public ControladorAdministracion(CalculadorTarifa calculador) {
        this.vehiculos = new HashMap<>();
        this.estaciones = new HashMap<>();
        this.personal = new HashMap<>();
        this.tecnicos = new HashMap<>();
        this.operadores = new HashMap<>();
        this.calculador = calculador;
    }

    public ControladorAdministracion(Map<String, Vehiculo> vehiculos, Map<String, Estacion> estaciones, Map<String, Personal> personal, CalculadorTarifa calculador) {
        this(vehiculos, estaciones, personal, new HashMap<>(), new HashMap<>(), calculador);
    }

    public ControladorAdministracion(Map<String, Vehiculo> vehiculos, Map<String, Estacion> estaciones, Map<String, Personal> personal, Map<String, Tecnico> tecnicos, Map<String, Operador> operadores, CalculadorTarifa calculador) {
        this.vehiculos = (vehiculos != null) ? vehiculos : new HashMap<>();
        this.estaciones = (estaciones != null) ? estaciones : new HashMap<>();
        this.personal = (personal != null) ? personal : new HashMap<>();
        this.tecnicos = (tecnicos != null) ? tecnicos : new HashMap<>();
        this.operadores = (operadores != null) ? operadores : new HashMap<>();
        this.calculador = calculador;
    }

    public Vehiculo altaVehiculo(TipoVehiculo tipo, String codigo, Map<String, Object> datos, String codEstacion) throws ReglaNegocioException {
        if (this.vehiculos.containsKey(codigo)) {
            throw new ReglaNegocioException("Ya existe un vehículo con el código: " + codigo);
        }

        Estacion estacion = this.estaciones.get(codEstacion);
        if (estacion == null) {
            throw new ReglaNegocioException("No se encontró la estación inicial: " + codEstacion);
        }

        if (estacion.estaLlenaPara(tipo)) {
            throw new EstacionLlenaException("La estación " + codEstacion + " no tiene lugar disponible para: " + tipo);
        }

        Vehiculo nuevo;
        LocalDate hoy = LocalDate.now();

        if (datos == null) {
            datos = new HashMap<>();
        }

        switch (tipo) {
            case BICICLETA_COMUN -> {
                int rodado = (int) datos.getOrDefault("rodado", 26);
                nuevo = new BicicletaComun(codigo, rodado, hoy);
            }
            case BICICLETA_ELECTRICA -> {
                int autonomia = (int) datos.getOrDefault("autonomia", 40);
                int bateria = (int) datos.getOrDefault("bateria", 100);
                nuevo = new BicicletaElectrica(codigo, autonomia, bateria, hoy);
            }
            case MONOPATIN_ELECTRICO -> {
                int velMax = (int) datos.getOrDefault("velocidadMaxima", 25);
                int bateria = (int) datos.getOrDefault("bateria", 100);
                nuevo = new MonopatinElectrico(codigo, velMax, bateria, hoy);
            }
            default -> throw new ReglaNegocioException("Tipo de vehículo no reconocido: " + tipo);
        }

        if (nuevo instanceof Electrico && ((Electrico) nuevo).getNivelBateria() < 15) {
            nuevo.setEstado(EstadoVehiculo.BATERIA_BAJA);
        }

        estacion.recibir(nuevo);
        this.vehiculos.put(codigo, nuevo);
        return nuevo;
    }

    public void bajaVehiculo(String codigo) throws ReglaNegocioException {
        Vehiculo v = this.vehiculos.get(codigo);
        if (v == null) {
            throw new VehiculoNoEncontradoException("No se encontró el vehículo con código: " + codigo);
        }
        if (v.getEstado() == EstadoVehiculo.EN_USO) {
            throw new ReglaNegocioException("No se puede dar de baja el vehículo " + codigo + " porque se encuentra en uso.");
        }
        v.setEstado(EstadoVehiculo.FUERA_DE_SERVICIO);
        for (Estacion e : this.estaciones.values()) {
            if (e.buscarVehiculo(codigo) != null) {
                e.entregar(codigo);
                break;
            }
        }
    }

    public void registrarPersonal(Personal p) throws ReglaNegocioException {
        if (p == null || p.getLegajo() == null || p.getLegajo().trim().isEmpty()
                || p.getNombre() == null || p.getNombre().trim().isEmpty()) {
            throw new ReglaNegocioException("Los datos obligatorios del personal (legajo y nombre) no pueden ser vacíos.");
        }
        if (this.personal.containsKey(p.getLegajo())) {
            throw new ReglaNegocioException("Ya existe personal registrado con el legajo: " + p.getLegajo());
        }
        if (p instanceof personal.Administrador) {
            for (Personal existente : this.personal.values()) {
                if (existente instanceof personal.Administrador) {
                    throw new ReglaNegocioException("Ya existe un administrador en el sistema.");
                }
            }
        }
        this.personal.put(p.getLegajo(), p);
        if (p instanceof Tecnico t) {
            this.tecnicos.put(t.getLegajo(), t);
        } else if (p instanceof Operador o) {
            this.operadores.put(o.getLegajo(), o);
        }
    }

    public void registrarTarifa(Tarifa t) throws ReglaNegocioException {
        if (t == null) {
            throw new ReglaNegocioException("La tarifa no puede ser nula.");
        }
        if (t.getCargoDesbloqueo() < 0 || t.getPrecioMinuto() < 0) {
            throw new ReglaNegocioException("Los valores de la tarifa no pueden ser negativos.");
        }
        if (t.getVigenteDesde() == null) {
            throw new ReglaNegocioException("La fecha de vigencia es inválida.");
        }
        this.calculador.agregarTarifa(t);
    }

    public void registrarPromocion(Promocion p) throws ReglaNegocioException {
        if (p == null) {
            throw new ReglaNegocioException("La promoción no puede ser nula.");
        }
        this.calculador.agregarPromocion(p);
    }

    public void agregarEstacion(Estacion e) { this.estaciones.put(e.getCodigo(), e); }
    public Map<String, Vehiculo> getVehiculos() { return vehiculos; }
    public Map<String, Estacion> getEstaciones() { return estaciones; }
    public Map<String, Personal> getPersonal() { return personal; }
    public CalculadorTarifa getCalculador() { return calculador; }
}