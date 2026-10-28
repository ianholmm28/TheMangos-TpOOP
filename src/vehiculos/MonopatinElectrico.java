package vehiculos;

import enums.EstadoVehiculo;
import enums.TipoVehiculo;

import java.time.LocalDate;

public class MonopatinElectrico extends Vehiculo implements Electrico {

    private int nivelBateria;
    private int velocidadMaxima;

    public MonopatinElectrico(String codigo, int velocidadMaxima, int nivelBateria, LocalDate fechaAlta){
        super(codigo, fechaAlta);
        this.velocidadMaxima = velocidadMaxima;
        this.nivelBateria = nivelBateria;
    }

    @Override
    public TipoVehiculo getTipo(){return TipoVehiculo.MONOPATIN_ELECTRICO;}

    @Override
    public boolean estaDisponible() {
        return super.estaDisponible() && this.nivelBateria >= 15;
    }

    @Override
    public int getNivelBateria(){return this.nivelBateria;}

    @Override public void actualizarBateria(int nivel){
        this.nivelBateria = nivel;
        if (this.nivelBateria < 15) {
            setEstado(EstadoVehiculo.BATERIA_BAJA);
        }
        else if (getEstado() == EstadoVehiculo.BATERIA_BAJA) {
            setEstado(EstadoVehiculo.DISPONIBLE);
        }
    }

    public int getVelocidadMaxima(){return velocidadMaxima;}
}
