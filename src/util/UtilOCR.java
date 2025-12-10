package util;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;
import java.util.Map;

public class UtilOCR 
{
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // Mapa com padrões de segmentos (a b c d e f g)
    private static final Map<String, Integer> mapa = new HashMap<>();

    static {
        mapa.put("1111110", 0);
        mapa.put("0110000", 1);
        mapa.put("1101101", 2);
        mapa.put("1111001", 3);
        mapa.put("0110011", 4);
        mapa.put("1011011", 5);
        mapa.put("1011111", 6);
        mapa.put("1110000", 7);
        mapa.put("1111111", 8);
        mapa.put("1111011", 9);
    }

    // ======== MÉTODO PRINCIPAL USADO PELO PAINEL ========
    public static String lerTexto(String caminhoImagem)
    {
        Mat img = Imgcodecs.imread(caminhoImagem);

        if (img.empty()) 
            return "";

        // ---- RECORTES DOS DÍGITOS ----
        // Ajuste fino pode ser necessário, mas isto funciona para maioria dos displays horizontais centrais.

        int w = img.width();
        int h = img.height();

        // Região total do display (central + superior)
        Rect display = new Rect(
                w/2 - 180,     // x
                h/2 - 120,     // y
                360,           // largura total
                140            // altura total
        );

        Mat disp = img.submat(display);

        // Cada dígito possui 60px de largura aproximadamente
        int digW = 60;
        int digH = 120;

        // 4 inteiros + "." + 2 decimais
        int[] xs = { 0, 60, 120, 180, 260, 320 }; // 6 posições úteis (pulando onde ficaria o ponto)

        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < 6; i++)
        {
            Mat dig = disp.submat(new Rect(xs[i], 0, digW, digH));

            int valor = reconhecerDigito(dig);

            if (i == 4) 
                resultado.append(".");  // insere decimal automaticamente

            resultado.append(valor);
        }

        return resultado.toString();
    }


    // ======== RECONHECER DÍGITO VIA SEGMENTOS ========
    private static int reconhecerDigito(Mat dig)
    {
        // 1. cinza
        Mat gray = new Mat();
        Imgproc.cvtColor(dig, gray, Imgproc.COLOR_BGR2GRAY);

        // 2. binário
        Mat bin = new Mat();
        Imgproc.threshold(gray, bin, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

        // 3. detectar segmentos
        boolean a = ligado(bin, new Rect(15, 5, 30, 15));
        boolean b = ligado(bin, new Rect(45, 20, 10, 30));
        boolean c = ligado(bin, new Rect(45, 65, 10, 30));
        boolean d = ligado(bin, new Rect(15, 95, 30, 15));
        boolean e = ligado(bin, new Rect(5, 65, 10, 30));
        boolean f = ligado(bin, new Rect(5, 20, 10, 30));
        boolean g = ligado(bin, new Rect(15, 50, 30, 10));

        String chave = 
            (a?"1":"0") +
            (b?"1":"0") +
            (c?"1":"0") +
            (d?"1":"0") +
            (e?"1":"0") +
            (f?"1":"0") +
            (g?"1":"0");

        return mapa.getOrDefault(chave, 0); // fallback: 0
    }


    private static boolean ligado(Mat bin, Rect r)
    {
        Mat sub = bin.submat(r);
        double media = Core.mean(sub).val[0];
        return media > 100; // limiar simples
    }
}