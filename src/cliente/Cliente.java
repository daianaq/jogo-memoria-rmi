package cliente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import compartilhado.EstadoPartida;
import compartilhado.JogoRemoto;
import compartilhado.OperacaoRejeitadaException;
import compartilhado.ResultadoJogo;
import compartilhado.SituacaoPartida;
import model.Carta;

public class Cliente {

    private final JogoRemoto jogo;
    private final int idJogador;
    private final String nomeJogador;

    private final ScheduledExecutorService comunicacao =
            Executors.newScheduledThreadPool(2);

    private final AtomicBoolean solicitacaoPendente =
            new AtomicBoolean(false);

    private final AtomicBoolean consultaPendente =
            new AtomicBoolean(false);

    private JFrame janela;

    private JButton[] botoesCartas;

    private JLabel labelNomeJogador0;
    private JLabel labelNomeJogador1;

    private JLabel labelPontosJogador0;
    private JLabel labelPontosJogador1;

    private JLabel labelTurno;
    private JLabel labelTempo;
    private JLabel labelMensagem;

    private JPanel painelJogador0;
    private JPanel painelJogador1;

    private final Color fundo = new Color(24, 28, 45);
    private final Color painel = new Color(37, 43, 65);
    private final Color azul = new Color(55, 105, 170);
    private final Color roxo = new Color(105, 75, 160);
    private final Color branco = Color.WHITE;

    public Cliente(String enderecoServidor, String nomeJogador)
            throws Exception {

        this.nomeJogador = nomeJogador;

        Registry registro = LocateRegistry.getRegistry(
                enderecoServidor, 1099
        );

        jogo = (JogoRemoto) registro.lookup("JogoMemoria");

        idJogador = jogo.entrarPartida(nomeJogador);

        System.out.println(
                "Conectado como " + nomeJogador
                + " (jogador " + idJogador + ")"
        );
    }

   
    private void criarInterface() {

        janela = new JFrame("Jogo da Memória");

        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setSize(720, 780);
        janela.setMinimumSize(new java.awt.Dimension(620, 680));
        janela.setLocationRelativeTo(null);
        janela.getContentPane().setBackground(fundo);
        janela.setLayout(new BorderLayout(15, 15));

        criarCabecalho();

        criarPainelCentral();

        criarPainelMensagem();

        janela.setVisible(true);

        comunicacao.scheduleWithFixedDelay(
                this::consultarEstado,
                0,
                500,
                TimeUnit.MILLISECONDS
        );
    }

 
    private void criarCabecalho() {

        JPanel painelPrincipal = new JPanel(
                new BorderLayout(10, 10)
        );

        painelPrincipal.setBackground(fundo);

        painelPrincipal.setBorder(
                new EmptyBorder(15, 20, 5, 20)
        );

        JLabel titulo = new JLabel(
                "JOGO DA MEMÓRIA",
                SwingConstants.CENTER
        );

        titulo.setFont(
                new Font("Arial", Font.BOLD, 27)
        );

        titulo.setForeground(branco);

        JLabel subtitulo = new JLabel(
                "Desafie seu adversário!",
                SwingConstants.CENTER
        );

        subtitulo.setFont(
                new Font("Arial", Font.PLAIN, 13)
        );

        subtitulo.setForeground(
                new Color(180, 190, 215)
        );

        JPanel cabecalho = new JPanel(
                new GridLayout(2, 1, 2, 2)
        );

        cabecalho.setBackground(fundo);

        cabecalho.add(titulo);
        cabecalho.add(subtitulo);

        painelPrincipal.add(
                cabecalho,
                BorderLayout.NORTH
        );

        criarPainelJogadores(painelPrincipal);

        janela.add(
                painelPrincipal,
                BorderLayout.NORTH
        );
    }

