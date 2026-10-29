package tarifas;

import enums.TipoVehiculo;

import java.time.LocalDate;

public class Tarifa {

    private TipoVehiculo tipo;
    private LocalDate vigenteDesde;
    private double cargoDesbloqueo;
    private double precioMinuto;

    public Tarifa(TipoVehiculo tipo, LocalDate vigenteDesde, double cargoDesbloqueo, double precioMinuto){
        this.tipo = tipo;
        this.vigenteDesde = vigenteDesde;
        this.cargoDesbloqueo = cargoDesbloqueo;
        this.precioMinuto = precioMinuto;
    }

    public double calcularBase(long minutos){
        if (minutos<=120){
            return this.cargoDesbloqueo + (minutos * this.precioMinuto);
        }
        else{
            long minutosNormales = 120;
            long minutosExcedentes = minutos - minutosNormales;
            double precioConRecargo = this.precioMinuto * 1.5;
            return this.cargoDesbloqueo + (minutosNormales * this.precioMinuto) + (minutosExcedentes * precioConRecargo);
        }
    }

    public TipoVehiculo getTipo(){return tipo;}

    public LocalDate getVigenteDesde(){return vigenteDesde;}

    public double getCargoDesbloqueo(){return cargoDesbloqueo;}

    public double getPrecioMinuto(){return precioMinuto;}
}
