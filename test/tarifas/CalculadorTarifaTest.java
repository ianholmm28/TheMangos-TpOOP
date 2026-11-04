package tarifas;

import enums.TipoMedioPago;

import enums.TipoVehiculo;
import excepciones.ReglaNegocioException;
import excepciones.SaldoInsuficienteException;
import excepciones.UsuarioInhabilitadoException;

import excepciones.VehiculoNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vehiculos.Vehiculo;
import viajes.NotaAjuste;
import viajes.PuntoRecorrido;
import viajes.Viaje;

import java.security.PrivateKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

public class CalculadorTarifaTest{
    private static final TipoVehiculo TIPO = TipoVehiculo.BICICLETA_COMUN;
    private static final TipoVehiculo OTROTIPO = TipoVehiculo.BICICLETA_ELECTRICA;
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 15, 10, 0);

    private CalculadorTarifa calculador;
    private Tarifa tarifa;

    @BeforeEach
    void CrearCalculador() {
        calculador = new CalculadorTarifa();
        tarifa = new Tarifa(TIPO, LocalDate.of(2026, 1, 1), 2000, 120);
        calculador.agregarTarifa(tarifa);
    }


    // creo un viaje falso con el tipo de vehiculo, la fecha de inicio y los minutos que le paso
    private Viaje CrearViaje(TipoVehiculo tipo, LocalDateTime inicio, long minutos) {
        Vehiculo vehiculo = mock(Vehiculo.class);
        when(vehiculo.getTipo()).thenReturn(tipo);
        Viaje viaje = mock(Viaje.class);
        when(viaje.getVehiculo()).thenReturn(vehiculo);
        when(viaje.getInicio()).thenReturn(inicio);
        when(viaje.getMinutosCobrables()).thenReturn(minutos);
        return viaje;
    }


    // creo una promocion falsa que aplica o no al viaje y da el descuento que le paso
    private Promocion CrearPromocion(Viaje viaje, boolean aplica, double descuento) {
        Promocion promo = mock(Promocion.class);
        when(promo.aplica(viaje)).thenReturn(aplica);
        when(promo.calcularDescuento(viaje, tarifa)).thenReturn(descuento);
        return promo;
    }


    @Test
    void CalcularSinPromocionesDevuelveLaTarifaBase() throws ReglaNegocioException {
        // creo un viaje
        Viaje viaje= CrearViaje(TIPO,INICIO,30);
        // 2000 + (120 * 30) = 5600
        assertEquals(5600,calculador.calcular(viaje),0.001);
    }


    @Test
    void SaldoMinimoEsLaTarifaBaseDe30Minutos() throws ReglaNegocioException {
        // 2000 + (30 * 120) = 5600
        assertEquals(5600, calculador.saldoMinimo(TIPO), 0.001);
    }


    @Test
    void CalcularConPromocionesQueNoAplicanDevuelveLaBaseYNolaAplica() throws ReglaNegocioException {
        //creo un viaje
        Viaje viaje= CrearViaje(TIPO,INICIO,30);
        //creo promocion
        Promocion Promocion = CrearPromocion(viaje,false,15);
        //agrego la promocion
        calculador.agregarPromocion(Promocion);
        //me fijo que la promocion no haya afectado el precio debido que al devolver
        //falso el metodo aplica no tendria que modificar nada
        assertEquals(5600,calculador.calcular(viaje),0.001);
        // verifico que la promocion nunca se haya aplicado al viaje
        verify(Promocion, never()).aplicar(viaje);
    }


    @Test
    void  CalcularConPromocionQueAplicaRestaElDescuentoYLaAplica()throws ReglaNegocioException {
        //creo un viaje
        Viaje viaje= CrearViaje(TIPO,INICIO,30);
        //creo promocion
        Promocion Promocion = CrearPromocion(viaje,true,1500);
        //agrego la promocion
        calculador.agregarPromocion(Promocion);
        // me fijo que se haya aplicado correctamente el descuento debido que siempre aplica
        // 5600 - 1500 = 4100
        assertEquals(4100,calculador.calcular(viaje),0.001);
        // verifico que la promocion se haya aplicado al viaje una vez
        verify(Promocion).aplicar(viaje);
    }


    @Test
    void CalcularConVariasPromocionesEligeLaDeMayorDescuentoYAplicaSoloEsa() throws ReglaNegocioException {
        //creo un viaje
        Viaje viaje= CrearViaje(TIPO,INICIO,30);
        //creo promocion mas barata
        Promocion PromocionMasBarata = CrearPromocion(viaje,true,1500);
        Promocion PromocionMasCara =  CrearPromocion(viaje,true,2000);
        //agrego las promociones
        calculador.agregarPromocion(PromocionMasBarata);
        calculador.agregarPromocion(PromocionMasCara);
        // me fijo que se haya aplicado las promocion que tiene mayor descuento correctamente
        // 5600 - 2000 = 3600
        assertEquals(3600,calculador.calcular(viaje),0.001);
        // verifico que se haya aplicado solo la de mayor descuento
        verify(PromocionMasCara).aplicar(viaje);
        verify(PromocionMasBarata, never()).aplicar(viaje);
    }


    @Test
    void  CalcularConDescuentoMayorQueLaBaseDevuelveCeroYNoNegativo() throws ReglaNegocioException {
        //creo un viaje
        Viaje viaje= CrearViaje(TIPO,INICIO,30);
        //creo promocion
        Promocion Promocion = CrearPromocion(viaje,true,5667);
        //agrego la promocion
        calculador.agregarPromocion(Promocion);
        // me fijo que se haya aplicado correctamente el descuento debido que siempre aplica
        // 5600 - 5667 = -67 tiene que devolver 0
        // verifico que devuelva 0
        assertEquals(0,calculador.calcular(viaje),0.001);
    }


    @Test
    void CalcularSinTarifaParaEseTipoDeVehiculoTiraException(){
        Viaje viaje= CrearViaje(TipoVehiculo.MONOPATIN_ELECTRICO,INICIO,30);
        // compruebo que el programa tira la excepcion
        assertThrows(ReglaNegocioException.class, ()-> calculador.calcular(viaje));

    }


    @Test
    void CalcularConTarifaQueTodaviaNoEstaVigenteTiraException(){
        // agrego una tarifa que todavia no esta vigente
        Tarifa TarifaNoVigente = new Tarifa(TipoVehiculo.MONOPATIN_ELECTRICO, LocalDate.of(2027, 1, 1), 2000, 120);
        // agrego una nueva tarifa a lista de tarifas
        calculador.agregarTarifa(TarifaNoVigente);
        Viaje viaje= CrearViaje(TipoVehiculo.MONOPATIN_ELECTRICO,INICIO,30);
        // compruebo que el programa tira la excepcion
        assertThrows(ReglaNegocioException.class, ()-> calculador.calcular(viaje));
    }


    @Test
    void CalcularConDosTarifasUsaLaMasRecienteQueYaEstaVigente() throws ReglaNegocioException {
        // agrego una tarifa mas reciente que la original pero que ya es valida al momento del viaje
        Tarifa tarifaMasReciente = new Tarifa(TIPO, LocalDate.of(2026, 9, 1), 2100, 120);
        calculador.agregarTarifa(tarifaMasReciente);
        Viaje viaje = CrearViaje(TIPO, INICIO, 30);
        // verifico que usa la nueva: 2100 + (30*120)
        assertEquals(5700, calculador.calcular(viaje), 0.001);
    }



}




