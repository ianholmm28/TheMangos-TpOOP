package vehiculos;

import enums.EstadoVehiculo;
import enums.TipoVehiculo;

import java.time.LocalDate;

public class BicicletaElectrica extends Vehiculo implements Electrico {
    private int nivelBateria;
    private int autonomia;

    public BicicletaElectrica(String codigo, int autonomia, int nivelBateria, LocalDate fechaAlta){
        super(codigo, fechaAlta);
        this.autonomia = autonomia;
        this.nivelBateria = nivelBateria;
    }

    @Override
     public void actualizarBateria(int nivel){
        this.nivelBateria = nivel;
        if (nivel<15){
            setEstado(EstadoVehiculo.BATERIA_BAJA);
        }
        else if (getEstado() == EstadoVehiculo.BATERIA_BAJA){
            setEstado(EstadoVehiculo.DISPONIBLE);
        }
    }

    @Override
    public TipoVehiculo getTipo(){return TipoVehiculo.BICICLETA_ELECTRICA;}

    @Override
    public int getNivelBateria(){return nivelBateria;}

    @Override
    public boolean estaDisponible(){
        return super.estaDisponible() && this.nivelBateria >= 15;
    }

    public int getAutonomia(){return autonomia;}

}
