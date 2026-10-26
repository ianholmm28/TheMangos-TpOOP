package usuarios;

import enums.TipoMedioPago;

public class MedioPago {
    private TipoMedioPago tipo;
    private String identificador;

    public MedioPago(TipoMedioPago tipo, String identificador) {
        this.tipo = tipo;
        this.identificador = identificador;
    }

    public TipoMedioPago getTipo() {
        return tipo;
    }

    public String getIdentificador() {
        return identificador;
    }
}
