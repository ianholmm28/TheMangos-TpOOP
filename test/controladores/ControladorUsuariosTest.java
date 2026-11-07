package controladores;

import enums.TipoMedioPago;
import enums.TipoMovimiento;
import excepciones.ReglaNegocioException;
import excepciones.SaldoInsuficienteException;
import excepciones.UsuarioNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reloj.RelojFijo;
import tarifas.CalculadorTarifa;
import usuarios.Cuenta;
import usuarios.MedioPago;
import usuarios.Movimiento;
import usuarios.PlanMensual;
import usuarios.Usuario;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ControladorUsuariosTest {

    private static final String DOCUMENTO = "11111111";
    private static final LocalDateTime BASE = LocalDateTime.of(2026, 10, 15, 10, 0);

    private ControladorUsuarios controlador;
    private CalculadorTarifa calculador;
    private RelojFijo reloj;
    private Map<String, Usuario> mapaUsuarios;


    @BeforeEach
    void CrearControlador() {
        // creo el hash de usuarios yo para poder agregarle usuarios y ver que se agrega al controlador
        mapaUsuarios = new HashMap<>();
        // ningun metodo del controlador usa el calculador, asi que alcanza con uno falso
        calculador = mock(CalculadorTarifa.class);
        reloj = new RelojFijo();
        controlador = new ControladorUsuarios(mapaUsuarios, calculador, reloj);
    }


    //creo un usuario falso con documento y lo cargo en el controlador
    private Usuario CrearUsuarioFalso(String documento) {
        Usuario usuario = mock(Usuario.class);
        mapaUsuarios.put(documento, usuario);
        return usuario;
    }


    // creo un usuario real con ese documento, una tarjeta MasterCard y saldo y lo cargo en el controlador
    private Usuario CrearUsuarioReal(String documento, double saldo) throws ReglaNegocioException {
        Usuario usuario = new Usuario("Juan Pereira", documento, "juan@gmail.com", reloj.ahora().toLocalDate());
        usuario.agregarMediosPago(TipoMedioPago.TARJETA, "MasterCard");
        if (saldo > 0) {
            usuario.recargarSaldo(saldo, TipoMedioPago.TARJETA, "MasterCard");
        }
        mapaUsuarios.put(documento, usuario);
        return usuario;
    }


    // creo un usuario falso que su cuenta devuelve la lista de movimientos que le paso
    private Usuario CrearUsuarioConMovimientos(List<Movimiento> movimientos) {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        Cuenta cuenta = mock(Cuenta.class);
        when(usuario.getCuenta()).thenReturn(cuenta);
        when(cuenta.getMovimientos()).thenReturn(movimientos);
        return usuario;
    }


    //creo un medio de pago falso
    private MedioPago CrearMedioPago(TipoMedioPago tipo, String identificador) {
        MedioPago medio = mock(MedioPago.class);
        when(medio.getTipo()).thenReturn(tipo);
        when(medio.getIdentificador()).thenReturn(identificador);
        return medio;
    }


    // creo un movimiento en esa fecha
    private Movimiento CrearMovimiento(LocalDateTime fecha) {
        return new Movimiento(fecha, 100, TipoMovimiento.RECARGA, "Mastercard");
    }


    @Test
    void RegistrarUsuarioValidoLoGuardaYLoDevuelve() throws Exception {
        Usuario nuevo = controlador.registrarUsuario("Juan Pereira", DOCUMENTO, "juan@gmail.com");
        //verifico que haya devuelto un usuario con el documento que le pase
        assertNotNull(nuevo);
        assertEquals(DOCUMENTO, nuevo.getDocumento());
        //verifico que el controlador lo haya guardado y que sea el mismo usuario
        assertEquals(1, controlador.getUsuarios().size());
        assertSame(nuevo, controlador.buscarUsuario(DOCUMENTO));
    }


    @Test
    void RegistrarUsuarioConNombreNullTiraException() {
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarUsuario(null, DOCUMENTO, "juan@gmail.com"));
        // verifico que no se haya guardado nada
        assertTrue(controlador.getUsuarios().isEmpty());
    }


    @Test
    void RegistrarUsuarioConNombreEnBlancoTiraException() {
        // pruebo con  a buscar un usuario con solo espacios de nombre
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarUsuario("   ", DOCUMENTO, "juan@gmail.com"));
        assertTrue(controlador.getUsuarios().isEmpty());
    }


    @Test
    void RegistrarUsuarioConDocumentoNullTiraException() {
        // creo un usuario con documento null
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarUsuario("Juan Pereira", null, "juan@gmail.com"));
        // me fijo que no se haya guardodro el usuario con el documento null
        assertTrue(controlador.getUsuarios().isEmpty());
    }


    @Test
    void RegistrarUsuarioConDocumentoEnBlancoTiraException() {
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarUsuario("Juan Pereira", "   ", "juan@gmail.com"));
        assertTrue(controlador.getUsuarios().isEmpty());
    }


    @Test
    void RegistrarUsuarioConEmailNullTiraException() {
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarUsuario("Juan Pereira", DOCUMENTO, null));
        assertTrue(controlador.getUsuarios().isEmpty());
    }


    @Test
    void RegistrarUsuarioConEmailEnBlancoTiraException() {
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarUsuario("Juan Pereira", DOCUMENTO, "   "));
        assertTrue(controlador.getUsuarios().isEmpty());
    }


    @Test
    void RegistrarUsuarioConDocumentoConLetrasTiraException() {
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarUsuario("Juan Pereira", "1234A678", "juan@gmail.com"));
        assertTrue(controlador.getUsuarios().isEmpty());
    }


    @Test
    void RegistrarUsuarioConNombreConNumerosTiraException() {
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarUsuario("Juan2", DOCUMENTO, "juan@gmail.com"));
        assertTrue(controlador.getUsuarios().isEmpty());
    }


    @Test
    void RegistrarUsuarioConNombreConAcentosYEniesLoAcepta() throws Exception {
        Usuario nuevo = controlador.registrarUsuario("José Muñoz", DOCUMENTO, "jose@gmail.com");
        // verifico que lo haya aceptado, las letras con acento y la ñ cuentan como letras
        assertSame(nuevo, controlador.buscarUsuario(DOCUMENTO));
    }


    @Test
    void RegistrarUsuarioConEmailSinArrobaTiraException() {
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarUsuario("Juan Pereira", DOCUMENTO, "juangmail.com"));
        assertTrue(controlador.getUsuarios().isEmpty());
    }


    @Test
    void RegistrarUsuarioConDocumentoRepetidoTiraExceptionYNoPisaAlOriginal() throws Exception {
        // registro un usuario
        Usuario original = controlador.registrarUsuario("Juan Pereira", DOCUMENTO, "juan@gmail.com");
        // intento registrar a otra persona con el mismo documento
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarUsuario("Pedro Gomez", DOCUMENTO, "pedro@gmail.com"));
        //verifico que siga habiendo un solo usuario y que sea el mismo
        assertEquals(1, controlador.getUsuarios().size());
        assertSame(original, controlador.buscarUsuario(DOCUMENTO));
    }


    @Test
    void AgregarMedioPagoLoAgregaAlUsuario() throws Exception {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        controlador.agregarMedioPago(DOCUMENTO, TipoMedioPago.TARJETA, "MasterCard");
        // verifico que le haya pasado al usuario el tipo y el identificador
        verify(usuario).agregarMediosPago(TipoMedioPago.TARJETA, "MasterCard");
    }


    @Test
    void AgregarMedioPagoConTipoNullTiraExceptionYNoLoAgrega() {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        assertThrows(ReglaNegocioException.class, () -> controlador.agregarMedioPago(DOCUMENTO, null, "MasterCard"));
        // verifico que el usuario nunca haya recibido un medio de pago
        verify(usuario, never()).agregarMediosPago(any(), any());
    }


    @Test
    void AgregarMedioPagoConIdentificadorNullTiraExceptionYNoLoAgrega() {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        assertThrows(ReglaNegocioException.class, () -> controlador.agregarMedioPago(DOCUMENTO, TipoMedioPago.TARJETA, null));
        verify(usuario, never()).agregarMediosPago(any(), any());
    }


    @Test
    void AgregarMedioPagoConIdentificadorEnBlancoTiraExceptionYNoLoAgrega() {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        // pruebo con solo espacios a agregar un identificador con solo espacios
        assertThrows(ReglaNegocioException.class, () -> controlador.agregarMedioPago(DOCUMENTO, TipoMedioPago.TARJETA, "   "));
        verify(usuario, never()).agregarMediosPago(any(), any());
    }


    @Test
    void AgregarMedioPagoConUsuarioInexistenteTiraException() {
        assertThrows(UsuarioNoEncontradoException.class, () -> controlador.agregarMedioPago("99999999", TipoMedioPago.TARJETA, "MasterCard"));
    }


    @Test
    void AgregarMedioPagoConObjetoMedioPagoLoAgregaConSuTipoYSuIdentificador() throws Exception {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        MedioPago medio = CrearMedioPago(TipoMedioPago.TARJETA, "MasterCard");
        controlador.agregarMedioPago(DOCUMENTO, medio);
        //verifico que haya sacado el tipo y el identificador del objeto y se los haya pasado al usuario
        verify(usuario).agregarMediosPago(TipoMedioPago.TARJETA, "MasterCard");
    }


    @Test
    void AgregarMedioPagoConObjetoMedioPagoNullTiraException() {
        CrearUsuarioFalso(DOCUMENTO);
        assertThrows(ReglaNegocioException.class, () -> controlador.agregarMedioPago(DOCUMENTO, null));
    }


    @Test
    void QuitarMedioPagoLoQuitaDelUsuario() throws Exception {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        controlador.quitarMedioPago(DOCUMENTO, TipoMedioPago.TARJETA, "MasterCard");
        // verifico que le haya pasado al usuario el tipo y el identificador del medio a quitar
        verify(usuario).quitarMedioPago(TipoMedioPago.TARJETA, "MasterCard");
    }


    @Test
    void CambiarNombreSeLoCambiaAlUsuarioSinLosEspaciosDeLosCostados() throws Exception {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        controlador.cambiarNombre(DOCUMENTO, "  Maria Lopez  ");
        // verifico que el usuario haya recibido el nombre ya limpio
        verify(usuario).cambiarNombre("Maria Lopez");
    }


    @Test
    void CambiarNombreConNombreNullTiraExceptionYNoLoCambia() throws Exception {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        assertThrows(ReglaNegocioException.class, () -> controlador.cambiarNombre(DOCUMENTO, null));
        // verifico que el usuario no haya recibido un nombre nuevo
        verify(usuario, never()).cambiarNombre(any());
    }


    @Test
    void CambiarNombreConNombreEnBlancoTiraExceptionYNoLoCambia() throws Exception {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        //pruebo con solo espacios
        assertThrows(ReglaNegocioException.class, () -> controlador.cambiarNombre(DOCUMENTO, "   "));
        verify(usuario, never()).cambiarNombre(any());
    }


    @Test
    void CambiarNombreConNumerosTiraExceptionYNoLoCambia() throws Exception {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        assertThrows(ReglaNegocioException.class, () -> controlador.cambiarNombre(DOCUMENTO, "Maria2"));
        verify(usuario, never()).cambiarNombre(any());
    }


    @Test
    void RecargarSaldoLeDelegaLaRecargaAlUsuario() throws Exception {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        controlador.recargarSaldo(DOCUMENTO, TipoMedioPago.TARJETA, "MasterCard", 5000);
        //verifico que el usuario haya recibido el monto, el tipo y el identificador
        verify(usuario).recargarSaldo(5000, TipoMedioPago.TARJETA, "MasterCard");
    }


    @Test
    void RecargarSaldoConMontoCeroTiraExceptionYNoRecarga() throws Exception {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        assertThrows(ReglaNegocioException.class, () -> controlador.recargarSaldo(DOCUMENTO, TipoMedioPago.TARJETA, "MasterCard", 0));
        // verifico que el usuario nunca haya recibido una recarga
        verify(usuario, never()).recargarSaldo(anyDouble(), any(), any());
    }


    @Test
    void RecargarSaldoConMontoNegativoTiraExceptionYNoRecarga() throws Exception {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        assertThrows(ReglaNegocioException.class, () -> controlador.recargarSaldo(DOCUMENTO, TipoMedioPago.TARJETA, "MasterCard", -1000));
        verify(usuario, never()).recargarSaldo(anyDouble(), any(), any());
    }


    @Test
    void RecargarSaldoConMontoMinimoPositivoLoAcepta() throws Exception {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        controlador.recargarSaldo(DOCUMENTO, TipoMedioPago.TARJETA, "MasterCard", 0.01);
        // verifico que 0.01 pase, es el borde porque la condicion es monto menor o igual a 0
        verify(usuario).recargarSaldo(0.01, TipoMedioPago.TARJETA, "MasterCard");
    }


    @Test
    void RecargarSaldoConUsuarioInexistenteTiraException() {
        assertThrows(UsuarioNoEncontradoException.class, () -> controlador.recargarSaldo("99999999", TipoMedioPago.TARJETA, "MasterCard", 5000));
    }


    @Test
    void RecargarSaldoConMedioDePagoNoAsociadoTiraExceptionYNoCambiaElSaldo() throws Exception {
        //creo un usuario real con 1000 de saldo y solo la tarjeta MasterCard
        Usuario usuario = CrearUsuarioReal(DOCUMENTO, 1000);
        // intento recargar con una tarjeta Visa que no esta asociada
        assertThrows(ReglaNegocioException.class, () -> controlador.recargarSaldo(DOCUMENTO, TipoMedioPago.TARJETA, "Visa", 500));
        // verifico que el saldo siga en 1000
        assertEquals(1000, usuario.getCuenta().getSaldo(), 0.001);
    }


    @Test
    void RecargarSaldoConObjetoMedioPagoLeDelegaLaRecargaAlUsuario() throws Exception {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        MedioPago medio = CrearMedioPago(TipoMedioPago.TARJETA, "MasterCard");
        controlador.recargarSaldo(DOCUMENTO, 5000, medio);
        //verifico que haya sacado el tipo y el identificador del objeto y se los haya pasado al usuario
        verify(usuario).recargarSaldo(5000, TipoMedioPago.TARJETA, "MasterCard");
    }


    @Test
    void RecargarSaldoConObjetoMedioPagoNullTiraException() {
        CrearUsuarioFalso(DOCUMENTO);
        assertThrows(ReglaNegocioException.class, () -> controlador.recargarSaldo(DOCUMENTO, 5000, null));
    }


    @Test
    void SuscribirPlanUsaElMesDelReloj() throws Exception {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        controlador.suscribirPlan(DOCUMENTO);
        // verifico que le haya pasado al usuario el mes en el que esta el reloj
        verify(usuario).suscribirPlan(YearMonth.from(reloj.ahora()));
    }


    @Test
    void SuscribirPlanDespuesDeAvanzarElRelojUsaElMesNuevo() throws Exception {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        // me suscribo en el mes actual
        YearMonth mesInicial = YearMonth.from(reloj.ahora());
        controlador.suscribirPlan(DOCUMENTO);
        // avanzo el reloj 40 dias y me suscribo de nuevo
        reloj.avanzar(Duration.ofDays(40));
        YearMonth mesNuevo = YearMonth.from(reloj.ahora());
        controlador.suscribirPlan(DOCUMENTO);
        // verifico que los dos meses sean distintos y que el usuario haya recibido cada uno
        assertNotEquals(mesInicial, mesNuevo);
        verify(usuario).suscribirPlan(mesInicial);
        verify(usuario).suscribirPlan(mesNuevo);
    }


    @Test
    void SuscribirPlanConSaldoJustoCreaElPlanDelMesActual() throws Exception {
        // creo un usuario con exactamente el saldo que cuesta el plan
        Usuario usuario = CrearUsuarioReal(DOCUMENTO, PlanMensual.COSTO_PLAN);
        controlador.suscribirPlan(DOCUMENTO);
        // verifico que tenga plan y que sea del mes en el que esta el reloj
        assertNotNull(usuario.getPlan());
        assertEquals(YearMonth.from(reloj.ahora()), usuario.getPlan().getMes());
    }


    @Test
    void SuscribirPlanSinSaldoTiraExceptionYNoCreaPlan() throws Exception {
        // creo un usuario sin saldo
        Usuario usuario = CrearUsuarioReal(DOCUMENTO, 0);
        assertThrows(SaldoInsuficienteException.class, () -> controlador.suscribirPlan(DOCUMENTO));
        assertNull(usuario.getPlan());
    }


    @Test
    void SuscribirPlanConUsuarioInexistenteTiraException() {
        assertThrows(UsuarioNoEncontradoException.class, () -> controlador.suscribirPlan("99999999"));
    }


    @Test
    void DarDeBajaPlanLeDelegaLaBajaAlUsuario() throws Exception {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        controlador.darDeBajaPlan(DOCUMENTO);
        verify(usuario).darDeBajaPlan();
    }


    @Test
    void ConsultarSaldoDeUnUsuarioNuevoEsCero() throws Exception {
        CrearUsuarioReal(DOCUMENTO, 0);
        assertEquals(0, controlador.consultarSaldo(DOCUMENTO), 0.001);
    }


    @Test
    void ConsultarSaldoDevuelveElSaldoActualDeLaCuenta() throws Exception {
        CrearUsuarioReal(DOCUMENTO, 20000);
        assertEquals(20000, controlador.consultarSaldo(DOCUMENTO), 0.001);
    }


    @Test
    void ConsultarSaldoConUsuarioInexistenteTiraException() {
        assertThrows(UsuarioNoEncontradoException.class, () -> controlador.consultarSaldo("99999999"));
    }


    @Test
    void ConsultarMovimientosNoModificaElOrdenDeLaListaDeLaCuenta() throws Exception {
        Movimiento primero = CrearMovimiento(BASE);
        Movimiento segundo = CrearMovimiento(BASE.plusDays(1));
        List<Movimiento> deLaCuenta = new ArrayList<>(List.of(segundo, primero));
        CrearUsuarioConMovimientos(deLaCuenta);
        controlador.consultarMovimientos(DOCUMENTO);
        // verifico que la lista de la cuenta siga en el orden en que la cargue
        assertEquals(List.of(segundo, primero), deLaCuenta);
    }


    @Test
    void ConsultarMovimientosSinMovimientosDevuelveListaVacia() throws Exception {
        CrearUsuarioConMovimientos(new ArrayList<>());
        assertTrue(controlador.consultarMovimientos(DOCUMENTO).isEmpty());
    }


    @Test
    void BuscarUsuarioExistenteLoDevuelve() throws Exception {
        Usuario usuario = CrearUsuarioFalso(DOCUMENTO);
        assertSame(usuario, controlador.buscarUsuario(DOCUMENTO));
    }


    @Test
    void BuscarUsuarioInexistenteTiraException() {
        assertThrows(UsuarioNoEncontradoException.class, () -> controlador.buscarUsuario("99999999"));
    }


    @Test
    void GetUsuariosDevuelveUnaCopiaYNoElMapaInterno() {
        CrearUsuarioFalso(DOCUMENTO);
        //intento vaciar el mapa que me devuelve
        controlador.getUsuarios().clear();
        //verifico que el controlador siga teniendo su usuario
        assertEquals(1, controlador.getUsuarios().size());
    }


    @Test
    void ConstructorConNullsArrancaVacioYPuedeRegistrarConElRelojDelSistema() throws Exception {
        //creo un controlador pasandole null en el mapa y en el reloj
        ControladorUsuarios vacio = new ControladorUsuarios(null, calculador, null);
        // verifico que arranque vacio y que registro funcione, porque usa el reloj por defecto
        assertTrue(vacio.getUsuarios().isEmpty());
        Usuario nuevo = vacio.registrarUsuario("Juan Pereira", DOCUMENTO, "juan@gmail.com");
        assertSame(nuevo, vacio.buscarUsuario(DOCUMENTO));
    }


    @Test
    void ConstructorSinRelojPuedeRegistrarConElRelojDelSistema() throws Exception {
        ControladorUsuarios sinReloj = new ControladorUsuarios(new HashMap<>(), calculador);
        Usuario nuevo = sinReloj.registrarUsuario("Juan Pereira", DOCUMENTO, "juan@gmail.com");
        assertSame(nuevo, sinReloj.buscarUsuario(DOCUMENTO));
    }
}