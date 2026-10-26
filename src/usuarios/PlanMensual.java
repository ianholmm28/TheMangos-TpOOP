package usuarios;
 
import java.time.YearMonth;

public class PlanMensual {
    
    private YearMonth mes;
    private int viajesUsados;
    private final int VIAJES_INCLUIDOS = 20;

    public YearMonth getMes() {
        return mes;
    }

    public int getVIAJES_INCLUIDOS() {
        return VIAJES_INCLUIDOS;
    }

    public PlanMensual(YearMonth mes) {
        this.mes = mes;
        this.viajesUsados = 0 ;
    }
     
    public boolean tieneViajesDisponibles(){
       return this.viajesUsados < VIAJES_INCLUIDOS;
    }
    
    public void registrarViaje(){
     this.viajesUsados ++;
    }
   
}
