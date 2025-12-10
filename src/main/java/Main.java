package src.main.java;

import src.main.java.facade.MonitoradorPastas;
import src.main.java.facade.PainelFacade;
import src.main.java.facade.TipoPasta;
import org.opencv.core.Core;

public class Main {
    public static void main(String[] args) {
        System.out.println("!!! VERSION CHECK: DEBUG MODE ENABLED - FALLBACK LOGIC ACTIVE !!!");
        try {
            // Carrega a biblioteca nativa do OpenCV (opencv_java4100.dll deve estar no
            // java.library.path)
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

            PainelFacade painel = new PainelFacade();
            MonitoradorPastas monitorador = new MonitoradorPastas();
            monitorador.adicionarObservador(painel);

            // String pastaColaborador = "imagens/colaborador";
            String pastaRodrigues = "C:/HidroRodri/Simulador-Hidrometro/Medicoes_199911250009";

            // monitorador.adicionarPasta(pastaColaborador, TipoPasta.COLABORADOR);
            monitorador.adicionarPasta(pastaRodrigues, TipoPasta.RODRIGUES);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {

                System.out.println("\n\nEncerrando sistema...");
                monitorador.pararMonitoramento();
            }));

            monitorador.iniciarMonitoramento();

        } catch (Exception e) {
            System.err.println("Erro ao iniciar sistema:");
            e.printStackTrace();
        }
    }
}