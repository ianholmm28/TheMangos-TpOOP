package vehiculos;

import enums.TipoVehiculo;

import java.time.LocalDate;

public class BicicletaComun extends Vehiculo {
    private int rodado;

    public BicicletaComun(String codigo, int rodado, LocalDate fechaAlta){
        super(codigo, fechaAlta);
        this.rodado = rodado;
    }

    @Override
    public TipoVehiculo getTipo(){
        return TipoVehiculo.BICICLETA_COMUN;
    }
    public int getRodado(){return rodado;}

}
