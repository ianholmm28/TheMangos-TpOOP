package usuarios;
import enums.TipoMedioPago;
import excepciones.ReglaNegocioException;
import excepciones.SaldoInsuficienteException;
import excepciones.UsuarioInhabilitadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vehiculos.Vehiculo;
import viajes.NotaAjuste;
import viajes.PuntoRecorrido;
import viajes.Viaje;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;


public class UsuarioTest {
    private Usuario usuario;
    private static final YearMonth MES = YearMonth.of(2026, 10);

    @BeforeEach
    void CrearUsuario() {
        usuario = new Usuario("Juan Pereira", "11111111", "juan@gmail.com");
    }

    @Test
    void CargarSaldoYSuscribirseAUnPlan() throws Exception {
        //agrego un metodo de pago
        usuario.agregarMediosPago(TipoMedioPago.TARJETA, "MasterCard");
        // cargo saldo con la tarjeta
        usuario.recargarSaldo(20000, TipoMedioPago.TARJETA, "MasterCard");
        //verifico que el saldo se haya acreditado correctamente
        assertEquals(20000, usuario.getCuenta().getSaldo(), 0.001);
        // me suscribo al plan
        usuario.suscribirPlan(MES);
        // verifico que se creo el plan y que se cobro correctamente el plan
        assertNotNull(usuario.getPlan());
        assertEquals(20000 - PlanMensual.costoPlan, usuario.getCuenta().getSaldo(), 0.001);
    }

    @Test
    void SuscribirseAPlanSinSaldoTiraException() {
        //intento susbcribirme a un plan sin saldo
        assertThrows(SaldoInsuficienteException.class, () -> usuario.suscribirPlan(MES));
        //verifico que el usuario no tenga ningun plan
        assertNull(usuario.getPlan());
    }

    @Test
    void RecargarSaldoConTarjetaQueNoExiste() {
        //agrego un medio de pago
        usuario.agregarMediosPago(TipoMedioPago.TARJETA, "MasterCard");
        // Intento Recargar saldo con un medio de pago que no esta registrado
        assertThrows(ReglaNegocioException.class, () -> usuario.recargarSaldo(20000, TipoMedioPago.TARJETA, "Visa"));
        // verifico que no se haya cargado nada en la tarjeta
        assertEquals(0, usuario.getCuenta().getSaldo(), 0.001);

        assertTrue(usuario.getCuenta().getMovimientos().isEmpty());
    }
    @Test
    void verificarHabilitacionUsuarioInhabilitadolanzaExcepcion() {
        //ihabilito al usuario
        usuario.inhabilitar();
        // verifico que el programa tire una excepcion al verificar al usuario ihabilitado
        assertThrows(UsuarioInhabilitadoException.class, () -> usuario.verificarHabilitacion(0));
    }

    @Test
    void RecargarSaldoUsuarioInhabilitadoQuedaRehabilitado() throws Exception {
        //inhabilito al usuario
        usuario.inhabilitar();
        //agrego medios de pago
        usuario.agregarMediosPago(TipoMedioPago.TARJETA, "Visa");
        // recargo saldo
        usuario.recargarSaldo(20000, TipoMedioPago.TARJETA, "Visa");
        // verifico que el usuario este habilitado
        assertDoesNotThrow(() -> usuario.verificarHabilitacion(0));
    }

    @Test
    void IniciarViajeGuardaElViajeEnCurso() {
        // creo un viaje fantasma con mock
        Viaje viaje = mock(Viaje.class);
        // inicio el viaje
        usuario.iniciarViaje(viaje);
        // verifico que el usuario tenga guardado el viaje
        assertSame(viaje, usuario.getViajeEnCurso());
    }
    @Test
    void rehabilitar_conSaldoNegativo_siguieInhabilitado() {
        // le pongo una multa al usuario
        usuario.getCuenta().debitar(100, "multa");// saldo -100
        // inhabilito al usuario
        usuario.inhabilitar();
        // rehabilito al usuario
        usuario.rehabilitar();
        // verifico que el usuario quedo inhabilitado debido al saldo negativo
        assertThrows(UsuarioInhabilitadoException.class, () -> usuario.verificarHabilitacion(0));
    }
}
