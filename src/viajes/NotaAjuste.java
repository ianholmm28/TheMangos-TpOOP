package viajes;

import java.io.Serializable;
import java.time.LocalDateTime;

public class NotaAjuste implements Serializable {
    private LocalDateTime fecha;
    private String motivo;
    private double importe;

    public NotaAjuste(LocalDateTime fecha, String motivo, double importe){
        this.fecha = fecha;
        this.motivo = motivo;
        this.importe = importe;
    }

    public LocalDateTime getFecha(){return fecha;}
    public String getMotivo(){return motivo;}
    public double getImporte(){return importe;}
}
