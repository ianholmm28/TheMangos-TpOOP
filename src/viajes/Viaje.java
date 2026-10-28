package viajes;

import enums.EstadoViaje;
import vehiculos.Vehiculo;

import java.time.LocalDateTime;

public class Viaje {
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private double distacia;
    private EstadoViaje estado;
    private double importe;
    private Vehiculo vehiculousado;
    private int minutosbonificados;

    public Viaje(Vehiculo vehiculousado,double distancia,double importe,int minutosbonificados){
        this.inicio = LocalDateTime.now();
        this.estado=EstadoViaje.EN_CURSO;
        this.importe=importe;
        this.vehiculousado=vehiculousado;
        this.minutosbonificados=minutosbonificados;

    }
    public Vehiculo getVehiculousado() {return vehiculousado;}

    public void agregarPunto(LocalDateTime hora,double longitud, double latitud){
        PuntoRecorrido punto= new PuntoRecorrido(hora,longitud,latitud);
    }
    public void cerrar(/**/,LocalDateTime fin){
        if(/*no hay lugar en la estacion*/){
            registrarIntentoEstacionLlena(LocalDateTime.now());
        }
        this.fin=LocalDateTime.now();
    }
    public void registrarIntentoEstacionLlena(LocalDateTime hora){

    }
}