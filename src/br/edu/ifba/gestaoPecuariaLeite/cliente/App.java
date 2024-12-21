package br.edu.ifba.gestaoPecuariaLeite.cliente;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifba.gestaoPecuariaLeite.cliente.impl.ClienteImpl;
import br.edu.ifba.gestaoPecuariaLeite.cliente.impl.SensoriamentoImpl;
import br.edu.ifba.gestaoPecuariaLeite.cliente.modelo.Vaca;

public class App {
    private static final int TOTAL_DE_VACAS = 10;
    private static final int PRODUCAO_MEDIA = 20;

    public static void main(String[] args) throws Exception {
        // Lista para armazenar threads que serão executadas em paralelo.
        List<Thread> processos = new ArrayList<>();

        // Lista fixa de nomes para as vacas.
        List<String> nomesDeVacas = List.of("Bessie", "Buttercup", "Clarabelle", "Daisy",
                "Elsie", "Gertie", "Mabel", "Molly", "Nellie", "Rosie");

        // Inicializa e configura as vacas e seus respectivos processos.
        for (int i = 1; i <= TOTAL_DE_VACAS; i++) {
            String id = String.valueOf(i);
            String nome = nomesDeVacas.get(i % nomesDeVacas.size());

            // Configuração do cliente.
            ClienteImpl cliente = new ClienteImpl();
            cliente.configurar(new Vaca(id, nome), new SensoriamentoImpl(), PRODUCAO_MEDIA);

            // Criação e inicialização da thread para o cliente.
            Thread processo = new Thread(cliente);
            processos.add(processo);
            processo.start(); // Inicia a execução paralela.
        }

        System.out.println("Enviando leituras de leite para o servidor...");

        // Aguarda a finalização de todas as threads.
        for (Thread processo : processos) {
            processo.join();
        }

        System.out.println("Leituras de leite enviadas, finalizando a execução.");
    }
}
