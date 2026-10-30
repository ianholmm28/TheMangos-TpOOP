package personal;

import java.io.Serializable;

public abstract class Personal implements Serializable {
    protected String legajo;
    protected String nombre;
    protected String telefono;

    public Personal(String legajo, String nombre, String telefono){
        this.legajo = legajo;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public abstract String getRol();

    public String getLegajo(){ return legajo;}
    public String getNombre(){ return nombre;}
    public String getTelefono(){ return telefono;}
}
