package estaciones;


import enums.TipoVehiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vehiculos.Vehiculo;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class EstacionTest {
    private Estacion estacion;
    private Map<TipoVehiculo, Integer> capacidades;


    @BeforeEach
    void CrearEstacion() {
        // creo un mapa con lugar para 6 bicicletas comunes y 4 monopatines, en total 10
        capacidades = new HashMap<>();
        capacidades.put(TipoVehiculo.BICICLETA_COMUN, 6);
        capacidades.put(TipoVehiculo.MONOPATIN_ELECTRICO, 4);
        // creo la estacion con ese mapa
        estacion = new Estacion("E1", "Av. Siempre Viva 123", -34.6, -58.4, capacidades);
    }


    // creo un vehiculo falso con el codigo y el tipo que le paso, y que esta (o no) disponible
    private Vehiculo CrearVehiculo(String codigo, TipoVehiculo tipo, boolean disponible) {
        Vehiculo vehiculo = mock(Vehiculo.class);
        when(vehiculo.getCodigo()).thenReturn(codigo);
        when(vehiculo.getTipo()).thenReturn(tipo);
        when(vehiculo.estaDisponible()).thenReturn(disponible);
        return vehiculo;
    }


    // recibo en la estacion la cantidad de vehiculos del tipo que le paso, todos disponibles
    private void RecibirVehiculos(TipoVehiculo tipo, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            estacion.recibir(CrearVehiculo(tipo + "-" + i, tipo, true));
        }
    }


    @Test
    void EstacionNuevaGuardaSusDatosYArrancaSinVehiculos() {
        // verifico que se hayan guardado los datos que le pase al constructor
        assertEquals("E1", estacion.getCodigo());
        assertEquals("Av. Siempre Viva 123", estacion.getDireccion());
        assertEquals(-34.6, estacion.getLatitud(), 0.001);
        assertEquals(-58.4, estacion.getLongitud(), 0.001);
        // verifico que la estacion arranque sin vehiculos
        assertTrue(estacion.getVehiculos().isEmpty());
    }


    @Test
    void ConstructorCopiaElMapaDeCapacidades() {
        // modifico el mapa original despues de crear la estacion
        capacidades.put(TipoVehiculo.BICICLETA_ELECTRICA, 50);
        // verifico que la estacion no se haya enterado y siga con capacidad 10
        assertEquals(10, estacion.getCapacidadTotal());
    }


    @Test
    void GetCapacidadPorTipoDevuelveUnaCopiaYNoElMapaInterno() {
        // intento vaciar el mapa que me devuelve
        estacion.getCapacidadPorTipo().clear();
        // verifico que la estacion siga con su capacidad
        assertEquals(10, estacion.getCapacidadTotal());
    }


    @Test
    void GetVehiculosDevuelveUnaCopiaYNoLaListaInterna() {
        // recibo 2 bicicletas
        RecibirVehiculos(TipoVehiculo.BICICLETA_COMUN, 2);
        // intento vaciar la lista que me devuelve
        estacion.getVehiculos().clear();
        // verifico que la estacion siga teniendo las 2 bicicletas
        assertEquals(2, estacion.getVehiculos().size());
    }


    @Test
    void RecibirVehiculoLoAgregaALaEstacion() {
        // creo un vehiculo y se lo doy a la estacion
        Vehiculo vehiculo = CrearVehiculo("B1", TipoVehiculo.BICICLETA_COMUN, true);
        estacion.recibir(vehiculo);
        // verifico que la estacion tenga ese vehiculo y nada mas
        assertEquals(1, estacion.getVehiculos().size());
        assertSame(vehiculo, estacion.getVehiculos().get(0));
    }


    @Test
    void RecibirNullNoAgregaNada() {
        // le doy null a la estacion
        estacion.recibir(null);
        // verifico que no haya agregado nada
        assertTrue(estacion.getVehiculos().isEmpty());
    }


    @Test
    void BuscarVehiculoExistenteLoDevuelveSinSacarloDeLaEstacion() {
        // agrego un vehiculo con codigo B1
        Vehiculo vehiculo = CrearVehiculo("B1", TipoVehiculo.BICICLETA_COMUN, true);
        estacion.recibir(vehiculo);
        // verifico que al buscarlo por su codigo me devuelva ese mismo vehiculo
        assertSame(vehiculo, estacion.buscarVehiculo("B1"));
        // verifico que buscar no lo haya sacado de la estacion
        assertEquals(1, estacion.getVehiculos().size());
    }


    @Test
    void BuscarVehiculoQueNoExisteDevuelveNull() {
        // agrego un vehiculo con codigo B1
        estacion.recibir(CrearVehiculo("B1", TipoVehiculo.BICICLETA_COMUN, true));
        // busco uno con un codigo que no esta registrado y verifico que devuelva null
        assertNull(estacion.buscarVehiculo("B2"));
    }


    @Test
    void EntregarVehiculoExistenteLoDevuelveYLoSacaSoloEsaUnidad() {
        // agrego dos vehiculos
        Vehiculo primero = CrearVehiculo("B1", TipoVehiculo.BICICLETA_COMUN, true);
        Vehiculo segundo = CrearVehiculo("B2", TipoVehiculo.BICICLETA_COMUN, true);
        estacion.recibir(primero);
        estacion.recibir(segundo);
        // entrego el primero
        Vehiculo entregado = estacion.entregar("B1");
        // verifico que me haya devuelto el que pedi
        assertSame(primero, entregado);
        // verifico que ya no este en la estacion
        assertNull(estacion.buscarVehiculo("B1"));
        // verifico que el otro siga estando
        assertSame(segundo, estacion.buscarVehiculo("B2"));
        assertEquals(1, estacion.getVehiculos().size());
    }


    @Test
    void EntregarVehiculoQueNoExisteDevuelveNullYNoModificaNada() {
        // agrego un vehiculo con codigo B1
        estacion.recibir(CrearVehiculo("B1", TipoVehiculo.BICICLETA_COMUN, true));
        // intento entregar uno que no esta registrado y verifico que devuelva null
        assertNull(estacion.entregar("B2"));
        // verifico que el vehiculo que si estaba siga en la estacion
        assertEquals(1, estacion.getVehiculos().size());
    }


    @Test
    void CantidadVehiculosPorTipoCuentaSoloLosDeEseTipo() {
        // recibo 2 bicicletas y 1 monopatin
        RecibirVehiculos(TipoVehiculo.BICICLETA_COMUN, 2);
        RecibirVehiculos(TipoVehiculo.MONOPATIN_ELECTRICO, 1);
        // verifico que cada tipo cuente solo los suyos
        assertEquals(2, estacion.cantidadVehiculosPorTipo(TipoVehiculo.BICICLETA_COMUN));
        assertEquals(1, estacion.cantidadVehiculosPorTipo(TipoVehiculo.MONOPATIN_ELECTRICO));
        // verifico que un tipo que no esta en la estacion de 0
        assertEquals(0, estacion.cantidadVehiculosPorTipo(TipoVehiculo.BICICLETA_ELECTRICA));
    }


    @Test
    void CantidadVehiculosDisponiblesPorTipoNoCuentaLosNoDisponibles() {
        // agrego 3 bicicletas, una de ellas no disponible
        estacion.recibir(CrearVehiculo("B1", TipoVehiculo.BICICLETA_COMUN, true));
        estacion.recibir(CrearVehiculo("B2", TipoVehiculo.BICICLETA_COMUN, false));
        estacion.recibir(CrearVehiculo("B3", TipoVehiculo.BICICLETA_COMUN, true));
        // verifico que de las 3 que hay solo cuente las 2 disponibles
        assertEquals(2, estacion.cantidadVehiculosDisponiblesPorTipo(TipoVehiculo.BICICLETA_COMUN));
        // verifico que el conteo normal si cuente las 3
        assertEquals(3, estacion.cantidadVehiculosPorTipo(TipoVehiculo.BICICLETA_COMUN));
    }


    @Test
    void LugaresLibresEsLaCapacidadMenosLosPresentes() {
        // recibo 2 bicicletas
        RecibirVehiculos(TipoVehiculo.BICICLETA_COMUN, 2);
        // verifico que queden 6 - 2 = 4 lugares libres para bicicletas
        assertEquals(4, estacion.lugaresLibres(TipoVehiculo.BICICLETA_COMUN));
        // verifico que los lugares de los monopatines no se hayan visto afectados
        assertEquals(4, estacion.lugaresLibres(TipoVehiculo.MONOPATIN_ELECTRICO));
    }


    @Test
    void LugaresLibresDeUnTipoSinCapacidadDefinidaEsCero() {
        // verifico que un tipo que la estacion no tiene definido no tenga lugares
        assertEquals(0, estacion.lugaresLibres(TipoVehiculo.BICICLETA_ELECTRICA));
    }


    @Test
    void EstaLlenaParaConLugaresLibresEsFalse() {
        // recibo 3 monopatines de los 4 que entran
        RecibirVehiculos(TipoVehiculo.MONOPATIN_ELECTRICO, 3);
        // verifico que todavia quede 1 lugar y por eso no este llena
        assertFalse(estacion.estaLlenaPara(TipoVehiculo.MONOPATIN_ELECTRICO));
    }


    @Test
    void EstaLlenaParaConExactamenteLaCapacidadEsTrue() {
        // recibo los 4 monopatines que entran
        RecibirVehiculos(TipoVehiculo.MONOPATIN_ELECTRICO, 4);
        // verifico que este llena, es el borde porque quedan 0 lugares
        assertTrue(estacion.estaLlenaPara(TipoVehiculo.MONOPATIN_ELECTRICO));
    }


    @Test
    void EstaLlenaParaConMasVehiculosQueLaCapacidadEsTrue() {
        // recibo 5 monopatines aunque solo entran 4
        RecibirVehiculos(TipoVehiculo.MONOPATIN_ELECTRICO, 5);
        // verifico que recibir no valida la capacidad y los lugares libres den negativo
        assertEquals(-1, estacion.lugaresLibres(TipoVehiculo.MONOPATIN_ELECTRICO));
        // verifico que igual figure como llena
        assertTrue(estacion.estaLlenaPara(TipoVehiculo.MONOPATIN_ELECTRICO));
    }


    @Test
    void EstaLlenaParaUnTipoSinCapacidadDefinidaEsTrue() {
        // verifico que si la estacion no tiene lugar para ese tipo figure como llena
        assertTrue(estacion.estaLlenaPara(TipoVehiculo.BICICLETA_ELECTRICA));
    }


    @Test
    void GetCapacidadTotalSumaLasCapacidadesDeTodosLosTipos() {
        // verifico que sume 6 + 4 = 10
        assertEquals(10, estacion.getCapacidadTotal());
    }


    @Test
    void GetOcupacionEsLaProporcionDeVehiculosSobreLaCapacidadTotal() {
        // recibo 5 bicicletas de una capacidad total de 10
        RecibirVehiculos(TipoVehiculo.BICICLETA_COMUN, 5);
        // verifico que la ocupacion sea 5 / 10 = 0.5
        assertEquals(0.5, estacion.getOcupacion(), 0.001);
    }


    @Test
    void GetOcupacionConCapacidadTotalCeroDevuelveCeroYNoDivideX0() {
        // creo una estacion sin capacidades y le agrego un vehiculo
        Estacion sinCapacidad = new Estacion("E2", "Calle Falsa 456", 0, 0, new HashMap<>());
        sinCapacidad.recibir(CrearVehiculo("B1", TipoVehiculo.BICICLETA_COMUN, true));
        // verifico que la ocupacion de 0 y no se rompa por dividir por 0
        assertEquals(0, sinCapacidad.getOcupacion(), 0.001);
    }


    @Test
    void NecesitaRedistribucionConPocosVehiculosEsTrue() {
        // recibo 1 bicicleta, la ocupacion queda en 0.1
        RecibirVehiculos(TipoVehiculo.BICICLETA_COMUN, 1);
        // verifico que necesite redistribucion porque 0.1 es menor a 0.2
        assertTrue(estacion.necesitaRedistribucion());
    }


    @Test
    void NecesitaRedistribucionConExactamente20PorCientoEsFalse() {
        // recibo 2 bicicletas, la ocupacion queda en 0.2
        RecibirVehiculos(TipoVehiculo.BICICLETA_COMUN, 2);
        // verifico que no necesite redistribucion, borde de abajo porque 0.2 no es menor a 0.2
        assertFalse(estacion.necesitaRedistribucion());
    }


    @Test
    void NecesitaRedistribucionConOcupacionNormalEsFalse() {
        // recibo 5 bicicletas, la ocupacion queda en 0.5
        RecibirVehiculos(TipoVehiculo.BICICLETA_COMUN, 5);
        // verifico que no necesite redistribucion
        assertFalse(estacion.necesitaRedistribucion());
    }


    @Test
    void NecesitaRedistribucionConExactamente90PorCientoEsFalse() {
        // recibo 9 bicicletas, la ocupacion queda en 0.9
        RecibirVehiculos(TipoVehiculo.BICICLETA_COMUN, 9);
        // verifico que no necesite redistribucion, borde de arriba porque 0.9 no es mayor a 0.9
        assertFalse(estacion.necesitaRedistribucion());
    }


    @Test
    void NecesitaRedistribucionConLaEstacionLlenaEsTrue() {
        // recibo 10 bicicletas, la ocupacion queda en 1.0
        RecibirVehiculos(TipoVehiculo.BICICLETA_COMUN, 10);
        // verifico que necesite redistribucion porque 1.0 es mayor a 0.9
        assertTrue(estacion.necesitaRedistribucion());
    }


    @Test
    void NecesitaRedistribucionSinCapacidadEsFalse() {
        // creo una estacion sin capacidades
        Estacion sinCapacidad = new Estacion("E2", "Calle Falsa 456", 0, 0, new HashMap<>());
        // verifico que no necesite redistribucion
        assertFalse(sinCapacidad.necesitaRedistribucion());
    }


    @Test
    void ModificarCambiaDireccionCoordenadasYCapacidadesPeroNoElCodigo() {
        // creo capacidades nuevas, solo para bicicletas electricas
        Map<TipoVehiculo, Integer> nuevas = new HashMap<>();
        nuevas.put(TipoVehiculo.BICICLETA_ELECTRICA, 8);
        // modifico la estacion
        estacion.modificar("Calle Nueva 789", -34.7, -58.5, nuevas);
        // verifico que se hayan cambiado la direccion y las coordenadas
        assertEquals("Calle Nueva 789", estacion.getDireccion());
        assertEquals(-34.7, estacion.getLatitud(), 0.001);
        assertEquals(-58.5, estacion.getLongitud(), 0.001);
        // verifico que la capacidad total sea la nueva
        assertEquals(8, estacion.getCapacidadTotal());
        // verifico que la capacidad vieja ya no exista
        assertEquals(0, estacion.lugaresLibres(TipoVehiculo.BICICLETA_COMUN));
        // verifico que el codigo siga siendo el mismo
        assertEquals("E1", estacion.getCodigo());
    }


    @Test
    void ModificarNoSacaLosVehiculosQueYaEstaban() {
        // recibo 3 bicicletas
        RecibirVehiculos(TipoVehiculo.BICICLETA_COMUN, 3);
        // modifico la estacion
        estacion.modificar("Calle Nueva 789", -34.7, -58.5, new HashMap<>(capacidades));
        // verifico que las 3 bicicletas sigan estando
        assertEquals(3, estacion.getVehiculos().size());
    }
}