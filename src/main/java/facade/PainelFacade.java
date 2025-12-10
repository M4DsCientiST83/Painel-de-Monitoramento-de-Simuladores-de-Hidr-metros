package src.main.java.facade;

import src.main.java.leitor_hidro.*;
import src.main.java.util.LeituraPonteiro;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import java.util.List;

public class PainelFacade implements ArquivoObserver {
    public PainelFacade() {
    }

    @Override
    public void onNovoArquivo(String caminhoImagem, TipoPasta tipo) {
        try {
            String nomeTipo = (tipo == TipoPasta.COLABORADOR) ? "Colaborador" : "Rodrigues";
            System.out.println("\n[" + nomeTipo.toUpperCase() + "] Processando");

            IHidrometroReader leitor = HidrometroReaderFactory.criarLeitor(tipo);
            ResultadoLeitura resultado = processarImagem(caminhoImagem, leitor, nomeTipo);

            exibirResultado(resultado);
        } catch (Exception e) {
            System.err.println("Erro ao processar arquivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ResultadoLeitura processarImagem(String caminhoImagem, IHidrometroReader leitor, String tipo) {
        try {
            java.io.File f = new java.io.File(caminhoImagem);
            System.out.println(
                    "Lendo arquivo: " + caminhoImagem + " | Size: " + f.length() + " | Mod: " + f.lastModified());
            Mat imagem = Imgcodecs.imread(caminhoImagem);

            if (imagem.empty()) {
                return new ResultadoLeitura(false, "Erro ao carregar imagem", null, null, tipo);
            }

            String valorDigital = leitor.lerValorDigital(imagem);
            List<LeituraPonteiro> ponteiros = leitor.lerPonteiros(imagem);

            return new ResultadoLeitura(true, "Sucesso", valorDigital, ponteiros, tipo);
        } catch (Exception e) {
            return new ResultadoLeitura(false, "Erro: " + e.getMessage(), null, null, tipo);
        }
    }

    public void exibirResultado(ResultadoLeitura resultado) {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║  PAINEL - LEITURA DE HIDRÔMETRO      ║");
        System.out.println("╠═══════════════════════════════════════╣");
        System.out.println("║ Tipo: " + resultado.tipo);
        System.out.println("║ Status: " + (resultado.sucesso ? "✓ SUCESSO" : "✗ FALHA"));

        if (resultado.sucesso) {
            System.out.println("║ Consumo Digital: " + resultado.valorDigital + " m³");
            System.out.println("║");
            System.out.println("║ Ponteiros Analógicos:");

            if (resultado.ponteiros != null && !resultado.ponteiros.isEmpty()) {
                // User requested to ignore clocks in output
                System.out.println("║   (Leitura de ponteiros processada internamente)");
                /*
                 * for (int i = 0; i < resultado.ponteiros.size(); i++) {
                 * System.out.println("║   → " + resultado.ponteiros.get(i));
                 * }
                 */
            } else {
                System.out.println("║   (Nenhum ponteiro detectado)");
            }
        } else {
            System.out.println("║ Mensagem: " + resultado.mensagem);
        }

        System.out.println("╚═══════════════════════════════════════╝");
    }
}