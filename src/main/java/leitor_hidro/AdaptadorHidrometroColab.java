package src.main.java.leitor_hidro;

import src.main.java.util.AnalisadorPonteiro;
import src.main.java.util.Circulo;
import src.main.java.util.LeituraPonteiro;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
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
    public String lerValorDigital(Mat imagem)
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
