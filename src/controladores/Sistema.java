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
import tarifas.Tarifa;
import usuarios.Usuario;
import vehiculos.OrdenMantenimiento;
import vehiculos.Vehiculo;
import viajes.Viaje;

import java.io.*;
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

    public void cargarDatos(){
        File archivo = new File("sistema.dat");
        if (!archivo.exists()){
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("sistema.dat"))){
            this.usuarios = (Map<String, Usuario>) ois.readObject();
            this.estaciones = (Map<String, Estacion>) ois.readObject();
            this.vehiculos = (Map<String, Vehiculo>) ois.readObject();
            this.personal = (Map<String, Personal>) ois.readObject();
            this.tecnicos = (Map<String, Tecnico>) ois.readObject();
            this.operadores = (Map<String, Operador>) ois.readObject();
            this.viajes = (List<Viaje>) ois.readObject();
            this.traslados = (List<Traslado>) ois.readObject();
            this.ordenes = (List<OrdenMantenimiento>) ois.readObject();

            List<Tarifa> tarifas = (List<Tarifa>) ois.readObject();
            for (Tarifa tarifa : tarifas){
                this.calculador.agregarTarifa(tarifa);
            }
         }
        catch (IOException | ClassNotFoundException e){
            System.out.println("Error de E/S: "+e.getMessage());
        }
    }

    public void guardarDatos(){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("sistema.dat"))){
            oos.writeObject(this.usuarios);
            oos.writeObject(this.estaciones);
            oos.writeObject(this.vehiculos);
            oos.writeObject(this.personal);
            oos.writeObject(this.tecnicos);
            oos.writeObject(this.operadores);
            oos.writeObject(this.viajes);
            oos.writeObject(this.traslados);
            oos.writeObject(this.ordenes);
            oos.writeObject(this.calculador.getTarifas());
        }
        catch (IOException e){
            System.out.println("Error de E/S: "+e.getMessage());
        }
    }

    public ControladorUsuarios getControladorUsuarios() { return controladorUsuarios; }
    public ControladorViajes getControladorViajes() { return controladorViajes; }
    public ControladorEstaciones getControladorEstaciones() { return controladorEstaciones; }
    public ControladorMantenimiento getControladorMantenimiento() { return controladorMantenimiento; }
    public ControladorAdministracion getControladorAdministracion() { return controladorAdministracion; }
    public GeneradorReportes getGeneradorReportes() { return generadorReportes; }
    public Reloj getReloj() { return reloj; }
    public CalculadorTarifa getCalculador() { return calculador; }
}
