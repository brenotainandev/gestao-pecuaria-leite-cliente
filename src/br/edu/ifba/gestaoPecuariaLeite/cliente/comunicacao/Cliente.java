package br.edu.ifba.gestaoPecuariaLeite.cliente.comunicacao;

import br.edu.ifba.gestaoPecuariaLeite.cliente.sensoriamento.Sensoriamento;

import java.util.List;

public interface Cliente<Monitorado, Sensor> {
    void configurar(Monitorado monitorado, Sensoriamento<Sensor> sensoriamento, List<Sensor> padrao);
    int detectarVacaProducaoBaixa();
    String enviar(Sensor sensor) throws Exception;
}