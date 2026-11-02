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
        this.nivelBateria = Math.max(0, Math.min(100, nivelBateria));
        if (this.nivelBateria < 15) {
            setEstado(EstadoVehiculo.BATERIA_BAJA);
        }
    }

    @Override
    public void actualizarBateria(int nivel){
        this.nivelBateria = Math.max(0, Math.min(100, nivel));
        if (getEstado() == EstadoVehiculo.DISPONIBLE && this.nivelBateria < 15){
            setEstado(EstadoVehiculo.BATERIA_BAJA);
        }
        else if (getEstado() == EstadoVehiculo.BATERIA_BAJA && this.nivelBateria >= 15){
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
