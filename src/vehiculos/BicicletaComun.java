package vehiculos;

import enums.TipoVehiculo;

public class BicicletaComun extends Vehiculo {
    private int rodado;

    @Override
    public TipoVehiculo getTipo(){
        return TipoVehiculo.BICICLETA_COMUN;
    }


}
