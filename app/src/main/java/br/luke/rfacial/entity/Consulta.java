package br.luke.rfacial.entity;

public class Consulta {

    private String cdPessoa;
    private String carregarImagens;
    private String app;

    public Consulta(String cdPessoa, String carregarImagens, String app) {
        this.cdPessoa = cdPessoa;
        this.carregarImagens = carregarImagens;
        this.app = app;
    }
}
