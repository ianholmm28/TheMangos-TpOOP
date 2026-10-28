package controladores;

import reloj.Reloj;
import vehiculos.Vehiculo;

import java.util.Map;

public class ControladorMantenimiento {
    private Map<String, Vehiculo> vehiculos;
    private Map<String, Tecnico> tecnicos;
    private List<OrdenMantenimiento> ordenes;
    private Reloj reloj;

    public ControladorMantenimiento(Reloj reloj){
        this.reloj = reloj;
    }

    public void reportarFalla(String codVehiculo, String descripcion){

    }
}
