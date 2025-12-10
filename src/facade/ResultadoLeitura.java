package facade;
import java.util.List;
import util.LeituraPonteiro;

public class ResultadoLeitura 
{
    boolean sucesso;
    String mensagem;
    String valorDigital;
    List<LeituraPonteiro> ponteiros;
    String tipo;

    public ResultadoLeitura(boolean sucesso, String mensagem, String valorDigital,

                           List<LeituraPonteiro> ponteiros, String tipo) 
    {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.valorDigital = valorDigital;
        this.ponteiros = ponteiros;
        this.tipo = tipo;
    }
}