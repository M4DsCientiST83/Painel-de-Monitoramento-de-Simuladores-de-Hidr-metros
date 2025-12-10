package src.main.java.leitor_hidro;

import src.main.java.util.Circulo;
import src.main.java.util.UtilOCR;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;


public class HidrometroColab 
{
    
    public Rect obterRegiaDoDisplay(Mat imagem) 
    {
        int altura = imagem.rows();
        int largura = imagem.cols();
        
        return new Rect(
            largura / 4,
            altura / 8,
            largura / 2,
            altura / 5
        );
    }
    
    public String extrairDigitos(Mat regiao)
    {
        Mat processada = preprocessar(regiao);
        return UtilOCR.lerTextoFromMat(processada);
    }
    
    public List<Circulo> identificarMostradores(Mat imagem) 
    {
        List<Circulo> mostradores = new ArrayList<>();
        
        Mat cinza = new Mat();
        Imgproc.cvtColor(imagem, cinza, Imgproc.COLOR_BGR2GRAY);
        
        Mat circulos = new Mat();
        Imgproc.HoughCircles(
            cinza, circulos, Imgproc.HOUGH_GRADIENT, 1,
            cinza.rows() / 8, 100, 30, 15, 50
        );
        
        for (int i = 0; i < circulos.cols(); i++) 
        {
            double[] c = circulos.get(0, i);
            mostradores.add(new Circulo(
                new Point(Math.round(c[0]), Math.round(c[1])),
                (int) Math.round(c[2])
            ));
        }
        
        return mostradores;
    }
    
    private Mat preprocessar(Mat regiao) 
    {
        Mat binaria = new Mat();
        Imgproc.threshold(regiao, binaria, 0, 255, 
            Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        return binaria;
    }
    
    public String realizarOCR(String caminhoImagem) 
    {
        try 
        {
            return UtilOCR.lerTexto(caminhoImagem);
        } 
        catch (Exception e) 
        {
            System.err.println("Erro ao realizar OCR (Colaborador): " + e.getMessage());
            return "";
        }
    }
}
