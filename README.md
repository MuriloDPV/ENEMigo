# ENEMigo

Um app Android no estilo Duolingo que, em vez de ensinar idiomas, treina para o ENEM com **questões reais de provas anteriores**, em lições curtas e diárias.

Feito como projeto da disciplina de Programação do meu curso técnico, em Java + XML no Android Studio.

## Como funciona

- O conteúdo é dividido nas áreas do ENEM, cada uma com uma **trilha de lições**.
- Cada lição tem **de 3 a 5 questões** (A a E) e leva uns **4 minutos**; a trilha mostra o tempo estimado ("~4 min"). Você responde, aperta **VERIFICAR** e vê na hora se acertou, com uma explicação curta.
- A próxima lição só abre quando você acerta pelo menos **60%** da anterior (2 de 3, 3 de 4, 3 de 5).
- Tudo funciona **offline**: as questões ficam dentro do app e o progresso fica salvo no celular.

## Funcionalidades

- Primeiro acesso com nome, meta diária de XP (20, 50 ou 100) e avatar (dá para pular)
- A **Lia**, mascote do app (uma raposa esperta, que fareja a pegadinha do enunciado), com 5 expressões e animações: aparece no primeiro acesso, no feedback do quiz, no resultado, no caderno de erros vazio e quando as vidas acabam
- **Avatar** montável: tom de pele, formato do rosto, cabelo e cor do cabelo, olhos, acessório (óculos, boné, fone) e cor de fundo, com prévia ao vivo e botão de sortear; aparece no Perfil e no Início
- **8 conquistas** com medalhas: primeira lição, ofensiva de 3 e de 7 dias, 100 e 500 XP, lição perfeita e duas de Matemática (concluir uma lição e concluir todas). No Perfil, as bloqueadas ficam em cinza
- **Celebrações em tela cheia**, com confete: lição perfeita, ofensiva aumentada, nível novo e conquista nova
- Sons de acerto, erro e lição concluída, e vibração curta ao errar
- Configurações: editar o nome, trocar a meta e o tamanho da letra, ligar ou desligar som, vibração e o lembrete (e escolher o horário dele) e resetar o progresso (com confirmação)
- Trilhas por área com lições concluídas, disponíveis e bloqueadas
- Quiz com feedback imediato e explicação de cada questão
- **Leitura no celular**: questões curtas, letra em 3 tamanhos (botão "Aa" no quiz ou nas Configurações, fica salva) e texto-base recolhível: depois de ler, "Recolher texto" deixa só o comando e as alternativas na tela, e um toque na faixa abre o texto de novo
- Figuras e fórmulas nas questões de Matemática, com zoom ao tocar na figura
- XP e níveis (+10 XP por acerto, +5 de bônus na lição perfeita, com 100% de acertos)
- 5 vidas: cada erro tira uma, e uma volta a cada 30 minutos
- Ofensiva diária (dias seguidos estudando) e meta diária
- **Lembrete diário** por notificação (padrão 19h), só nos dias em que você ainda não estudou: a Lia avisa quando a ofensiva está em risco, numa frase diferente a cada dia. Funciona offline e continua depois de reiniciar o celular (WorkManager). No Android 13+, a permissão é pedida depois da primeira lição concluída, com explicação antes
- Caderno de erros: as questões que você errou vão para a aba Revisão, e acertar lá devolve uma vida
- Perfil com nível, XP, ofensiva, recorde, lições concluídas, taxa de acerto, tempo médio por questão e minutos estudados hoje (o tempo é medido no quiz, só com a tela aberta)
- Depois de 10 questões, o tempo da trilha se ajusta ao seu ritmo
- Visual "caderno de estudo", com modo claro e escuro (segue o tema do celular)

## Visual

- Tema DayNight: as cores mudam sozinhas com o modo escuro do celular.
- Interface em **Plus Jakarta Sans** e texto-base das questões em **Literata**, as duas com licença OFL e guardadas dentro do app (funcionam offline).
- Botões e cartões "em relevo", com uma sombra sólida embaixo, que afundam ao tocar (`ui/componente/`).
- Trilha em zigue-zague, com a lição atual pulsando e as lições agrupadas em unidades.
- O texto-base fica num cartão de papel, com a margem de caderno. No modo escuro esse papel, e as alternativas com figura ou fórmula, continuam claros: as figuras do ENEM são desenhadas com "multiplicar" e as fórmulas têm tinta escura, então sumiriam num fundo escuro. O teste `PapelModoEscuroTest` confere isso.

