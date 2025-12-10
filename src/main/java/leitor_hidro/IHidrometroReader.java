package src.main.java.leitor_hidro;

import src.main.java.util.LeituraPonteiro;
import org.opencv.core.Mat;
import java.util.List;

public interface IHidrometroReader {
    String lerValorDigital(Mat imagem);

    List<LeituraPonteiro> lerPonteiros(Mat imagem);
}
