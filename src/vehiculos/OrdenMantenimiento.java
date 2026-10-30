package vehiculos;

import excepciones.ReglaNegocioException;
import personal.Tecnico;

import java.io.Serializable;
import java.time.LocalDate;

public class OrdenMantenimiento implements Serializable {
    private int numero;
    private LocalDate fechaApertura;
    private LocalDate fechaCierre;
    private String tareas;
    private String repuestos;
    private Vehiculo vehiculo;
    private Tecnico tecnico;

    public OrdenMantenimiento(int numero, Vehiculo vehiculo, Tecnico tecnico, LocalDate fechaApertura){
        this.numero = numero;
        this.vehiculo = vehiculo;
        this.tecnico = tecnico;
        this.fechaApertura = fechaApertura;
        this.fechaCierre = null;
        this.tareas = "";
        this.repuestos = "";
    }

    public void cerrar(LocalDate fecha, String tareas, String respuestos) throws ReglaNegocioException {
        if (fecha == null || tareas == null || tareas.trim().isEmpty() || tecnico == null){
            throw new ReglaNegocioException("No se puede cerrar la orden sin fecha, tareas, y técnico asignado.");
        }
        this.fechaCierre = fecha;
        this.tareas = tareas;
        this.repuestos = respuestos;
    }

    public boolean estaAbierta(){return this.fechaCierre == null;}
    public int getNumero(){return numero;}
    public Vehiculo getVehiculo(){return vehiculo;}
    public Tecnico getTecnico(){return tecnico;}
    public LocalDate getFechaApertura(){return fechaApertura;}
    public LocalDate getFechaCierre(){return fechaCierre;}
    public String getTareas(){return tareas;}
    public String getRepuestos(){return repuestos;}
}
