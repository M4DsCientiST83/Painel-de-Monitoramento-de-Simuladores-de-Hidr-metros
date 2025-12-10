package src.main.java.leitor_hidro;

import src.main.java.util.LeituraPonteiro;
import src.main.java.util.TipoHidrometro;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.util.List;

public class ClienteLeitorHidrometro 
{
    
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
    
    public static void main(String[] args) 
    {
        try 
        {
            Mat imagem = Imgcodecs.imread("hidrometro.jpg");
            
            if (imagem.empty()) 
            {
                System.err.println("Erro: Não foi possível carregar a imagem!");
                return;
            }
            
            // Cliente trabalha apenas com a interface
            IHidrometroReader leitor = selecionarLeitor(imagem);
            
            String valorDigital = leitor.lerValorDigital(imagem);
            List<LeituraPonteiro> ponteiros = leitor.lerPonteiros(imagem);
            
            exibirResultados(valorDigital, ponteiros, leitor.getClass().getSimpleName());
            
        } 
        catch (Exception e) 
        {
            System.err.println("Erro ao processar hidrômetro:");
            e.printStackTrace();
        }
    }
    
    private static IHidrometroReader selecionarLeitor(Mat imagem) 
    {
        TipoHidrometro tipo = detectarTipo(imagem);
        
        switch (tipo) 
        {
            case COLABORADOR:
                System.out.println("→ Detectado: Hidrômetro Colaborador");
                System.out.println("→ Usando: AdaptadorHidrometroColab\n");
                HidrometroColab hidroColab = new HidrometroColab();
                return new AdaptadorHidrometroColab(hidroColab);
                
            case RODRIGUES:
                System.out.println("→ Detectado: Hidrômetro Rodrigues");
                System.out.println("→ Usando: AdaptadorHidrometroRodri\n");
                HidrometroRodri hidroRodri = new HidrometroRodri();
                return new AdaptadorHidrometroRodri(hidroRodri);
                
            default:
                throw new IllegalArgumentException("Tipo não reconhecido");
        }
    }
    
    private static TipoHidrometro detectarTipo(Mat imagem) 
    {
        Mat hsv = new Mat();
        Imgproc.cvtColor(imagem, hsv, Imgproc.COLOR_BGR2HSV);
        
        Mat mascaraAzul = new Mat();
        Core.inRange(hsv, new Scalar(100, 50, 50), new Scalar(130, 255, 255), mascaraAzul);
        
        int pixelsAzuis = Core.countNonZero(mascaraAzul);
        int totalPixels = imagem.rows() * imagem.cols();
        
        return (pixelsAzuis > totalPixels * 0.15) ? 
            TipoHidrometro.RODRIGUES : TipoHidrometro.COLABORADOR;
    }
    
    private static void exibirResultados(String valor, List<LeituraPonteiro> ponteiros, 
                                        String adapter) 
    {
        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║   LEITURA DO HIDRÔMETRO           ║");
        System.out.println("╠════════════════════════════════════╣");
        System.out.println("║ Adapter: " + adapter);
        System.out.println("║ Valor Digital: " + valor + " m³");
        System.out.println("║");
        System.out.println("║ Ponteiros:");
        for (int i = 0; i < ponteiros.size(); i++) 
        {
            System.out.println("║   " + (i + 1) + ". " + ponteiros.get(i));
        }
        System.out.println("╚════════════════════════════════════╝");
    }
}