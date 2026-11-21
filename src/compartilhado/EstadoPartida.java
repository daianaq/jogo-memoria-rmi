package compartilhado;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Carta;

public final class EstadoPartida implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Carta> cartas;
    private final int[] pontuacoes;
    private final String[] nomesJogadores;

    private final SituacaoPartida situacao;
    private final int jogadorDaVez;
    private final long tempoRestanteSegundos;

    private final boolean bloqueada;
    private final int vencedor;
    private final boolean empate;

    public EstadoPartida(
            List<Carta> cartas,
            int[] pontuacoes,
            String[] nomesJogadores,
            SituacaoPartida situacao,
            int jogadorDaVez,
            long tempoRestanteSegundos,
            boolean bloqueada,
            int vencedor,
            boolean empate
    ) {
        this.cartas = copiarCartas(cartas);
        this.pontuacoes = pontuacoes.clone();
        this.nomesJogadores = nomesJogadores.clone();

        this.situacao = situacao;
        this.jogadorDaVez = jogadorDaVez;
        this.tempoRestanteSegundos = tempoRestanteSegundos;

        this.bloqueada = bloqueada;
        this.vencedor = vencedor;
        this.empate = empate;
    }

    private static List<Carta> copiarCartas(List<Carta> originais) {
        List<Carta> copia = new ArrayList<>();

        for (Carta original : originais) {
            boolean visivel = original.isRevelada()
                    || original.isPareada();

            // O valor real de uma carta oculta não sai do servidor.
            int valor = visivel ? original.getValor() : -1;

            Carta carta = new Carta(original.getPosicao(), valor);

            if (original.isPareada()) {
                carta.marcarComoPareada();
            } else if (original.isRevelada()) {
                carta.revelar();
            }

            copia.add(carta);
        }

        return Collections.unmodifiableList(copia);
    }

    public List<Carta> getCartas() {
        return copiarCartas(cartas);
    }

    public int getPontuacaoJogador(int idJogador) {
        if (idJogador < 0 || idJogador >= pontuacoes.length) {
            throw new IllegalArgumentException("Jogador inválido.");
        }

        return pontuacoes[idJogador];
    }

    public int[] getPontuacoes() {
        return pontuacoes.clone();
    }

    public String[] getNomesJogadores() {
        return nomesJogadores.clone();
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

    public boolean isBloqueada() {
        return bloqueada;
    }

    public int getVencedor() {
        return vencedor;
    }

    public boolean isEmpate() {
        return empate;
    }
}