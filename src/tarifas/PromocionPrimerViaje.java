package tarifas;

import viajes.Viaje;

public class PromocionPrimerViaje implements Promocion {

    @Override
    public boolean aplica(Viaje viaje){
        return viaje.getUsuario().esPrimerViaje();
    }

    @Override
    public double calcularDescuento(Viaje viaje, Tarifa tarifa){
        long minutosBonificados = Math.min(viaje.getMinutosCobrables(), 15);
        return tarifa.getCargoDesbloqueo() + (minutosBonificados * tarifa.getPrecioMinuto());
    }
}
