package tarifas;

import usuarios.PlanMensual;
import usuarios.Usuario;
import viajes.Viaje;

import java.time.YearMonth;

public class PromocionPlanMensual implements Promocion {

    @Override
    public boolean aplica(Viaje viaje) {
        Usuario usuario = viaje.getUsuario();
        PlanMensual plan = usuario.getPlan();
        if (plan == null) {
            return false;
        }
        YearMonth mesViaje = YearMonth.from(viaje.getInicio());
        return plan.getMes().equals(mesViaje) && plan.tieneViajesDisponibles();
    }

    @Override
    public double calcularDescuento(Viaje viaje, Tarifa tarifa){
        long minutosBonificados = Math.min(viaje.getMinutosCobrables(), 30);
        return tarifa.getCargoDesbloqueo() + (minutosBonificados * tarifa.getPrecioMinuto());
    }
}
