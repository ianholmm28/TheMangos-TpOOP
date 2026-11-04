package vista;

import enums.TipoVehiculo;
import estaciones.Estacion;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import javafx.util.StringConverter;
import vehiculos.Electrico;
import vehiculos.Vehiculo;
import viajes.Viaje;

// Textos que se muestran en varias pantallas, escritos en un solo lugar
public class Formato {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String plata(double monto) {
        return String.format("$ %,.2f", monto);
    }

    public static String nombreTipo(TipoVehiculo tipo) {
        switch (tipo) {
            case BICICLETA_COMUN:
                return "Bicicleta";
            case BICICLETA_ELECTRICA:
                return "Bicicleta eléctrica";
            case MONOPATIN_ELECTRICO:
                return "Monopatín eléctrico";
            default:
                return tipo.toString();
        }
    }

    // "E01 - Av. Corrientes 1234"
    public static String estacion(Estacion estacion) {
        return estacion == null ? "" : estacion.getCodigo() + " - " + estacion.getDireccion();
    }

    // Un renglon por vehiculo: "M001  -  Monopatín eléctrico  -  DISPONIBLE  -  Batería 80%"
    public static String vehiculo(Vehiculo vehiculo) {
        String texto = vehiculo.getCodigo() + "  -  " + nombreTipo(vehiculo.getTipo()) + "  -  " + vehiculo.getEstado();
        if (vehiculo instanceof Electrico) {
            texto += "  -  Batería " + ((Electrico) vehiculo).getNivelBateria() + "%";
        }
        return texto;
    }

    // Un renglon por viaje: "40123456 Ian  -  04/11/2026 16:45  -  M001  -  E01 → E02  -  $ 580,00"
    public static String viaje(Viaje viaje) {
        String destino = viaje.getDestino() == null ? "sin destino" : viaje.getDestino().getCodigo();
        return viaje.getUsuario().getDocumento() + " " + viaje.getUsuario().getNombre()
                + "  -  " + viaje.getInicio().format(FORMATO_FECHA)
                + "  -  " + viaje.getVehiculo().getCodigo()
                + "  -  " + viaje.getOrigen().getCodigo() + " → " + destino
                + "  -  " + plata(viaje.getImporte());
    }

    // Lugares libres en el destino para el tipo del vehiculo elegido
    public static String lugaresLibres(Estacion destino, Vehiculo vehiculo) {
        if (destino == null || vehiculo == null) {
            return "";
        }
        String tipo = nombreTipo(vehiculo.getTipo());
        int libres = destino.lugaresLibres(vehiculo.getTipo());
        if (libres <= 0) {
            return "No hay lugares libres para " + tipo + " en esta estación";
        }
        return "Lugares libres para " + tipo + ": " + libres;
    }

    // Dice como mostrar cada objeto en un desplegable (ComboBox), por ejemplo:
    // cmbTecnico.setConverter(Formato.conversor(tecnico -> tecnico.getLegajo() + " - " + tecnico.getNombre()));
    public static <T> StringConverter<T> conversor(Function<T, String> texto) {
        return new StringConverter<T>() {
            @Override
            public String toString(T objeto) {
                return objeto == null ? "" : texto.apply(objeto);
            }

            @Override
            public T fromString(String cadena) {
                return null;
            }
        };
    }
}
