package vista;

import controladores.ControladorAdministracion;
import controladores.ControladorEstaciones;
import controladores.ControladorMantenimiento;
import controladores.ControladorUsuarios;
import controladores.ControladorViajes;
import controladores.Sistema;
import reportes.GeneradorReportes;

public abstract class Pantalla {
    private static final Sistema sistema = new Sistema();

    public static Sistema getSistema() {return sistema;}
    public ControladorUsuarios getControladorUsuarios() {return sistema.getControladorUsuarios();}
    public ControladorViajes getControladorViajes() {return sistema.getControladorViajes();}
    public ControladorEstaciones getControladorEstaciones() {return sistema.getControladorEstaciones();}
    public ControladorMantenimiento getControladorMantenimiento() {return sistema.getControladorMantenimiento();}
    public ControladorAdministracion getControladorAdministracion() {return sistema.getControladorAdministracion();}
    public GeneradorReportes getGeneradorReportes() {return sistema.getGeneradorReportes();}
}


