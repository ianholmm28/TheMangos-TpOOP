package controladores;

import enums.EstadoVehiculo;
import enums.EstadoViaje;
import enums.TipoMedioPago;
import enums.TipoMovimiento;
import enums.TipoVehiculo;
import estaciones.Estacion;
import excepciones.EstacionLlenaException;
import excepciones.ReglaNegocioException;
import excepciones.SaldoInsuficienteException;
import excepciones.UsuarioInhabilitadoException;
import excepciones.UsuarioNoEncontradoException;
import excepciones.VehiculoNoDisponibleException;
import excepciones.ViajeAbiertoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reloj.RelojFijo;
import tarifas.CalculadorTarifa;
import usuarios.Movimiento;
import usuarios.Usuario;
import vehiculos.BicicletaElectrica;
import vehiculos.Vehiculo;
import viajes.NotaAjuste;
import viajes.Viaje;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ControladorViajesTest {
    private static final String DOCUMENTO = "11111111";

    private ControladorViajes controlador;
    private CalculadorTarifa calculador;
    private RelojFijo reloj;
    private Usuario usuario;
    private Estacion estacionOrigen;
    private Estacion estacionDestino;
    private Vehiculo bicicleta;
    private List<Viaje> viajes;
    private List<String> avisosAlerta;


    @BeforeEach
    void CrearControlador() throws Exception {

        //creo un calculador falso: el saldo minimo es 1000 y todos los viajes cuestan 5000
        calculador = mock(CalculadorTarifa.class);
        when(calculador.saldoMinimo(any(), any())).thenReturn(1000.0);
        when(calculador.calcular(any(Viaje.class))).thenReturn(5000.0);
        reloj = new RelojFijo();

        // le paso yo las listas al controlador para poder ver que agrega
        viajes = new ArrayList<>();
        avisosAlerta = new ArrayList<>();
        controlador = new ControladorViajes(new HashMap<>(), new HashMap<>(), viajes, avisosAlerta, calculador, reloj);

        // creo un usuario con 20000 de saldo
        usuario = new Usuario("Juan Pereira", DOCUMENTO, "juan@gmail.com");
        usuario.agregarMediosPago(TipoMedioPago.TARJETA, "MasterCard");
        usuario.recargarSaldo(20000, TipoMedioPago.TARJETA, "MasterCard");
        controlador.agregarUsuario(usuario);

        // creo la estacion de origen con la bicicleta B1 y 3 bicicletas de relleno
        bicicleta = CrearVehiculo("B1", TipoVehiculo.BICICLETA_COMUN, true);
        estacionOrigen = CrearEstacion("ORIGEN");
        estacionOrigen.recibir(bicicleta);
        AgregarRellenos(estacionOrigen, TipoVehiculo.BICICLETA_COMUN, 3);

        // creo la estacion de destino con 3 bicicletas de relleno
        estacionDestino = CrearEstacion("DESTINO");
        AgregarRellenos(estacionDestino, TipoVehiculo.BICICLETA_COMUN, 3);
        controlador.agregarEstacion(estacionOrigen);
        controlador.agregarEstacion(estacionDestino);
    }


    //creo un vehiculo falso
    private Vehiculo CrearVehiculo(String codigo, TipoVehiculo tipo, boolean disponible) {
        Vehiculo vehiculo = mock(Vehiculo.class);
        when(vehiculo.getCodigo()).thenReturn(codigo);
        when(vehiculo.getTipo()).thenReturn(tipo);
        when(vehiculo.estaDisponible()).thenReturn(disponible);
        return vehiculo;
    }


    // creo una bicicleta electrica
    private BicicletaElectrica CrearElectrica(String codigo, int nivelBateria) {
        BicicletaElectrica electrica = mock(BicicletaElectrica.class);
        when(electrica.getCodigo()).thenReturn(codigo);
        when(electrica.getTipo()).thenReturn(TipoVehiculo.BICICLETA_ELECTRICA);
        when(electrica.estaDisponible()).thenReturn(true);
        when(electrica.getNivelBateria()).thenReturn(nivelBateria);
        return electrica;
    }


    // creo una estacion real con lugar para 5 bicicletas comunes y 5 electricas
    private Estacion CrearEstacion(String codigo) {
        Map<TipoVehiculo, Integer> capacidades = new HashMap<>();
        capacidades.put(TipoVehiculo.BICICLETA_COMUN, 5);
        capacidades.put(TipoVehiculo.BICICLETA_ELECTRICA, 5);
        return new Estacion(codigo, "Direccion " + codigo, 0, 0, capacidades);
    }


    // le agrego a la estacion esa cantidad de vehiculos de relleno del tipo que le paso
    private void AgregarRellenos(Estacion estacion, TipoVehiculo tipo, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            estacion.recibir(CrearVehiculo(estacion.getCodigo() + "-" + tipo + "-" + i, tipo, true));
        }
    }


    // creo otro usuario con ese documento y ese saldo y lo agrego al controlador
    private Usuario CrearOtroUsuario(String documento, double saldo) throws ReglaNegocioException {
        Usuario otro = new Usuario("Usuario " + documento, documento, documento + "@mail.com");
        if (saldo > 0) {
            otro.agregarMediosPago(TipoMedioPago.TARJETA, "Visa");
            otro.recargarSaldo(saldo, TipoMedioPago.TARJETA, "Visa");
        }
        controlador.agregarUsuario(otro);
        return otro;
    }


    // dejo una bicicleta electrica en el origen con ese nivel de bateria y la retiro con el usuario
    private BicicletaElectrica RetirarElectrica(int nivelBateria) throws ReglaNegocioException {
        BicicletaElectrica electrica = CrearElectrica("E1", nivelBateria);
        estacionOrigen.recibir(electrica);
        controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "E1");
        return electrica;
    }


    // retiro la bicicleta B1, dejo pasar 30 minutos y la devuelvo en el destino
    private Viaje CerrarUnViaje() throws ReglaNegocioException {
        Viaje viaje = controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        reloj.avanzar(Duration.ofMinutes(30));
        controlador.devolverVehiculo(DOCUMENTO, "DESTINO");
        return viaje;
    }


    @Test
    void RetirarVehiculoDisponibleCreaElViajeEnCurso() throws Exception {
        Viaje viaje = controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        // verifico que el viaje tenga los datos del retiro
        assertSame(usuario, viaje.getUsuario());
        assertSame(bicicleta, viaje.getVehiculo());
        assertSame(estacionOrigen, viaje.getOrigen());
        assertEquals(EstadoViaje.EN_CURSO, viaje.getEstado());
        assertEquals(reloj.ahora(), viaje.getInicio());
        // verifico que el viaje ya tenga el punto de partida, que es la ubicacion de la estacion
        assertEquals(1, viaje.getPuntos().size());
        assertEquals(estacionOrigen.getLatitud(), viaje.getPuntos().get(0).getLatitud(), 0.001);
        // verifico que no se haya generado ninguna alerta porque la estacion quedo con ocupacion normal
        assertTrue(avisosAlerta.isEmpty());
    }


    @Test
    void RetirarVehiculoSacaElVehiculoDeLaEstacionYLoPoneEnUso() throws Exception {
        controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        //verifico que la bicicleta ya no este en la estacion de origen
        assertNull(estacionOrigen.buscarVehiculo("B1"));
        assertEquals(3, estacionOrigen.getVehiculos().size());
        //verifico que se le haya cambiado el estado a en uso
        verify(bicicleta).setEstado(EstadoVehiculo.EN_USO);
    }


    @Test
    void RetirarVehiculoDejaAlUsuarioConElViajeEnCursoYLoRegistraEnElControlador() throws Exception {
        Viaje viaje = controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        //verifico que el usuario tenga ese viaje en curso
        assertSame(viaje, usuario.getViajeEnCurso());
        //verifico que el controlador lo haya guardado en su lista de viajes
        assertEquals(List.of(viaje), controlador.getViajes());
    }


    @Test
    void RetirarVehiculoPideElSaldoMinimoDelTipoDeVehiculoYLaFechaDeHoy() throws Exception {
        controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        //verifico que le haya pedido al calculador el saldo minimo para ese tipo de vehiculo en la fecha del reloj
        verify(calculador).saldoMinimo(TipoVehiculo.BICICLETA_COMUN, reloj.ahora().toLocalDate());
    }


    @Test
    void RetirarVehiculoQueNoExisteEnLaEstacionTiraException() {
        // intento retirar un vehiculo con un codigo que no esta en la estacion
        assertThrows(VehiculoNoDisponibleException.class, () -> controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "XX"));
        //verifico que no se haya creado ningun viaje
        assertTrue(viajes.isEmpty());
    }


    @Test
    void RetirarVehiculoNoDisponibleTiraExceptionYNoLoSacaDeLaEstacion() {
        //agrego un vehiculo que no esta disponible
        Vehiculo roto = CrearVehiculo("B2", TipoVehiculo.BICICLETA_COMUN, false);
        estacionOrigen.recibir(roto);
        assertThrows(VehiculoNoDisponibleException.class, () -> controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B2"));
        // verifico que el vehiculo siga en la estacion y que no haya viaje
        assertSame(roto, estacionOrigen.buscarVehiculo("B2"));
        assertTrue(viajes.isEmpty());
    }


    @Test
    void RetirarVehiculoConUsuarioInexistenteTiraException() {
        assertThrows(UsuarioNoEncontradoException.class, () -> controlador.retirarVehiculo("99999999", "ORIGEN", "B1"));
    }


    @Test
    void RetirarVehiculoConEstacionInexistenteTiraException() {
        assertThrows(ReglaNegocioException.class, () -> controlador.retirarVehiculo(DOCUMENTO, "NOEXISTE", "B1"));
    }


    @Test
    void RetirarVehiculoConUsuarioInhabilitadoTiraExceptionYNoModificaNada() {
        usuario.inhabilitar();
        assertThrows(UsuarioInhabilitadoException.class, () -> controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1"));
        // verifico que la bicicleta siga en la estacion, que no haya viaje y que el usuario no tenga viaje en curso
        assertSame(bicicleta, estacionOrigen.buscarVehiculo("B1"));
        assertTrue(viajes.isEmpty());
        assertNull(usuario.getViajeEnCurso());
    }


    @Test
    void RetirarVehiculoConSaldoInsuficienteTiraExceptionYNoModificaNada() throws Exception {
        // creo un usuario sin saldo, y el minimo es 1000
        CrearOtroUsuario("22222222", 0);
        assertThrows(SaldoInsuficienteException.class, () -> controlador.retirarVehiculo("22222222", "ORIGEN", "B1"));
        // verifico que la bicicleta siga en la estacion y que no haya viaje
        assertSame(bicicleta, estacionOrigen.buscarVehiculo("B1"));
        assertTrue(viajes.isEmpty());
    }


    @Test
    void RetirarVehiculoConUnViajeEnCursoTiraException() throws Exception {
        // agrego una segunda bicicleta y retiro la primera
        Vehiculo segunda = CrearVehiculo("B2", TipoVehiculo.BICICLETA_COMUN, true);
        estacionOrigen.recibir(segunda);
        controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        // intento retirar la segunda sin haber devuelto la primera
        assertThrows(ViajeAbiertoException.class, () -> controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B2"));
        // verifico que la segunda siga en la estacion y que haya un solo viaje
        assertSame(segunda, estacionOrigen.buscarVehiculo("B2"));
        assertEquals(1, viajes.size());
    }


    @Test
    void RetirarVehiculoQueDejaPocosVehiculosEnLaEstacionGeneraUnaAlerta() throws Exception {
        // creo una estacion con 2 bicicletas, al retirar una queda en 1 de 10 que es 0.1 y es menos de 0.2
        Estacion pocos = CrearEstacion("POCOS");
        pocos.recibir(CrearVehiculo("B9", TipoVehiculo.BICICLETA_COMUN, true));
        AgregarRellenos(pocos, TipoVehiculo.BICICLETA_COMUN, 1);
        controlador.agregarEstacion(pocos);
        controlador.retirarVehiculo(DOCUMENTO, "POCOS", "B9");
        // verifico que se haya generado una sola alerta y que mencione a la estacion
        assertEquals(1, avisosAlerta.size());
        assertTrue(avisosAlerta.get(0).contains("POCOS"));
    }


    @Test
    void CancelarRetiroSinViajeEnCursoTiraException() {
        assertThrows(ReglaNegocioException.class, () -> controlador.cancelarRetiro(DOCUMENTO));
    }


    @Test
    void CancelarRetiroAntesDeLos2MinutosCancelaElViajeYDevuelveElVehiculo() throws Exception {
        Viaje viaje = controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        reloj.avanzar(Duration.ofMinutes(1));
        controlador.cancelarRetiro(DOCUMENTO);
        // verifico que el viaje haya quedado cancelado y que siga en el historial del controlador
        assertEquals(EstadoViaje.CANCELADO, viaje.getEstado());
        assertEquals(1, controlador.getViajes().size());
        // verifico que la bicicleta haya vuelto a la estacion de origen y quede disponible
        assertSame(bicicleta, estacionOrigen.buscarVehiculo("B1"));
        verify(bicicleta).setEstado(EstadoVehiculo.DISPONIBLE);
        // verifico que el usuario ya no tenga un viaje en curso
        assertNull(usuario.getViajeEnCurso());
    }


    @Test
    void CancelarRetiroConExactamente2MinutosTodaviaSePuedeCancelar() throws Exception {
        Viaje viaje = controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        // paso justo 120 segundos, es el borde porque la condicion es mayor a 120
        reloj.avanzar(Duration.ofMinutes(2));
        controlador.cancelarRetiro(DOCUMENTO);
        assertEquals(EstadoViaje.CANCELADO, viaje.getEstado());
    }


    @Test
    void CancelarRetiroConMasDe2MinutosTiraExceptionYNoCancelaNada() throws Exception {
        Viaje viaje = controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        // paso 121 segundos, uno mas que el limite
        reloj.avanzar(Duration.ofSeconds(121));
        assertThrows(ReglaNegocioException.class, () -> controlador.cancelarRetiro(DOCUMENTO));
        // verifico que el viaje siga en curso y que la bicicleta no haya vuelto a la estacion
        assertEquals(EstadoViaje.EN_CURSO, viaje.getEstado());
        assertSame(viaje, usuario.getViajeEnCurso());
        assertNull(estacionOrigen.buscarVehiculo("B1"));
    }


    @Test
    void CancelarRetiroConVehiculoConTresFallasLoEnviaAReparacion() throws Exception {
        when(bicicleta.fallasUltimos30Dias(any())).thenReturn(3);
        controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        controlador.cancelarRetiro(DOCUMENTO);
        // verifico que se haya enviado a reparacion y no se haya marcado como disponible
        verify(bicicleta).enviarAReparacion();
        verify(bicicleta, never()).setEstado(EstadoVehiculo.DISPONIBLE);
    }


    @Test
    void CancelarRetiroConBateriaBajaDejaElVehiculoConBateriaBaja() throws Exception {
        BicicletaElectrica electrica = RetirarElectrica(10);
        controlador.cancelarRetiro(DOCUMENTO);
        // verifico que como tiene menos de 15 de bateria quede con bateria baja
        verify(electrica).setEstado(EstadoVehiculo.BATERIA_BAJA);
    }


    @Test
    void CancelarRetiroConBateriaDeExactamente15QuedaDisponible() throws Exception {
        BicicletaElectrica electrica = RetirarElectrica(15);
        controlador.cancelarRetiro(DOCUMENTO);
        // verifico que 15 no cuente como bateria baja, es el borde porque la condicion es menor a 15
        verify(electrica).setEstado(EstadoVehiculo.DISPONIBLE);
        verify(electrica, never()).setEstado(EstadoVehiculo.BATERIA_BAJA);
    }


    @Test
    void RegistrarPuntoSinViajeEnCursoTiraException() {
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarPunto(DOCUMENTO, 1.0, 2.0));
    }


    @Test
    void RegistrarPuntoAgregaElPuntoAlViajeEnCurso() throws Exception {
        Viaje viaje = controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        controlador.registrarPunto(DOCUMENTO, 1.5, 2.5);
        // verifico que ahora tenga 2 puntos, el de partida y el nuevo
        assertEquals(2, viaje.getPuntos().size());
        assertEquals(1.5, viaje.getPuntos().get(1).getLatitud(), 0.001);
        assertEquals(2.5, viaje.getPuntos().get(1).getLongitud(), 0.001);
    }


    @Test
    void DevolverVehiculoSinViajeEnCursoTiraException() {
        assertThrows(ReglaNegocioException.class, () -> controlador.devolverVehiculo(DOCUMENTO, "DESTINO"));
    }


    @Test
    void DevolverVehiculoEnEstacionInexistenteTiraExceptionYElViajeSigueEnCurso() throws Exception {
        Viaje viaje = controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        assertThrows(ReglaNegocioException.class, () -> controlador.devolverVehiculo(DOCUMENTO, "NOEXISTE"));
        assertEquals(EstadoViaje.EN_CURSO, viaje.getEstado());
        assertSame(viaje, usuario.getViajeEnCurso());
    }


    @Test
    void DevolverVehiculoCierraElViajeEnLaEstacionDeDestino() throws Exception {
        Viaje viaje = controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        reloj.avanzar(Duration.ofMinutes(30));
        controlador.devolverVehiculo(DOCUMENTO, "DESTINO");
        // verifico que el viaje quede cerrado con el destino y el fin correctos
        assertEquals(EstadoViaje.CERRADO, viaje.getEstado());
        assertSame(estacionDestino, viaje.getDestino());
        assertEquals(reloj.ahora(), viaje.getFin());
        // verifico que se haya agregado el punto de llegada, ahora tiene 2 puntos
        assertEquals(2, viaje.getPuntos().size());
    }


    @Test
    void DevolverVehiculoDejaElVehiculoDisponibleEnLaEstacionDeDestino() throws Exception {
        Viaje viaje = CerrarUnViaje();
        // verifico que la bicicleta este en el destino y ya no en el origen
        assertSame(bicicleta, estacionDestino.buscarVehiculo("B1"));
        assertNull(estacionOrigen.buscarVehiculo("B1"));
        // verifico que quede disponible y que se le hayan sumado los km del viaje
        verify(bicicleta).setEstado(EstadoVehiculo.DISPONIBLE);
        verify(bicicleta).agregarKm(viaje.getDistancia());
    }


    @Test
    void DevolverVehiculoCobraElImporteDelCalculadorYLoRegistraComoDebito() throws Exception {
        Viaje viaje = CerrarUnViaje();
        // verifico que le haya pedido el importe al calculador con ese viaje
        verify(calculador).calcular(viaje);
        // verifico que el importe quede en el viaje y se haya descontado del saldo: 20000 - 5000 = 15000
        assertEquals(5000, viaje.getImporte(), 0.001);
        assertEquals(15000, usuario.getCuenta().getSaldo(), 0.001);
        // verifico que el ultimo movimiento de la cuenta sea el debito del cierre
        List<Movimiento> movimientos = usuario.getCuenta().getMovimientos();
        Movimiento ultimo = movimientos.get(movimientos.size() - 1);
        assertEquals(TipoMovimiento.DEBITO, ultimo.getTipo());
        assertEquals(5000, ultimo.getImporte(), 0.001);
        assertEquals("Cierre de viaje #0", ultimo.getConcepto());
    }


    @Test
    void DevolverVehiculoLiberaAlUsuarioYYaNoEsSuPrimerViaje() throws Exception {
        assertTrue(usuario.esPrimerViaje());
        CerrarUnViaje();
        // verifico que el usuario ya no tenga viaje en curso y que haya dejado de ser su primer viaje
        assertNull(usuario.getViajeEnCurso());
        assertFalse(usuario.esPrimerViaje());
    }


    @Test
    void DevolverVehiculoConSaldoQueQuedaNegativoInhabilitaAlUsuario() throws Exception {
        // hago que el viaje cueste 30000 y el usuario tiene 20000
        when(calculador.calcular(any(Viaje.class))).thenReturn(30000.0);
        CerrarUnViaje();
        assertEquals(-10000, usuario.getCuenta().getSaldo(), 0.001);
        // verifico que haya quedado inhabilitado
        assertThrows(UsuarioInhabilitadoException.class, () -> usuario.verificarHabilitacion(0));
    }


    @Test
    void DevolverVehiculoConSaldoQueQuedaEnCeroNoInhabilitaAlUsuario() throws Exception {
        // hago que el viaje cueste justo los 20000 que tiene el usuario
        when(calculador.calcular(any(Viaje.class))).thenReturn(20000.0);
        CerrarUnViaje();
        assertEquals(0, usuario.getCuenta().getSaldo(), 0.001);
        // verifico que siga habilitado, es el borde porque solo se inhabilita con saldo menor a 0
        assertDoesNotThrow(() -> usuario.verificarHabilitacion(0));
    }


    @Test
    void DevolverVehiculoEnEstacionLlenaTiraExceptionYBonifica10MinutosSinCerrarElViaje() throws Exception {
        // creo una estacion con las 5 bicicletas comunes que le entran
        Estacion llena = CrearEstacion("LLENA");
        AgregarRellenos(llena, TipoVehiculo.BICICLETA_COMUN, 5);
        controlador.agregarEstacion(llena);
        Viaje viaje = controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        assertThrows(EstacionLlenaException.class, () -> controlador.devolverVehiculo(DOCUMENTO, "LLENA"));
        // verifico que se hayan bonificado 10 minutos
        assertEquals(10, viaje.getMinutosBonificados());
        // verifico que el viaje siga en curso, que la bicicleta no este en la estacion llena y que no se haya cobrado
        assertEquals(EstadoViaje.EN_CURSO, viaje.getEstado());
        assertSame(viaje, usuario.getViajeEnCurso());
        assertNull(llena.buscarVehiculo("B1"));
        assertEquals(20000, usuario.getCuenta().getSaldo(), 0.001);
    }


    @Test
    void DevolverVehiculoElectricoConNivelDeBateriaLoActualiza() throws Exception {
        BicicletaElectrica electrica = RetirarElectrica(80);
        controlador.devolverVehiculo(DOCUMENTO, "DESTINO", 80);
        // verifico que se haya actualizado la bateria con el nivel que le pase y que no se haya calculado por distancia
        verify(electrica).actualizarBateria(80);
        verify(electrica, never()).consumirPorDistancia(anyDouble());
        verify(electrica).setEstado(EstadoVehiculo.DISPONIBLE);
    }


    @Test
    void DevolverVehiculoElectricoSinNivelConsumeLaBateriaSegunLaDistancia() throws Exception {
        BicicletaElectrica electrica = RetirarElectrica(80);
        Viaje viaje = usuario.getViajeEnCurso();
        controlador.devolverVehiculo(DOCUMENTO, "DESTINO");
        // verifico que haya consumido bateria segun la distancia del viaje y que no se haya pisado el nivel
        verify(electrica).consumirPorDistancia(viaje.getDistancia());
        verify(electrica, never()).actualizarBateria(anyInt());
    }


    @Test
    void DevolverVehiculoElectricoConBateriaBajaLoMarcaConBateriaBaja() throws Exception {
        BicicletaElectrica electrica = RetirarElectrica(10);
        controlador.devolverVehiculo(DOCUMENTO, "DESTINO");
        verify(electrica).setEstado(EstadoVehiculo.BATERIA_BAJA);
        verify(electrica, never()).setEstado(EstadoVehiculo.DISPONIBLE);
    }


    @Test
    void DevolverVehiculoElectricoConBateriaDeExactamente15QuedaDisponible() throws Exception {
        BicicletaElectrica electrica = RetirarElectrica(15);
        controlador.devolverVehiculo(DOCUMENTO, "DESTINO");
        // verifico que 15 no cuente como bateria baja, es el borde
        verify(electrica).setEstado(EstadoVehiculo.DISPONIBLE);
        verify(electrica, never()).setEstado(EstadoVehiculo.BATERIA_BAJA);
    }


    @Test
    void DevolverVehiculoConTresFallasLoEnviaAReparacion() throws Exception {
        when(bicicleta.fallasUltimos30Dias(any())).thenReturn(3);
        CerrarUnViaje();
        verify(bicicleta).enviarAReparacion();
        verify(bicicleta, never()).setEstado(EstadoVehiculo.DISPONIBLE);
    }


    @Test
    void DevolverVehiculoConDosFallasQuedaDisponible() throws Exception {
        when(bicicleta.fallasUltimos30Dias(any())).thenReturn(2);
        CerrarUnViaje();
        // verifico que con 2 fallas no se envie a reparacion, es el borde porque la condicion es 3 o mas
        verify(bicicleta, never()).enviarAReparacion();
        verify(bicicleta).setEstado(EstadoVehiculo.DISPONIBLE);
    }


    @Test
    void DevolverVehiculoQueYaEstabaEnReparacionLoVuelveAEnviarAReparacion() throws Exception {
        when(bicicleta.getEstado()).thenReturn(EstadoVehiculo.EN_REPARACION);
        CerrarUnViaje();
        verify(bicicleta).enviarAReparacion();
    }


    @Test
    void DevolverVehiculoEnEstacionQueQuedaMuyLlenaGeneraUnaAlerta() throws Exception {
        // creo una estacion con 9 vehiculos, al devolver queda 10 de 10 que es 1.0 y es mas de 0.9
        Estacion casiLlena = CrearEstacion("CASILLENA");
        AgregarRellenos(casiLlena, TipoVehiculo.BICICLETA_ELECTRICA, 5);
        AgregarRellenos(casiLlena, TipoVehiculo.BICICLETA_COMUN, 4);
        controlador.agregarEstacion(casiLlena);
        controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        controlador.devolverVehiculo(DOCUMENTO, "CASILLENA");
        // verifico que se haya generado una sola alerta y que mencione a la estacion
        assertEquals(1, avisosAlerta.size());
        assertTrue(avisosAlerta.get(0).contains("CASILLENA"));
    }


    @Test
    void RegistrarNotaAjusteConViajeNullTiraException() {
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarNotaAjuste(null, "Motivo", 100));
    }


    @Test
    void RegistrarNotaAjusteConViajeQueNoEsDelControladorTiraException() {
        // creo un viaje por fuera del controlador
        Viaje ajeno = new Viaje(usuario, bicicleta, estacionOrigen, reloj.ahora());
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarNotaAjuste(ajeno, "Motivo", 100));
    }


    @Test
    void RegistrarNotaAjusteConViajeSinCerrarTiraException() throws Exception {
        Viaje enCurso = controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarNotaAjuste(enCurso, "Motivo", 100));
        assertTrue(enCurso.getNotasAjuste().isEmpty());
    }


    @Test
    void RegistrarNotaAjusteConMotivoNullTiraException() throws Exception {
        Viaje cerrado = CerrarUnViaje();
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarNotaAjuste(cerrado, null, 100));
    }


    @Test
    void RegistrarNotaAjusteConMotivoEnBlancoTiraException() throws Exception {
        Viaje cerrado = CerrarUnViaje();
        // pruebo con solo espacios
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarNotaAjuste(cerrado, "   ", 100));
    }


    @Test
    void RegistrarNotaAjusteConImporteCeroTiraException() throws Exception {
        Viaje cerrado = CerrarUnViaje();
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarNotaAjuste(cerrado, "Motivo", 0));
    }


    @Test
    void RegistrarNotaAjusteGuardaLaNotaEnElViajeConSusDatos() throws Exception {
        Viaje cerrado = CerrarUnViaje();
        controlador.registrarNotaAjuste(cerrado, "Descuento por demora", 500);
        // verifico que el viaje tenga la nota con el motivo, el importe y la fecha del reloj
        assertEquals(1, cerrado.getNotasAjuste().size());
        NotaAjuste nota = cerrado.getNotasAjuste().get(0);
        assertEquals("Descuento por demora", nota.getMotivo());
        assertEquals(500, nota.getImporte(), 0.001);
        assertEquals(reloj.ahora(), nota.getFecha());
    }


    @Test
    void RegistrarNotaAjusteQueDejaElSaldoPositivoRehabilitaAlUsuario() throws Exception {
        // dejo al usuario inhabilitado: el viaje cuesta 30000 y tiene 20000, queda en -10000
        when(calculador.calcular(any(Viaje.class))).thenReturn(30000.0);
        Viaje cerrado = CerrarUnViaje();
        assertThrows(UsuarioInhabilitadoException.class, () -> usuario.verificarHabilitacion(0));
        // aplico una nota de 15000 a su favor, el saldo pasa a 5000
        controlador.registrarNotaAjuste(cerrado, "Cobro de mas", 15000);
        // verifico que haya quedado habilitado de nuevo
        assertDoesNotThrow(() -> usuario.verificarHabilitacion(0));
    }


    @Test
    void RegistrarNotaAjusteQueDejaElSaldoNegativoInhabilitaAlUsuario() throws Exception {
        // el usuario queda con 15000 y habilitado
        Viaje cerrado = CerrarUnViaje();
        assertDoesNotThrow(() -> usuario.verificarHabilitacion(0));
        // aplico una nota de -20000, el saldo pasa a -5000
        controlador.registrarNotaAjuste(cerrado, "Cargo extra", -20000);
        // verifico que haya quedado inhabilitado
        assertThrows(UsuarioInhabilitadoException.class, () -> usuario.verificarHabilitacion(0));
    }


    @Test
    void ControlarViajesVencidosConExactamente24HorasMarcaElViajeComoPerdido() throws Exception {
        Viaje viaje = controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        // paso justo 24 horas, es el borde porque la condicion es 24 o mas
        reloj.avanzar(Duration.ofHours(24));
        List<Viaje> vencidos = controlador.controlarViajesVencidos();
        // verifico que lo devuelva en la lista y que quede perdido con el cargo fijo
        assertEquals(List.of(viaje), vencidos);
        assertEquals(EstadoViaje.PERDIDO, viaje.getEstado());
        assertEquals(150000, viaje.getImporte(), 0.001);
    }


    @Test
    void ControlarViajesVencidosCobraElCargoDejaElVehiculoFueraDeServicioYLiberaAlUsuario() throws Exception {
        controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        reloj.avanzar(Duration.ofHours(25));
        controlador.controlarViajesVencidos();
        // verifico que el vehiculo quede fuera de servicio
        verify(bicicleta).setEstado(EstadoVehiculo.FUERA_DE_SERVICIO);
        // verifico que se hayan descontado los 150000 del saldo: 20000 - 150000 = -130000
        assertEquals(-130000, usuario.getCuenta().getSaldo(), 0.001);
        // verifico que el usuario quede sin viaje en curso e inhabilitado por el saldo negativo
        assertNull(usuario.getViajeEnCurso());
        assertThrows(UsuarioInhabilitadoException.class, () -> usuario.verificarHabilitacion(0));
    }


    @Test
    void ControlarViajesVencidosConMenosDe24HorasNoHaceNada() throws Exception {
        Viaje viaje = controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        // paso 23 horas y 59 minutos
        reloj.avanzar(Duration.ofHours(23).plusMinutes(59));
        assertTrue(controlador.controlarViajesVencidos().isEmpty());
        assertEquals(EstadoViaje.EN_CURSO, viaje.getEstado());
        assertSame(viaje, usuario.getViajeEnCurso());
    }


    @Test
    void ControlarViajesVencidosIgnoraLosViajesYaCerrados() throws Exception {
        Viaje cerrado = CerrarUnViaje();
        reloj.avanzar(Duration.ofHours(48));
        // verifico que un viaje cerrado hace mucho no se marque como perdido
        assertTrue(controlador.controlarViajesVencidos().isEmpty());
        assertEquals(EstadoViaje.CERRADO, cerrado.getEstado());
    }


    @Test
    void ControlarViajesVencidosConSaldoSuficienteNoInhabilitaAlUsuario() throws Exception {
        // le cargo 200000 mas al usuario, tiene 220000 y el cargo es de 150000
        usuario.recargarSaldo(200000, TipoMedioPago.TARJETA, "MasterCard");
        controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        reloj.avanzar(Duration.ofHours(24));
        controlador.controlarViajesVencidos();
        assertEquals(70000, usuario.getCuenta().getSaldo(), 0.001);
        // verifico que siga habilitado
        assertDoesNotThrow(() -> usuario.verificarHabilitacion(0));
    }


    @Test
    void GetEstacionesConLugarNoIncluyeLasEstacionesLlenas() {
        // creo una estacion con las 5 bicicletas comunes que le entran
        Estacion llena = CrearEstacion("LLENA");
        AgregarRellenos(llena, TipoVehiculo.BICICLETA_COMUN, 5);
        controlador.agregarEstacion(llena);
        List<Estacion> conLugar = controlador.getEstacionesConLugar(TipoVehiculo.BICICLETA_COMUN);
        // verifico que esten el origen y el destino pero no la llena
        assertEquals(2, conLugar.size());
        assertFalse(conLugar.contains(llena));
    }


    @Test
    void GetViajesUsuarioDevuelveSoloLosViajesDeEseUsuario() throws Exception {
        // creo un segundo usuario y una segunda bicicleta, y cada uno retira una
        Usuario otro = CrearOtroUsuario("22222222", 20000);
        estacionOrigen.recibir(CrearVehiculo("B2", TipoVehiculo.BICICLETA_COMUN, true));
        controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        controlador.retirarVehiculo("22222222", "ORIGEN", "B2");
        // verifico que cada usuario vea solo su viaje
        assertEquals(1, controlador.getViajesUsuario(DOCUMENTO).size());
        assertSame(usuario, controlador.getViajesUsuario(DOCUMENTO).get(0).getUsuario());
        assertSame(otro, controlador.getViajesUsuario("22222222").get(0).getUsuario());
        // verifico que un usuario sin viajes de lista vacia
        assertTrue(controlador.getViajesUsuario("33333333").isEmpty());
    }


    @Test
    void GetViajesDevuelveUnaCopiaYNoLaListaInterna() throws Exception {
        controlador.retirarVehiculo(DOCUMENTO, "ORIGEN", "B1");
        // intento vaciar la lista que me devuelve
        controlador.getViajes().clear();
        assertEquals(1, controlador.getViajes().size());
    }


    @Test
    void GetEstacionesDevuelveUnaCopiaYNoElMapaInterno() {
        // intento vaciar el mapa que me devuelve
        controlador.getEstaciones().clear();
        assertEquals(2, controlador.getEstaciones().size());
    }


    @Test
    void ConstructorConNullsArrancaSinViajesNiEstaciones() {
        // creo un controlador pasandole null en todos los parametros menos calculador y reloj
        ControladorViajes vacio = new ControladorViajes(null, null, null, null, calculador, reloj);
        // verifico que no se rompa y que arranque vacio
        assertTrue(vacio.getViajes().isEmpty());
        assertTrue(vacio.getEstaciones().isEmpty());
    }
}