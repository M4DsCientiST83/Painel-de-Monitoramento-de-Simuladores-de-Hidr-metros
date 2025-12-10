package facade;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class MonitoradorPastas 
{
    private WatchService watchService;
    private Map<WatchKey, Path> keyPathMap;
    private PainelFacade painel;
    private boolean rodando;

    public MonitoradorPastas(PainelFacade painel) throws IOException 
    {
        this.painel = painel;
        this.watchService = FileSystems.getDefault().newWatchService();
        this.keyPathMap = new HashMap<>();
        this.rodando = false;
    }

    public void adicionarPasta(String caminhoPasta, TipoPasta tipo) throws IOException 
    {
        Path path = Paths.get(caminhoPasta);

        if (!Files.exists(path)) 
        {
            Files.createDirectories(path);
            System.out.println("✓ Pasta criada: " + caminhoPasta);
        }

        WatchKey key = path.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY
        );

        keyPathMap.put(key, path);
        System.out.println("Monitorando pasta " + tipo + ": " + caminhoPasta);
    }

    public void iniciarMonitoramento() 
    {
        rodando = true;
        System.out.println("\nSISTEMA DE MONITORAMENTO ATIVO");

        while (rodando) 
        {
            WatchKey key;

            try 
            {
                key = watchService.take();
            } 
            catch (InterruptedException e) 
            {
                System.err.println("Monitoramento interrompido");
                break;
            }

            Path pasta = keyPathMap.get(key);

            for (WatchEvent<?> event : key.pollEvents()) 
            {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW) 
                {
                    continue;
                }

                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path nomeArquivo = ev.context();
                Path caminhoCompleto = pasta.resolve(nomeArquivo);

                if (isImagemValida(nomeArquivo.toString())) 
                {
                    processarNovaImagem(caminhoCompleto.toString(), pasta.toString());
                }
            }

            boolean valid = key.reset();

            if (!valid) 
            {
                keyPathMap.remove(key);
                if (keyPathMap.isEmpty()) 
                {
                    break;
                }
            }
        }
    }

    public void pararMonitoramento() 
    {
        rodando = false;
        try 
        {
            watchService.close();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    private void processarNovaImagem(String caminhoImagem, String pasta) 
    {
        try 
        {
            Thread.sleep(500);
        } 
        catch (InterruptedException e) 
        {
            e.printStackTrace();
        }

        ResultadoLeitura resultado;

        if (pasta.contains("colaborador")) 
        {
            resultado = painel.processarImagemColaborador(caminhoImagem);
        } 
        else if (pasta.contains("199911250009")) 
        {
            resultado = painel.processarImagemRodrigues(caminhoImagem);
        } 
        else 
        {
            System.err.println("Pasta não reconhecida: " + pasta);
            return;
        }

        painel.exibirResultado(resultado);
    }

    private boolean isImagemValida(String nomeArquivo) 
    {
        String lower = nomeArquivo.toLowerCase();
        return lower.endsWith(".jpg") || 
               lower.endsWith(".jpeg") || 
               lower.endsWith(".png") || 
               lower.endsWith(".bmp");
    }
}
