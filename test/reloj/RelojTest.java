package reloj;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
public class RelojTest {
    //declaro los objetos que voy a usar para hacer los testeos

    private RelojFijo relojPrueba;
    private LocalDateTime ahora;
    //creo los objetos principales

    @BeforeEach
    void crearobjetos(){
        relojPrueba = new RelojFijo();
        ahora = relojPrueba.ahora();
    }

    @Test
    public void avanzarReloj_por10Min(){
        // aniado 10 minutos
        Duration duracion = Duration.ofMinutes(10);
        relojPrueba.avanzar(duracion);
        // chequeo que se haya aumentado
        assertEquals(ahora.plusMinutes(10), relojPrueba.ahora());
    }
    @Test
    public void mandar_null_avanzarreloj(){
        relojPrueba.avanzar(null);
        //chequeo que no haya cambiado el resultado
        assertEquals(ahora, relojPrueba.ahora());
    }
}
