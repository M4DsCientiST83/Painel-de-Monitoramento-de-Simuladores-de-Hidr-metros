package leitor_hidro;

import util.LeituraPonteiro;
import org.opencv.core.Mat;
import net.sourceforge.tess4j.TesseractException;
import java.util.List;

public interface IHidrometroReader 
{
    String lerValorDigital(Mat imagem) throws TesseractException;
    List<LeituraPonteiro> lerPonteiros(Mat imagem);
}
