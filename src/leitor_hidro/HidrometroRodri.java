package leitor_hidro;

import util.Circulo;
import util.UtilOCR;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import net.sourceforge.tess4j.TesseractException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class HidrometroRodri 
{
    
    public Rect obterAreaDisplay(Mat imagem) 
    {
        int altura = imagem.rows();
        int largura = imagem.cols();
        
        return new Rect(
            (int)(largura * 0.3),
            (int)(altura * 0.15),
            (int)(largura * 0.4),
            (int)(altura * 0.15)
        );
    }
    
    public String reconhecerNumeros(Mat regiao) throws TesseractException 
    {
        Mat processada = aplicarPreProcessamento(regiao);
        return fazerOCR(processada);
    }
    
    public List<Circulo> detectarCirculosPonteiros(Mat imagem) 
    {
        List<Circulo> circulos = new ArrayList<>();
        
        Mat cinza = new Mat();
        Imgproc.cvtColor(imagem, cinza, Imgproc.COLOR_BGR2GRAY);
        
        Mat detectados = new Mat();
        Imgproc.HoughCircles(
            cinza, detectados, Imgproc.HOUGH_GRADIENT, 1,
            cinza.rows() / 6, 120, 35, 20, 60
        );
        
        for (int i = 0; i < detectados.cols(); i++) 
        {
            double[] c = detectados.get(0, i);
            circulos.add(new Circulo(
                new Point(Math.round(c[0]), Math.round(c[1])),
                (int) Math.round(c[2])
            ));
        }
        
        return circulos;
    }
    
    private Mat aplicarPreProcessamento(Mat regiao) 
    {
        Mat binaria = new Mat();
        Imgproc.threshold(regiao, binaria, 0, 255, 
            Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        
        Mat semRuido = new Mat();
        Imgproc.medianBlur(binaria, semRuido, 3);
        
        return semRuido;
    }
    
    public String fazerOCR(String caminhoImagem) 
    {
        try 
        {
            return UtilOCR.lerTexto(caminhoImagem);
        } 
        catch (Exception e) 
        {
            System.err.println("Erro ao fazer OCR (Rodri): " + e.getMessage());
            return "";
        }
    }
}