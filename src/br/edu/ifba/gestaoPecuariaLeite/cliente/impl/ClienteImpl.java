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

    private static final int TOTAL_DE_LEITURAS = 1;

    private static final String URL_SERVIDOR = "http://localhost:8080/";
    private static final String URL_ENVIAR_LEITE = URL_SERVIDOR + "gestao-leite/ordenha/";

    private static final int LIMIAR_OSCILACAO_DE_PRODUCAO_DE_LEITE = 5;
    private static final int PRODUCAO_MEDIA = 20;

    private static final int TAMANHO_MAXIMO_HISTORICO = 30;

    private Vaca monitorado = null;
    private Sensoriamento<Leite> sensoriamento = null;

    private Queue<Leite> historicoDeLeituras = new LinkedList<>();
    private Leite ultimaLeitura = new Leite(0);

    @Override
    public void configurar(Vaca vaca, Sensoriamento<Leite> sensoriamento, List<Leite> padrao) {
        this.monitorado = vaca;
        this.sensoriamento = sensoriamento;
    }

    @Override
    public int detectarVacaProducaoBaixa() {
        int totalDeDeteccoes = 0;
        List<Leite> historico = new ArrayList<>(historicoDeLeituras);
        for (Leite leitura : historico) {
            if (leitura.getQuantidade() < PRODUCAO_MEDIA) {
                totalDeDeteccoes++;
            }
        }
        return totalDeDeteccoes;
    }

    @Override
    public String enviar(Leite leite) throws Exception {
        int producaoBaixa = detectarVacaProducaoBaixa();

        URL url = new URL(URL_ENVIAR_LEITE + monitorado.getId() + "/" + URLEncoder.encode(monitorado.getNome(), StandardCharsets.UTF_8.toString()) + "/"
                + leite.getQuantidade() + "/" + producaoBaixa);

        System.out.println("enviando leitura de leite para o servidor: " + url);
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
        conexao.setRequestMethod("GET");
        if (conexao.getResponseCode() != 200) {
            throw new Exception("servidor de gestão de pecuária de leite indisponível");
        }

        InputStreamReader in = new InputStreamReader(conexao.getInputStream());
        BufferedReader br = new BufferedReader(in);
        String resposta = br.readLine();

        conexao.disconnect();

        return resposta;
    }

    @Override
    public void run() {
        List<Leite> leituras = sensoriamento.gerar(TOTAL_DE_LEITURAS);
        List<Leite> leiturasParaEnviar = new ArrayList<>();

        for (Leite leitura : leituras) {
            if (Math.abs(leitura.getQuantidade()) > LIMIAR_OSCILACAO_DE_PRODUCAO_DE_LEITE) {
                ultimaLeitura = leitura;
                historicoDeLeituras.add(ultimaLeitura);
                if (historicoDeLeituras.size() > TAMANHO_MAXIMO_HISTORICO) {
                    historicoDeLeituras.remove();
                }
                leiturasParaEnviar.add(leitura);
            } else {
                System.out.println("leitura ignorada, não ocorreu oscilação significativa");
            }
        }

        // Enviar todas as leituras acumuladas ao final da ordenha
        for (Leite leitura : leiturasParaEnviar) {
            try {
                String resposta = enviar(leitura);
                System.out.println(resposta.equals("ok") ? "leitura enviada com sucesso" : "falha ao enviar leitura");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}