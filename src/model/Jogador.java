package model;

public class Jogador {

    private final int id;
    private final String nome;
    private int pontuacao;

    public Jogador(int id, String nome) {
        this.id = id;
        this.nome = nome;
        this.pontuacao = 0;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public void adicionarPonto() {
        pontuacao++;
    }
}
