package controladores;

import enums.TipoVehiculo;
import estaciones.Estacion;
import excepciones.ReglaNegocioException;
import estaciones.Traslado;
import personal.Administrador;
import personal.Operador;
import personal.Personal;
import personal.Tecnico;
import reloj.Reloj;
import reloj.RelojSistema;
import reportes.GeneradorReportes;
import tarifas.CalculadorTarifa;
import tarifas.Tarifa;
import usuarios.Usuario;
import vehiculos.OrdenMantenimiento;
import vehiculos.Vehiculo;
import viajes.Viaje;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sistema {

    // Tarifa fija, igual para todos los vehiculos
    public static final double CARGO_DESBLOQUEO = 500;
    public static final double PRECIO_MINUTO = 80;

    private final Reloj reloj;
    private final CalculadorTarifa calculador;
    private final GeneradorReportes generadorReportes;

    private Map<String, Usuario> usuarios;
    private Map<String, Estacion> estaciones;
    private Map<String, Vehiculo> vehiculos;
    private Map<String, Personal> personal;
    private Map<String, Tecnico> tecnicos;
    private Map<String, Operador> operadores;
    private List<Viaje> viajes;
    private List<Traslado> traslados;
    private List<OrdenMantenimiento> ordenes;

    private final ControladorUsuarios controladorUsuarios;
    private final ControladorViajes controladorViajes;
    private final ControladorEstaciones controladorEstaciones;
    private final ControladorMantenimiento controladorMantenimiento;
    private final ControladorAdministracion controladorAdministracion;
    private final ControladorPersistencia controladorPersistencia;

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

        this.controladorPersistencia = new ControladorPersistencia(usuarios, estaciones, vehiculos, personal, tecnicos, operadores, viajes, traslados, ordenes, calculador);
        this.controladorPersistencia.cargarDatos();

        this.controladorUsuarios = new ControladorUsuarios(usuarios, calculador);
        this.controladorViajes = new ControladorViajes(usuarios, estaciones, viajes, calculador, reloj);
        this.controladorEstaciones = new ControladorEstaciones(estaciones, operadores, traslados, reloj);
        this.controladorMantenimiento = new ControladorMantenimiento(vehiculos, tecnicos, ordenes, reloj);
        this.controladorAdministracion = new ControladorAdministracion(vehiculos, estaciones, personal, tecnicos, operadores, calculador);
        this.generadorReportes = new GeneradorReportes(viajes, vehiculos, estaciones);

        cargarTarifaFija();
        cargarPersonalInicial();

        this.controladorViajes.controlarViajesVencidos();
    }

    // Carga la tarifa fija para cada tipo de vehiculo si todavia no tiene una
    private void cargarTarifaFija() {
        for (TipoVehiculo tipo : TipoVehiculo.values()) {
            boolean tieneTarifa = false;
            for (Tarifa tarifa : calculador.getTarifas()) {
                if (tarifa.getTipo() == tipo) {
                    tieneTarifa = true;
                }
            }
            if (!tieneTarifa) {
                calculador.agregarTarifa(new Tarifa(tipo, LocalDate.of(2000, 1, 1), CARGO_DESBLOQUEO, PRECIO_MINUTO));
            }
        }
    }

    // Crea un administrador, un operador y un tecnico de prueba si todavia no existen
    private void cargarPersonalInicial() {
        cargarPersonalSiFalta(new Administrador("1", "Administrador", "-"));
        cargarPersonalSiFalta(new Operador("1001", "Operador", "-"));
        cargarPersonalSiFalta(new Tecnico("2001", "Técnico", "-"));
    }

    private void cargarPersonalSiFalta(Personal nuevo) {
        if (personal.containsKey(nuevo.getLegajo())) {
            return;
        }
        try {
            controladorAdministracion.registrarPersonal(nuevo);
        } catch (ReglaNegocioException e) {
            System.out.println("No se pudo cargar el personal inicial: " + e.getMessage());
        }
    }

    public ControladorUsuarios getControladorUsuarios() { return controladorUsuarios; }
    public ControladorViajes getControladorViajes() { return controladorViajes; }
    public ControladorEstaciones getControladorEstaciones() { return controladorEstaciones; }
    public ControladorMantenimiento getControladorMantenimiento() { return controladorMantenimiento; }
    public ControladorAdministracion getControladorAdministracion() { return controladorAdministracion; }
    public ControladorPersistencia getControladorPersistencia() {return controladorPersistencia;}
    public GeneradorReportes getGeneradorReportes() { return generadorReportes; }
}
