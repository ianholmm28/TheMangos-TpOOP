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
    private Reloj reloj;
    private int proximoNumeroOrden;

    public ControladorMantenimiento(Reloj reloj){
        this.reloj = reloj;
        this.vehiculos = new HashMap<>();
        this.tecnicos = new HashMap<>();
        this.ordenes = new ArrayList<>();
        this.proximoNumeroOrden = 1;
    }

    public void reportarFalla(String codVehiculo, String descripcion) throws ReglaNegocioException {
        Vehiculo vehiculo = buscarVehiculo(codVehiculo);
        LocalDateTime ahora = this.reloj.ahora();

        int fallasUltimos30Dias = vehiculo.fallasUltimos30Dias(ahora);
        if (fallasUltimos30Dias >=3){
            vehiculo.enviarAReparacion();
            notificarOperador(vehiculo);
        }
    }

    public void notificarOperador(Vehiculo vehiculo){
        System.out.println("El vehículo "+vehiculo.getCodigo()+" acumuló 3 fallas en 30 días y pasó a EN_REPARACIÓN.");
    }

    public OrdenMantenimiento generarOrden(String codVehiculo, String legajoTecnico) throws ReglaNegocioException{
        Vehiculo vehiculo = buscarVehiculo(codVehiculo);
        Tecnico tecnico = buscarTecnico(legajoTecnico);

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
        OrdenMantenimiento orden = buscarOrden(nroOrden);
        if (!orden.estaAbierta()){
            throw new ReglaNegocioException("La orden número "+nroOrden+" ya se encuentra cerrada.");
        }

        LocalDate fechaCierre = this.reloj.ahora().toLocalDate();
        orden.cerrar(fechaCierre, tareas, repuestos);
        orden.getVehiculo().setEstado(EstadoVehiculo.DISPONIBLE);
    }

    public void registrarCarga(Electrico vehiculo, int nuevoNivel) throws ReglaNegocioException{
        vehiculo.actualizarBateria(nuevoNivel);
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
    public List<OrdenMantenimiento> getOrdenes() {return ordenes;}
}
