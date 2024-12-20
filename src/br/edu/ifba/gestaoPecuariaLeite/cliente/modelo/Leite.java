package br.edu.ifba.gestaoPecuariaLeite.cliente.modelo;

public class Leite implements Comparable<Leite> {

    private Integer quantidade;

    public Leite(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    @Override
    public String toString() {
        return "Quantidade de leite: " + quantidade + " L";
    }

    @Override
    public int compareTo(Leite o) {
        return this.quantidade.compareTo(o.getQuantidade());
    }
}