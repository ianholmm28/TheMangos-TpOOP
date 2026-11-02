package controladores;

import estaciones.Estacion;
import estaciones.Traslado;
import personal.Operador;
import personal.Personal;
import personal.Tecnico;
import tarifas.Tarifa;
import usuarios.Usuario;
import vehiculos.OrdenMantenimiento;
import vehiculos.Vehiculo;
import viajes.Viaje;
import tarifas.CalculadorTarifa;

import java.io.*;
import java.util.List;
import java.util.Map;

public class ControladorPersistencia {

    private CalculadorTarifa calculador;
    private Map<String, Usuario> usuarios;
    private Map<String, Estacion> estaciones;
    private Map<String, Vehiculo> vehiculos;
    private Map<String, Personal> personal;
    private Map<String, Tecnico> tecnicos;
    private Map<String, Operador> operadores;
    private List<Viaje> viajes;
    private List<Traslado> traslados;
    private List<OrdenMantenimiento> ordenes;

    public ControladorPersistencia(
            Map<String, Usuario> usuarios,
            Map<String, Estacion> estaciones,
            Map<String, Vehiculo> vehiculos,
            Map<String, Personal> personal,
            Map<String, Tecnico> tecnicos,
            Map<String, Operador> operadores,
            List<Viaje> viajes,
            List<Traslado> traslados,
            List<OrdenMantenimiento> ordenes,
            CalculadorTarifa calculador) {
        this.usuarios = usuarios;
        this.estaciones = estaciones;
        this.vehiculos = vehiculos;
        this.personal = personal;
        this.tecnicos = tecnicos;
        this.operadores = operadores;
        this.viajes = viajes;
        this.traslados = traslados;
        this.ordenes = ordenes;
        this.calculador = calculador;
    }

    public void cargarDatos(){
        File archivo = new File("sistema.dat");
        if (!archivo.exists()){
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("sistema.dat"))){
            this.usuarios.putAll((Map<String, Usuario>) ois.readObject());
            this.estaciones.putAll((Map<String, Estacion>) ois.readObject());
            this.vehiculos.putAll((Map<String, Vehiculo>) ois.readObject());
            this.personal.putAll((Map<String, Personal>) ois.readObject());
            this.tecnicos.putAll((Map<String, Tecnico>) ois.readObject());
            this.operadores.putAll((Map<String, Operador>) ois.readObject());
            this.viajes.addAll((List<Viaje>) ois.readObject());
            this.traslados.addAll((List<Traslado>) ois.readObject());
            this.ordenes.addAll((List<OrdenMantenimiento>) ois.readObject());

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
}