    private void criarPainelJogadores(JPanel painelPrincipal) {

        JPanel painelJogadores = new JPanel(
                new GridLayout(1, 2, 15, 0)
        );

        painelJogadores.setBackground(fundo);

        painelJogador0 = new JPanel(
                new GridLayout(2, 1, 5, 5)
        );

        painelJogador1 = new JPanel(
                new GridLayout(2, 1, 5, 5)
        );

        painelJogador0.setBackground(painel);
        painelJogador1.setBackground(painel);

        painelJogador0.setBorder(
                criarBordaJogador(azul, 2)
        );

        painelJogador1.setBorder(
                criarBordaJogador(roxo, 2)
        );

        labelNomeJogador0 = new JLabel(
                "Jogador 1",
                SwingConstants.CENTER
        );

        labelNomeJogador1 = new JLabel(
                "Jogador 2",
                SwingConstants.CENTER
        );

        labelPontosJogador0 = new JLabel(
                "0 pontos",
                SwingConstants.CENTER
        );

        labelPontosJogador1 = new JLabel(
                "0 pontos",
                SwingConstants.CENTER
        );

        configurarLabelJogador(labelNomeJogador0);
        configurarLabelJogador(labelNomeJogador1);

        configurarLabelPontuacao(labelPontosJogador0);
        configurarLabelPontuacao(labelPontosJogador1);

        painelJogador0.add(labelNomeJogador0);
        painelJogador0.add(labelPontosJogador0);

        painelJogador1.add(labelNomeJogador1);
        painelJogador1.add(labelPontosJogador1);

        painelJogadores.add(painelJogador0);
        painelJogadores.add(painelJogador1);

        painelPrincipal.add(
                painelJogadores,
                BorderLayout.CENTER
        );
    }

    private EmptyBorder criarBordaJogador(
            Color cor, int espessura) {

        return new EmptyBorder(12, 12, 12, 12);
    }

    private void configurarLabelJogador(JLabel label) {

        label.setForeground(branco);

        label.setFont(
                new Font("Arial", Font.BOLD, 17)
        );
    }

    private void configurarLabelPontuacao(JLabel label) {

        label.setForeground(
                new Color(210, 220, 245)
        );

        label.setFont(
                new Font("Arial", Font.BOLD, 22)
        );
    }

    private void criarPainelCentral() {

        JPanel painelCentro = new JPanel(
                new BorderLayout(10, 12)
        );

        painelCentro.setBackground(fundo);

        painelCentro.setBorder(
                new EmptyBorder(0, 20, 0, 20)
        );

        criarPainelStatus(painelCentro);

        criarTabuleiro(painelCentro);

        janela.add(
                painelCentro,
                BorderLayout.CENTER
        );
    }

    private void criarPainelStatus(JPanel painelCentro) {

        JPanel painelStatus = new JPanel(
                new GridLayout(2, 1, 5, 5)
        );

        painelStatus.setBackground(painel);

        painelStatus.setBorder(
                new EmptyBorder(12, 10, 12, 10)
        );

        labelTurno = new JLabel(
                "Aguardando jogadores...",
                SwingConstants.CENTER
        );

        labelTurno.setForeground(branco);

        labelTurno.setFont(
                new Font("Arial", Font.BOLD, 16)
        );

        labelTempo = new JLabel(
                "Tempo restante: --",
                SwingConstants.CENTER
        );

        labelTempo.setForeground(
                new Color(130, 190, 255)
        );

        labelTempo.setFont(
                new Font("Arial", Font.BOLD, 18)
        );

        painelStatus.add(labelTurno);
        painelStatus.add(labelTempo);

        painelCentro.add(
                painelStatus,
                BorderLayout.NORTH
        );
    }

