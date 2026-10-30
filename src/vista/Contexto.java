package vista;

import controladores.ControladorAdministracion;
import controladores.ControladorEstaciones;
import controladores.ControladorMantenimiento;
import controladores.ControladorUsuarios;
import controladores.ControladorViajes;
import reloj.Reloj;
import reloj.RelojSistema;
import tarifas.CalculadorTarifa;

/**
 * Crea una sola vez los controladores de negocio y los comparte entre todas
 * las pantallas, para que todas trabajen sobre los mismos datos.
 */
public class Contexto {
    private final Reloj reloj;
    private final CalculadorTarifa calculador;
    private final ControladorUsuarios controladorUsuarios;
    private final ControladorViajes controladorViajes;
    private final ControladorEstaciones controladorEstaciones;
    private final ControladorMantenimiento controladorMantenimiento;
    private final ControladorAdministracion controladorAdministracion;

    public Contexto() {
        this.reloj = new RelojSistema();
        this.calculador = new CalculadorTarifa();
        this.controladorUsuarios = new ControladorUsuarios();
        this.controladorViajes = new ControladorViajes(calculador, reloj);
        this.controladorEstaciones = new ControladorEstaciones(reloj);
        this.controladorMantenimiento = new ControladorMantenimiento(reloj);
        this.controladorAdministracion = new ControladorAdministracion(calculador);
    }

    public ControladorUsuarios getControladorUsuarios() {
        return controladorUsuarios;
    }

    public ControladorViajes getControladorViajes() {
        return controladorViajes;
    }

    public ControladorEstaciones getControladorEstaciones() {
        return controladorEstaciones;
    }

    public ControladorMantenimiento getControladorMantenimiento() {
        return controladorMantenimiento;
    }

    public ControladorAdministracion getControladorAdministracion() {
        return controladorAdministracion;
    }
}
