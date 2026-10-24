package com.murilo.enemigo.ui;

import static org.junit.Assert.assertEquals;

import com.murilo.enemigo.model.Avatar.Parte;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cada opção do model/Avatar precisa ter a sua peça (desenho) ou a sua cor. Se alguém criar um
 * cabelo novo e esquecer de aumentar as opções (ou o contrário), este teste avisa.
 */
public class PecasDoAvatarTest {

    private static final Path RES = Paths.get("src", "main", "res");

    @Test
    public void cadaFormaTemOSeuDesenho() throws IOException {
        assertEquals(Parte.ROSTO.opcoes, contarDesenhos("avatar_rosto_*.xml"));
        assertEquals(Parte.OLHOS.opcoes, contarDesenhos("avatar_olhos_*.xml"));
        // Os cabelos da frente; a parte de trás de alguns termina em _tras.
        assertEquals(Parte.CABELO.opcoes, contarDesenhos("avatar_cabelo_*.xml") - contarDesenhos("avatar_cabelo_*_tras.xml"));
        // "Nenhum" não tem desenho.
        assertEquals(Parte.ACESSORIO.opcoes - 1, contarDesenhos("avatar_acessorio_*.xml"));
    }

    @Test
    public void cadaCorTemONome() throws IOException {
        String cores = new String(Files.readAllBytes(RES.resolve("values/colors.xml")), StandardCharsets.UTF_8);
        assertEquals(Parte.PELE.opcoes, contar(cores, "avatar_pele_\\d+"));
        assertEquals(Parte.COR_CABELO.opcoes, contar(cores, "avatar_cabelo_\\d+"));
        assertEquals(Parte.FUNDO.opcoes, contar(cores, "avatar_fundo_\\d+"));
    }

    private static int contarDesenhos(String padrao) throws IOException {
        int total = 0;
        try (DirectoryStream<Path> arquivos = Files.newDirectoryStream(RES.resolve("drawable"), padrao)) {
            for (Path ignorado : arquivos) {
                total++;
            }
        }
        return total;
    }

    private static int contar(String xml, String nome) {
        Matcher m = Pattern.compile("<color name=\"" + nome + "\">").matcher(xml);
        int total = 0;
        while (m.find()) {
            total++;
        }
        return total;
    }
}
