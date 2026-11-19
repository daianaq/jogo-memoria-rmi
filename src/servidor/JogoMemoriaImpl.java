package servidor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import compartilhado.EstadoPartida;
import compartilhado.JogoRemoto;
import compartilhado.OperacaoRejeitadaException;
import compartilhado.ResultadoJogo;
import model.Partida;

public class JogoMemoriaImpl extends UnicastRemoteObject
        implements JogoRemoto {

    private static final long serialVersionUID = 1L;

    private final Partida partida;

    public JogoMemoriaImpl() throws RemoteException {
        super();
        this.partida = new Partida();
    }

    @Override
    public synchronized int entrarPartida()
            throws RemoteException, OperacaoRejeitadaException {

        return partida.adicionarJogador();
    }

    @Override
    public synchronized ResultadoJogo virarCarta(
            int idJogador, int posicao)
            throws RemoteException, OperacaoRejeitadaException {

        return partida.virarCarta(idJogador, posicao);
    }

    @Override
    public synchronized EstadoPartida obterEstado()
            throws RemoteException {

        return new EstadoPartida(
                partida.getCartas(),
                partida.getPontuacoes(),
                partida.getSituacao(),
                partida.getJogadorDaVez(),
                partida.getTempoRestanteSegundos()
        );
    }
}
