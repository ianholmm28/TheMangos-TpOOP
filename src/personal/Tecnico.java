package personal;

public class Tecnico extends Personal {

    public Tecnico(String legajo, String nombre, String telefono){
        super(legajo, nombre, telefono);
    }

    @Override
    public String getRol(){
        return("Técnico");
    }
}
