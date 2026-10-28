package personal;

public class Administrador extends Personal {

    public Administrador(String legajo, String nombre, String telefono){
        super(legajo, nombre, telefono);
    }

    @Override
    public String getRol(){
        return("Administrador");
    }
}
