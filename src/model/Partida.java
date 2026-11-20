package model;

import compartilhado.OperacaoRejeitadaException;
import compartilhado.ResultadoJogo;
import compartilhado.SituacaoPartida;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Partida {

    public static final int TOTAL_PARES = 8;
    public static final int TOTAL_JOGADORES = 2;

    private static final int TEMPO_LIMITE_TURNO_SEGUNDOS = 20;
    private static final long TEMPO_EXIBICAO_PAR_INCORRETO_MILLIS = 2000;

    private final List<Carta> cartas;
    private final List<Jogador> jogadores;

    private SituacaoPartida situacao;
    private int jogadorDaVez;

    private int primeiraCartaDoTurno;
    private int segundaCartaDoParIncorreto;

    private long inicioTurnoNanos;
    private long inicioExibicaoParIncorretoNanos;

    public Partida() {

        cartas = new ArrayList<>();
        jogadores = new ArrayList<>();

        situacao = SituacaoPartida.AGUARDANDO;
        jogadorDaVez = -1;

        primeiraCartaDoTurno = -1;
        segundaCartaDoParIncorreto = -1;

        inicioTurnoNanos = 0;
        inicioExibicaoParIncorretoNanos = 0;

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

    public synchronized int adicionarJogador(String nome)
            throws OperacaoRejeitadaException {

        if (jogadores.size() >= TOTAL_JOGADORES) {
            throw new OperacaoRejeitadaException(
                    "Partida cheia. Já existem dois jogadores conectados."
            );
        }

        if (nome == null || nome.trim().isEmpty()) {
            throw new OperacaoRejeitadaException(
                    "O nome do jogador não pode estar vazio."
            );
        }

        int id = jogadores.size();

        Jogador jogador = new Jogador(id, nome.trim());
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

        primeiraCartaDoTurno = -1;
        segundaCartaDoParIncorreto = -1;

        inicioTurnoNanos = System.nanoTime();
    }

    public synchronized ResultadoJogo virarCarta(
            int idJogador, int posicao)
            throws OperacaoRejeitadaException {

        if (idJogador < 0 || idJogador >= jogadores.size()) {
            throw new OperacaoRejeitadaException(
                    "Jogador inválido."
            );
        }

        atualizarTempo();

        if (situacao != SituacaoPartida.EM_ANDAMENTO) {
            throw new OperacaoRejeitadaException(
                    "A partida não está em andamento."
            );
        }

        if (inicioExibicaoParIncorretoNanos != 0) {
            throw new OperacaoRejeitadaException(
                    "Aguarde a ocultação das cartas."
            );
        }

        if (idJogador != jogadorDaVez) {
            throw new OperacaoRejeitadaException(
                    "Não é a sua vez de jogar."
            );
        }

        if (posicao < 0 || posicao >= cartas.size()) {
            throw new OperacaoRejeitadaException(
                    "Posição de carta inválida."
            );
        }

        Carta cartaSelecionada = cartas.get(posicao);

        if (!cartaSelecionada.estaDisponivel()) {
            throw new OperacaoRejeitadaException(
                    "Esta carta não está disponível."
            );
        }

        cartaSelecionada.revelar();

        if (primeiraCartaDoTurno == -1) {

            primeiraCartaDoTurno = posicao;

            return new ResultadoJogo(
                    ResultadoJogo.TipoResultado.CARTA_REVELADA
            );
        }

        Carta primeiraCarta = cartas.get(primeiraCartaDoTurno);

        if (primeiraCarta.getValor() == cartaSelecionada.getValor()) {

            primeiraCarta.marcarComoPareada();
            cartaSelecionada.marcarComoPareada();

            jogadores.get(idJogador).adicionarPonto();

            primeiraCartaDoTurno = -1;

            boolean todasPareadas = true;

            for (Carta carta : cartas) {
                if (!carta.isPareada()) {
                    todasPareadas = false;
                    break;
                }
            }

            if (todasPareadas) {

                situacao = SituacaoPartida.ENCERRADA;

                return new ResultadoJogo(
                        ResultadoJogo.TipoResultado.PARTIDA_ENCERRADA
                );
            }

            inicioTurnoNanos = System.nanoTime();

            return new ResultadoJogo(
                    ResultadoJogo.TipoResultado.PAR_ENCONTRADO
            );
        }

        segundaCartaDoParIncorreto = posicao;

        inicioExibicaoParIncorretoNanos = System.nanoTime();

        return new ResultadoJogo(
                ResultadoJogo.TipoResultado.PAR_INCORRETO
        );
    }

    private void atualizarTempo() {

        if (situacao != SituacaoPartida.EM_ANDAMENTO) {
            return;
        }

        long agora = System.nanoTime();

        if (inicioExibicaoParIncorretoNanos != 0) {

            long tempoDecorrido = agora
                    - inicioExibicaoParIncorretoNanos;

            long tempoLimite = TEMPO_EXIBICAO_PAR_INCORRETO_MILLIS
                    * 1_000_000L;

            if (tempoDecorrido >= tempoLimite) {

                cartas.get(primeiraCartaDoTurno).ocultar();
                cartas.get(segundaCartaDoParIncorreto).ocultar();

                primeiraCartaDoTurno = -1;
                segundaCartaDoParIncorreto = -1;

                inicioExibicaoParIncorretoNanos = 0;
                
                jogadorDaVez = (jogadorDaVez + 1) % TOTAL_JOGADORES;

               
                inicioTurnoNanos = System.nanoTime();
            }

            return;
        }

        long tempoDecorrido = agora - inicioTurnoNanos;

        long tempoLimite = TEMPO_LIMITE_TURNO_SEGUNDOS
                * 1_000_000_000L;

        if (tempoDecorrido >= tempoLimite) {

            if (primeiraCartaDoTurno != -1) {
                cartas.get(primeiraCartaDoTurno).ocultar();
            }

            primeiraCartaDoTurno = -1;

            jogadorDaVez = (jogadorDaVez + 1) % TOTAL_JOGADORES;

            inicioTurnoNanos = System.nanoTime();
        }
    }
    
    public synchronized String[] getNomesJogadores() {

        String[] nomes = new String[jogadores.size()];

        for (int i = 0; i < jogadores.size(); i++) {
            nomes[i] = jogadores.get(i).getNome();
        }

        return nomes;
    }

    public synchronized SituacaoPartida getSituacao() {

        atualizarTempo();

        return situacao;
    }

    public synchronized int getJogadorDaVez() {

        atualizarTempo();

        return jogadorDaVez;
    }

    public synchronized int getQuantidadeJogadores() {

        return jogadores.size();
    }

    public synchronized List<Carta> getCartas() {

        atualizarTempo();

        return Collections.unmodifiableList(
                new ArrayList<>(cartas)
        );
    }

    public synchronized int[] getPontuacoes() {

        atualizarTempo();

        int[] pontuacoes = new int[jogadores.size()];

        for (int i = 0; i < jogadores.size(); i++) {
            pontuacoes[i] = jogadores.get(i).getPontuacao();
        }

        return pontuacoes;
    }

    public synchronized long getTempoRestanteSegundos() {

        atualizarTempo();

        if (situacao != SituacaoPartida.EM_ANDAMENTO) {
            return 0;
        }

        if (inicioExibicaoParIncorretoNanos != 0) {
            return TEMPO_LIMITE_TURNO_SEGUNDOS;
        }

        long tempoLimite = TEMPO_LIMITE_TURNO_SEGUNDOS
                * 1_000_000_000L;

        long tempoDecorrido = System.nanoTime() - inicioTurnoNanos;

        long tempoRestante = tempoLimite - tempoDecorrido;

        if (tempoRestante <= 0) {
            return 0;
        }

        return (tempoRestante + 999_999_999L)
                / 1_000_000_000L;
    }

    
    public synchronized int getVencedor() {

        atualizarTempo();

        if (situacao != SituacaoPartida.ENCERRADA) {
            return -1;
        }

        int pontuacaoJogador0 = jogadores.get(0).getPontuacao();
        int pontuacaoJogador1 = jogadores.get(1).getPontuacao();

        if (pontuacaoJogador0 > pontuacaoJogador1) {
            return 0;
        }

        if (pontuacaoJogador1 > pontuacaoJogador0) {
            return 1;
        }

        return -1;
    }

  
    public synchronized boolean isEmpate() {

        atualizarTempo();

        if (situacao != SituacaoPartida.ENCERRADA) {
            return false;
        }

        return jogadores.get(0).getPontuacao()
                == jogadores.get(1).getPontuacao();
    }
}
