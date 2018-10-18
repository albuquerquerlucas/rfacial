package br.luke.rfacial.entity;

public class Verificacao {

    private String cdPessoa;
    private String[] imagens;

    public Verificacao() {
    }

    public Verificacao(String cdPessoa, String[] imagens) {
        this.cdPessoa = cdPessoa;
        this.imagens = imagens;
    }

    public String getCdPessoa() {
        return cdPessoa;
    }

    public void setCdPessoa(String cdPessoa) {
        this.cdPessoa = cdPessoa;
    }

    public String[] getImagens() {
        return imagens;
    }

    public void setImagens(String[] imagens) {
        this.imagens = imagens;
    }
}
