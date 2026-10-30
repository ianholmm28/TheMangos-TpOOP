package controladores;

import estaciones.Estacion;
import estaciones.Traslado;
import personal.Operador;
import personal.Personal;
import personal.Tecnico;
import reloj.Reloj;
import reloj.RelojSistema;
import reportes.GeneradorReportes;
import tarifas.CalculadorTarifa;
import usuarios.Usuario;
import vehiculos.OrdenMantenimiento;
import vehiculos.Vehiculo;
import viajes.Viaje;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sistema {

    private final Reloj reloj;
    private final CalculadorTarifa calculador;
    private final GeneradorReportes generadorReportes;

    private final Map<String, Usuario> usuarios;
    private final Map<String, Estacion> estaciones;
    private final Map<String, Vehiculo> vehiculos;
    private final Map<String, Personal> personal;
    private final Map<String, Tecnico> tecnicos;
    private final Map<String, Operador> operadores;
    private final List<Viaje> viajes;
    private final List<Traslado> traslados;
    private final List<OrdenMantenimiento> ordenes;

    private final ControladorUsuarios controladorUsuarios;
    private final ControladorViajes controladorViajes;
    private final ControladorEstaciones controladorEstaciones;
    private final ControladorMantenimiento controladorMantenimiento;
    private final ControladorAdministracion controladorAdministracion;

    public Sistema() {
        this.reloj = new RelojSistema();
        this.calculador = new CalculadorTarifa();
        this.calculador.agregarPromocion(new tarifas.PromocionPrimerViaje());
        this.calculador.agregarPromocion(new tarifas.PromocionPlanMensual());

        this.usuarios = new HashMap<>();
        this.estaciones = new HashMap<>();
        this.vehiculos = new HashMap<>();
        this.personal = new HashMap<>();
        this.tecnicos = new HashMap<>();
        this.operadores = new HashMap<>();
        this.viajes = new ArrayList<>();
        this.traslados = new ArrayList<>();
        this.ordenes = new ArrayList<>();

        cargarDatos();

        this.controladorUsuarios = new ControladorUsuarios(usuarios, calculador);
        this.controladorViajes = new ControladorViajes(usuarios, estaciones, viajes, calculador, reloj);
        this.controladorEstaciones = new ControladorEstaciones(estaciones, operadores, traslados, reloj);
        this.controladorMantenimiento = new ControladorMantenimiento(vehiculos, tecnicos, ordenes, reloj);
        this.controladorAdministracion = new ControladorAdministracion(vehiculos, estaciones, personal, tecnicos, operadores, calculador);

        this.generadorReportes = new GeneradorReportes(viajes, vehiculos, estaciones);
    }

    public void cargarDatos(){}

    public void guardarDatos(){}

    public ControladorUsuarios getControladorUsuarios() { return controladorUsuarios; }
    public ControladorViajes getControladorViajes() { return controladorViajes; }
    public ControladorEstaciones getControladorEstaciones() { return controladorEstaciones; }
    public ControladorMantenimiento getControladorMantenimiento() { return controladorMantenimiento; }
    public ControladorAdministracion getControladorAdministracion() { return controladorAdministracion; }
    public GeneradorReportes getGeneradorReportes() { return generadorReportes; }
    public Reloj getReloj() { return reloj; }
    public CalculadorTarifa getCalculador() { return calculador; }
}
