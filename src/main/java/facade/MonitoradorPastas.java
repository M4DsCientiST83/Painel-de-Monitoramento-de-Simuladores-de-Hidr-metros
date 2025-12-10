package src.main.java.facade;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class MonitoradorPastas {
    private WatchService watchService;
    private Map<WatchKey, Path> keyPathMap;
    private Map<WatchKey, TipoPasta> keyTypeMap;
    private List<ArquivoObserver> observadores;
    private boolean rodando;
    private Map<String, Long> lastProcessed;
    private static final long DEBOUNCE_DELAY = 2000;

    private java.util.concurrent.ExecutorService processingQueue;

    public MonitoradorPastas() throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.keyPathMap = new HashMap<>();
        this.keyTypeMap = new HashMap<>();
        this.observadores = new ArrayList<>();
        this.rodando = false;
        this.lastProcessed = new HashMap<>();
        this.processingQueue = java.util.concurrent.Executors.newSingleThreadExecutor();
    }

    public void adicionarObservador(ArquivoObserver observador) {
        this.observadores.add(observador);
    }

    public void adicionarPasta(String caminhoPasta, TipoPasta tipo) throws IOException {
        Path path = Paths.get(caminhoPasta);

        if (!Files.exists(path)) {
            Files.createDirectories(path);
            System.out.println("✓ Pasta criada: " + caminhoPasta);
        }

        WatchKey key = path.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        keyPathMap.put(key, path);
        keyTypeMap.put(key, tipo);
        System.out.println("Monitorando pasta " + tipo + ": " + caminhoPasta);
    }

    public void iniciarMonitoramento() {
        rodando = true;
        System.out.println("\nSISTEMA DE MONITORAMENTO ATIVO");

        while (rodando) {
            WatchKey key;

            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                System.err.println("Monitoramento interrompido");
                break;
            }

            Path pasta = keyPathMap.get(key);
            TipoPasta tipo = keyTypeMap.get(key);

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path nomeArquivo = ev.context();
                Path caminhoCompleto = pasta.resolve(nomeArquivo);

                if (isImagemValida(nomeArquivo.toString())) {
                    long now = System.currentTimeMillis();
                    Long lastTime = lastProcessed.get(caminhoCompleto.toString());

                    // Debounce check
                    if (lastTime == null || (now - lastTime > DEBOUNCE_DELAY)) {
                        lastProcessed.put(caminhoCompleto.toString(), now);

                        // Submit to queue instead of blocking
                        processingQueue.submit(() -> {
                            notificarObservadores(caminhoCompleto.toString(), tipo);
                        });
                    }
                }
            }

            boolean valid = key.reset();

            if (!valid) {
                keyPathMap.remove(key);
                keyTypeMap.remove(key);
                if (keyPathMap.isEmpty()) {
                    break;
                }
            }
        }
    }

    public void pararMonitoramento() {
        rodando = false;
        try {
            watchService.close();
            processingQueue.shutdownNow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notificarObservadores(String caminhoImagem, TipoPasta tipo) {
        try {
            System.out.println("Detectado novo arquivo: " + caminhoImagem);
            System.out.println("Aguardando estabilização do arquivo...");
            Thread.sleep(5500); // Aguarda 5.5 segundos (respeitando intervalo de 5s + buffer)
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        for (ArquivoObserver obs : observadores) {
            obs.onNovoArquivo(caminhoImagem, tipo);
        }
    }

    private boolean isImagemValida(String nomeArquivo) {
        String lower = nomeArquivo.toLowerCase();
        return lower.endsWith(".jpg") ||
                lower.endsWith(".jpeg") ||
                lower.endsWith(".png") ||
                lower.endsWith(".bmp");
    }
}
