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

public class AdaptadorHidrometroColab implements IHidrometroReader 
{
    private HidrometroColab hidrometroColab;
    
    public AdaptadorHidrometroColab(HidrometroColab hidrometroColab) 
    {
        this.hidrometroColab = hidrometroColab;
    }
    
    @Override
    public String lerValorDigital(Mat imagem) throws TesseractException 
    {
        Rect regiao = hidrometroColab.obterRegiaDoDisplay(imagem);
        
        Mat cinza = new Mat();
        Imgproc.cvtColor(imagem, cinza, Imgproc.COLOR_BGR2GRAY);
        Mat recorte = new Mat(cinza, regiao);
        
        return hidrometroColab.extrairDigitos(recorte);
    }
    
    @Override
    public List<LeituraPonteiro> lerPonteiros(Mat imagem) 
    {
        List<LeituraPonteiro> leituras = new ArrayList<>();
        List<Circulo> mostradores = hidrometroColab.identificarMostradores(imagem);
        
        for (Circulo mostrador : mostradores) 
        {
            double angulo = AnalisadorPonteiro.detectarAngulo(imagem, 
                mostrador.centro, mostrador.raio);
            int valor = AnalisadorPonteiro.anguloParaValor(angulo);
            leituras.add(new LeituraPonteiro(valor, angulo, mostrador.centro));
        }
        
        return leituras;
    }
}