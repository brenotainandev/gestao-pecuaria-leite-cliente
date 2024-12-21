package br.edu.ifba.gestaoPecuariaLeite.cliente.sensoriamento;

import java.util.List;

public interface Sensoriamento<TipoDado> {
    List<TipoDado> gerar(int totalLeituras);
}
