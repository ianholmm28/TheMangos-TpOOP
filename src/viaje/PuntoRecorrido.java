package viaje;

import java.time.LocalDateTime;

public class PuntoRecorrido {
    private double latitud;
    private double longitud;
    private LocalDateTime hora;
    public PuntoRecorrido(LocalDateTime hora, double longitud, double latitud){
        this.hora= hora;
        this.latitud= latitud;
        this.longitud= longitud;
    }
}
