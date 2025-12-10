package src.main.java.util;

public enum TipoHidrometro 
{
    COLABORADOR("Hidrômetro Colaborador - fundo cinza/roxo"),
    RODRIGUES("Hidrômetro Rodrigues - fundo azul");
    
    private String descricao;
    
    TipoHidrometro(String descricao) 
    {
        this.descricao = descricao;
    }
    
    public String getDescricao() 
    {
        return descricao;
    }
}