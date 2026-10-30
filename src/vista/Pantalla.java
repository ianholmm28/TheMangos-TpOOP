package vista;

import controladores.ControladorAdministracion;
import controladores.ControladorEstaciones;
import controladores.ControladorMantenimiento;
import controladores.ControladorUsuarios;
import controladores.ControladorViajes;
import reloj.Reloj;
import reloj.RelojSistema;
import tarifas.CalculadorTarifa;


public abstract class Pantalla {
    private static final Reloj reloj = new RelojSistema();
    private static final CalculadorTarifa calculador = new CalculadorTarifa();
    private static final ControladorUsuarios controladorUsuarios = new ControladorUsuarios();
    private static final ControladorViajes controladorViajes = new ControladorViajes(calculador, reloj);
    private static final ControladorEstaciones controladorEstaciones = new ControladorEstaciones(reloj);
    private static final ControladorMantenimiento controladorMantenimiento = new ControladorMantenimiento(reloj);
    private static final ControladorAdministracion controladorAdministracion = new ControladorAdministracion(calculador);

    public ControladorUsuarios getControladorUsuarios() {return controladorUsuarios;}
    public ControladorViajes getControladorViajes() {return controladorViajes;}
    public ControladorEstaciones getControladorEstaciones() {return controladorEstaciones;}
    public ControladorMantenimiento getControladorMantenimiento() {return controladorMantenimiento;}
    public ControladorAdministracion getControladorAdministracion() {return controladorAdministracion;}
}

