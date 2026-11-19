package compartilhado;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Carta;

public class EstadoPartida implements Serializable {

    private static final long serialVersionUID = 1L;
    private final List<Carta> cartas;
    private final int[] pontuacoes;
    private final SituacaoPartida situacao;
    private final int jogadorDaVez;
    private final long tempoRestanteSegundos;
    
    
    public EstadoPartida(
            List<Carta> cartas,
            int[] pontuacoes,
            SituacaoPartida situacao,
            int jogadorDaVez,
            long tempoRestanteSegundos) {

        this.cartas = copiarCartas(cartas);
        this.pontuacoes = pontuacoes.clone();
        this.situacao = situacao;
        this.jogadorDaVez = jogadorDaVez;
        this.tempoRestanteSegundos = tempoRestanteSegundos;
    }

    
    private List<Carta> copiarCartas(List<Carta> cartasOriginais) {

        List<Carta> copia = new ArrayList<>();

        for (Carta cartaOriginal : cartasOriginais) {

            Carta carta = new Carta(
                    cartaOriginal.getPosicao(),
                    cartaOriginal.getValor()
            );

       
            if (cartaOriginal.isPareada()) {

                carta.marcarComoPareada();

            } else if (cartaOriginal.isRevelada()) {

                carta.revelar();

            }

            copia.add(carta);
        }

 
        return Collections.unmodifiableList(copia);
    }


    
    public List<Carta> getCartas() {
        return cartas;
    }


    public int getPontuacaoJogador(int idJogador) {
        return pontuacoes[idJogador];
    }


    public int[] getPontuacoes() {
        return pontuacoes.clone();
    }


    public SituacaoPartida getSituacao() {
        return situacao;
    }


    public int getJogadorDaVez() {
        return jogadorDaVez;
    }


    public long getTempoRestanteSegundos() {
        return tempoRestanteSegundos;
    }

}
