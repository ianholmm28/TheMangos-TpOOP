package usuarios;
import enums.TipoMovimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class CuentaTest {
    private Cuenta cuenta;
        @BeforeEach
        //creo el objeto cuenta
        void CrearCuenta() {
            cuenta = new Cuenta(0, new ArrayList<>());
        }

        @Test
        void RecargarSumaSaldoYRegistraMovimiento() {
            //cargo a la tarjeta 1000 pesos
            cuenta.recargar(1000, "Mastercard");
            // verifico que la recarga se haya hecho bien
            assertEquals(1000, cuenta.getSaldo());
            // verifico que se haya aniadido un movimiento a la cuenta
            assertEquals(1, cuenta.getMovimientos().size());
            //verifico que el movimiento que se registro sea el correcto
            assertEquals(TipoMovimiento.RECARGA, cuenta.getMovimientos().getFirst().getTipo());
        }

        @Test
        void DebitarEestaSaldoYRegistraMovimiento() {
            //cargo a la tarjeta 1000 pesos
            cuenta.recargar(1000, "Mastercard");
            // debito 400 pesos del saldo
            cuenta.debitar(400, "Plan");
            // verifico que se haya debitado bien el saldo
            assertEquals(600, cuenta.getSaldo());
            // verifico que la cuenta tenga registrados los 2 movimientos que se hicieron antes
            assertEquals(2, cuenta.getMovimientos().size());
            // verifico que el ultimo movimiento que se tenga sea debitar
            assertEquals(TipoMovimiento.DEBITO, cuenta.getMovimientos().get(1).getTipo());
        }
    }

