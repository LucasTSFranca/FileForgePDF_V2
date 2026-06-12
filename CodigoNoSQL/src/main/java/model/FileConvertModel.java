
package model;

import java.time.LocalDate;

public class FileConvertModel {
    
    private Long id;
    private String nome;
    private String novo_caminho;
    private LocalDate dt_criacao;
    private Double tamanho;

    public FileConvertModel(String nome, String novo_caminho, double pdfSize) {
        this.nome = nome;
        this.novo_caminho = novo_caminho;
        this.tamanho = pdfSize;
    }

    public FileConvertModel(LocalDate dt_criacao) {
        this.dt_criacao = LocalDate.now();
    }

    public FileConvertModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNovoCaminho() {
        return novo_caminho;
    }

    public void setNovoCaminho(String novo_caminho) {
        this.novo_caminho = novo_caminho;
    }

    public LocalDate getDt_criacao() {
        return dt_criacao;
    }

    public void setDt_criacao(LocalDate dt_criacao) {
        this.dt_criacao = dt_criacao;
    }

    public Double getTamanho() {
        return tamanho;
    }

    public void setTamanho(Double tamanho) {
        this.tamanho = tamanho;
    }
    
    
}
