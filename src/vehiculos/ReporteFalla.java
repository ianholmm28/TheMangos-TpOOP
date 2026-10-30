package vehiculos;

import java.time.LocalDateTime;

public class ReporteFalla {
    private LocalDateTime fecha;
    private String descripcion;

    public ReporteFalla(LocalDateTime fecha, String descripcion){
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }
}