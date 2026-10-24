package com.murilo.enemigo.ui;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mascote, medalhas e peças do avatar usam só cores com nome (@color/...), nunca um hex fixo
 * no desenho. Assim eles acompanham a troca de marca e o modo escuro.
 */
public class CoresDaMarcaTest {

    private static final Path DRAWABLE = Paths.get("src", "main", "res", "drawable");
    private static final String[] PREFIXOS = {"mascote_", "medalha_", "avatar_"};
    private static final Pattern HEX = Pattern.compile("#[0-9A-Fa-f]{3,8}\\b");

    @Test
    public void desenhosUsamSoCoresComNome() throws IOException {
        List<String> problemas = new ArrayList<>();
        int conferidos = 0;
        try (DirectoryStream<Path> arquivos = Files.newDirectoryStream(DRAWABLE, "*.xml")) {
            for (Path arquivo : arquivos) {
                String nome = arquivo.getFileName().toString();
                if (!comecaComPrefixo(nome)) {
                    continue;
                }
                conferidos++;
                String xml = new String(Files.readAllBytes(arquivo), StandardCharsets.UTF_8);
                Matcher m = HEX.matcher(xml);
                if (m.find()) {
                    problemas.add(nome + " usa " + m.group());
                }
            }
        }
        assertTrue("nenhum desenho conferido: o caminho mudou?", conferidos > 0);
        assertTrue("cores fixas em vez de @color: " + problemas, problemas.isEmpty());
    }

    private static boolean comecaComPrefixo(String nome) {
        for (String prefixo : PREFIXOS) {
            if (nome.startsWith(prefixo)) {
                return true;
            }
        }
        return false;
    }
}
