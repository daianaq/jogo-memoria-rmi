package compartilhado;

import java.io.Serializable;

public class ResultadoJogo implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum TipoResultado {
        CARTA_REVELADA,
        PAR_ENCONTRADO,
        PAR_INCORRETO,
        PARTIDA_ENCERRADA
    }

    private final TipoResultado tipoResultado;

    public ResultadoJogo(TipoResultado tipoResultado) {
        this.tipoResultado = tipoResultado;
    }

    public TipoResultado getTipoResultado() {
        return tipoResultado;
    }
}