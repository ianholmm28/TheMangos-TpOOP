package tarifas;

import enums.TipoVehiculo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;


public class TarifaTest {
    private Tarifa tarifa;

    @BeforeEach
    void CrearTarifa() {
        tarifa = new Tarifa(TipoVehiculo.BICICLETA_COMUN, LocalDate.now(), 2000, 120);
    }


    @Test
    void calcularBaseCeroMinutosCobraSoloElDesbloqueo() {
        assertEquals(2000, tarifa.calcularBase(0), 0.001);
    }


    @Test
    void CalcularBaseMas130MinutosTranscurridos() {
        assertEquals(18200, tarifa.calcularBase(130), 0.001); //2000 + (120*120) + (10*(120*1.5))
    }


    @Test
    void Calcularbasecon120MinutosTranscurridos(){
        assertEquals(16400, tarifa.calcularBase(120), 0.001);
    }


    @Test
    void CalcularBase30MinutosCobraDesbloqueoMasMinutos() {
        assertEquals(5600, tarifa.calcularBase(30), 0.001);   // 2000 + 30*120
    }


    @Test
    void CalcularBase121MinutosRecargoSoloAlMinutoExcedente() {
        assertEquals(16580, tarifa.calcularBase(121), 0.001); // 2000 + 14400 + 1*180
    }


    @Test
    void CalcularBase150MinutosRecargoDel50PorCientoAlExcedente() {
        assertEquals(21800, tarifa.calcularBase(150), 0.001); // 2000 + 14400 + 30*180
    }

}