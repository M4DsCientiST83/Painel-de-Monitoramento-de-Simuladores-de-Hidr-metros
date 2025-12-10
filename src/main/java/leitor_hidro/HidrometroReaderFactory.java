package src.main.java.leitor_hidro;

import src.main.java.facade.TipoPasta;

public class HidrometroReaderFactory {
    public static IHidrometroReader criarLeitor(TipoPasta tipo) {
        if (tipo == TipoPasta.COLABORADOR) {
            return new AdaptadorHidrometroColab(new HidrometroColab());
        } else if (tipo == TipoPasta.RODRIGUES) {
            return new AdaptadorHidrometroRodri(new HidrometroRodri());
        }
        throw new IllegalArgumentException("Tipo de pasta desconhecido");
    }
}
