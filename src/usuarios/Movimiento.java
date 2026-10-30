package usuarios;

import java.time.LocalDateTime;
import enums.TipoMovimiento;

public class Movimiento implements Comparable<Movimiento>{
    private LocalDateTime fecha;
    private double importe;
    private TipoMovimiento tipo;
    private String concepto;
    
    //Agregue concepto, cuando hay una recarga cocepto actual como el MedioPago.
  
        public Movimiento(LocalDateTime fecha, double importe, TipoMovimiento tipo, String concepto) {
        this.fecha = fecha;
        this.importe = importe;
        this.tipo = tipo;
        this.concepto = concepto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public double getImporte() {
        return importe;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public String getConcepto() {
        return concepto;
    }

    @Override
    public int compareTo(Movimiento otro){
        return otro.fecha.compareTo(this.fecha);
    }
}
