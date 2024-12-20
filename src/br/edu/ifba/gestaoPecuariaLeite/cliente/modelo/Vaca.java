package br.edu.ifba.gestaoPecuariaLeite.cliente.modelo;

public class Vaca implements Comparable<Vaca> {
    private String id;
    private String nome;

    public Vaca(String id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return "id: " + id + ", nome: " + nome;
    }

    @Override
    public int compareTo(Vaca outraVaca) {
        return Integer.compare(Integer.parseInt(this.id), Integer.parseInt(outraVaca.id));
    }
}