## Mascote, avatar, conquistas e sons

Tudo foi feito para o app, sem nada baixado da internet:

- **Lia** (`res/drawable/mascote_*.xml`): VectorDrawables das 5 expressões (neutro, feliz, comemorando, triste e pensando) e da pose sem vidas. O `MascoteView` troca a expressão e anima com os animators do Android: balançar, pular, comemorar e tremer. Sem vidas, ela fica de orelhas caídas e desbotada. Os desenhos, o ícone e o logo saem de `tools/gerar_mascote.py`: para mudar a Lia, edite o script e rode `python tools/gerar_mascote.py`.
- **Avatar** (`res/drawable/avatar_*.xml`): 24 peças vetoriais no mesmo quadro de 120x120. Pele e cabelo são peças brancas que recebem a cor com tint; o `AvatarDrawable` empilha as peças e recorta num círculo. O avatar é salvo como texto no SharedPreferences (`model/Avatar`).
- **Medalhas** (`res/drawable/medalha_*.xml`): uma para cada conquista. As condições ficam em `util/Conquistas.java`, conferidas no fim de cada sessão.
- **Confete**: o `ConfeteView` desenha os papeizinhos na própria View (sem Lottie), com gravidade, giro e o papel "virando" no ar. Com "remover animações" ligado no celular, nada se mexe.
- **Sons** (`res/raw/som_*.wav`): gerados por `tools/gerar_sons.py`, que sintetiza as notas só com a biblioteca padrão do Python. Para mudar um som, edite as notas no script e rode `python tools/gerar_sons.py`.

Mascote, medalhas e avatar usam só cores com nome (`@color/...`), e o teste `CoresDaMarcaTest` avisa se alguém colocar uma cor fixa num desses desenhos.

## Questões

São **80 questões oficiais do ENEM (2011 a 2023)** em **21 lições**: Linguagens (5), Ciências Humanas (5), Ciências da Natureza (5) e Matemática (6). Cada questão mostra a fonte, por exemplo "ENEM 2019 – Questão 24".

### Questões curtas, para ler no celular

Questão longa cansa no celular. Por isso cada questão tem no máximo **100 palavras** (texto-base + comando + alternativas) em Humanas, Natureza e Matemática, e **140 em Linguagens**, onde quase não há questão só de texto mais curta que isso. O limite fica em `LIMITE_PALAVRAS`, no `montar_questoes.py`, que não grava o banco se uma questão passar dele (o validador também confere).

### Gabarito oficial e anuladas

- O gabarito de cada questão é conferido com o **gabarito oficial do INEP** (PDF do caderno azul), em `GABARITO_INEP`. A API enem.dev erra alguns gabaritos (ex.: 2021-15, 2021-139 e 2022-143), então a letra da API só vale quando bate com a do INEP.
- Cada questão foi resolvida antes de ver o gabarito (a letra "cega" no script), e as três letras precisam bater.
- Questões **anuladas** ou abandonadas pelo INEP na correção pela TRI (microdados, tabela ITENS_PROVA) ficam em `ANULADAS` e nunca entram no banco.
- As explicações foram escritas para o app, sem copiar resoluções de cursinho.

### Lições de uns 4 minutos

O `montar_questoes.py` estima o tempo de cada questão e agrupa as lições para darem uns 4 minutos:

- leitura a **180 palavras por minuto** (texto-base, enunciado e alternativas);
- mais o tempo de resolver: **Linguagens 20 s, Humanas 30 s, Natureza 30 s, Matemática 75 s**;
- mais **15 s** se a questão tem figura ou fórmula.

