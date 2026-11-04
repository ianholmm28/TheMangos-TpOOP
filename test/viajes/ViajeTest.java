package viajes;

import enums.EstadoViaje;
import estaciones.Estacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usuarios.Usuario;
import vehiculos.Vehiculo;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ViajeTest {
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 15, 10, 0);

    private Usuario usuario;
    private Vehiculo vehiculo;
    private Estacion origen;
    private Viaje viaje;

    @BeforeEach
    void CrearViaje() {
            // creo los objetos falsos que necesita el viaje, a Viaje solo le importa guardarlos
            usuario = mock(Usuario.class);
            vehiculo = mock(Vehiculo.class);
            origen = mock(Estacion.class);
            viaje = new Viaje(usuario, vehiculo, origen, INICIO);
    }


    @Test
    void ViajeNuevoArrancaEnCursoYSinDatos() {
        assertEquals(EstadoViaje.EN_CURSO, viaje.getEstado());
        assertNull(viaje.getFin());
        assertNull(viaje.getDestino());
        assertEquals(0, viaje.getDistancia(), 0.001);
        assertEquals(0, viaje.getImporte(), 0.001);
        assertEquals(0, viaje.getMinutosBonificados());
        assertTrue(viaje.getPuntos().isEmpty());
        assertTrue(viaje.getNotasAjuste().isEmpty());
    }


    @Test
    void ViajeNuevoGuardaUsuarioVehiculoOrigenEInicio() {
        assertSame(usuario, viaje.getUsuario());
        assertSame(vehiculo, viaje.getVehiculo());
        assertSame(origen, viaje.getOrigen());
        assertEquals(INICIO, viaje.getInicio());
    }


    @Test
    void CerrarGuardaDestinoYFinYCambiaElEstadoACerrado() {
        Estacion destino = mock(Estacion.class);
        LocalDateTime fin = INICIO.plusMinutes(30);
        viaje.cerrar(destino, fin);
        // verifico que se hayan guardado el destino y el fin, y que el viaje quede cerrado
        assertSame(destino, viaje.getDestino());
        assertEquals(fin, viaje.getFin());
        assertEquals(EstadoViaje.CERRADO, viaje.getEstado());
    }

    @Test
    void MinutosCobrablesDeUnViajeSinCerrarEsCero() {
        // sin fin, el calculo usa el inicio como fin, entonces no pasa ningun tiempo
        assertEquals(0, viaje.getMinutosCobrables());
    }


    @Test
    void MinutosCobrablesExactosNoSeRedondean() {
        viaje.cerrar(mock(Estacion.class), INICIO.plusMinutes(30));
        assertEquals(30, viaje.getMinutosCobrables());
    }

    @Test
    void MinutosCobrablesRedondeaHaciaArribaSiSobranSegundos() {
        // 30 minutos y 1 segundo se cobran como 31
        viaje.cerrar(mock(Estacion.class), INICIO.plusMinutes(30).plusSeconds(1));
        assertEquals(31, viaje.getMinutosCobrables());
    }


    @Test
    void MinutosCobrablesRestaLosMinutosBonificados() {
        viaje.registrarIntentoEstacionLlena();
        viaje.cerrar(mock(Estacion.class), INICIO.plusMinutes(30));
        // 30 - 10 = 20
        assertEquals(20, viaje.getMinutosCobrables());
    }


    @Test
    void MinutosCobrablesNuncaEsNegativo() {
        viaje.registrarIntentoEstacionLlena();
        // el viaje dura 5 minutos y se bonifican 10, tiene que quedar en 0 y no en -5
        viaje.cerrar(mock(Estacion.class), INICIO.plusMinutes(5));
        assertEquals(0, viaje.getMinutosCobrables());
    }


    @Test
    void RegistrarIntentoEstacionLlenaBonifica10Minutos() {
        viaje.registrarIntentoEstacionLlena();
        assertEquals(10, viaje.getMinutosBonificados());
    }


    @Test
    void RegistrarIntentoEstacionLlenaConHoraBonifica10Minutos() {
        viaje.registrarIntentoEstacionLlena(INICIO.plusMinutes(20));
        assertEquals(10, viaje.getMinutosBonificados());
    }


    @Test
    void RegistrarIntentoEstacionLlenaDosVecesNoAcumulaLaBonificacion() {
        viaje.registrarIntentoEstacionLlena();
        viaje.registrarIntentoEstacionLlena();
        // verifico que sigan siendo 10 y no 20
        assertEquals(10, viaje.getMinutosBonificados());
    }


    @Test
    void AgregarElPrimerPuntoNoSumaDistancia() {
        viaje.agregarPunto(0.0, 0.0, INICIO);
        assertEquals(1, viaje.getPuntos().size());
        assertEquals(0, viaje.getDistancia(), 0.001);
    }

    @Test
    void AgregarDosPuntosSumaLaDistanciaEntreEllos() {
        viaje.agregarPunto(0.0, 0.0, INICIO);
        viaje.agregarPunto(0.03, 0.04, INICIO.plusMinutes(1));
        // sqrt(0.03^2 + 0.04^2) = 0.05 grados, y 0.05 * 111 = 5.55 km
        assertEquals(5.55, viaje.getDistancia(), 0.001);
    }


    @Test
    void AgregarTresPuntosAcumulaLaDistanciaDeCadaTramo() {
        viaje.agregarPunto(0.0, 0.0, INICIO);
        viaje.agregarPunto(0.0, 0.1, INICIO.plusMinutes(1));
        viaje.agregarPunto(0.1, 0.1, INICIO.plusMinutes(2));
        // cada tramo mide 0.1 grados = 11.1 km, en total 22.2
        assertEquals(22.2, viaje.getDistancia(), 0.001);
        assertEquals(3, viaje.getPuntos().size());
    }


    @Test
    void AgregarPuntoConLaHoraPrimeroGuardaLatitudYLongitudEnSuLugar() {
        // uso la sobrecarga que recibe (hora, longitud, latitud)
        viaje.agregarPunto(INICIO, 10.0, 20.0);
        PuntoRecorrido punto = viaje.getPuntos().get(0);
        // verifico que no se hayan mezclado la latitud y la longitud
        assertEquals(20.0, punto.getLatitud(), 0.001);
        assertEquals(10.0, punto.getLongitud(), 0.001);
        assertEquals(INICIO, punto.getHora());
    }


    @Test
    void GetPuntosDevuelveUnaCopiaYNoLaListaInterna() {
        viaje.agregarPunto(0.0, 0.0, INICIO);
        //intento vaciar la lista que me devuelve
        viaje.getPuntos().clear();
        assertEquals(1, viaje.getPuntos().size());
    }


    @Test
    void AgregarNotaAjusteGuardaLaNotaConSusDatos() {
        viaje.agregarNotaAjuste(INICIO, "Descuento por demora", 500);
        assertEquals(1, viaje.getNotasAjuste().size());
        NotaAjuste nota = viaje.getNotasAjuste().getFirst();
        assertEquals(INICIO, nota.getFecha());
        assertEquals("Descuento por demora", nota.getMotivo());
        assertEquals(500, nota.getImporte(), 0.001);
    }


    @Test
    void GetNotasAjusteDevuelveUnaCopiaYNoLaListaInterna() {
        viaje.agregarNotaAjuste(INICIO, "Descuento por demora", 500);
        viaje.getNotasAjuste().clear();
        assertEquals(1, viaje.getNotasAjuste().size());
    }
}

