package facade;

import leitor_hidro.*;
import util.LeituraPonteiro;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import java.util.List;

public class PainelFacade 
{
    static 
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    }

    private IHidrometroReader leitorColab;
    private IHidrometroReader leitorRodri;

    public PainelFacade() 
    {
        HidrometroColab hidroColab = new HidrometroColab();
        this.leitorColab = new AdaptadorHidrometroColab(hidroColab);

        HidrometroRodri hidroRodri = new HidrometroRodri();
        this.leitorRodri = new AdaptadorHidrometroRodri(hidroRodri);
    }

    public ResultadoLeitura processarImagemColaborador(String caminhoImagem) 
    {
        System.out.println("\n[COLABORADOR] Processando: " + caminhoImagem);
        return processarImagem(caminhoImagem, leitorColab, "Colaborador");
    }

    public ResultadoLeitura processarImagemRodrigues(String caminhoImagem) 
    {
        System.out.println("\n[RODRIGUES] Processando: " + caminhoImagem);
        return processarImagem(caminhoImagem, leitorRodri, "Rodrigues");
    }

    private ResultadoLeitura processarImagem(String caminhoImagem, 

                                            IHidrometroReader leitor,

                                            String tipo) 
    {
        try 
        {
            Mat imagem = Imgcodecs.imread(caminhoImagem);

            if (imagem.empty()) 
            {
                return new ResultadoLeitura(false, "Erro ao carregar imagem", null, null, tipo);
            }

            String valorDigital = leitor.lerValorDigital(imagem);
            List<LeituraPonteiro> ponteiros = leitor.lerPonteiros(imagem);

            return new ResultadoLeitura(true, "Sucesso", valorDigital, ponteiros, tipo);
        } 
        catch (Exception e) 
        {
            return new ResultadoLeitura(false, "Erro: " + e.getMessage(), null, null, tipo);
        }
    }

    public void exibirResultado(ResultadoLeitura resultado) 
    {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║  PAINEL - LEITURA DE HIDRÔMETRO      ║");
        System.out.println("╠═══════════════════════════════════════╣");
        System.out.println("║ Tipo: " + resultado.tipo);
        System.out.println("║ Status: " + (resultado.sucesso ? "✓ SUCESSO" : "✗ FALHA"));

        if (resultado.sucesso)
        {
            System.out.println("║ Consumo Digital: " + resultado.valorDigital + " m³");
            System.out.println("║");
            System.out.println("║ Ponteiros Analógicos:");

            if (resultado.ponteiros != null && !resultado.ponteiros.isEmpty()) 
            {
                for (int i = 0; i < resultado.ponteiros.size(); i++) 
                {
                    System.out.println("║   → " + resultado.ponteiros.get(i));
                }
            } 
            else 
            {
                System.out.println("║   (Nenhum ponteiro detectado)");
            }
        } 
        else 
        {
            System.out.println("║ Mensagem: " + resultado.mensagem);
        }

        System.out.println("╚═══════════════════════════════════════╝");
    }
}