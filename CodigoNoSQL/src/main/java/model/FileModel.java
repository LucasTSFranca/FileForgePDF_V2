
package model;

public class FileModel {
    
    private Long id;
    private String nome;
    private String caminho;
    private Double tamanho;

    public FileModel(String nome, String caminho, double originalSize) {
        this.nome = nome;
        this.caminho = caminho;
        this.tamanho = originalSize;
    }

    public FileModel() {
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

    public String getCaminho() {
        return caminho;
    }

    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }

    public Double getTamanho() {
        return tamanho;
    }

    public void setTamanho(Double tamanho) {
        this.tamanho = tamanho;
    }
    
    
}