Os números são constantes no começo do script (`PALAVRAS_POR_MINUTO`, `RESOLUCAO_S`, `EXTRA_FIGURA_S`, `ALVO_LICAO_S`). Mudou algum? Rode o `montar_questoes.py` e o `validar_questoes.py`: o validador confere 3 a 5 questões por lição, o teto de 6 min, a média de cada área perto do alvo e imprime o tempo mínimo e máximo por área. Hoje as lições ficam entre 3,6 e 4,2 min, e as de Matemática entre 5,0 e 7,4 min (3 questões de conta já passam de 4 min).

Quem já tinha progresso de uma versão antiga do banco (a primeira, com 4 lições de 5, ou a 1.0-demo) é migrado na primeira abertura: cada lição atual cujas questões estavam todas em lições concluídas já aparece concluída, até a primeira que falta (a trilha não fica com buraco). O caderno de erros troca os ids antigos pelos atuais, e as questões que saíram do banco saem dele. XP, ofensiva e conquistas não mudam. Como 54 das 80 questões são novas, na prática quase toda lição recomeça.

Em Matemática, as figuras (gráficos, tabelas, plantas) ficam dentro do app, em `app/src/main/assets/imagens/`, então tudo continua funcionando offline. A API entrega as fórmulas como imagens muito pequenas, que ficariam borradas no celular; por isso as alternativas que são fórmula foram redigitadas em LaTeX e renderizadas em alta resolução pelo script de montagem. Os gráficos que vieram ruins da API (baixa resolução ou um diferente dos outros, o que entregava a resposta) foram recortados da prova oficial do INEP.

O texto das 80 questões foi conferido contra a prova oficial do INEP (caderno azul): o script de montagem corrige os erros de OCR da API (palavra trocada, ":" a mais no comando, texto-base grudado no comando, versos que viraram parágrafos, ponto de milhar) e deixa as alternativas com a caixa e a pontuação do caderno.

