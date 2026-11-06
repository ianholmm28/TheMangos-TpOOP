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
    private List<String> avisosAlerta;

    private final ControladorUsuarios controladorUsuarios;
    private final ControladorViajes controladorViajes;
    private final ControladorEstaciones controladorEstaciones;
    private final ControladorMantenimiento controladorMantenimiento;
    private final ControladorAdministracion controladorAdministracion;
    private final ControladorPersistencia controladorPersistencia;

    public Sistema() {
        this(new RelojSistema());
    }

    public Sistema(Reloj reloj) {
        this.reloj = (reloj != null) ? reloj : new RelojSistema();
        this.calculador = new CalculadorTarifa(this.reloj);
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
        this.avisosAlerta = new ArrayList<>();

        this.controladorPersistencia = new ControladorPersistencia(usuarios, estaciones, vehiculos, personal, tecnicos, operadores, viajes, traslados, ordenes, calculador);
        this.controladorPersistencia.cargarDatos();

        this.controladorUsuarios = new ControladorUsuarios(usuarios, calculador, this.reloj);
        this.controladorViajes = new ControladorViajes(usuarios, estaciones, viajes, avisosAlerta, calculador, this.reloj);
        this.controladorEstaciones = new ControladorEstaciones(estaciones, operadores, traslados, avisosAlerta, this.reloj);
        this.controladorMantenimiento = new ControladorMantenimiento(vehiculos, tecnicos, ordenes, this.reloj);
        this.controladorAdministracion = new ControladorAdministracion(vehiculos, estaciones, personal, tecnicos, operadores, ordenes, calculador, this.reloj);
        this.generadorReportes = new GeneradorReportes(viajes, vehiculos, estaciones);

        cargarTarifasIniciales();
        cargarPersonalInicial();

        this.controladorViajes.controlarViajesVencidos();
    }

    private void cargarTarifasIniciales() {
        cargarTarifaBase(TipoVehiculo.BICICLETA_COMUN, 200, 20);
        cargarTarifaBase(TipoVehiculo.BICICLETA_ELECTRICA, 300, 45);
        cargarTarifaBase(TipoVehiculo.MONOPATIN_ELECTRICO, 300, 50);
    }

    private void cargarTarifaBase(TipoVehiculo tipo, double desbloqueo, double precioMinuto) {
        LocalDate fechaBase = LocalDate.of(2000, 1, 1);
        boolean tieneTarifa = false;
        for (Tarifa tarifa : calculador.getTarifas()) {
            if (tarifa.getTipo() == tipo) {
                tieneTarifa = true;
                break;
            }
        }
        if (!tieneTarifa) {
            calculador.agregarTarifa(new Tarifa(tipo, fechaBase, desbloqueo, precioMinuto));
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
