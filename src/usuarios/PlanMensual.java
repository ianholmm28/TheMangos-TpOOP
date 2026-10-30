package usuarios;
 
import java.time.YearMonth;

public class PlanMensual {
    
    private YearMonth mes;
    private int viajesUsados;
    public static final double COSTO_PLAN = 15000;
    public static final int VIAJES_INCLUIDOS = 20;
    private boolean renovable;

    public YearMonth getMes() {
        return mes;
    }

    public int getViajesIncluidos() {
        return VIAJES_INCLUIDOS;
    }

    public int getViajesUsados() {
        return viajesUsados;
    }

    public double getCostoPlan() {
        return COSTO_PLAN;
    }
    
    public PlanMensual(YearMonth mes) {
        this.mes = mes;
        this.viajesUsados = 0;
        this.renovable = true;
    }
     
    public boolean tieneViajesDisponibles(){
       return this.viajesUsados < VIAJES_INCLUIDOS;
    }
    
    public void registrarViaje(){
        this.viajesUsados++;
    }

    public void cancelarRenovacion() {
        this.renovable = false;
    }

    public boolean isRenovable() {
        return this.renovable;
    }
}
