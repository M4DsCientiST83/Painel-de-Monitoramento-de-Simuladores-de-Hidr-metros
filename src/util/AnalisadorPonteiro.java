package util;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class AnalisadorPonteiro 
{
    
    public static double detectarAngulo(Mat imagem, Point centro, int raio) 
    {
        int tamanhoROI = (int)(raio * 1.5);
        int x = Math.max(0, (int)(centro.x - tamanhoROI));
        int y = Math.max(0, (int)(centro.y - tamanhoROI));
        int w = Math.min(imagem.cols() - x, tamanhoROI * 2);
        int h = Math.min(imagem.rows() - y, tamanhoROI * 2);
        
        Rect roi = new Rect(x, y, w, h);
        Mat regiaPonteiro = new Mat(imagem, roi);
        
        Mat hsv = new Mat();
        Imgproc.cvtColor(regiaPonteiro, hsv, Imgproc.COLOR_BGR2HSV);
        Mat mascaraVermelha = detectarCorVermelha(hsv);
        
        Mat linhas = new Mat();
        Imgproc.HoughLinesP(mascaraVermelha, linhas, 1, Math.PI / 180, 20, 10, 5);
        
        if (linhas.rows() > 0) {
            double[] linha = linhas.get(0, 0);
            return Math.toDegrees(Math.atan2(linha[3] - linha[1], linha[2] - linha[0]));
        }
        
        return 0;
    }
    
    public static int anguloParaValor(double angulo) 
    {
        angulo = (angulo + 90 + 360) % 360;
        return (int)((angulo / 360.0) * 10) % 10;
    }
    
    private static Mat detectarCorVermelha(Mat hsv) 
    {
        Mat mascara1 = new Mat();
        Core.inRange(hsv, new Scalar(0, 100, 100), new Scalar(10, 255, 255), mascara1);
        
        Mat mascara2 = new Mat();
        Core.inRange(hsv, new Scalar(160, 100, 100), new Scalar(180, 255, 255), mascara2);
        
        Mat resultado = new Mat();
        Core.addWeighted(mascara1, 1.0, mascara2, 1.0, 0.0, resultado);
        return resultado;
    }
}