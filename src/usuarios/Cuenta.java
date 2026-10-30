package usuarios;

import enums.TipoMovimiento;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Cuenta implements Serializable {
    private double saldo;
    private List<Movimiento> movimientos;

    public Cuenta(double saldo, List<Movimiento> movimientos) {
        this.saldo = saldo;
        this.movimientos = movimientos;
    }
    
    public void recargar(double monto,String concepto){
       Movimiento movimiento = new Movimiento(LocalDateTime.now(),monto,TipoMovimiento.RECARGA,concepto);
       this.saldo += monto;
       this.movimientos.add(movimiento);  
    }

    public void recargar(double monto, MedioPago medio){
       String detalle = (medio != null) ? ("Recarga con " + medio.getTipo() + " " + medio.getIdentificador()) : "Recarga";
       recargar(monto, detalle);
    }
    
    public void debitar(double monto,String concepto){
       Movimiento movimiento = new Movimiento(LocalDateTime.now(),monto,TipoMovimiento.DEBITO,concepto);
       this.saldo -= monto;
       this.movimientos.add(movimiento);
    }

    public void ajustar(double monto, String concepto){
       Movimiento movimiento = new Movimiento(LocalDateTime.now(), Math.abs(monto), TipoMovimiento.AJUSTE, concepto);
       this.saldo += monto;
       this.movimientos.add(movimiento);
    }
    
    public boolean tieneSaldo(double monto){
        return this.saldo >= monto;
    }

    public double getSaldo() {
        return saldo;
    }
    
    public List<Movimiento> getMovimientos() {
        return movimientos;
    }
}
