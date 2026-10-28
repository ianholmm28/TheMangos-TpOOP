package reloj;

import java.time.Duration;
import java.time.LocalDateTime;

public class RelojFijo implements Reloj {

    private LocalDateTime instante;

    //Constructor que toma el momento de creacion
    public RelojFijo(){
        this.instante = LocalDateTime.now();
    }

    //Constructor que te deja agarrar otro momento
    public RelojFijo(LocalDateTime instanteInicial){
        this.instante = instanteInicial;
    }

    @Override
    public LocalDateTime ahora(){
        return this.instante;
    }

    public void avanzar(Duration d){
        if (d != null){
            this.instante = this.instante.plus(d);
        }
    }
}
