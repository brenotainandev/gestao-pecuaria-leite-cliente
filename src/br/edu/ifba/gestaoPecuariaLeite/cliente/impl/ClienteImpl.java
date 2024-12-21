package br.edu.ifba.gestaoPecuariaLeite.cliente.impl;

import br.edu.ifba.gestaoPecuariaLeite.cliente.comunicacao.Cliente;
import br.edu.ifba.gestaoPecuariaLeite.cliente.modelo.Leite;
import br.edu.ifba.gestaoPecuariaLeite.cliente.modelo.Vaca;
import br.edu.ifba.gestaoPecuariaLeite.cliente.sensoriamento.Sensoriamento;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ClienteImpl implements Cliente<Vaca, Leite>, Runnable {

    private static final int TOTAL_DE_LEITURAS = 100;
    private static final int PRODUCAO_FORA_DA_MEDIA = 0;
    private static final int PRODUCAO_DENTRO_DA_MEDIA = 1;

    private static final String URL_SERVIDOR = "http://localhost:8080/";
    private static final String URL_ENVIAR_LEITE = URL_SERVIDOR + "gestao-leite/ordenha/";

    private static final int LIMIAR_OSCILACAO_DE_PRODUCAO_DE_LEITE = 5;

    private static final int TAMANHO_MAXIMO_HISTORICO = 30;

    private Vaca monitorado = null;
    private Sensoriamento<Leite> sensoriamento = null;
    private int mediaIdealProducao;

    private final Queue<Leite> historicoDeLeituras = new LinkedList<>();

    @Override
    public void configurar(Vaca vaca, Sensoriamento<Leite> sensoriamento, int mediaIdealProducao) {
        this.monitorado = vaca;
        this.sensoriamento = sensoriamento;
        this.mediaIdealProducao = mediaIdealProducao;
    }

    /**
     * Detecta se a vaca está com produção de leite abaixo da média.
     * Baseado no histórico de leituras.
     *
     * Complexidade: O(N), onde N é o número de leituras no histórico.
     * O método percorre todas as leituras armazenadas no histórico.
     *
     * @return 0 se a produção está fora da média, 1 caso contrário.
     */
    @Override
    public int detectarVacaProducaoBaixa() {
        int producaoAcimaMedia = 0;
        int producaoAbaixoMedia = 0;

        for (Leite leitura : historicoDeLeituras) {
            if (leitura.getQuantidade() > mediaIdealProducao) {
                producaoAcimaMedia++;
            } else {
                producaoAbaixoMedia++;
            }
        }

        return producaoAbaixoMedia > producaoAcimaMedia ? PRODUCAO_FORA_DA_MEDIA : PRODUCAO_DENTRO_DA_MEDIA;
    }

    /**
     * Envia uma leitura de produção de leite ao servidor.
     *
     * Complexidade: O(1) para montar a URL e enviar os dados,
     * mas pode variar dependendo do tempo de resposta do servidor.
     *
     * @param leite Dados da leitura de leite.
     * @return Resposta do servidor.
     * @throws Exception Caso ocorra erro de comunicação.
     */
    @Override
    public String enviar(Leite leite) throws Exception {
        int producaoBaixa = detectarVacaProducaoBaixa();

        // Monta a URL para envio.
        String urlString = URL_ENVIAR_LEITE +
                monitorado.getId() + "/" +
                URLEncoder.encode(monitorado.getNome(), StandardCharsets.UTF_8) + "/" +
                leite.getQuantidade() + "/" +
                producaoBaixa;

        System.out.println("Enviando leitura de leite para o servidor: " + urlString);

        URL url = new URL(urlString);
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
        conexao.setRequestMethod("GET");

        if (conexao.getResponseCode() != 200) {
            throw new Exception("Servidor de gestão de pecuária de leite indisponível");
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conexao.getInputStream()))) {
            return br.readLine();
        } finally {
            conexao.disconnect();
        }
    }

    /**
     * Executa o processo de sensoriamento e envio das leituras de leite.
     *
     * Complexidade: O(N), onde N é o número de leituras geradas pelo sensoriamento.
     * Cada leitura é processada uma vez, e o histórico é atualizado com O(1).
     */
    @Override
    public void run() {
        List<Leite> leituras = sensoriamento.gerar(TOTAL_DE_LEITURAS);
        List<Leite> leiturasParaEnviar = new ArrayList<>();

        // Processa cada leitura gerada pelo sensoriamento.
        for (Leite leitura : leituras) {
            if (Math.abs(leitura.getQuantidade()) > LIMIAR_OSCILACAO_DE_PRODUCAO_DE_LEITE) {
                historicoDeLeituras.add(leitura);

                // Garante que o histórico não ultrapasse o tamanho máximo.
                if (historicoDeLeituras.size() > TAMANHO_MAXIMO_HISTORICO) {
                    historicoDeLeituras.poll(); // O(1) para remover o elemento mais antigo.
                }

                leiturasParaEnviar.add(leitura);
            } else {
                System.out.println("Leitura ignorada: oscilação insignificante.");
            }
        }

        // Envia todas as leituras acumuladas.
        for (Leite leitura : leiturasParaEnviar) {
            try {
                String resposta = enviar(leitura);
                System.out.println("Leitura enviada: " + resposta);
            } catch (Exception e) {
                System.err.println("Erro ao enviar leitura: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
