package br.edu.ifba.gestaoPecuariaLeite.cliente;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifba.gestaoPecuariaLeite.cliente.impl.ClienteImpl;
import br.edu.ifba.gestaoPecuariaLeite.cliente.impl.SensoriamentoImpl;
import br.edu.ifba.gestaoPecuariaLeite.cliente.modelo.Leite;
import br.edu.ifba.gestaoPecuariaLeite.cliente.modelo.Vaca;

public class App {
    private static final int TOTAL_DE_VACAS = 10;

    public static void main(String[] args) throws Exception {
        List<Thread> processos = new ArrayList<>();
        List<String> nomesDeVacas = List.of("Bessie", "Buttercup", "Clarabelle", "Daisy", "Elsie", "Gertie", "Mabel", "Molly", "Nellie", "Rosie");

        for (int i = 1; i <= TOTAL_DE_VACAS; i++) {
            String id = String.valueOf(i);
            String nome = nomesDeVacas.get(i % nomesDeVacas.size());

            List<Leite> padrao = new ArrayList<>();
            padrao.add(new Leite(20));
            padrao.add(new Leite(25));

            ClienteImpl cliente = new ClienteImpl();
            cliente.configurar(new Vaca(id, nome), new SensoriamentoImpl(), padrao);

            Thread processo = new Thread(cliente);
            processos.add(processo);
            processo.start();
        }

        System.out.println("enviando leituras de leite para o servidor");

        for (Thread processo : processos) {
            processo.join();
        }

        System.out.println("leituras de leite enviadas, finalizando a execução");
    }
}