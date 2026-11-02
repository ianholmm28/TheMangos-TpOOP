package controladores;

import enums.EstadoVehiculo;
import excepciones.ReglaNegocioException;
import excepciones.VehiculoNoEncontradoException;
import personal.Tecnico;
import reloj.Reloj;
import vehiculos.Electrico;
import vehiculos.OrdenMantenimiento;
import vehiculos.Vehiculo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControladorMantenimiento {
    private Map<String, Vehiculo> vehiculos;
    private Map<String, Tecnico> tecnicos;
    private List<OrdenMantenimiento> ordenes;
    private List<String> notificaciones;
    private Reloj reloj;
    private int proximoNumeroOrden;

    public ControladorMantenimiento(Map<String, Vehiculo> vehiculos, Map<String, Tecnico> tecnicos, List<OrdenMantenimiento> ordenes, Reloj reloj) {
        this.reloj = reloj;
        this.vehiculos = (vehiculos != null) ? vehiculos : new HashMap<>();
        this.tecnicos = (tecnicos != null) ? tecnicos : new HashMap<>();
        this.ordenes = (ordenes != null) ? ordenes : new ArrayList<>();
        this.notificaciones = new ArrayList<>();
        this.proximoNumeroOrden = this.ordenes.size() + 1;
    }

    public void reportarFalla(String codVehiculo, String descripcion) throws ReglaNegocioException {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new ReglaNegocioException("La descripción de la falla no puede estar vacía.");
        }
        Vehiculo vehiculo = buscarVehiculo(codVehiculo);
        if (vehiculo.getEstado() == EstadoVehiculo.FUERA_DE_SERVICIO) {
            throw new ReglaNegocioException("No se pueden reportar fallas sobre un vehículo fuera de servicio.");
        }
        LocalDateTime ahora = this.reloj.ahora();
        vehiculo.registrarFalla(descripcion, ahora);

        int fallasUltimos30Dias = vehiculo.fallasUltimos30Dias(ahora);
        if (fallasUltimos30Dias >= 3){
            vehiculo.enviarAReparacion();
            notificarOperador(vehiculo);
        }
    }

    public void notificarOperador(Vehiculo vehiculo){
        this.notificaciones.add("El vehículo " + vehiculo.getCodigo() + " acumuló 3 fallas en 30 días y pasó a EN_REPARACIÓN.");
    }

    public OrdenMantenimiento generarOrden(String codVehiculo, String legajoTecnico) throws ReglaNegocioException{
        Vehiculo vehiculo = buscarVehiculo(codVehiculo);
        Tecnico tecnico = buscarTecnico(legajoTecnico);

        if (vehiculo.getEstado() == EstadoVehiculo.EN_USO) {
            throw new ReglaNegocioException("No se puede generar una orden de mantenimiento para un vehículo en uso.");
        }
        if (vehiculo.getEstado() == EstadoVehiculo.FUERA_DE_SERVICIO) {
            throw new ReglaNegocioException("No se puede generar una orden de mantenimiento para un vehículo fuera de servicio.");
        }

        for (OrdenMantenimiento orden : this.ordenes){
            if (orden.getVehiculo().getCodigo().equals(codVehiculo) && orden.estaAbierta()){
                throw new ReglaNegocioException("El vehículo ya tiene una orden de mantenimiento abierta.");
            }
        }

        LocalDate fechaApertura = this.reloj.ahora().toLocalDate();
        OrdenMantenimiento nuevaOrden = new OrdenMantenimiento(proximoNumeroOrden++, vehiculo, tecnico, fechaApertura);
        vehiculo.enviarAReparacion();
        this.ordenes.add(nuevaOrden);
        return nuevaOrden;
    }

    public void cerrarOrden(int nroOrden, String tareas, String repuestos) throws ReglaNegocioException{
        cerrarOrden(nroOrden, this.reloj.ahora().toLocalDate(), tareas, repuestos);
    }

    public void cerrarOrden(int nroOrden, LocalDate fecha, String tareas, String repuestos) throws ReglaNegocioException{
        OrdenMantenimiento orden = buscarOrden(nroOrden);
        if (!orden.estaAbierta()){
            throw new ReglaNegocioException("La orden número "+nroOrden+" ya se encuentra cerrada.");
        }

        LocalDate fechaCierre = (fecha != null) ? fecha : this.reloj.ahora().toLocalDate();
        orden.cerrar(fechaCierre, tareas, repuestos);
        Vehiculo v = orden.getVehiculo();
        if (v instanceof Electrico && ((Electrico) v).getNivelBateria() < 15) {
            v.setEstado(EstadoVehiculo.BATERIA_BAJA);
        } else {
            v.setEstado(EstadoVehiculo.DISPONIBLE);
        }
    }

    public void registrarCarga(String codVehiculo, int nuevoNivel) throws ReglaNegocioException{
        Vehiculo vehiculo = buscarVehiculo(codVehiculo);
        if (!(vehiculo instanceof Electrico)) {
            throw new ReglaNegocioException("El vehículo " + codVehiculo + " no es eléctrico.");
        }
        if (nuevoNivel < 0 || nuevoNivel > 100) {
            throw new ReglaNegocioException("El nivel de batería debe estar entre 0 y 100.");
        }
        ((Electrico) vehiculo).actualizarBateria(nuevoNivel);
    }

    public Vehiculo buscarVehiculo(String codigo) throws VehiculoNoEncontradoException{
        Vehiculo vehiculo = this.vehiculos.get(codigo);
        if (vehiculo == null){
            throw new VehiculoNoEncontradoException("No se encontró el vehículo con código: "+codigo);
        }
        return vehiculo;
    }

    public Tecnico buscarTecnico(String legajo) throws ReglaNegocioException{
        Tecnico tecnico = this.tecnicos.get(legajo);
        if (tecnico == null){
            throw new ReglaNegocioException("No se encontró el técnico con el legajo: "+legajo);

        }
        return tecnico;
    }

    public OrdenMantenimiento buscarOrden(int numOrden) throws ReglaNegocioException {
        for (OrdenMantenimiento orden : this.ordenes) {
            if (orden.getNumero() == numOrden) {
                return orden;
            }
        }
        throw new ReglaNegocioException("No existe la orden número: "+numOrden);
    }

    public void agregarVehiculo(Vehiculo vehiculo){
        this.vehiculos.put(vehiculo.getCodigo(), vehiculo);
    }
    public void agregarTecnico(Tecnico tecnico) {
        this.tecnicos.put(tecnico.getLegajo(), tecnico);
    }
    public List<OrdenMantenimiento> getOrdenes() {return new ArrayList<>(this.ordenes);}
    public Map<String, Vehiculo> getVehiculos(){return new HashMap<>(this.vehiculos);}
    public Map<String, Tecnico> getTecnicos(){return new HashMap<>(this.tecnicos);}
    public List<String> getNotificaciones(){return new ArrayList<>(this.notificaciones);}
}
