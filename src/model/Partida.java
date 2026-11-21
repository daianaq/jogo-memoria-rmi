package model;

import compartilhado.EstadoPartida;
import compartilhado.OperacaoRejeitadaException;
import compartilhado.ResultadoJogo;
import compartilhado.SituacaoPartida;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Partida {

    public static final int TOTAL_PARES = 8;
    public static final int TOTAL_JOGADORES = 2;

    private static final long TEMPO_TURNO_NANOS =
            TimeUnit.SECONDS.toNanos(20);

    private static final long TEMPO_ERRO_NANOS =
            TimeUnit.SECONDS.toNanos(2);

    private final List<Carta> cartas;
    private final List<Jogador> jogadores;

    private SituacaoPartida situacao;
    private int jogadorDaVez;

    private int primeiraCartaDoTurno;
    private int segundaCartaDoParIncorreto;

    private long inicioTurnoNanos;
    private long inicioExibicaoParIncorretoNanos;

    private boolean bloqueada;
    private int vencedor;
    private boolean empate;

    public Partida() {
        cartas = new ArrayList<>();
        jogadores = new ArrayList<>();

        situacao = SituacaoPartida.AGUARDANDO;
        jogadorDaVez = -1;

        primeiraCartaDoTurno = -1;
        segundaCartaDoParIncorreto = -1;

        bloqueada = false;
        vencedor = -1;
        empate = false;

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
            cartas.add(new Carta(posicao, valores.get(posicao)));
        }
    }

    public synchronized int adicionarJogador(String nome)
            throws OperacaoRejeitadaException {

        if (jogadores.size() >= TOTAL_JOGADORES) {
            throw new OperacaoRejeitadaException(
                    "Partida cheia. Já existem dois jogadores conectados."
            );
        }

        if (nome == null || nome.isBlank()) {
            throw new OperacaoRejeitadaException(
                    "O nome do jogador não pode estar vazio."
            );
        }

        int id = jogadores.size();
        jogadores.add(new Jogador(id, nome.strip()));

        if (jogadores.size() == TOTAL_JOGADORES) {
            iniciar();
        }

        return id;
    }

    private void iniciar() {
        jogadorDaVez = ThreadLocalRandom.current()
                .nextInt(TOTAL_JOGADORES);

        situacao = SituacaoPartida.EM_ANDAMENTO;

        limparSelecao();
        bloqueada = false;

        inicioTurnoNanos = System.nanoTime();
    }

    public synchronized ResultadoJogo virarCarta(
            int idJogador,
            int posicao
    ) throws OperacaoRejeitadaException {

        if (idJogador < 0 || idJogador >= jogadores.size()) {
            throw new OperacaoRejeitadaException(
                    "Jogador inválido."
            );
        }

        long agora = System.nanoTime();
        atualizarTempo(agora);

        if (situacao != SituacaoPartida.EM_ANDAMENTO) {
            throw new OperacaoRejeitadaException(
                    "A partida não está em andamento."
            );
        }

        if (bloqueada) {
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

        Carta selecionada = cartas.get(posicao);

        if (!selecionada.estaDisponivel()) {
            throw new OperacaoRejeitadaException(
                    "Esta carta não está disponível."
            );
        }

        selecionada.revelar();

        if (primeiraCartaDoTurno == -1) {
            primeiraCartaDoTurno = posicao;

            return new ResultadoJogo(
                    ResultadoJogo.TipoResultado.CARTA_REVELADA
            );
        }

        Carta primeira = cartas.get(primeiraCartaDoTurno);

        if (primeira.getValor() == selecionada.getValor()) {
            primeira.marcarComoPareada();
            selecionada.marcarComoPareada();

            jogadores.get(idJogador).adicionarPonto();
            limparSelecao();

            if (todosOsParesEncontrados()) {
                encerrar();

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
        bloqueada = true;
        inicioExibicaoParIncorretoNanos = System.nanoTime();

        return new ResultadoJogo(
                ResultadoJogo.TipoResultado.PAR_INCORRETO
        );
    }

    private void atualizarTempo(long agora) {
        if (situacao != SituacaoPartida.EM_ANDAMENTO) {
            return;
        }

        if (bloqueada) {
            long decorrido = agora - inicioExibicaoParIncorretoNanos;

            if (decorrido >= TEMPO_ERRO_NANOS) {
                ocultarSelecionadas();
                limparSelecao();

                bloqueada = false;
                passarVez(agora);
            }

            return;
        }

        long decorrido = agora - inicioTurnoNanos;

        if (decorrido >= TEMPO_TURNO_NANOS) {
            ocultarSelecionadas();
            limparSelecao();
            passarVez(agora);
        }
    }

    private void ocultarSelecionadas() {
        if (primeiraCartaDoTurno != -1) {
            cartas.get(primeiraCartaDoTurno).ocultar();
        }

        if (segundaCartaDoParIncorreto != -1) {
            cartas.get(segundaCartaDoParIncorreto).ocultar();
        }
    }

    private void limparSelecao() {
        primeiraCartaDoTurno = -1;
        segundaCartaDoParIncorreto = -1;
    }

    private void passarVez(long agora) {
        jogadorDaVez = (jogadorDaVez + 1) % TOTAL_JOGADORES;
        inicioTurnoNanos = agora;
    }

    private boolean todosOsParesEncontrados() {
        for (Carta carta : cartas) {
            if (!carta.isPareada()) {
                return false;
            }
        }

        return true;
    }

    private void encerrar() {
        situacao = SituacaoPartida.ENCERRADA;
        jogadorDaVez = -1;
        bloqueada = false;

        int pontos0 = jogadores.get(0).getPontuacao();
        int pontos1 = jogadores.get(1).getPontuacao();

        empate = pontos0 == pontos1;
        vencedor = empate ? -1 : (pontos0 > pontos1 ? 0 : 1);
    }

    private long calcularTempoRestante(long agora) {
        if (situacao != SituacaoPartida.EM_ANDAMENTO || bloqueada) {
            return 0;
        }

        long restante = TEMPO_TURNO_NANOS
                - (agora - inicioTurnoNanos);

        if (restante <= 0) {
            return 0;
        }

        return (restante + 999_999_999L) / 1_000_000_000L;
    }

    public synchronized EstadoPartida obterEstado() {
        long agora = System.nanoTime();
        atualizarTempo(agora);

        int[] pontuacoes = new int[TOTAL_JOGADORES];
        String[] nomes = {"Jogador 1", "Jogador 2"};

        for (Jogador jogador : jogadores) {
            int id = jogador.getId();

            pontuacoes[id] = jogador.getPontuacao();
            nomes[id] = jogador.getNome();
        }

        return new EstadoPartida(
                cartas,
                pontuacoes,
                nomes,
                situacao,
                jogadorDaVez,
                calcularTempoRestante(agora),
                bloqueada,
                vencedor,
                empate
        );
    }
}