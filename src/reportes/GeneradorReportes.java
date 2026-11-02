package reportes;

import estaciones.Estacion;
import vehiculos.Vehiculo;
import viajes.Viaje;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GeneradorReportes {
    private List<Viaje> viajes;
    private Collection<Vehiculo> vehiculos;
    private Collection<Estacion> estaciones;

    public GeneradorReportes(List<Viaje> viajes, List<Vehiculo> vehiculos, List<Estacion> estaciones) {
        this.viajes = (viajes != null) ? viajes : new ArrayList<>();
        this.vehiculos = (vehiculos != null) ? vehiculos : new ArrayList<>();
        this.estaciones = (estaciones != null) ? estaciones : new ArrayList<>();
    }

    public GeneradorReportes(List<Viaje> viajes, Map<String, Vehiculo> vehiculos, Map<String, Estacion> estaciones) {
        this.viajes = (viajes != null) ? viajes : new ArrayList<>();
        this.vehiculos = (vehiculos != null) ? vehiculos.values() : new ArrayList<>();
        this.estaciones = (estaciones != null) ? estaciones.values() : new ArrayList<>();
    }

    public List<Viaje> viajesPorUsuario(LocalDate desde, LocalDate hasta) {
        List<Viaje> resultado = new ArrayList<>();
        for (Viaje viaje : this.viajes) {
            LocalDate fechaViaje = viaje.getInicio().toLocalDate();
            boolean cumpleDesde = (desde == null) || !fechaViaje.isBefore(desde);
            boolean cumpleHasta = (hasta == null) || !fechaViaje.isAfter(hasta);
            if (cumpleDesde && cumpleHasta) {
                resultado.add(viaje);
            }
        }

        Collections.sort(resultado, new Comparator<Viaje>() {
            @Override
            public int compare(Viaje v1, Viaje v2) {
                String doc1 = v1.getUsuario().getDocumento();
                String doc2 = v2.getUsuario().getDocumento();
                int comp = doc1.compareTo(doc2);
                if (comp != 0) {
                    return comp;
                }
                return v1.getInicio().compareTo(v2.getInicio());
            }
        });
        return resultado;
    }

    public List<String> usoPorEstacion(LocalDate desde, LocalDate hasta) {
        List<String> resultado = new ArrayList<>();

        for (Estacion estacion : this.estaciones) {
            int retiros = 0;
            int devoluciones = 0;

            for (Viaje v : this.viajes) {
                LocalDate fechaViaje = v.getInicio().toLocalDate();
                boolean cumpleDesde = (desde == null) || !fechaViaje.isBefore(desde);
                boolean cumpleHasta = (hasta == null) || !fechaViaje.isAfter(hasta);
                if (cumpleDesde && cumpleHasta) {
                    if (v.getOrigen() != null && v.getOrigen().getCodigo().equals(estacion.getCodigo())) {
                        retiros++;
                    }
                    if (v.getDestino() != null && v.getDestino().getCodigo().equals(estacion.getCodigo())) {
                        devoluciones++;
                    }
                }
            }
            int total = retiros + devoluciones;
            String reporteLinea = "Estación "+estacion.getCodigo()+" ("+estacion.getDireccion()+") - "+"Retiros: "+retiros+", Devoluciones: "+devoluciones+", Total: "+total;
            resultado.add(reporteLinea);
        }
        return resultado;
    }

    public List<Vehiculo> vehiculosConMasFallas() {
        List<Vehiculo> ranking = new ArrayList<>(this.vehiculos);
        Collections.sort(ranking, new Comparator<Vehiculo>() {
            @Override
            public int compare(Vehiculo v1, Vehiculo v2) {
                if (v2.getCantidadFallas() > v1.getCantidadFallas()) {
                    return 1;
                } else if (v2.getCantidadFallas() < v1.getCantidadFallas()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        return ranking;
    }
}
