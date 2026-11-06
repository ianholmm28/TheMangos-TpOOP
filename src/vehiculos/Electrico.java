package vehiculos;

public interface Electrico {
    int getNivelBateria();
    void actualizarBateria(int nivel);
    void consumirPorDistancia(double distanciaKm);
}