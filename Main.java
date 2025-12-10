import facade.MonitoradorPastas;
import facade.PainelFacade;
import facade.TipoPasta;

public class Main 
{
    public static void main(String[] args) 
    {
        try 
        {
            PainelFacade painel = new PainelFacade();
            MonitoradorPastas monitorador = new MonitoradorPastas(painel);

            //String pastaColaborador = "imagens/colaborador";
            String pastaRodrigues = "C:/HidroRodri/Simulador-Hidrometro/Medicoes_199911250009";

            //monitorador.adicionarPasta(pastaColaborador, TipoPasta.COLABORADOR);
            monitorador.adicionarPasta(pastaRodrigues, TipoPasta.RODRIGUES);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {

                System.out.println("\n\nEncerrando sistema...");
                monitorador.pararMonitoramento();
            }));

            monitorador.iniciarMonitoramento();

        } 
        catch (Exception e) 
        {
            System.err.println("Erro ao iniciar sistema:");
            e.printStackTrace();
        }
    }
} 