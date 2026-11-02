package tarifas;

import viajes.Viaje;

public interface Promocion {

    boolean aplica(Viaje viaje);

    double calcularDescuento(Viaje viaje, Tarifa tarifa);

    default void aplicar(Viaje viaje) {}
}
