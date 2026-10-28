package personal;

public class Operador extends Personal {

    public Operador(String legajo, String nombre, String telefono){
        super(legajo, nombre, telefono);
    }

    @Override
    public String getRol(){
        return("Operador");
    }
}
