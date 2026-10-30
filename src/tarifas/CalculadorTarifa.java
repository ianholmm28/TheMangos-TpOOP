package tarifas;

import enums.TipoVehiculo;
import excepciones.ReglaNegocioException;
import viajes.Viaje;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalculadorTarifa {

    private List<Tarifa> tarifas;
    private List<Promocion> promociones;

    public CalculadorTarifa(){
        this.tarifas = new ArrayList<>();
        this.promociones = new ArrayList<>();
    }

    public void agregarTarifa(Tarifa tarifa){
        this.tarifas.add(tarifa);
    }

    public void agregarPromocion(Promocion promocion){
        this.promociones.add(promocion);
    }

    public double saldoMinimo(TipoVehiculo tipo) throws ReglaNegocioException {
        Tarifa tarifa = tarifaVigente(tipo, LocalDate.now());
        return tarifa.calcularBase(30);
    }

    public double calcular(Viaje viaje) throws ReglaNegocioException{
        Tarifa tarifa = tarifaVigente(viaje.getVehiculo().getTipo(), viaje.getInicio().toLocalDate());
        double base = tarifa.calcularBase(viaje.getMinutosCobrables());
        double mayorDescuento = 0.0;
        Promocion promoElegida = null;

        for (Promocion promo : this.promociones){
            if (promo.aplica(viaje)){
                double descuento = promo.calcularDescuento(viaje, tarifa);
                if (descuento > mayorDescuento){
                    mayorDescuento = descuento;
                    promoElegida = promo;
                }
            }
        }

        if (promoElegida != null){
            promoElegida.aplicar(viaje);
        }
        return Math.max(0.0, base - mayorDescuento);
    }

    private Tarifa tarifaVigente(TipoVehiculo tipo, LocalDate fecha) throws ReglaNegocioException{
        Tarifa vigente = null;
        for (Tarifa tarifa : this.tarifas){
            if (tarifa.getTipo() == tipo && !tarifa.getVigenteDesde().isAfter(fecha)){
                if (vigente == null || tarifa.getVigenteDesde().isAfter(vigente.getVigenteDesde())){
                    vigente = tarifa;
                }
            }
        }
        if (vigente == null){
            throw new ReglaNegocioException("No hay tarifa vigente para "+tipo+" en fecha "+fecha);
        }
        return vigente;
    }
}
