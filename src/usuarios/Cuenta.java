package usuarios;

import enums.TipoMovimiento;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Cuenta implements Serializable {
    private double saldo;
    private List<Movimiento> movimientos;

    public Cuenta(double saldo, List<Movimiento> movimientos) {
        this.saldo = saldo;
        this.movimientos = movimientos;
    }
    
    public void recargar(double monto, String concepto){
       recargar(monto, concepto, LocalDateTime.now());
    }

    public void recargar(double monto, String concepto, LocalDateTime fecha){
       Movimiento movimiento = new Movimiento(fecha != null ? fecha : LocalDateTime.now(), monto, TipoMovimiento.RECARGA, concepto);
       this.saldo += monto;
       this.movimientos.add(movimiento);  
    }

    public void recargar(double monto, MedioPago medio){
       String detalle = (medio != null) ? ("Recarga con " + medio.getTipo() + " " + medio.getIdentificador()) : "Recarga";
       recargar(monto, detalle);
    }
    
    public void debitar(double monto, String concepto){
       debitar(monto, concepto, LocalDateTime.now());
    }

    public void debitar(double monto, String concepto, LocalDateTime fecha){
       Movimiento movimiento = new Movimiento(fecha != null ? fecha : LocalDateTime.now(), monto, TipoMovimiento.DEBITO, concepto);
       this.saldo -= monto;
       this.movimientos.add(movimiento);
    }

    public void ajustar(double monto, String concepto){
       ajustar(monto, concepto, LocalDateTime.now());
    }

    public void ajustar(double monto, String concepto, LocalDateTime fecha){
       Movimiento movimiento = new Movimiento(fecha != null ? fecha : LocalDateTime.now(), monto, TipoMovimiento.AJUSTE, concepto);
       this.saldo += monto;
       this.movimientos.add(movimiento);
    }
    
    public boolean tieneSaldo(double monto){
        return this.saldo >= monto;
    }

    public double getSaldo() {
        return saldo;
    }
    
    public List<Movimiento> getMovimientos(){return new ArrayList<>(this.movimientos);}
}