    private void criarTabuleiro(JPanel painelCentro) {

        JPanel painelCartas = new JPanel(
                new GridLayout(4, 4, 10, 10)
        );

        painelCartas.setBackground(fundo);

        painelCartas.setBorder(
                new EmptyBorder(10, 0, 10, 0)
        );

        botoesCartas = new JButton[16];

        for (int i = 0; i < botoesCartas.length; i++) {

            final int posicao = i;

            JButton botao = new JButton("?");

            botao.setFont(
                    new Font("Arial", Font.BOLD, 27)
            );

            botao.setForeground(branco);

            botao.setBackground(
                    new Color(48, 59, 88)
            );

            botao.setFocusPainted(false);
            botao.setOpaque(true);

            botao.setBorder(
                    BorderFactory.createLineBorder(
                            new Color(90, 115, 160), 2
                    )
            );

            botao.addActionListener(event ->
                    solicitarJogada(posicao)
            );

            botoesCartas[i] = botao;

            painelCartas.add(botao);
        }

        painelCentro.add(
                painelCartas,
                BorderLayout.CENTER
        );
    }


    private void criarPainelMensagem() {

        labelMensagem = new JLabel(
                "Aguardando início da partida...",
                SwingConstants.CENTER
        );

        labelMensagem.setForeground(branco);

        labelMensagem.setFont(
                new Font("Arial", Font.BOLD, 14)
        );

        labelMensagem.setBackground(painel);
        labelMensagem.setOpaque(true);

        labelMensagem.setBorder(
                new EmptyBorder(15, 10, 15, 10)
        );

        janela.add(
                labelMensagem,
                BorderLayout.SOUTH
        );
    }

   
    private void solicitarJogada(int posicao) {

        if (!solicitacaoPendente.compareAndSet(false, true)) {
            return;
        }

        comunicacao.execute(() -> {

            try {

                ResultadoJogo resultado =
                        jogo.virarCarta(idJogador, posicao);

                SwingUtilities.invokeLater(() ->
                        mostrarResultado(resultado)
                );

                consultarEstado();

            } catch (OperacaoRejeitadaException e) {

                SwingUtilities.invokeLater(() ->
                        labelMensagem.setText(e.getMessage())
                );

            } catch (Exception e) {

                SwingUtilities.invokeLater(() ->
                        labelMensagem.setText(
                                "Erro de comunicação com o servidor."
                        )
                );

            } finally {

                solicitacaoPendente.set(false);
            }
        });
    }


    private void mostrarResultado(ResultadoJogo resultado) {

        switch (resultado.getTipoResultado()) {

            case CARTA_REVELADA:

                labelMensagem.setText(
                        "Carta revelada."
                );

                break;

            case PAR_ENCONTRADO:

                labelMensagem.setText(
                        "Par encontrado! Você joga novamente."
                );

                break;

            case PAR_INCORRETO:

                labelMensagem.setText(
                        "Par incorreto. Aguarde a próxima jogada."
                );

                break;

            case PARTIDA_ENCERRADA:

                labelMensagem.setText(
                        "Partida encerrada!"
                );

                break;
        }
    }

 
    private void consultarEstado() {

        if (!consultaPendente.compareAndSet(false, true)) {
            return;
        }

        try {

            EstadoPartida estado = jogo.obterEstado();

            SwingUtilities.invokeLater(() ->
                    atualizarInterface(estado)
            );

        } catch (Exception e) {

            SwingUtilities.invokeLater(() ->
                    labelMensagem.setText(
                            "Não foi possível consultar o servidor."
                    )
            );

        } finally {

            consultaPendente.set(false);
        }
    }


