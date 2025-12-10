package leitor_hidro;

import util.AnalisadorPonteiro;
import util.Circulo;
import util.LeituraPonteiro;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import net.sourceforge.tess4j.TesseractException;
import java.util.ArrayList;
import java.util.List;

public class AdaptadorHidrometroRodri implements IHidrometroReader 
{
    private HidrometroRodri hidrometroRodri;
    
    public AdaptadorHidrometroRodri(HidrometroRodri hidrometroRodri) 
    {
        this.hidrometroRodri = hidrometroRodri;
    }
    
    @Override
    public String lerValorDigital(Mat imagem) throws TesseractException 
    {
        Rect area = hidrometroRodri.obterAreaDisplay(imagem);
        
        Mat cinza = new Mat();
        Imgproc.cvtColor(imagem, cinza, Imgproc.COLOR_BGR2GRAY);
        Mat regiao = new Mat(cinza, area);
        
        return hidrometroRodri.reconhecerNumeros(regiao);
    }
    
    @Override
    public List<LeituraPonteiro> lerPonteiros(Mat imagem) 
    {
        List<LeituraPonteiro> leituras = new ArrayList<>();
        List<Circulo> circulos = hidrometroRodri.detectarCirculosPonteiros(imagem);
        
        for (Circulo circulo : circulos) 
        {
            double angulo = AnalisadorPonteiro.detectarAngulo(imagem, 
                circulo.centro, circulo.raio);
            int valor = AnalisadorPonteiro.anguloParaValor(angulo);
            leituras.add(new LeituraPonteiro(valor, angulo, circulo.centro));
        }
        
        return leituras;
    }
}