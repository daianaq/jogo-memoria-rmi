package model;

import java.io.Serializable;

public class Carta implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int posicao;
    private final int valor;

    private boolean revelada;
    private boolean pareada;

    public Carta(int posicao, int valor) {
        this.posicao = posicao;
        this.valor = valor;

        this.revelada = false;
        this.pareada = false;
    }

    public int getPosicao() {
        return posicao;
    }

    public int getValor() {
        return valor;
    }

    public boolean isRevelada() {
        return revelada;
    }

    public boolean isPareada() {
        return pareada;
    }

    public boolean estaDisponivel() {
        return !revelada && !pareada;
    }

    public void revelar() {
        revelada = true;
    }

    public void ocultar() {
        if (!pareada) {
            revelada = false;
        }
    }

    public void marcarComoPareada() {
        pareada = true;
        revelada = true;
    }
}
