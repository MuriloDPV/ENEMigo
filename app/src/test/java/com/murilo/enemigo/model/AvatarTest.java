package com.murilo.enemigo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.murilo.enemigo.model.Avatar.Parte;

import org.junit.Test;

import java.util.Random;

public class AvatarTest {

    @Test
    public void partes_comAsOpcoesPedidas() {
        assertTrue("tons de pele variados", Parte.PELE.opcoes >= 6);
        assertEquals(3, Parte.ROSTO.opcoes);
        // Nenhum, óculos, boné e fone
        assertEquals(4, Parte.ACESSORIO.opcoes);
    }

    @Test
    public void padrao_temTodasAsEscolhasValidas() {
        Avatar padrao = Avatar.padrao();
        for (Parte parte : Parte.values()) {
            int escolha = padrao.get(parte);
            assertTrue(parte + " fora da faixa", escolha >= 0 && escolha < parte.opcoes);
        }
        assertEquals("o padrão não tem acessório", 0, padrao.get(Parte.ACESSORIO));
    }

    @Test
    public void com_trocaSoAParteEscolhida() {
        Avatar padrao = Avatar.padrao();
        Avatar novo = padrao.com(Parte.CABELO, 4);
        assertEquals(4, novo.get(Parte.CABELO));
        for (Parte parte : Parte.values()) {
            if (parte != Parte.CABELO) {
                assertEquals(parte.toString(), padrao.get(parte), novo.get(parte));
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void com_opcaoQueNaoExiste_falha() {
        Avatar.padrao().com(Parte.ROSTO, 3);
    }

    @Test
    public void com_naoMudaOOriginal() {
        Avatar padrao = Avatar.padrao();
        padrao.com(Parte.FUNDO, 5);
        assertEquals(Avatar.padrao(), padrao);
    }

    @Test
    public void texto_idaEVolta() {
        Avatar avatar = Avatar.padrao().com(Parte.PELE, 7).com(Parte.OLHOS, 3).com(Parte.ACESSORIO, 2);
        assertEquals(avatar, Avatar.deTexto(avatar.paraTexto()));
    }

    @Test
    public void texto_ehAsEscolhasSeparadasPorVirgula() {
        Avatar avatar = Avatar.padrao().com(Parte.PELE, 1).com(Parte.ROSTO, 2).com(Parte.CABELO, 3)
                .com(Parte.COR_CABELO, 4).com(Parte.OLHOS, 1).com(Parte.ACESSORIO, 3).com(Parte.FUNDO, 6);
        assertEquals("1,2,3,4,1,3,6", avatar.paraTexto());
    }

    @Test
    public void deTexto_nadaSalvo_viraOPadrao() {
        assertEquals(Avatar.padrao(), Avatar.deTexto(null));
        assertEquals(Avatar.padrao(), Avatar.deTexto(""));
    }

    @Test
    public void deTexto_quantidadeErradaOuLixo_viraOPadrao() {
        assertEquals(Avatar.padrao(), Avatar.deTexto("1,2,3"));
        assertEquals(Avatar.padrao(), Avatar.deTexto("a,b,c,d,e,f,g"));
    }

    @Test
    public void deTexto_valorForaDaFaixa_usaOPadraoSoNaquelaParte() {
        // Pele 99 não existe (ex.: versão antiga com mais tons): só a pele volta ao padrão.
        Avatar lido = Avatar.deTexto("99,1,2,3,1,1,4");
        assertEquals(Avatar.padrao().get(Parte.PELE), lido.get(Parte.PELE));
        assertEquals(1, lido.get(Parte.ROSTO));
        assertEquals(4, lido.get(Parte.FUNDO));
    }

    @Test
    public void sortear_ficaDentroDasFaixas() {
        Random sorteio = new Random(42);
        for (int i = 0; i < 300; i++) {
            Avatar avatar = Avatar.sortear(sorteio);
            for (Parte parte : Parte.values()) {
                int escolha = avatar.get(parte);
                assertTrue(parte + " sorteado fora da faixa: " + escolha, escolha >= 0 && escolha < parte.opcoes);
            }
        }
    }

    @Test
    public void sortear_naoDaSempreOMesmo() {
        Random sorteio = new Random(7);
        Avatar primeiro = Avatar.sortear(sorteio);
        boolean mudou = false;
        for (int i = 0; i < 20 && !mudou; i++) {
            mudou = !primeiro.equals(Avatar.sortear(sorteio));
        }
        assertTrue(mudou);
    }

    @Test
    public void iguais_peloConteudo() {
        assertEquals(Avatar.padrao(), Avatar.padrao());
        assertEquals(Avatar.padrao().hashCode(), Avatar.padrao().hashCode());
        assertNotEquals(Avatar.padrao(), Avatar.padrao().com(Parte.OLHOS, 2));
    }
}
