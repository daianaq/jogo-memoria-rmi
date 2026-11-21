package compartilhado;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface JogoRemoto extends Remote {

    int entrarPartida(String nome)
            throws RemoteException, OperacaoRejeitadaException;

    ResultadoJogo virarCarta(int idJogador, int posicao)
            throws RemoteException, OperacaoRejeitadaException;

    EstadoPartida obterEstado()
            throws RemoteException;
}