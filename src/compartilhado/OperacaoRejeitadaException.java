package compartilhado;

public class OperacaoRejeitadaException extends Exception {

    private static final long serialVersionUID = 1L;

    public OperacaoRejeitadaException(String mensagem) {
        super(mensagem);
    }
}