As questões vêm da API pública [enem.dev](https://enem.dev), e os scripts que montam o banco ficam em [`tools/`](tools):

- `baixar_enem.py`: baixa as provas da API para `tools/enem_raw/` (essa pasta não vai para o repositório). Sem argumentos, baixa os anos que o banco usa (2011 e 2013 a 2023)
- `filtrar_enem.py`: separa as questões aproveitáveis (100% texto nas três primeiras áreas; em Matemática, também as com figura ou fórmula)
- `montar_questoes.py`: gera o `app/src/main/assets/questoes.json` (com o tempo estimado de cada questão e as lições), baixa as figuras e renderiza as fórmulas
- `recortar_figuras.py`: recorta da prova oficial do INEP as figuras que vieram ruins da API e grava em `tools/figuras/`, que o `montar_questoes.py` usa no lugar do download (só precisa rodar de novo se mudar um recorte)
- `validar_questoes.py`: confere o JSON, o gabarito do INEP, as anuladas, o limite de palavras, o tempo das lições e as imagens contra a especificação e contra o original da API
- `gerar_sons.py`: gera os sons de acerto, erro e lição concluída em `app/src/main/res/raw/`

## Como rodar

1. Clone o repositório e abra a pasta no **Android Studio**.
2. Espere o Gradle sincronizar.
3. Rode no emulador ou num celular com Android 8.0 (API 26) ou mais novo.

Para regerar o banco de questões (opcional, precisa de Python 3 e do matplotlib para as fórmulas):

```bash
pip install -r tools/requirements.txt
python tools/baixar_enem.py
python tools/filtrar_enem.py
python tools/montar_questoes.py
python tools/validar_questoes.py
```

## Gerar o APK de release

1. Crie uma keystore **fora** do repositório com o `keytool` (vem no JDK), por exemplo com o alias `enemigo`.
2. Copie `keystore.properties.example` para `keystore.properties` na raiz e preencha caminho, alias e senhas. Esse arquivo e os `.jks`/`.keystore` são ignorados pelo git: nunca os commite.
3. Rode `./gradlew assembleRelease`. O APK sai em `app/build/outputs/apk/release/app-release.apk`.
4. Confira a assinatura com `apksigner verify --print-certs` (fica no `build-tools` do Android SDK).

Sem o `keystore.properties` o build gera um APK sem assinatura. O release não instala por cima de um build debug (assinaturas diferentes): desinstale o debug antes.

## Estrutura

```text
app/src/main/java/com/murilo/enemigo/
├── data/    # leitura do questoes.json, progresso e preferências salvos (SharedPreferences)
├── model/   # Area, Licao, Questao, ResultadoSessao, Conquista e Avatar
├── notificacao/ # lembrete diário: agenda no WorkManager, o worker e a notificação
├── ui/      # Activities, Fragments, Adapters e componentes (ui/componente: relevo, mascote, avatar, confete)
└── util/    # regras de gamificação (XP, vidas, ofensiva, conquistas, celebrações, lembrete) e constantes
```

As regras de gamificação ficam em `Gamificacao.java`, as das conquistas em `Conquistas.java`, a fila de celebrações em `Celebracoes.java`, as de tempo em `TempoEstudo.java`, as do lembrete (notificar ou não) em `Lembrete.java`, os tamanhos de letra em `TamanhoLetra.java` e a migração do progresso em `Migracao.java`, todas com testes em `app/src/test` (o `model/Avatar` também).

## Personalizar a marca

Dá para trocar a identidade visual sem mexer no código Java:

- **Nome do app:** `app_name` em `app/src/main/res/values/strings.xml`. O nome aparece no ícone, no topo do Início e na boas-vindas.
- **Cores:** `app/src/main/res/values/colors.xml` (modo claro) e `app/src/main/res/values-night/colors.xml` (modo escuro). Os nomes dizem a função da cor, então basta trocar os valores:
  - `marca_primaria` (botões, seleção, links) e `marca_primaria_escura` (sombra dos botões)
  - `marca_acento` (XP, meta diária, destaques)
  - `marca_acerto` e `marca_erro` (feedback do quiz)
  - `fundo`, `superficie`, `texto_principal`...
  - `area_*` (cor de cada área, a versão `_escura` e a cor do texto `_sobre` ela)
  - `papel*`: precisa continuar claro nos dois modos (o `PapelModoEscuroTest` avisa se não estiver).
- **Fontes:** `app/src/main/res/font/marca_interface.xml` (interface), `marca_titulo.xml` (títulos) e `marca_leitura.xml` (texto-base). Para usar outra fonte do Google Fonts, ajuste e rode `tools/gerar_fontes.py`, que gera os arquivos de cada peso e recorta só os caracteres que o app usa.
- **Ícone e abertura:** a cabeça da Lia sobre `marca_primaria`. `app/src/main/res/drawable/ic_launcher_foreground.xml` (a abertura usa o mesmo desenho), `ic_launcher_monocromatico.xml` (ícone temático) e `marca_logo.xml` (logo dentro do app), todos gerados por `tools/gerar_mascote.py`.
- **Mascote:** o nome fica em `mascote_nome` (strings.xml) e as cores em `mascote_*` (colors.xml), que são apelidos das cores da marca. Trocando a marca, a Lia, as medalhas e os fundos do avatar acompanham sozinhos (o pelo dela é `marca_ofensiva` e o lenço é `marca_primaria`).

Depois de trocar, rode `./gradlew test` e confira as telas nos dois modos.

A especificação completa do app (telas, regras de negócio, plano das aulas) está em [`docs/PROJETO_APP_ANDROID.md`](docs/PROJETO_APP_ANDROID.md).

## Próximos passos

- Lembrete diário por notificação, com a Lia
- Conquistas para as outras áreas, como as de Matemática
- Mais peças de avatar (roupas, cabelos), algumas liberadas por conquistas
- Testes de interface (Espresso) para o quiz, o resultado e as celebrações

## Créditos e licença

- Questões: provas do ENEM, elaboradas e publicadas pelo **INEP**. Obtidas pela API [enem.dev](https://enem.dev).
- Fontes: [Plus Jakarta Sans](https://github.com/tokotype/PlusJakartaSans) e [Literata](https://github.com/googlefonts/literata), sob a SIL Open Font License 1.1 (textos das licenças em `app/src/main/assets/licencas/`).
- Mascote, avatar, medalhas, confete e sons: feitos para o app (vetores desenhados à mão e sons sintetizados por script), sem assets de terceiros.
- O código do app está sob a licença MIT (veja [LICENSE](LICENSE)). A licença não se aplica às questões do ENEM, que continuam pertencendo ao INEP.
