package reloj;

import java.time.LocalDateTime;

public class RelojSistema implements Reloj {

    @Override
    public LocalDateTime ahora(){
        return LocalDateTime.now();
    }
}
