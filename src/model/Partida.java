package model;

import compartilhado.OperacaoRejeitadaException;
import compartilhado.SituacaoPartida;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Partida {

    public static final int TOTAL_PARES = 8;
    public static final int TOTAL_JOGADORES = 2;
    private final List<Carta> cartas;
    private final List<Jogador> jogadores;

    private SituacaoPartida situacao;
    private int jogadorDaVez;

    public Partida() {
        cartas = new ArrayList<>();
        jogadores = new ArrayList<>();

        situacao = SituacaoPartida.AGUARDANDO;
        jogadorDaVez = -1;

        criarTabuleiro();
    }

    private void criarTabuleiro() {
        List<Integer> valores = new ArrayList<>();

        for (int valor = 0; valor < TOTAL_PARES; valor++) {
            valores.add(valor);
            valores.add(valor);
        }

        Collections.shuffle(valores);

        for (int posicao = 0; posicao < valores.size(); posicao++) {
            Carta carta = new Carta(
                    posicao,
                    valores.get(posicao)
            );

            cartas.add(carta);
        }
    }

    public synchronized int adicionarJogador()
            throws OperacaoRejeitadaException {

        if (jogadores.size() >= TOTAL_JOGADORES) {
            throw new OperacaoRejeitadaException(
                    "Partida cheia. Já existem dois jogadores conectados."
            );
        }

        int id = jogadores.size();

        Jogador jogador = new Jogador(id);
        jogadores.add(jogador);

        if (jogadores.size() == TOTAL_JOGADORES) {
            iniciar();
        }

        return id;
    }

    private void iniciar() {
        jogadorDaVez = ThreadLocalRandom.current()
                .nextInt(TOTAL_JOGADORES);

        situacao = SituacaoPartida.EM_ANDAMENTO;
    }

    public synchronized SituacaoPartida getSituacao() {
        return situacao;
    }

    public synchronized int getJogadorDaVez() {
        return jogadorDaVez;
    }

    public synchronized int getQuantidadeJogadores() {
        return jogadores.size();
    }
}