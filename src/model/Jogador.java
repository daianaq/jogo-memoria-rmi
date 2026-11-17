package model;

public class Jogador {

    private final int id;
    private int pontuacao;

    public Jogador(int id) {
        this.id = id;
        this.pontuacao = 0;
    }

    public int getId() {
        return id;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public void adicionarPonto() {
        pontuacao++;
    }
}