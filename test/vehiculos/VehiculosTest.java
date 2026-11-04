package vehiculos;



import enums.EstadoVehiculo;
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

import static enums.EstadoVehiculo.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;


public class VehiculosTest {
    private BicicletaElectrica vehiculo;


    @BeforeEach
    void CrearVehiculo(){
        vehiculo = new BicicletaElectrica("1111",67,67,LocalDate.now());
    }
    @Test
    void ActualizarLaBateriaYVerAQueEstado() {
        //verifico que el vehiculo esta disponible
        assertEquals(DISPONIBLE, vehiculo.getEstado());
        // actualizo la bateria 14 %
        vehiculo.actualizarBateria(14);
        // verifico que cambio a bateria baja
        assertEquals(BATERIA_BAJA, vehiculo.getEstado());
    }
    @Test
    void ActualizarLaBateriaYDisponible() {
        // actualizo la bateria 14 %
        vehiculo.actualizarBateria(14);
        // verifico que cambio a bateria baja
        assertEquals(BATERIA_BAJA, vehiculo.getEstado());
        // cambio la bateria a 100%
        vehiculo.actualizarBateria(100);
        // verifico que el vehiculo esta disponible por el cambio de bateria
        assertEquals(DISPONIBLE,vehiculo.getEstado());
    }


}













