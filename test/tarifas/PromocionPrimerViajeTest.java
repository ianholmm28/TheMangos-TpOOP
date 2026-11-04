package tarifas;

import enums.TipoVehiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usuarios.Usuario;
import viajes.Viaje;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PromocionPrimerViajeTest {
    private PromocionPrimerViaje promocion;
    private Tarifa tarifa;

    @BeforeEach
    void CrearPromocion() {
        promocion = new PromocionPrimerViaje();
        tarifa = new Tarifa(TipoVehiculo.BICICLETA_COMUN, LocalDate.of(2026, 1, 1), 2000, 120);
    }

    // creo un viaje falso con un usuario que es (o no) su primer viaje y los minutos que le paso
    private Viaje CrearViaje(boolean esPrimerViaje, long minutos) {
        Usuario usuario = mock(Usuario.class);
        when(usuario.esPrimerViaje()).thenReturn(esPrimerViaje);
        Viaje viaje = mock(Viaje.class);
        when(viaje.getUsuario()).thenReturn(usuario);
        when(viaje.getMinutosCobrables()).thenReturn(minutos);
        return viaje;
    }

    @Test
    void AplicaSiElUsuarioEsPrimerViaje() {
        Viaje viaje = CrearViaje(true, 10);
        // verifico que la promocion aplique
        assertTrue(promocion.aplica(viaje));
    }

    @Test
    void NoAplicaSiElUsuarioYaViajo() {
        Viaje viaje = CrearViaje(false, 10);
        // verifico que la promocion no aplique
        assertFalse(promocion.aplica(viaje));
    }

    @Test
    void CalcularDescuentoBonificaLosMinutosDelViaje() {
        Viaje viaje = CrearViaje(true, 10);
        // 2000 + (10 * 120) = 3200
        assertEquals(3200, promocion.calcularDescuento(viaje, tarifa), 0.001);
    }

    @Test
    void CalcularDescuentoConExactamente15MinutosBonificaTodoElViaje() {
        Viaje viaje = CrearViaje(true, 15);
        // 2000 + (15 * 120) = 3800
        assertEquals(3800, promocion.calcularDescuento(viaje, tarifa), 0.001);
    }

    @Test
    void CalcularDescuentoConMasDe15MinutosTieneTope() {
        Viaje viaje = CrearViaje(true, 40);
        // solo se bonifican 15 minutos: 2000 + (15 * 120) = 3800
        assertEquals(3800, promocion.calcularDescuento(viaje, tarifa), 0.001);
    }
}