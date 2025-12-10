package src.main.java.leitor_hidro;

import src.main.java.util.Circulo;
import src.main.java.util.UtilOCR;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

public class HidrometroRodri {

    public Rect obterAreaDisplay(Mat imagem) {
        int altura = imagem.rows();
        int largura = imagem.cols();

        return new Rect(
                (int) (largura * 0.3),
                (int) (altura * 0.30), // Deslocado para baixo (era 0.15)
                (int) (largura * 0.4),
                (int) (altura * 0.30) // Aumentado altura para garantir (era 0.15)
        );
    }

    public String reconhecerNumeros(Mat regiao) {
        Mat processada = aplicarPreProcessamento(regiao);
        return UtilOCR.lerTextoFromMat(processada);
    }

    public List<Circulo> detectarCirculosPonteiros(Mat imagem) {
        List<Circulo> circulos = new ArrayList<>();

        Mat cinza = new Mat();
        Imgproc.cvtColor(imagem, cinza, Imgproc.COLOR_BGR2GRAY);

        Mat detectados = new Mat();
        Imgproc.HoughCircles(
                cinza, detectados, Imgproc.HOUGH_GRADIENT, 1,
                cinza.rows() / 6, 120, 35, 20, 60);

        for (int i = 0; i < detectados.cols(); i++) {
            double[] c = detectados.get(0, i);
            circulos.add(new Circulo(
                    new Point(Math.round(c[0]), Math.round(c[1])),
                    (int) Math.round(c[2])));
        }

        return circulos;
    }

    private Mat aplicarPreProcessamento(Mat regiao) {
        // Don't binarize here, UtilOCR does its own Adaptive Thresholding.
        // Just enhance and denoise.
        Mat cinza = new Mat();
        if (regiao.channels() > 1) {
            Imgproc.cvtColor(regiao, cinza, Imgproc.COLOR_BGR2GRAY);
        } else {
            cinza = regiao.clone();
        }

        // Remove high frequency noise (salt and pepper)
        Mat semRuido = new Mat();
        Imgproc.medianBlur(cinza, semRuido, 3);

        // Increase contrast ?? Optional. Let's trust UtilOCR's adaptive threshold for
        // now.
        return semRuido;
    }

    public String fazerOCR(String caminhoImagem) {
        try {
            return UtilOCR.lerTexto(caminhoImagem);
        } catch (Exception e) {
            System.err.println("Erro ao fazer OCR (Rodri): " + e.getMessage());
            return "";
        }
    }
}
