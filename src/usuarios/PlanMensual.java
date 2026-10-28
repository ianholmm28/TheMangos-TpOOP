package usuarios;
 
import java.time.YearMonth;

public class PlanMensual {
    
    private YearMonth mes;
    private int viajesUsados;
    static final double costoPlan  = 15000;
    static final int viajesIncluidos = 20;

    public YearMonth getMes() {
        return mes;
    }

    public int getViajesIncluidos() {
        return viajesIncluidos;
    }

    public double getCostoPlan() {
        return costoPlan;
    }

    
    public PlanMensual(YearMonth mes) {
        this.mes = mes;
        this.viajesUsados = 0 ;
    }
     
    public boolean tieneViajesDisponibles(){
       return this.viajesUsados < viajesIncluidos;
    }
    
    public void registrarViaje(){
     this.viajesUsados ++;
    }
   
}