    private void atualizarInterface(EstadoPartida estado) {

        int[] pontuacoes = estado.getPontuacoes();

        String[] nomes = estado.getNomesJogadores();

        int pontosJogador0 =
                pontuacoes.length > 0 ? pontuacoes[0] : 0;

        int pontosJogador1 =
                pontuacoes.length > 1 ? pontuacoes[1] : 0;

        String nomeJogador0 =
                nomes.length > 0
                        ? nomes[0]
                        : "Aguardando jogador";

        String nomeJogador1 =
                nomes.length > 1
                        ? nomes[1]
                        : "Aguardando jogador";
      
        labelNomeJogador0.setText(nomeJogador0);
        labelNomeJogador1.setText(nomeJogador1);

   
        labelPontosJogador0.setText(
                pontosJogador0 + " pontos"
        );

        labelPontosJogador1.setText(
                pontosJogador1 + " pontos"
        );

     
        atualizarDestaqueJogador(estado);

        
        if (estado.getSituacao() == SituacaoPartida.AGUARDANDO) {

            labelTurno.setText(
                    "Aguardando o segundo jogador..."
            );

        } else if (
                estado.getSituacao() == SituacaoPartida.ENCERRADA) {

            labelTurno.setText(
                    "Partida encerrada."
            );

        } else {

            int jogadorAtual = estado.getJogadorDaVez();

            String nomeAtual = jogadorAtual == 0
                    ? nomeJogador0
                    : nomeJogador1;

            labelTurno.setText(
                    "Vez de jogar: " + nomeAtual
            );
        }

       
        labelTempo.setText(
                "Tempo restante: "
                + estado.getTempoRestanteSegundos()
                + " segundos"
        );

        
        List<Carta> cartas = estado.getCartas();

        for (int i = 0; i < cartas.size(); i++) {

            Carta carta = cartas.get(i);

            JButton botao = botoesCartas[i];

            if (carta.isRevelada() || carta.isPareada()) {

                botao.setText(
                        String.valueOf(carta.getValor() + 1)
                );

                botao.setBackground(
                        new Color(55, 105, 170)
                );

                botao.setForeground(branco);

            } else {

                botao.setText("?");

                botao.setBackground(
                        new Color(48, 59, 88)
                );

                botao.setForeground(branco);
            }

            boolean podeJogar =
                    estado.getSituacao()
                            == SituacaoPartida.EM_ANDAMENTO
                    && estado.getJogadorDaVez() == idJogador
                    && carta.estaDisponivel();

            botao.setEnabled(podeJogar);
        }

        
        if (estado.getSituacao() == SituacaoPartida.ENCERRADA) {

            if (pontosJogador0 == pontosJogador1) {

                labelMensagem.setText(
                        "A partida terminou empatada!"
                );

            } else if (pontosJogador0 > pontosJogador1) {

                labelMensagem.setText(
                        nomeJogador0 + " venceu!"
                );

            } else {

                labelMensagem.setText(
                        nomeJogador1 + " venceu!"
                );
            }
        }
    }

   
    private void atualizarDestaqueJogador(
            EstadoPartida estado) {

        Color destaque = new Color(100, 175, 255);
        Color normal = new Color(70, 75, 95);

        if (estado.getSituacao() == SituacaoPartida.EM_ANDAMENTO) {

            if (estado.getJogadorDaVez() == 0) {

                painelJogador0.setBorder(
                        criarBordaJogador(destaque, 3)
                );

                painelJogador1.setBorder(
                        criarBordaJogador(normal, 1)
                );

            } else {

                painelJogador1.setBorder(
                        criarBordaJogador(destaque, 3)
                );

                painelJogador0.setBorder(
                        criarBordaJogador(normal, 1)
                );
            }
        }
    }

    
    public static void main(String[] args) {

        String enderecoServidor = "localhost";

        if (args.length > 0) {
            enderecoServidor = args[0];
        }

        String nomeJogador = JOptionPane.showInputDialog(
                null,
                "Digite seu nome:",
                "Entrar na partida",
                JOptionPane.QUESTION_MESSAGE
        );

        if (nomeJogador == null) {
            return;
        }

        nomeJogador = nomeJogador.trim();

        if (nomeJogador.isEmpty()) {

            JOptionPane.showMessageDialog(
                    null,
                    "Digite um nome para entrar na partida.",
                    "Nome inválido",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            Cliente cliente = new Cliente(
                    enderecoServidor,
                    nomeJogador
            );

            SwingUtilities.invokeLater(
                    cliente::criarInterface
            );

        } catch (OperacaoRejeitadaException e) {

            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage(),
                    "Entrada não permitida",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Não foi possível conectar ao servidor.\n"
                    + e.getMessage(),
                    "Erro de conexão",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }
}
