package tarifas;

import enums.TipoVehiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usuarios.PlanMensual;
import usuarios.Usuario;
import viajes.Viaje;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PromocionPlanMensualTest {
    private static final YearMonth MES = YearMonth.of(2026, 10);
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 15, 10, 0);

    private PromocionPlanMensual promocion;
    private Tarifa tarifa;

    @BeforeEach
    void CrearPromocion() {
        promocion = new PromocionPlanMensual();
        tarifa = new Tarifa(TipoVehiculo.BICICLETA_COMUN, LocalDate.of(2026, 1, 1), 2000, 120);
    }

    // creo un plan falso del mes que le paso y que tiene (o no) viajes disponibles
    private PlanMensual CrearPlan(YearMonth mes, boolean tieneViajesDisponibles) {
        PlanMensual plan = mock(PlanMensual.class);
        when(plan.getMes()).thenReturn(mes);
        when(plan.tieneViajesDisponibles()).thenReturn(tieneViajesDisponibles);
        return plan;
    }

    // creo un viaje falso de un usuario con el plan que le paso (puede ser null)
    private Viaje CrearViaje(PlanMensual plan, long minutos) {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getPlan()).thenReturn(plan);
        Viaje viaje = mock(Viaje.class);
        when(viaje.getUsuario()).thenReturn(usuario);
        when(viaje.getInicio()).thenReturn(INICIO);
        when(viaje.getMinutosCobrables()).thenReturn(minutos);
        return viaje;
    }

    @Test
    void NoAplicaSiElUsuarioNoTienePlan() {
        Viaje viaje = CrearViaje(null, 30);
        assertFalse(promocion.aplica(viaje));
    }

    @Test
    void NoAplicaSiElPlanEsDeOtroMes() {
        // creo un plan de septiembre y un viaje en octubre
        PlanMensual plan = CrearPlan(YearMonth.of(2026, 9), true);
        Viaje viaje = CrearViaje(plan, 30);
        assertFalse(promocion.aplica(viaje));
    }

    @Test
    void NoAplicaSiElPlanNoTieneViajesDisponibles() {
        // el mes es el correcto pero ya no le quedan viajes
        PlanMensual plan = CrearPlan(MES, false);
        Viaje viaje = CrearViaje(plan, 30);
        assertFalse(promocion.aplica(viaje));
    }

    @Test
    void AplicaConPlanDelMesYViajesDisponibles() {
        PlanMensual plan = CrearPlan(MES, true);
        Viaje viaje = CrearViaje(plan, 30);
        assertTrue(promocion.aplica(viaje));
    }

    @Test
    void CalcularDescuentoBonificaLosMinutosDelViaje() {
        PlanMensual plan = CrearPlan(MES, true);
        Viaje viaje = CrearViaje(plan, 20);
        // 2000 + (20 * 120) = 4400
        assertEquals(4400, promocion.calcularDescuento(viaje, tarifa), 0.001);
    }

    @Test
    void CalcularDescuentoConMasDe30MinutosTieneTope() {
        PlanMensual plan = CrearPlan(MES, true);
        Viaje viaje = CrearViaje(plan, 45);
        // solo se bonifican 30 minutos: 2000 + (30 * 120) = 5600
        assertEquals(5600, promocion.calcularDescuento(viaje, tarifa), 0.001);
    }

    @Test
    void AplicarRegistraUnViajeEnElPlan() {
        PlanMensual plan = CrearPlan(MES, true);
        Viaje viaje = CrearViaje(plan, 30);
        promocion.aplicar(viaje);
        // verifico que se le haya avisado al plan que se uso un viaje
        verify(plan).registrarViaje();
    }

    @Test
    void AplicarSinPlanNoTiraException() {
        Viaje viaje = CrearViaje(null, 30);
        // verifico que aplicar no explote si el usuario no tiene plan
        assertDoesNotThrow(() -> promocion.aplicar(viaje));
    }
}