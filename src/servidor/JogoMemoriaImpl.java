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
        partida = new Partida();
    }

    @Override
    public int entrarPartida(String nome)
            throws RemoteException, OperacaoRejeitadaException {

        return partida.adicionarJogador(nome);
    }

    @Override
    public ResultadoJogo virarCarta(int idJogador, int posicao)
            throws RemoteException, OperacaoRejeitadaException {

        return partida.virarCarta(idJogador, posicao);
    }

    @Override
    public EstadoPartida obterEstado() throws RemoteException {
        return partida.obterEstado();
    }
}