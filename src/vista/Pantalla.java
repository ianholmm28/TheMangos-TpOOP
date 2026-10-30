package vista;

/**
 * Clase base de los controladores de pantalla. Main le pasa el Contexto al
 * cargar cada FXML, asi todas las pantallas usan los mismos controladores.
 *
 * Ojo: el contexto se asigna despues de initialize(), asi que se usa en los
 * metodos de los eventos (botones, etc.), no dentro de initialize().
 */
public abstract class Pantalla {
    protected Contexto contexto;

    public void setContexto(Contexto contexto) {
        this.contexto = contexto;
    }
}
