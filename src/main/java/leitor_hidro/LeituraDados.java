package src.main.java.leitor_hidro;

public class LeituraDados 
{
    private String idSHA;
    private double valor;
    private java.io.File imagem; 

    public LeituraDados(String idSHA, java.io.File imagem) 
    {
        this.idSHA = idSHA;
        this.imagem = imagem;
        this.valor = 0.0; 
    }

    public LeituraDados(String idSHA, double valor) 
    {
        this.idSHA = idSHA;
        this.valor = valor;
    }

    public String getIdSHA() 
    {
        return idSHA;
    }

    public double getValor() 
    {
        return valor;
    }

    public void setValor(double valor) 
    {
        this.valor = valor;
    }

    public java.io.File getImagem() 
    {
        return imagem;
    }
}