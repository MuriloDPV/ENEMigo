# PROJETO_APP_ANDROID — ENEMigo

> Especificação do aplicativo Android desenvolvido na disciplina de Programação.
> Ferramentas: Android Studio, Java, XML, Android SDK e GitHub Copilot.
> Este documento **não contém código**. Ele é a base para a implementação guiada pelos prompts da seção 17.
>
> **Nota:** esta especificação foi escrita prevendo o uso do GitHub Copilot, mas a implementação acabou sendo feita com o Claude Code.

**Ideia original do aluno:**

> Um app no estilo Duolingo, mas em vez de ensinar idiomas ele treina a pessoa com questões do ENEM, em lições curtas e diárias, pra estudar um pouco todo dia sem perceber.

---

## Sumário

1. Visão geral
2. Conceito do aplicativo
3. Funcionalidades
4. Telas
5. Experiência do usuário
6. Fluxo de navegação
7. Interface
8. Modelo de dados
9. Armazenamento
10. Regras de negócio
11. Validações
12. Tratamento de erros
13. Recursos visuais
14. Estrutura do projeto Android
15. Arquitetura
16. Plano de desenvolvimento em 4 aulas
17. Prompts para o GitHub Copilot
18. Testes
19. Checklist final
20. Possíveis melhorias

---

## 1. Visão geral

### Nome do aplicativo

**ENEMigo** (ENEM + amigo). O nome passa a ideia de um parceiro de estudos que acompanha o aluno todo dia, e não de uma prova que assusta.

### Descrição

O ENEMigo é um aplicativo de estudo para o ENEM em formato de jogo. O conteúdo fica organizado nas quatro áreas da prova (Linguagens, Ciências Humanas, Ciências da Natureza e Matemática). Cada área tem uma **trilha de lições curtas** (uns 4 minutos cada), e cada lição tem **de 3 a 5 questões de múltipla escolha** (A a E) retiradas de provas anteriores do ENEM ou escritas no estilo da prova. Depois de cada resposta o app mostra na hora se o aluno acertou e explica o porquê.

Para incentivar o estudo diário, o app usa as mecânicas que tornaram o Duolingo conhecido: **XP**, **ofensiva diária** (dias seguidos estudando), **vidas**, **meta diária**, **níveis**, **desbloqueio progressivo** e **conquistas**. As questões erradas vão para um **caderno de erros**, onde o aluno pode revisá-las.

### Objetivo

Fazer o estudante resolver algumas questões do ENEM **todos os dias**, em sessões de 3 a 5 minutos, até isso virar hábito. A ideia é trocar maratonas de estudo que cansam e são abandonadas por pequenas doses diárias que se acumulam.

### Problema que resolve

- Estudar para o ENEM parece uma tarefa enorme e desanima: são 180 questões e muito conteúdo.
- Os simulados completos são longos e cansativos, e a maioria dos alunos só faz na véspera.
- Muitos alunos resolvem questões sem entender por que erraram e acabam repetindo os mesmos erros.
- Falta motivação para manter constância, e sem constância o estudo não rende.

O ENEMigo quebra o estudo em partes pequenas, dá retorno imediato e usa recompensas (XP, ofensiva, conquistas) para criar o hábito.

### Público-alvo

- Estudantes do ensino médio (principalmente 2º e 3º ano) que vão prestar o ENEM.
- Pessoas que terminaram o ensino médio e querem voltar a estudar para o ENEM.
- Alunos de cursos técnicos integrados ao ensino médio.
- Perfil: usam o celular o tempo todo, têm pouco tempo livre e se motivam com jogos e metas.

### Diferenciais da ideia

- **Lições de uns 4 minutos** (3 a 5 questões), que dá para fazer na fila, no ônibus ou no intervalo. A trilha mostra o tempo estimado de cada uma.
- **Feedback imediato com explicação** em cada questão, e não só o gabarito no final.
- **Gamificação completa**: ofensiva, vidas, XP, níveis e conquistas, a mesma mecânica que prende as pessoas no Duolingo, aplicada ao ENEM.
- **Caderno de erros automático**: o aluno revisa exatamente o que errou, e acertar na revisão devolve uma vida, o que transforma o erro em algo útil.
- **Funciona 100% offline**: as questões ficam dentro do app, sem precisar de internet nem de conta.
- **Questões reais do ENEM** com a fonte indicada (ex.: "ENEM 2019"), retiradas das provas públicas do INEP.

---

## 2. Conceito do aplicativo

### Como funciona

1. **Primeiro acesso:** o aluno informa o nome e escolhe uma meta diária de XP: Casual (20 XP), Regular (50 XP) ou Intenso (100 XP). Isso leva menos de 30 segundos.
2. **Tela Início:** mostra no topo a ofensiva (🔥), o XP total (⭐) e as vidas (❤️). Abaixo, um cartão com o progresso da meta diária e os 4 cartões das áreas do ENEM.
3. **Trilha da área:** ao tocar numa área, o aluno vê a lista de lições em sequência, cada uma com o tempo estimado ("~4 min"). A primeira fica liberada; as outras aparecem com cadeado até a anterior ser concluída com pelo menos 60% de acertos.
4. **Lição (Quiz):** de 3 a 5 questões, uma por vez. O aluno toca numa alternativa e aperta **VERIFICAR**. Aparece um painel **verde** ("Mandou bem!") ou **vermelho** ("Resposta correta: C") com a explicação. O botão **CONTINUAR** leva à próxima questão. Cada erro tira 1 vida.
5. **Resultado:** ao terminar, o app mostra acertos, XP ganho, bônus de lição perfeita, se a próxima lição foi liberada, se a meta diária foi batida e as conquistas novas.
6. **Revisão:** as questões erradas ficam guardadas no caderno de erros. Na aba Revisão o aluno refaz essas questões; acertar tira a questão do caderno e devolve 1 vida.
7. **Perfil:** mostra nível, XP, ofensiva atual e recorde, lições concluídas, taxa de acerto, tempo médio por questão, minutos estudados hoje e conquistas.

### Experiência pretendida

O app deve dar a sensação de **jogo rápido**, e não de "tela de prova":

- cores vivas, botões grandes e arredondados, fonte amigável;
- respostas imediatas, com cor e mensagem curta;
- recompensas visíveis a todo momento (XP subindo, fogo da ofensiva aceso);
- um pouco de "pressão boa": vidas limitadas e ofensiva que zera se o aluno pular um dia.

### O ciclo do hábito

```text
Abrir o app → ver a ofensiva em risco → fazer 1 lição (3 a 5 min)
     ↑                                            │
     └──── voltar amanhã ← ganhar XP/conquista ←──┘
```

O objetivo do desenho é que **uma lição por dia** já conte como "estudei hoje". Esse é o "estudar sem perceber" da ideia original.

---

## 3. Funcionalidades

As funcionalidades foram divididas para caber em 4 aulas. As **principais** fazem o app cumprir a proposta e precisam estar prontas até o fim da Aula 3. As **complementares** deixam o app mais completo e entram na Aula 3 e na Aula 4, conforme o tempo. As **futuras** ficam fora do escopo.

### Funcionalidades principais

| # | Funcionalidade | Descrição |
|---|---|---|
| P1 | Onboarding | Primeiro acesso: pede o nome e a meta diária (20/50/100 XP). Só aparece uma vez. |
| P2 | Áreas do ENEM | Tela Início com as 4 áreas: Linguagens, Humanas, Natureza e Matemática. |
| P3 | Trilha de lições | Lista de lições de cada área, em ordem, com os estados concluída, disponível e bloqueada e o tempo estimado de cada uma ("~4 min"). |
| P4 | Desbloqueio sequencial | A próxima lição só abre quando a anterior tem pelo menos 60% de acertos (2 de 3, 3 de 4, 3 de 5). |
| P5 | Quiz da lição | 3 a 5 questões (A a E), com seleção de alternativa, VERIFICAR, painel de feedback verde ou vermelho com explicação e CONTINUAR. |
| P6 | Barra de progresso da lição | Mostra em que questão o aluno está ("Questão 2 de 4"), com qualquer tamanho de lição. |
| P7 | Resultado da lição | Acertos, XP ganho, bônus e se desbloqueou a próxima lição. |
| P8 | XP e nível | +10 XP por acerto, +5 de bônus com 100% de acertos. Nível = XP ÷ 100 + 1. |
| P9 | Vidas | 5 vidas. Cada erro na lição tira 1. Recupera 1 a cada 30 minutos. Sem vidas, não dá para iniciar uma lição. |
| P10 | Ofensiva diária | Conta os dias seguidos em que o aluno terminou pelo menos uma sessão. Zera se ele pular um dia. |
| P11 | Meta diária | Barra "XP de hoje / meta" na tela Início, zerada todo dia. |
| P12 | Persistência | Todo o progresso fica salvo no aparelho (SharedPreferences) e continua lá depois de fechar o app. |
| P13 | Banco de questões local | Questões lidas de `assets/questoes.json`, com a fonte ("ENEM 2019"). |

### Funcionalidades complementares

| # | Funcionalidade | Descrição |
|---|---|---|
| C1 | Caderno de erros | Toda questão errada numa lição é guardada (sem repetir). |
| C2 | Modo Revisão | Aba Revisão: refaz até 5 questões do caderno. Acertar remove a questão do caderno, devolve +1 vida e dá +5 XP. Errar na revisão não tira vida. |
| C3 | Conquistas | 6 conquistas: Primeira lição, Ofensiva de 3 dias, Ofensiva de 7 dias, 100 XP, 500 XP e Lição perfeita. |
| C4 | Perfil com estatísticas | Nível, barra até o próximo nível, XP, ofensiva atual e recorde, lições concluídas, taxa de acerto, tempo médio por questão, minutos estudados hoje e conquistas. |
| C5 | Configurações | Editar nome, trocar a meta diária, ver "Sobre" e **resetar o progresso** (com confirmação). |
| C6 | Contagem da próxima vida | Mostra "Próxima vida em X min" quando o aluno tem menos de 5 vidas. |
| C7 | Cartão "Continuar" | Na tela Início, um atalho para a próxima lição disponível da última área estudada. |
| C8 | Fogo aceso/apagado | O ícone da ofensiva fica colorido se o aluno já estudou hoje e cinza se ainda não estudou (lembrete visual). |
| C9 | Estados vazios | Mensagens amigáveis quando não há erros no caderno, conquistas ou questões. |
| C10 | Vibração ao errar | Vibração curta ao errar, que pode ser desligada nas Configurações (opcional). |
| C11 | Lembrete diário | Notificação no horário escolhido (padrão 19h), só se o aluno ainda não estudou no dia, com a Lia e a ofensiva em risco ("Sua ofensiva de 5 dias está em risco!"), em 6 frases que se revezam. WorkManager (sobrevive a reinício, sem internet e sem alarme exato); até 3 h de atraso, depois disso o aviso do dia é pulado. No Android 13+, a permissão é pedida depois da primeira lição concluída, com explicação antes, e uma recusa é respeitada. |

**Se faltar tempo**, corte nesta ordem: C10 → C7 → C6 → C8 → C3. O caderno de erros (C1/C2) é o complementar mais importante, porque é o que faz o aluno aprender com o erro.

### Possíveis funcionalidades futuras

- Questões com imagem em Linguagens, Humanas e Natureza (mapas, charges). Matemática já tem figuras e fórmulas (ver 9.3).
- Banco de questões online (API ou Firebase), com novas questões sem precisar atualizar o app.
- Simulado cronometrado (ex.: 20 questões em 50 minutos).
- Ligas e ranking semanal entre amigos (exige login e servidor).
- Login e sincronização entre aparelhos.
- Modo escuro.
- Estatísticas por habilidade da matriz do ENEM e gráfico de evolução.
- Treino de redação com temas antigos e checklist das 5 competências.
- "Congelar ofensiva" comprável com XP.
- Animações com Lottie e sons de acerto e erro.

---

## 4. Telas

O app tem **9 telas**: 6 Activities e 3 Fragments dentro da MainActivity.

> **Conceito: Activity e Fragment.** Uma *Activity* é uma tela inteira do app. Um *Fragment* é um "pedaço de tela" que vive dentro de uma Activity. Aqui a `MainActivity` tem uma barra inferior (BottomNavigationView) que troca entre 3 Fragments (Início, Revisão e Perfil) sem abrir outra tela.

---

### 4.1 Tela de Onboarding (`OnboardingActivity`)

- **Objetivo:** fazer o cadastro inicial do aluno no primeiro acesso.
- **Componentes:** ImageView (logo), TextView (título e subtítulo), TextInputLayout + TextInputEditText (nome), RadioGroup com 3 RadioButtons (meta), MaterialButton "Começar".
- **Informações exibidas:** boas-vindas ("Oi! Eu sou o ENEMigo 👋"), explicação curta do app e as opções de meta com a descrição de cada uma.
- **Campos:**
  - Nome (texto, obrigatório, de 2 a 20 caracteres).
  - Meta diária (Casual: 20 XP/dia, Regular: 50 XP/dia, Intenso: 100 XP/dia). "Regular" já vem marcado.
- **Botões:** "COMEÇAR".
- **Ações:** validar o nome, salvar nome e meta, marcar o onboarding como concluído e abrir a MainActivity.
- **Comportamentos:**
  - Se o nome for inválido, mostra o erro no próprio campo (`setError` do TextInputLayout) e não avança.
  - Espaços no começo e no fim do nome são removidos (`trim`).
  - Depois de concluído, a tela não aparece mais, a não ser que o progresso seja resetado.
  - Ao abrir a MainActivity, a Onboarding é finalizada (`finish()`), para o botão Voltar não retornar a ela.
- **Navegação:** Onboarding → MainActivity (aba Início).

---

### 4.2 Tela Principal (`MainActivity`)

- **Objetivo:** servir de "casca" do app, com a barra de navegação inferior.
- **Componentes:** FrameLayout (container dos fragments) e BottomNavigationView com 3 itens.
- **Informações exibidas:** o fragment da aba selecionada.
- **Campos:** nenhum.
- **Botões:** abas **Início** (ícone de casa), **Revisão** (ícone de caderno) e **Perfil** (ícone de pessoa).
- **Ações:** trocar o fragment exibido ao tocar numa aba.
- **Comportamentos:**
  - No `onCreate`, verifica se o onboarding foi concluído. Se não foi, abre a OnboardingActivity e finaliza a MainActivity.
  - A aba inicial é Início.
  - Tocar de novo na aba que já está aberta não faz nada.
- **Navegação:** Início, Revisão e Perfil (fragments). Voltar na MainActivity fecha o app.

---

### 4.3 Início (`InicioFragment`)

- **Objetivo:** ser o painel diário do aluno e o ponto de partida para as lições.
- **Componentes:** barra de status (layout incluído `layout_status`), TextView de saudação, MaterialCardView da meta diária com LinearProgressIndicator, MaterialCardView "Continuar" (complementar) e 4 MaterialCardViews das áreas (grade 2×2).
- **Informações exibidas:**
  - 🔥 ofensiva (número de dias), ⭐ XP total e ❤️ vidas (0 a 5).
  - "Oi, {nome}! Bora manter o ritmo?"
  - Meta diária: "32 / 50 XP hoje" e barra de progresso. Com a meta batida: "Meta batida! 🎉".
  - Cartão Continuar: "Continuar: Matemática — Lição 3".
  - Cartão de cada área: ícone, nome e "2/3 lições".
- **Campos:** nenhum.
- **Botões:** os cartões das 4 áreas e o cartão Continuar são clicáveis.
- **Ações:** tocar numa área abre a TrilhaActivity daquela área. Tocar em Continuar abre direto a lição sugerida.
- **Comportamentos:**
  - Todos os dados são atualizados no `onResume`, para refletir o progresso quando o aluno volta de uma lição.
  - Vidas e ofensiva são recalculadas ao abrir (recuperação de vidas pelo tempo e ofensiva zerada se o aluno pulou um dia).
  - Fogo colorido se já estudou hoje; cinza se ainda não.
- **Navegação:** Início → Trilha (área) → Quiz; Início → Quiz (atalho Continuar).

---

### 4.4 Trilha da área (`TrilhaActivity`)

- **Objetivo:** mostrar a sequência de lições de uma área e o estado de cada uma.
- **Componentes:** MaterialToolbar (nome da área e botão voltar), barra de status (`layout_status`), TextView com o resumo "2 de 3 lições concluídas" e RecyclerView com os itens de lição.
- **Informações exibidas (por lição):** número e tempo estimado ("Lição 1 · ~4 min", ver RN36), título (ex.: "Porcentagem, proporção e juros"), estado e melhor resultado ("Melhor: 3/4").
  - **Concluída** (melhor com 60% ou mais): círculo na cor da área com ✓.
  - **Disponível:** círculo na cor da área e o texto "Começar".
  - **Bloqueada:** círculo cinza com 🔒 e o texto "Acerte 60% na lição anterior".
- **Campos:** nenhum.
- **Botões:** cada item da lista é clicável; seta de voltar na toolbar.
- **Ações:**
  - Tocar numa lição disponível ou concluída: se tiver vidas, abre o Quiz.
  - Tocar numa lição bloqueada: Toast "Complete a lição anterior com pelo menos 60% de acertos".
  - Tocar numa lição sem ter vidas: diálogo "Sem vidas 💔", com a opção de ir para a Revisão ou esperar ("Próxima vida em X min").
- **Comportamentos:** a lista é recarregada no `onResume`. A cor da toolbar segue a cor da área. Lições concluídas podem ser refeitas para melhorar a nota.
- **Navegação:** Trilha → Quiz; Voltar → Início.

---

### 4.5 Quiz (`QuizActivity`)

- **Objetivo:** apresentar as questões e corrigir cada resposta. É a tela mais importante do app.
- **Modos:** `LICAO` (as 3 a 5 questões de uma lição) ou `REVISAO` (até 5 questões do caderno de erros).
- **Componentes:** ImageButton "X" (fechar), LinearProgressIndicator (progresso), TextView de vidas (❤️ 4), ScrollView com o conteúdo, MaterialCardView do texto-base (quando existir), TextView do enunciado, TextView da fonte, 5 alternativas (layout `item_alternativa` incluído 5 vezes), MaterialButton VERIFICAR, painel de feedback (LinearLayout escondido) com título, explicação e botão CONTINUAR.
- **Informações exibidas:** texto-base, enunciado, fonte ("ENEM 2019"), alternativas A a E, progresso ("Questão 2 de 4", com o total da lição), vidas e, depois de verificar, o feedback com a explicação.
- **Campos:** seleção de uma alternativa (escolha única).
- **Botões:** X, alternativas A a E, VERIFICAR, CONTINUAR.
- **Ações:**
  1. Tocar numa alternativa: ela fica destacada (azul) e o VERIFICAR é habilitado.
  2. VERIFICAR: compara com o gabarito.
     - **Acerto:** alternativa fica verde; painel verde "Mandou bem! 🎉" + explicação.
     - **Erro:** alternativa escolhida fica vermelha e a correta verde; painel vermelho "Resposta correta: C" + explicação. No modo LICAO, perde 1 vida e a questão vai para o caderno de erros.
     - As alternativas ficam travadas até o aluno tocar em CONTINUAR.
  3. CONTINUAR: vai para a próxima questão. Na última, abre o Resultado.
  4. X ou Voltar do sistema: diálogo "Sair da lição? Você vai perder o progresso desta lição." (Sair / Continuar estudando).
- **Comportamentos:**
  - VERIFICAR começa desabilitado e é desabilitado de novo depois do clique (evita clique duplo).
  - Se as vidas chegarem a 0 no modo LICAO, o CONTINUAR abre o diálogo "Suas vidas acabaram 💔" (Ir para Revisão / Sair). A lição termina sem salvar conclusão nem XP.
  - No modo REVISAO, errar não tira vida; acertar remove a questão do caderno, devolve +1 vida (até 5) e dá +5 XP.
  - A barra de progresso avança a cada CONTINUAR.
  - Cada questão é cronometrada do momento em que aparece até o VERIFICAR, só com a tela aberta (RN38).
  - Opcional: vibração curta ao errar (C10).
- **Navegação:** Quiz → Resultado (a QuizActivity se finaliza); X → volta para a tela anterior.

---

### 4.6 Resultado (`ResultadoActivity`)

- **Objetivo:** fechar a sessão com recompensa e motivação.
- **Componentes:** TextView grande (emoji e título), TextView de acertos, cartões de XP/ofensiva/meta, TextView de mensagens (desbloqueio e conquistas), MaterialButton CONTINUAR e MaterialButton REFAZER (outlined).
- **Informações exibidas:**
  - Título conforme o desempenho: 100% "Perfeito! 🏆"; 60% ou mais "Lição concluída! ✅"; menos de 60% "Quase lá! 💪".
  - "Você acertou 3 de 4".
  - "+30 XP" (e "+5 bônus de lição perfeita" com 100%).
  - "🔥 Ofensiva: 6 dias".
  - "Meta diária batida! 🎯" (só na sessão em que a meta foi atingida).
  - "Próxima lição desbloqueada! 🔓" ou "Para liberar a próxima lição, acerte 3 de 4".
  - Conquistas novas: "🏅 Conquista desbloqueada: Lição perfeita".
  - No modo REVISAO: "Você tirou 3 questões do caderno e recuperou 3 vidas".
- **Campos:** nenhum.
- **Botões:** CONTINUAR (volta para a Trilha ou para a aba Revisão) e REFAZER LIÇÃO (só no modo LICAO; abre o Quiz da mesma lição de novo).
- **Ações:** apenas exibe. O progresso já foi salvo pela QuizActivity antes de abrir esta tela.
- **Comportamentos:** o Voltar do sistema funciona como CONTINUAR. Se não houver vidas, o REFAZER mostra o diálogo "Sem vidas".
- **Navegação:** Resultado → Trilha (ou MainActivity) / Resultado → Quiz (refazer).

---

### 4.7 Revisão (`RevisaoFragment`)

- **Objetivo:** mostrar o caderno de erros e iniciar a revisão.
- **Componentes:** TextView de título, TextView com o contador, MaterialButton "REVISAR AGORA", RecyclerView com as questões do caderno e um layout de estado vazio (ImageView/TextView).
- **Informações exibidas:** "Você tem 7 questões no caderno de erros"; texto de apoio "Acerte na revisão para ganhar ❤️ +1". Para cada questão: etiqueta colorida da área, começo do enunciado (2 linhas) e fonte.
- **Campos:** nenhum.
- **Botões:** "REVISAR AGORA" (desabilitado se o caderno estiver vazio).
- **Ações:** "REVISAR AGORA" abre o Quiz no modo REVISAO com até 5 questões sorteadas do caderno.
- **Comportamentos:**
  - Caderno vazio: esconde a lista e mostra "Nenhum erro por aqui! 🎉 Continue fazendo lições."
  - Atualiza no `onResume`.
  - Tocar num item da lista não faz nada nesta versão (a revisão é feita pelo botão).
- **Navegação:** Revisão → Quiz (modo REVISAO) → Resultado → volta para a MainActivity.

---

### 4.8 Perfil (`PerfilFragment`)

- **Objetivo:** mostrar a evolução do aluno.
- **Componentes:** MaterialToolbar com ícone de engrenagem (Configurações), TextView do avatar (círculo com a inicial do nome), TextViews de nome e nível, LinearProgressIndicator (XP até o próximo nível), MaterialCardViews de estatísticas (grade) e RecyclerView de conquistas (grade de 3 colunas).
- **Informações exibidas:**
  - Avatar com a inicial, nome e "Nível 4".
  - "35 / 100 XP para o nível 5".
  - Estatísticas: XP total, ofensiva atual, maior ofensiva, lições concluídas, questões respondidas, taxa de acerto (%), tempo médio por questão ("72 s") e minutos estudados hoje ("12 min"), medidos no Quiz (RN38).
  - Conquistas: emoji, título e descrição. As bloqueadas aparecem apagadas (transparência) com 🔒.
- **Campos:** nenhum.
- **Botões:** engrenagem (Configurações).
- **Ações:** abrir a ConfiguracoesActivity.
- **Comportamentos:** atualiza no `onResume`. Sem nenhuma questão respondida, a taxa de acerto e o tempo médio mostram "—".
- **Navegação:** Perfil → Configurações.

---

### 4.9 Configurações (`ConfiguracoesActivity`)

- **Objetivo:** permitir ajustes e o reset do progresso.
- **Componentes:** MaterialToolbar com Voltar, TextInputLayout + TextInputEditText (nome), RadioGroup (meta), SwitchMaterial (vibração, opcional), MaterialSwitch (lembrete diário) com a linha do horário (MaterialTimePicker de 24 h), MaterialButton "SALVAR", MaterialButton "RESETAR PROGRESSO" (vermelho) e TextView "Sobre".
- **Informações exibidas:** nome e meta atuais já preenchidos, e o texto Sobre (versão, fonte das questões e aviso de que o app não tem vínculo com o INEP).
- **Campos:** nome (mesma validação do onboarding), meta diária (20/50/100), vibração (liga/desliga) e lembrete diário (liga/desliga e horário, padrão 19h). Com o lembrete ligado e as notificações bloqueadas no Android, aparece um aviso que pede a permissão de novo ou abre as configurações de notificação do app.
- **Botões:** SALVAR e RESETAR PROGRESSO.
- **Ações:**
  - SALVAR: valida e grava. Toast "Configurações salvas" e volta ao Perfil.
  - RESETAR PROGRESSO: AlertDialog "Apagar todo o progresso? XP, ofensiva, lições e conquistas serão perdidos. Essa ação não pode ser desfeita." (Cancelar / Apagar tudo). Ao confirmar, limpa todos os dados e abre o Onboarding limpando a pilha de telas.
- **Comportamentos:** se o aluno sair sem salvar, nada muda.
- **Navegação:** Configurações → Perfil (Voltar/Salvar); Configurações → Onboarding (após o reset).

---

## 5. Experiência do usuário

### Primeiro uso (cerca de 2 minutos)

1. O aluno instala e abre o ENEMigo. Aparece a tela de boas-vindas com o logo e "Oi! Eu sou o ENEMigo 👋".
2. Ele digita o nome ("Ana"), mantém a meta **Regular (50 XP/dia)** e toca em **COMEÇAR**.
3. Cai na tela **Início**: 🔥 0, ⭐ 0, ❤️ 5. "Oi, Ana! Bora começar?". A meta mostra "0 / 50 XP hoje".
4. Toca no cartão **Matemática**. A trilha mostra a Lição 1 disponível e as demais com cadeado.
5. Toca na **Lição 1 — Porcentagem, proporção e juros (~6 min)**. Abre o Quiz: barra de progresso vazia, ❤️ 5, a primeira das 3 questões.
6. Toca na alternativa B, que fica azul, e aperta **VERIFICAR**. O painel verde sobe: "Mandou bem! 🎉 Descontos sucessivos se multiplicam...". Toca em **CONTINUAR**.
7. Na questão 3 ela erra: a alternativa escolhida fica vermelha, a certa fica verde, e o painel vermelho mostra "Resposta correta: D" com a explicação. ❤️ cai para 4.
8. Termina com **2/3**: "Lição concluída! ✅ +20 XP · 🔥 Ofensiva: 1 dia · Próxima lição desbloqueada! 🔓 · 🏅 Conquista: Primeira lição".
9. Toca em **CONTINUAR**, volta para a trilha e a Lição 2 agora está aberta.

### Uso diário (3 a 5 minutos)

1. No dia seguinte, a Ana abre o app. O fogo está **cinza** (ainda não estudou hoje) e a ofensiva mostra 1.
2. O cartão **Continuar** sugere "Matemática — Lição 2". Um toque e ela já está no Quiz.
3. Termina a lição, a ofensiva vai para **2** e o fogo fica colorido.
4. Na aba **Revisão**, vê "1 questão no caderno de erros". Toca em **REVISAR AGORA**, acerta e recupera ❤️ +1.
5. Na aba **Perfil**, acompanha o nível subindo e as conquistas aparecendo.

### Situações de "fricção boa"

- **Sem vidas:** ao tentar abrir uma lição, o app sugere ir para a Revisão (onde dá para recuperar vidas acertando) ou esperar ("Próxima vida em 12 min"). Isso incentiva a revisar os erros.
- **Pulou um dia:** a ofensiva volta a 0 e começa de novo na próxima lição. O recorde fica guardado no Perfil.

---

## 6. Fluxo de navegação

```text
Abertura do app
    │
    ├── [primeiro acesso] Onboarding (nome + meta)
    │                          │
    │                          ▼
    └── [demais acessos] ─► MainActivity (BottomNavigationView)
                               │
                               ├── Aba Início
                               │     ├── Cartão da área ─► Trilha da área
                               │     │                        └── Lição ─► Quiz (LICAO)
                               │     │                                     └── Resultado
                               │     │                                           ├── Continuar ─► Trilha
                               │     │                                           └── Refazer ─► Quiz
                               │     └── Cartão Continuar ─► Quiz (LICAO) ─► Resultado
                               │
                               ├── Aba Revisão
                               │     └── Revisar agora ─► Quiz (REVISAO)
                               │                            └── Resultado ─► Aba Revisão
                               │
                               └── Aba Perfil
                                     └── Engrenagem ─► Configurações
                                                         ├── Salvar ─► Perfil
                                                         └── Resetar progresso ─► Onboarding
```

**Pilha de telas (back stack):**

- A Quiz sempre chama `finish()` ao abrir o Resultado, então o Voltar no Resultado nunca retorna a uma questão já respondida.
- O Onboarding chama `finish()` ao abrir a MainActivity.
- O reset abre o Onboarding com as flags `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`, que apagam toda a pilha anterior.

---

## 7. Interface

> **Atualização (redesign visual):** a identidade visual foi refeita depois desta especificação. Hoje o app usa o tema "caderno de estudo" com **DayNight** (modo claro e escuro), as fontes **Plus Jakarta Sans** (interface) e **Literata** (texto-base), botões e cartões em relevo e ícones vetoriais próprios no lugar dos emojis 🔥⭐❤️. As cores têm nomes de função (`marca_primaria`, `marca_acento`...) em `values/colors.xml` e `values-night/colors.xml`. Veja "Personalizar a marca" no README. As seções 7.1 a 7.4 e 13 descrevem a versão original e ficam aqui como registro do projeto.
>
> **Atualização (mascote e ícone):** o app ganhou uma mascote própria, a **Lia**, uma raposa esperta que fareja a pegadinha do enunciado (laranja da ofensiva, máscara clara e lenço azul-tinta). Ela aparece no onboarding, no feedback do quiz, no resultado, nas celebrações, no caderno vazio e nos avisos de sem vidas (orelhas caídas e desbotada). O ícone do app, a abertura e o logo usam a cabeça dela sobre `marca_primaria`. Tudo é vetor desenhado para o app (`tools/gerar_mascote.py`), sem coruja, sem o verde do Duolingo e sem assets de terceiros.

### 7.1 Diretrizes gerais

- **Tema:** `Theme.Material3.Light.NoActionBar`. Use o tema **Light** fixo: com o DayNight padrão do Android Studio, o modo escuro do celular pode deixar texto escuro sobre fundo escuro, já que as cores são definidas à mão. O modo escuro fica como melhoria futura.
- **Orientação:** somente retrato (`android:screenOrientation="portrait"` em cada Activity do Manifest). Assim o aluno não precisa tratar a perda de estado ao girar a tela no meio do quiz.
  > Observação: no Android 16, em tablets e telas grandes, o sistema pode ignorar essa trava. Em celular funciona normalmente, e é o foco do projeto.
- **Fonte:** Nunito (Google Fonts, gratuita), com o padrão do sistema como alternativa. Títulos em negrito.
- **Cantos arredondados:** 16dp nos cartões e 12dp nos botões.
- **Espaçamentos:** margem lateral de 16dp e espaço de 12dp entre cartões.
- **Botões principais:** altura mínima de 56dp, texto em maiúsculas e negrito, cor primária verde.
- **Toque:** todo elemento clicável com pelo menos 48dp de altura.
- **Textos:** sempre em `strings.xml`, nada fixo no layout.

### 7.2 Paleta de cores (`colors.xml`)

| Nome | Hex | Uso |
|---|---|---|
| `verde_primario` | `#58CC02` | Botões principais, acerto |
| `verde_escuro` | `#58A700` | Texto do feedback de acerto, sombra dos botões |
| `verde_claro` | `#D7FFB8` | Fundo do painel de acerto |
| `vermelho` | `#FF4B4B` | Erro, botão de reset |
| `vermelho_escuro` | `#EA2B2B` | Texto do feedback de erro |
| `vermelho_claro` | `#FFDFE0` | Fundo do painel de erro |
| `azul_selecao` | `#1CB0F6` | Borda da alternativa selecionada |
| `azul_claro` | `#DDF4FF` | Fundo da alternativa selecionada |
| `cinza_borda` | `#E5E5E5` | Bordas de cartões e alternativas |
| `cinza_bloqueado` | `#AFAFAF` | Lição bloqueada, fogo apagado |
| `texto_principal` | `#3C3C3C` | Textos |
| `texto_secundario` | `#777777` | Legendas e fonte da questão |
| `fundo` | `#FFFFFF` | Fundo das telas |
| `laranja_fogo` | `#FF9600` | Ofensiva |
| `amarelo_xp` | `#FFC800` | XP e conquistas |
| `area_linguagens` | `#1CB0F6` | Azul |
| `area_humanas` | `#FF9600` | Laranja |
| `area_natureza` | `#58CC02` | Verde |
| `area_matematica` | `#CE82FF` | Roxo |

### 7.3 Componentes reutilizáveis

**`layout_status.xml`** (barra de status, incluída com `<include>` no Início e na Trilha):

```text
LinearLayout (horizontal, 3 partes iguais)
├── TextView  "🔥 3"   (laranja se estudou hoje, cinza se não)
├── TextView  "⭐ 240"
└── TextView  "❤️ 5"
```

**`item_alternativa.xml`** (incluído 5 vezes no Quiz, com ids `alt_a` a `alt_e`):

```text
MaterialCardView (clicável, borda 2dp cinza, raio 12dp)
└── LinearLayout (horizontal, padding 12dp)
    ├── TextView  letra "A" (círculo 32dp, fundo com borda)
    └── TextView  texto da alternativa (quebra de linha livre)
```

Estados visuais da alternativa (definidos no Java trocando a cor da borda e do fundo do card):

| Estado | Borda | Fundo |
|---|---|---|
| Normal | `cinza_borda` | branco |
| Selecionada | `azul_selecao` | `azul_claro` |
| Correta (após verificar) | `verde_primario` | `verde_claro` |
| Errada (após verificar) | `vermelho` | `vermelho_claro` |

> **Conceito: `<include>`.** Permite escrever um pedaço de layout uma vez e reaproveitar em vários lugares. No Java, cada alternativa é encontrada pelo id do include (`alt_a`) e, dentro dela, pelos ids internos (`txt_letra`, `txt_texto`).

### 7.4 Interface de cada tela

**Onboarding (`activity_onboarding.xml`)**

```text
ScrollView
└── LinearLayout (vertical, centralizado, padding 24dp)
    ├── ImageView       logo (120dp)
    ├── TextView        "Oi! Eu sou o ENEMigo 👋" (24sp, negrito)
    ├── TextView        "Vou te ajudar a estudar pro ENEM um pouquinho todo dia."
    ├── TextInputLayout (hint "Como você quer ser chamado?", contador 20)
    │   └── TextInputEditText (inputType textPersonName, maxLength 20)
    ├── TextView        "Qual a sua meta diária?"
    ├── RadioGroup
    │   ├── RadioButton "Casual — 20 XP por dia (≈ 2 lições)"
    │   ├── RadioButton "Regular — 50 XP por dia (≈ 5 lições)"  [marcado]
    │   └── RadioButton "Intenso — 100 XP por dia (≈ 10 lições)"
    └── MaterialButton  "COMEÇAR" (largura total)
```

**MainActivity (`activity_main.xml`)**

```text
ConstraintLayout
├── FrameLayout (id fragment_container, ocupa tudo acima da barra)
└── BottomNavigationView (id bottom_nav, menu @menu/menu_bottom_nav, fixo embaixo)
```

**Início (`fragment_inicio.xml`)**

```text
ScrollView
└── LinearLayout (vertical, padding 16dp)
    ├── include layout_status
    ├── TextView        "Oi, Ana! Bora manter o ritmo?" (20sp)
    ├── MaterialCardView (meta diária)
    │   ├── TextView    "Meta diária"
    │   ├── TextView    "32 / 50 XP"
    │   └── LinearProgressIndicator (max = meta)
    ├── MaterialCardView (Continuar — complementar, GONE se não houver sugestão)
    │   ├── TextView    "Continuar de onde parou"
    │   └── TextView    "Matemática · Lição 3 — Funções"
    ├── TextView        "Áreas do ENEM"
    └── GridLayout (2 colunas) ou 2 LinearLayouts horizontais
        ├── MaterialCardView Linguagens  (ImageView ícone + TextView nome + TextView "1/3 lições")
        ├── MaterialCardView Humanas
        ├── MaterialCardView Natureza
        └── MaterialCardView Matemática
```

> Como as áreas são sempre 4, cartões fixos no XML são mais simples que uma RecyclerView. A RecyclerView fica para as listas de tamanho variável (lições, caderno e conquistas).

**Trilha (`activity_trilha.xml` + `item_licao.xml`)**

```text
LinearLayout (vertical)
├── MaterialToolbar     título = nome da área, navigationIcon = seta voltar, cor da área
├── include layout_status
├── TextView            "2 de 3 lições concluídas"
└── RecyclerView        (LinearLayoutManager vertical)

item_licao.xml:
MaterialCardView
└── LinearLayout (horizontal)
    ├── TextView  círculo 56dp com "✓", número ou "🔒" (fundo na cor da área ou cinza)
    └── LinearLayout (vertical)
        ├── TextView  "Lição 2 · ~4 min"
        ├── TextView  "Interpretação de gráficos" (negrito)
        └── TextView  "Melhor: 3/4" | "Começar" | "Acerte 60% na lição anterior"
```

**Quiz (`activity_quiz.xml`)**

```text
ConstraintLayout
├── LinearLayout (topo, horizontal)
│   ├── ImageButton               "X" (ic_close)
│   ├── LinearProgressIndicator   (peso 1, max = questões da lição, cantos arredondados)
│   └── TextView                  "❤️ 4"  (GONE no modo REVISAO)
├── ScrollView (entre o topo e a área inferior)
│   └── LinearLayout (vertical, padding 16dp)
│       ├── TextView           "Questão 2 de 4"
│       ├── MaterialCardView   texto-base (fundo cinza claro; GONE se vazio)
│       │   └── TextView
│       ├── TextView           enunciado (17sp)
│       ├── TextView           "Fonte: ENEM 2019" (12sp, cinza)
│       ├── include item_alternativa (alt_a)
│       ├── include item_alternativa (alt_b)
│       ├── include item_alternativa (alt_c)
│       ├── include item_alternativa (alt_d)
│       └── include item_alternativa (alt_e)
├── MaterialButton  "VERIFICAR" (fixo embaixo, desabilitado até escolher)
└── LinearLayout    painel_feedback (fixo embaixo, GONE; fundo verde_claro ou vermelho_claro)
    ├── TextView        "Mandou bem! 🎉" / "Resposta correta: C" (negrito)
    ├── ScrollView (altura máxima ~160dp)
    │   └── TextView    explicação
    └── MaterialButton  "CONTINUAR" (verde no acerto, vermelho no erro)
```

**Resultado (`activity_resultado.xml`)**

```text
LinearLayout (vertical, centralizado, padding 24dp)
├── TextView        "🏆" (64sp)
├── TextView        "Perfeito!" / "Lição concluída!" / "Quase lá!" (26sp, negrito)
├── TextView        "Você acertou 3 de 4"
├── LinearLayout (horizontal, 2 cartões)
│   ├── MaterialCardView  "⭐ +45 XP"
│   └── MaterialCardView  "🔥 6 dias"
├── TextView        mensagens (meta batida, desbloqueio, conquistas; uma por linha)
├── MaterialButton  "CONTINUAR"
└── MaterialButton  "REFAZER LIÇÃO" (estilo outlined; GONE no modo REVISAO)
```

**Revisão (`fragment_revisao.xml` + `item_caderno.xml`)**

```text
LinearLayout (vertical, padding 16dp)
├── TextView        "Caderno de erros" (22sp, negrito)
├── TextView        "Você tem 7 questões para revisar"
├── TextView        "Acerte na revisão e ganhe ❤️ +1"
├── MaterialButton  "REVISAR AGORA"
├── RecyclerView    lista do caderno
└── LinearLayout    estado vazio (GONE quando há itens)
    ├── TextView "🎉" (48sp)
    └── TextView "Nenhum erro por aqui! Continue fazendo lições."

item_caderno.xml:
MaterialCardView
└── LinearLayout (vertical)
    ├── TextView  etiqueta da área (fundo arredondado na cor da área, texto branco)
    ├── TextView  enunciado (maxLines 2, ellipsize end)
    └── TextView  "ENEM 2019"
```

**Perfil (`fragment_perfil.xml` + `item_conquista.xml`)**

```text
ScrollView
└── LinearLayout (vertical)
    ├── MaterialToolbar     "Perfil" + item de menu engrenagem
    ├── TextView            avatar "A" (80dp, círculo verde, texto branco 36sp)
    ├── TextView            "Ana"
    ├── TextView            "Nível 4"
    ├── LinearProgressIndicator (xp % 100)
    ├── TextView            "35 / 100 XP para o nível 5"
    ├── GridLayout (2 colunas) com 6 MaterialCardViews de estatística
    │   (XP total · Ofensiva atual · Maior ofensiva · Lições concluídas · Questões respondidas · Taxa de acerto)
    ├── TextView            "Conquistas (2/6)"
    └── RecyclerView        GridLayoutManager(3), nestedScrollingEnabled = false

item_conquista.xml:
LinearLayout (vertical, centralizado)
├── TextView  emoji (36sp)  — alpha 0.3 se bloqueada
├── TextView  título (negrito, 12sp)
└── TextView  descrição (11sp, cinza)
```

**Configurações (`activity_configuracoes.xml`)**

```text
LinearLayout (vertical)
├── MaterialToolbar   "Configurações" + seta voltar
└── ScrollView
    └── LinearLayout (padding 16dp)
        ├── TextInputLayout + TextInputEditText (nome)
        ├── TextView  "Meta diária"
        ├── RadioGroup (20 / 50 / 100)
        ├── SwitchMaterial "Vibrar ao errar" (opcional)
        ├── MaterialButton "SALVAR"
        ├── View (divisor)
        ├── MaterialButton "RESETAR PROGRESSO" (fundo vermelho)
        └── TextView "Sobre: ENEMigo v1.0 — questões das provas oficiais do ENEM (INEP)..."
```

**Diálogos (`AlertDialog` do `MaterialAlertDialogBuilder`):**

- Sair da lição (Sair / Continuar estudando).
- Sem vidas (Ir para Revisão / OK).
- Vidas acabaram durante a lição (Ir para Revisão / Sair).
- Resetar progresso (Cancelar / Apagar tudo).

---

## 8. Modelo de dados

O app tem dois tipos de dados:

1. **Conteúdo (somente leitura):** áreas, lições e questões. Vêm do arquivo `assets/questoes.json` e ficam em memória.
2. **Progresso do aluno (leitura e escrita):** perfil, XP, vidas, ofensiva, resultados das lições, caderno de erros e conquistas. Ficam no SharedPreferences.

### 8.1 Area (enum Java `Area`)

As áreas são fixas, por isso viram um `enum`, e não uma classe comum.

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `id` | String | Sim | Identificador usado no JSON: `LINGUAGENS`, `HUMANAS`, `NATUREZA`, `MATEMATICA` |
| `nome` | String | Sim | Nome exibido: "Linguagens", "Ciências Humanas", "Ciências da Natureza", "Matemática" |
| `sigla` | String | Sim | Prefixo dos ids: `LIN`, `HUM`, `NAT`, `MAT` |
| `corRes` | int | Sim | Recurso de cor (`R.color.area_linguagens` etc.) |
| `iconeRes` | int | Sim | Recurso de ícone (`R.drawable.ic_area_linguagens` etc.) |

### 8.2 Licao (classe `Licao`)

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `id` | String | Sim | Ex.: `MAT-01` (sigla + número de 2 dígitos) |
| `area` | Area | Sim | Área a que a lição pertence |
| `ordem` | int | Sim | Posição na trilha (0, 1, 2...), definida pela ordem no JSON |
| `titulo` | String | Sim | Tema da lição, ex.: "Porcentagem, proporção e juros" |
| `questoes` | List\<Questao\> | Sim | De 3 a 5 questões |

`getSegundosEstimados()` soma a estimativa das questões; é o que a trilha mostra ("~4 min").

### 8.3 Questao (classe `Questao`)

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `id` | String | Sim | Único no app. Ex.: `MAT-01-03` (lição + número da questão) |
| `area` | Area | Sim | Preenchido pelo repositório a partir da área do JSON |
| `licaoId` | String | Sim | Lição a que pertence |
| `texto` | String | Não | Texto-base (trecho, poema, notícia). Vazio se não houver |
| `enunciado` | String | Sim | O comando da questão |
| `alternativas` | String[5] | Sim | Textos das alternativas A, B, C, D e E, nessa ordem. Em `texto`, `enunciado` e `alternativas` podem aparecer os marcadores `[[imagem:arquivo.png]]` e `[[formula:arquivo.png]]` (arquivos em `assets/imagens/`, só em Matemática) |
| `correta` | int | Sim | Índice da correta (0 = A ... 4 = E). No JSON é a letra ("C") e o repositório converte |
| `explicacao` | String | Sim | Por que a alternativa correta está certa (1 a 3 frases) |
| `fonte` | String | Sim | Origem: "ENEM 2019", "ENEM 2022 — 2º dia" ou "Autoral (estilo ENEM)" |
| `segundosEstimados` | int | Não | Tempo estimado para ler e responder (ver 9.4). 0 se o JSON não tiver |

### 8.4 Conquista (enum Java `Conquista`)

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `id` | String | Sim | `PRIMEIRA_LICAO`, `OFENSIVA_3`, `OFENSIVA_7`, `XP_100`, `XP_500`, `LICAO_PERFEITA` |
| `titulo` | String | Sim | Ex.: "Primeiro passo" |
| `descricao` | String | Sim | Ex.: "Conclua sua primeira lição" |
| `emoji` | String | Sim | Ex.: "🎯" |

Lista de conquistas:

| id | Emoji | Título | Condição |
|---|---|---|---|
| `PRIMEIRA_LICAO` | 🎯 | Primeiro passo | Terminar a primeira lição (qualquer nota) |
| `OFENSIVA_3` | 🔥 | Pegando fogo | Ofensiva de 3 dias |
| `OFENSIVA_7` | 🌋 | Semana imbatível | Ofensiva de 7 dias |
| `XP_100` | ⭐ | Cem na conta | 100 XP totais |
| `XP_500` | 🌟 | Estudante dedicado | 500 XP totais |
| `LICAO_PERFEITA` | 🏆 | Gabaritou! | Acertar todas as questões de uma lição |

### 8.5 Progresso do aluno (gerenciado pela classe `ProgressoManager`)

Não precisa de uma classe de modelo: o `ProgressoManager` expõe *getters* e métodos que leem e gravam cada valor no SharedPreferences.

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `nome` | String | Sim | Nome do aluno (2 a 20 caracteres) |
| `metaDiaria` | int | Sim | 20, 50 ou 100. Padrão 50 |
| `onboardingConcluido` | boolean | Sim | `true` depois do primeiro cadastro |
| `xpTotal` | int | Sim | XP acumulado. Padrão 0 |
| `xpHoje` | int | Sim | XP ganho no dia atual |
| `dataXpHoje` | String (data ISO) | Não | Dia a que `xpHoje` se refere, ex.: "2026-09-24" |
| `ofensiva` | int | Sim | Dias seguidos estudando |
| `maiorOfensiva` | int | Sim | Recorde de ofensiva |
| `ultimaDataEstudo` | String (data ISO) | Não | Último dia com sessão concluída |
| `vidas` | int | Sim | 0 a 5. Padrão 5 |
| `ultimaRecargaVida` | long (ms) | Sim | Momento de referência para contar a recuperação de vidas |
| `melhorPorLicao` | int (por lição) | Sim | Melhor número de acertos de cada lição (−1 = nunca feita) |
| `cadernoErros` | Set\<String\> | Sim | Ids das questões erradas pendentes |
| `conquistas` | Set\<String\> | Sim | Ids das conquistas desbloqueadas |
| `licoesPerfeitas` | int | Sim | Quantas vezes acertou todas as questões de uma lição |
| `totalRespondidas` | int | Sim | Total de questões respondidas (lições + revisão) |
| `totalAcertos` | int | Sim | Total de acertos (lições + revisão) |
| `ultimaAreaEstudada` | String | Não | Id da última área, usado no cartão Continuar |
| `vibracaoAtiva` | boolean | Sim | Padrão `true` (opcional C10) |
| `tempoTotalMs`, `tempoQuestoes`, `tempoEstimadoS` | long, int, long | Sim | Somas do tempo real medido, de quantas questões e da estimativa delas (RN38, RN39) |
| `tempoHojeMs` + `dataTempoHoje` | long + String (data ISO) | Não | Tempo respondendo questões no dia atual |
| `versaoProgresso` | int | Sim | Formato do progresso salvo; abaixo de 2, roda a migração (RN37) |

### 8.6 ResultadoSessao (classe `ResultadoSessao`)

Objeto simples devolvido pelo `ProgressoManager` quando uma sessão termina. A QuizActivity copia os campos para os *extras* da Intent do Resultado.

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `modo` | String | Sim | `LICAO` ou `REVISAO` |
| `acertos` | int | Sim | Acertos na sessão |
| `total` | int | Sim | Questões respondidas (as 3 a 5 da lição; até 5 na revisão) |
| `xpGanho` | int | Sim | XP dos acertos (sem o bônus) |
| `bonus` | int | Sim | 5 se foi lição perfeita, senão 0 |
| `ofensiva` | int | Sim | Ofensiva depois da sessão |
| `metaBatidaAgora` | boolean | Sim | `true` só se a meta foi atingida nesta sessão |
| `desbloqueouProxima` | boolean | Sim | `true` se esta sessão liberou a lição seguinte pela primeira vez |
| `vidasRecuperadas` | int | Sim | Vidas ganhas (modo REVISAO) |
| `novasConquistas` | ArrayList\<String\> | Sim | Ids das conquistas desbloqueadas nesta sessão |

### 8.7 Relacionamentos

```text
Area (4) 1 ──── N Licao 1 ──── 3..5 Questao
                  │                  │
                  │ id               │ id
                  ▼                  ▼
   melhorPorLicao[licaoId]    cadernoErros {questaoId, ...}
```

- Uma **Area** tem várias **Lições**, e a ordem no JSON define a sequência da trilha.
- Uma **Lição** tem de **3 a 5 Questões**, agrupadas para dar uns 4 minutos (ver 9.4).
- O progresso **não guarda objetos**, só ids. O `melhorPorLicao` é indexado pelo id da lição, e o caderno de erros guarda ids de questões. Para exibir, o app busca a questão completa no `QuestaoRepository` pelo id.
- Os **estados da lição** (concluída, disponível, bloqueada) **não são salvos**: são calculados a partir do `melhorPorLicao` (ver RN05 e RN06). Assim não existe risco de o estado salvo contradizer o resultado.

---

## 9. Armazenamento

### 9.1 Estratégia escolhida

| Dado | Onde fica | Por quê |
|---|---|---|
| Questões, lições e áreas | `app/src/main/assets/questoes.json`, lido com **org.json** e guardado em memória | É conteúdo fixo que vai junto com o app. Não muda durante o uso, então basta ler uma vez |
| Progresso do aluno | **SharedPreferences** (arquivo `enemigo_prefs`) | São poucos valores simples (números, textos e conjuntos de ids). O SharedPreferences é a forma mais simples de guardar isso no Android |
| Estado do quiz em andamento | **Memória** (variáveis da QuizActivity) | Dura só enquanto a lição está aberta. Se o aluno sair, a lição é descartada (RN12) |

> **Conceito: `assets/`.** É uma pasta do projeto cujos arquivos vão dentro do APK exatamente como estão. O app lê com `getAssets().open("questoes.json")`, que devolve um `InputStream`. Esse conteúdo vira uma `String` (em UTF-8, por causa dos acentos) e depois um `JSONObject`.

> **Conceito: `org.json`.** As classes `JSONObject` e `JSONArray` já fazem parte do Android SDK. Não é preciso adicionar nenhuma dependência no Gradle.

> **Conceito: SharedPreferences.** É um "dicionário" chave → valor salvo num arquivo XML privado do app. Guarda `int`, `long`, `boolean`, `String` e `Set<String>`. Para gravar: `edit().putInt(...).apply()`. O `apply()` grava em segundo plano, sem travar a tela.

### 9.2 Por que não usar Room (nem SQLite direto) neste escopo

O Room é a biblioteca oficial de banco de dados do Android, mas aqui ele traria mais trabalho do que benefício:

- **Volume pequeno:** o progresso tem cerca de 20 valores, mais dois conjuntos de ids. Não existem tabelas com milhares de linhas nem consultas com filtro ou ordenação.
- **O conteúdo é somente leitura:** as questões nunca são editadas pelo usuário. Copiar o JSON para um banco só criaria uma segunda cópia do mesmo dado.
- **Configuração extra:** o Room exige dependências no Gradle, *annotation processor*, classes `@Entity`, interfaces `@Dao`, uma classe `@Database` e, principalmente, **acesso fora da thread principal** (por padrão, o Room proíbe consultas na UI thread). Isso obrigaria a usar threads ou `Executor`, um tema que atrapalharia um projeto de 4 aulas.
- **Migrações:** qualquer mudança nas tabelas exige escrever uma migração de versão.

**Quando o Room passaria a fazer sentido:** se o app guardasse o histórico de cada resposta (data, questão, alternativa marcada) para gerar estatísticas por período ou por habilidade, ou se tivesse milhares de questões com filtros por ano e assunto. Isso está nas melhorias futuras (seção 20).

### 9.3 Formato do `questoes.json`

Estrutura: um objeto raiz com `versao` (hoje `2`), a lista `areas` e o mapa `ids_v1`. Cada área tem a lista `licoes`, e cada lição tem a lista `questoes`.

Regras do arquivo:

- `id` da área deve ser um dos 4 valores do enum `Area`.
- A ordem das lições no array é a ordem da trilha.
- Cada lição deve ter **de 3 a 5 questões** (lição fora disso é ignorada pelo app).
- `segundos_estimados` (inteiro) é o tempo estimado da questão, calculado pelo `montar_questoes.py` (ver 9.4). A trilha soma os da lição.
- `ids_v1` leva o id de cada questão na versão 1 do banco (4 lições de 5 por área) ao id atual, ex.: `"LIN-01-02": "LIN-06-01"`. Só serve para migrar o progresso salvo (RN37).
- `alternativas` deve ter **exatamente 5 textos**, sem a letra na frente (o app coloca "A", "B"...).
- `correta` é uma letra de `"A"` a `"E"`.
- `texto` pode ser `""` quando a questão não tem texto-base.
- Aspas dentro de textos: use aspas tipográficas “assim” para não precisar escapar, ou escape com `\"`.
- O arquivo deve ser salvo em **UTF-8**.

**Exemplo reduzido** (3 áreas, 1 lição cada, **1 questão por lição** só para ilustrar; no arquivo real cada lição tem de 3 a 5 questões, a área Humanas também aparece e o `ids_v1` tem as 80 questões):

```json
{
  "versao": 2,
  "areas": [
    {
      "id": "LINGUAGENS",
      "licoes": [
        {
          "id": "LIN-01",
          "titulo": "Variação linguística",
          "questoes": [
            {
              "id": "LIN-01-01",
              "texto": "Galera, amanhã a aula começa mais cedo! Bora chegar às 7h, que o professor vai fazer a revisão pro simulado. Não vacila, hein!",
              "enunciado": "A mensagem acima foi enviada no grupo de conversa de uma turma do ensino médio. O uso de expressões como “galera”, “bora” e “não vacila” revela que o autor da mensagem",
              "alternativas": [
                "cometeu desvios que impedem a compreensão da mensagem pelos colegas.",
                "deveria ter empregado a norma-padrão, já que o assunto tratado é escolar.",
                "utilizou gírias de uma única região do país, desconhecidas pelos colegas.",
                "adequou a linguagem à situação informal e ao grupo com quem se comunica.",
                "demonstrou desconhecimento das regras gramaticais da língua portuguesa."
              ],
              "correta": "D",
              "explicacao": "A linguagem deve ser adequada à situação de comunicação. Numa conversa entre colegas, o registro informal é o esperado e não atrapalha o entendimento. Seria inadequado usar gírias numa redação ou num documento oficial, não num grupo de amigos.",
              "fonte": "Autoral (estilo ENEM)",
              "segundos_estimados": 52
            }
          ]
        }
      ]
    },
    {
      "id": "NATUREZA",
      "licoes": [
        {
          "id": "NAT-01",
          "titulo": "Mudanças de estado físico",
          "questoes": [
            {
              "id": "NAT-01-01",
              "texto": "",
              "enunciado": "Ao retirar uma lata de refrigerante da geladeira em um dia quente, uma pessoa percebe que, após alguns minutos, a superfície externa da lata fica coberta de gotículas de água. Esse fenômeno ocorre porque",
              "alternativas": [
                "o refrigerante atravessa as paredes de alumínio da lata.",
                "o gás carbônico do refrigerante se transforma em água ao escapar da lata.",
                "o vapor de água presente no ar se condensa ao entrar em contato com a superfície fria da lata.",
                "o gelo acumulado na geladeira derrete quando a lata é exposta ao ambiente.",
                "a lata libera a água que absorveu enquanto estava refrigerada."
              ],
              "correta": "C",
              "explicacao": "O ar contém vapor de água. Ao encostar na lata gelada, esse vapor perde calor e passa para o estado líquido (condensação). A água vem do ar, e não de dentro da lata, que é impermeável.",
              "fonte": "Autoral (estilo ENEM)",
              "segundos_estimados": 58
            }
          ]
        }
      ]
    },
    {
      "id": "MATEMATICA",
      "licoes": [
        {
          "id": "MAT-01",
          "titulo": "Porcentagem",
          "questoes": [
            {
              "id": "MAT-01-01",
              "texto": "",
              "enunciado": "Uma loja anunciou uma promoção: todos os produtos com 20% de desconto e, para pagamento via Pix, mais 10% de desconto sobre o preço já promocional. Um cliente comprou, pagando via Pix, um tênis que custava R$ 250,00 antes da promoção. Quanto ele pagou pelo tênis?",
              "alternativas": [
                "R$ 175,00",
                "R$ 180,00",
                "R$ 187,50",
                "R$ 195,00",
                "R$ 225,00"
              ],
              "correta": "B",
              "explicacao": "Descontos sucessivos se multiplicam: 250 × 0,80 = 200 e 200 × 0,90 = 180. O erro comum é somar os descontos (30%), o que daria R$ 175,00.",
              "fonte": "Autoral (estilo ENEM)",
              "segundos_estimados": 92
            }
          ]
        }
      ]
    }
  ],
  "ids_v1": {
    "LIN-01-01": "LIN-01-01",
    "NAT-01-01": "NAT-01-01",
    "MAT-01-01": "MAT-01-01"
  }
}
```

**Gabarito conferido do exemplo:** LIN-01-01 = D · NAT-01-01 = C · MAT-01-01 = B.

### 9.4 Como montar o banco de questões

- **Quantidade mínima:** 2 lições por área na Aula 2 (40 questões). Meta para a apresentação: **3 lições por área (60 questões)**.
- **Fonte oficial:** as provas e gabaritos do ENEM são públicos no site do **INEP** (gov.br/inep → "Provas e gabaritos"). Escolha **apenas questões só de texto**, sem gráfico, mapa, tabela em imagem ou charge. Em Matemática, dê preferência a questões sem figura.
- **Sempre** preencha `fonte` com o ano (ex.: "ENEM 2019") e **confira o gabarito no gabarito oficial** do INEP.
- As explicações são escritas pelo aluno. Se usar IA para ajudar a redigir, **revise uma por uma**: a IA pode errar a justificativa.
- Questões muito longas funcionam (a tela tem rolagem), mas lições curtas ficam mais agradáveis com textos-base menores.
- Distribua bem as letras corretas (não deixe tudo "C").
- Use ids consistentes: `SIGLA-LL-QQ` (ex.: `HUM-02-04`). Repetir um id quebra o caderno de erros.

**Tempo estimado e tamanho das lições.** O banco é gerado pelo `tools/montar_questoes.py` e conferido pelo `tools/validar_questoes.py`. As constantes ficam no começo do `montar_questoes.py`:

- `PALAVRAS_POR_MINUTO = 180`: leitura do texto-base + enunciado + alternativas (os marcadores de figura não contam como palavra).
- `RESOLUCAO_S`: tempo para resolver, por área: Linguagens 20 s, Humanas 30 s, Natureza 30 s, Matemática 75 s.
- `EXTRA_FIGURA_S = 15`: a mais, uma vez, quando a questão tem figura ou fórmula.
- `ALVO_LICAO_S = 240` (4 min) e `MIN_QUESTOES, MAX_QUESTOES = 3, 5`.

Tempo da questão = palavras × 60 ÷ 180 + resolução da área (+ 15 s), arredondado, gravado em `segundos_estimados`. As lições ficam em `AREAS`, escritas à mão para manter o tema no título, e o validador cobra: de 3 a 5 questões por lição; nenhuma lição acima de 6 min (`TETO_LICAO_S`); média da área no máximo 1 min acima do alvo (`FOLGA_MEDIA_S`); e a ordem da trilha seguindo a das lições da versão 1, para a migração (RN37) não deixar buraco. Quando nem com o máximo de lições a área chega perto do alvo (Matemática: 3 questões já passam de 5 min), as regras de tempo não se aplicam e a área fica com o maior número de lições possível, de 3 ou 4 questões.

Mudou uma constante? Rode `python tools/montar_questoes.py` e `python tools/validar_questoes.py`: o validador aponta a lição que saiu do alvo e imprime o tempo mínimo e máximo de cada área. Hoje: Linguagens 6 lições (3,9 a 5,1 min), Humanas 5 (3,5 a 5,7), Natureza 6 (3,0 a 5,8) e Matemática 6 (5,6 a 7,9).

### 9.5 Chaves do SharedPreferences

Arquivo: `enemigo_prefs` (modo `Context.MODE_PRIVATE`). Todas as chaves ficam como constantes na classe `Constantes`.

| Chave | Tipo | Padrão |
|---|---|---|
| `nome` | String | `""` |
| `meta_diaria` | int | `50` |
| `onboarding_ok` | boolean | `false` |
| `xp_total` | int | `0` |
| `xp_hoje` | int | `0` |
| `data_xp_hoje` | String | `""` |
| `ofensiva` | int | `0` |
| `maior_ofensiva` | int | `0` |
| `ultima_data_estudo` | String | `""` |
| `vidas` | int | `5` |
| `ultima_recarga_vida` | long | `0` |
| `melhor_<licaoId>` (ex.: `melhor_MAT-01`) | int | `-1` |
| `caderno_erros` | Set\<String\> | vazio |
| `conquistas` | Set\<String\> | vazio |
| `licoes_perfeitas` | int | `0` |
| `total_respondidas` | int | `0` |
| `total_acertos` | int | `0` |
| `ultima_area` | String | `""` |
| `vibracao` | boolean | `true` |
| `versao_progresso` | int | `1` (sem a chave = progresso da versão 1, que é migrado e passa a `2`) |
| `tempo_total_ms` | long | `0` |
| `tempo_questoes` | int | `0` |
| `tempo_estimado_s` | long | `0` |
| `tempo_hoje_ms` | long | `0` |
| `data_tempo_hoje` | String | `""` |
| `lembrete_ativo` | boolean | `true` |
| `lembrete_horario` | int (minutos desde a meia-noite) | `1140` (19h) |
| `notificacao_pedida` | boolean | `false` (a Lia já pediu a permissão de notificações) |

> ⚠️ **Pegadinha importante do `getStringSet`:** o `Set` devolvido pelo SharedPreferences **não pode ser modificado diretamente**. Se você der `add()` nele e salvar de novo, o Android pode não gravar a mudança. Sempre crie uma cópia: `new HashSet<>(prefs.getStringSet(chave, new HashSet<>()))`, altere a cópia e salve a cópia.

> **Datas:** guarde datas como texto ISO (`LocalDate.now().toString()` → `"2026-09-24"`) e leia com `LocalDate.parse(...)`. É por isso que o `minSdk` é 26: a classe `java.time.LocalDate` só existe a partir do Android 8.0 (API 26).

### 9.6 APIs externas

Não são usadas nesta versão. O app funciona 100% offline e não pede a permissão de Internet. Um banco de questões online está em "melhorias futuras" (seção 20).

---

## 10. Regras de negócio

**Perfil e acesso**

- **RN01:** o onboarding aparece somente se `onboarding_ok` for `false`. Depois de concluído, o app sempre abre na MainActivity.
- **RN02:** a meta diária aceita somente 20, 50 ou 100 XP. O padrão é 50.

**Conteúdo**

- **RN03:** as lições de uma área aparecem na ordem do JSON. A primeira tem ordem 0.
- **RN04:** cada lição tem de 3 a 5 questões, exibidas na ordem do JSON. As alternativas **não** são embaralhadas (a letra faz parte da questão original).
- **RN36:** cada lição mira uns **4 minutos** pela estimativa (ver 9.4): leitura a 180 palavras/min + resolução por área (Linguagens 20 s, Humanas 30 s, Natureza 30 s, Matemática 75 s) + 15 s com figura ou fórmula. A trilha mostra a soma arredondada, "~4 min" (no mínimo 1 min).

**Trilha e desbloqueio**

- **RN05:** uma lição está **disponível** se for a primeira da área (ordem 0) **ou** se a lição anterior estiver concluída.
- **RN06:** uma lição está **concluída** se o `melhor` dela tiver pelo menos **60% de acertos**, arredondado para cima: `melhor ≥ ⌈0,6 × questões⌉` (2 de 3, 3 de 4, 3 de 5). Caso contrário, e se não estiver disponível, está **bloqueada**.
- **RN07:** o `melhor` de uma lição só é atualizado se o novo resultado for maior que o anterior.
- **RN08:** lições concluídas podem ser refeitas quantas vezes o aluno quiser e dão XP normalmente.
- **RN09:** "desbloqueou a próxima" é `true` quando o `melhor` anterior não concluía a lição (RN06), o novo resultado conclui e existe uma lição seguinte na área.

**Quiz**

- **RN10:** o VERIFICAR só é habilitado depois que uma alternativa é selecionada. Depois de verificar, as alternativas ficam travadas até o CONTINUAR.
- **RN11:** no modo LICAO, cada erro tira 1 vida **imediatamente** (salva na hora, para o aluno não "fugir" da perda fechando o app) e adiciona o id da questão ao caderno de erros (sem duplicar, porque é um `Set`).
- **RN12:** se o aluno sair da lição antes do fim (X, Voltar ou vidas zeradas), a lição **não** é registrada: não ganha XP, não atualiza o `melhor` nem a ofensiva. As vidas perdidas e os erros já adicionados ao caderno continuam.
- **RN13:** se as vidas chegarem a 0 durante uma lição, depois do feedback daquela questão a lição é encerrada com o diálogo "Suas vidas acabaram".

**XP, nível e meta**

- **RN14:** cada acerto em lição vale **+10 XP**. Lição com 100% de acertos ganha **+5 XP de bônus**. Uma lição de 3 vale no máximo 35 XP; uma de 5, 55 XP.
- **RN15:** cada acerto na revisão vale **+5 XP**.
- **RN16:** o XP é somado ao `xpTotal` e ao `xpHoje` **somente quando a sessão termina** (na hora de abrir o Resultado).
- **RN17:** nível = `xpTotal / 100 + 1` (divisão inteira). XP dentro do nível = `xpTotal % 100`.
- **RN18:** se `data_xp_hoje` for diferente da data de hoje, o `xpHoje` volta a 0 antes de somar ou exibir.
- **RN19:** "meta batida agora" é `true` quando `xpHoje` antes da sessão era menor que a meta e depois ficou maior ou igual.

**Vidas**

- **RN20:** o máximo é 5 vidas e o mínimo é 0.
- **RN21:** quando o aluno perde uma vida estando com 5, `ultima_recarga_vida` recebe o momento atual (o "relógio" começa a contar).
- **RN22:** recuperação (calculada sempre que as vidas são lidas, sem serviço em segundo plano):
  - `passou = agora − ultimaRecarga`;
  - `ganhas = passou / 30 minutos` (divisão inteira);
  - se `ganhas > 0`: `vidas = min(5, vidas + ganhas)` e `ultimaRecarga = ultimaRecarga + ganhas × 30 min`;
  - se `vidas == 5`, o relógio para.
- **RN23:** minutos para a próxima vida = `30 − (passou em minutos % 30)`. Só aparece se `vidas < 5`.
- **RN24:** para começar uma lição é preciso ter pelo menos 1 vida. A revisão pode ser feita com 0 vidas.
- **RN25:** no modo REVISAO, errar não tira vida. Acertar remove a questão do caderno e devolve +1 vida (sem passar de 5).

**Ofensiva**

- **RN26:** uma sessão conta como "estudou hoje" quando o aluno termina uma lição (todas as questões, com qualquer nota) ou termina uma revisão.
- **RN27:** ao terminar uma sessão:
  - se `ultimaDataEstudo` estiver vazia → ofensiva = 1;
  - se for hoje → ofensiva não muda;
  - se for ontem → ofensiva + 1;
  - se for antes de ontem → ofensiva = 1;
  - se for uma data **futura** (relógio do celular foi alterado) → ofensiva não muda;
  - depois: `ultimaDataEstudo = hoje` e `maiorOfensiva = max(maiorOfensiva, ofensiva)`.
- **RN28:** ao exibir, se `ultimaDataEstudo` não for hoje nem ontem, a ofensiva mostrada é 0 (e é salva como 0).
- **RN29:** o fogo fica colorido se `ultimaDataEstudo == hoje` e cinza caso contrário.

**Caderno de erros**

- **RN30:** a revisão usa até 5 questões sorteadas do caderno. Se houver menos de 5, usa todas.
- **RN31:** ids do caderno que não existem mais no JSON (ex.: questão removida numa atualização) são descartados silenciosamente ao carregar.

**Conquistas**

- **RN32:** as conquistas são verificadas ao fim de cada sessão, **depois** de atualizar XP e ofensiva. Uma conquista desbloqueada nunca é perdida (a não ser com o reset).
- **RN33:** só entram em `novasConquistas` as que não estavam desbloqueadas antes da sessão.

**Configurações**

- **RN34:** o reset apaga **todas** as chaves do SharedPreferences (`edit().clear()`) e volta para o onboarding, gravando só `versao_progresso = 2` (senão a próxima abertura migraria o progresso novo como se fosse da versão 1). As questões (assets) não são afetadas.

**Progresso salvo antes das lições curtas**

- **RN37:** o progresso salvo com o banco da versão 1 (4 lições de 5 por área) é migrado uma vez, na primeira abertura (`versao_progresso` ausente ou menor que 2). Conta como **concluída** a lição nova cujas questões estavam **todas** em lições antigas concluídas (melhor ≥ 3), com o `melhor` mínimo para concluir (ex.: 2/3); as outras recomeçam. O caderno de erros troca cada id antigo pelo novo (`ids_v1` do JSON). As chaves `melhor_` antigas são apagadas; XP, ofensiva, vidas, totais e conquistas não mudam. Como a ordem das lições novas segue a das antigas, as concluídas sempre ficam em sequência, sem buraco na trilha.

**Tempo real**

- **RN38:** o Quiz mede cada questão do momento em que ela aparece até o VERIFICAR, nos dois modos. O cronômetro para quando a tela sai (`onPause`, app em segundo plano) e continua ao voltar; tempo acima de **10 minutos** é descartado. O app guarda só somas (tempo total, quantas questões, a estimativa delas e o tempo de hoje), e o Perfil mostra o **tempo médio por questão** e os **minutos estudados hoje** (zera ao mudar o dia, como o XP de hoje).
- **RN39:** depois de **10 questões** medidas, o tempo da trilha segue o ritmo do aluno: estimativa × (tempo real ÷ estimativa das questões medidas), com o fator limitado entre 0,5 e 2.

**Cartão Continuar (complementar)**

- **RN35:** sugere a primeira lição disponível e ainda não concluída da `ultima_area`. Se a área estiver toda concluída, ou se não houver `ultima_area`, sugere a primeira lição não concluída de qualquer área, na ordem Linguagens → Humanas → Natureza → Matemática. Se tudo estiver concluído, o cartão fica escondido.

---

## 11. Validações

| Campo/Ação | Regra | Mensagem |
|---|---|---|
| Nome (Onboarding/Configurações) | Obrigatório (depois do `trim`) | "Digite seu nome" |
| Nome | Mínimo de 2 caracteres | "O nome precisa ter pelo menos 2 letras" |
| Nome | Máximo de 20 caracteres (também limitado no XML com `maxLength`) | "Use no máximo 20 caracteres" |
| Nome | Só letras e espaços (aceita acentos) | "Use apenas letras" |
| Meta diária | Um RadioButton marcado (20/50/100) | "Escolha uma meta diária" |
| Abrir lição bloqueada | Anterior concluída (60% de acertos, RN06) | Toast: "Complete a lição anterior com pelo menos 60% de acertos" |
| Abrir lição | Vidas ≥ 1 | Diálogo: "Sem vidas 💔 Próxima vida em X min. Revise seus erros para recuperar vidas agora!" |
| VERIFICAR sem alternativa | Botão desabilitado até escolher | (não há mensagem; o botão fica cinza) |
| Clique duplo em VERIFICAR | Botão desabilitado após o primeiro clique | (nenhuma) |
| Revisar com caderno vazio | Botão desabilitado se o caderno estiver vazio | Estado vazio: "Nenhum erro por aqui! 🎉" |
| Sair da lição (X/Voltar) | Confirmação obrigatória | "Sair da lição? Você vai perder o progresso desta lição." |
| Resetar progresso | Confirmação obrigatória | "Apagar todo o progresso? XP, ofensiva, lições e conquistas serão perdidos. Essa ação não pode ser desfeita." |
| Vidas zeradas durante a lição | Encerrar a lição | "Suas vidas acabaram 💔 Revise seus erros para recuperar vidas." |
| Salvar Configurações com sucesso | Nome válido | Toast: "Configurações salvas" |
| Questão do JSON (ao carregar) | `enunciado` não vazio, 5 alternativas, `correta` de A a E, `id` único | Log de aviso; a questão é ignorada (ver seção 12) |
| Lição do JSON (ao carregar) | De 3 a 5 questões válidas | Log de aviso; a lição é ignorada |

> **Dica:** a regra "só letras e espaços" pode ser feita com a expressão regular `^[\p{L} ]+$` (`\p{L}` aceita qualquer letra, inclusive com acento).

---

## 12. Tratamento de erros

| Situação | Como o app reage |
|---|---|
| `questoes.json` não existe ou não pode ser lido (`IOException`) | O repositório registra o erro com `Log.e`, devolve listas vazias e a tela Início mostra o aviso "Não foi possível carregar as questões." no lugar dos cartões. O app **não fecha** |
| JSON com erro de sintaxe (`JSONException`) | Igual ao caso acima. Dica: valide o arquivo num validador de JSON antes de rodar |
| Questão inválida (menos de 5 alternativas, `correta` fora de A–E, enunciado vazio, id repetido) | A questão é ignorada com `Log.w("QuestaoRepository", "Questão inválida: " + id)` |
| Lição com menos de 3 ou mais de 5 questões válidas | A lição é ignorada (com log). As seguintes continuam na ordem |
| Banco sem `ids_v1` ou que não carregou | A migração (RN37) não roda e tenta de novo na próxima abertura |
| Tempo de questão absurdo (mais de 10 min ou relógio estranho) | Não é registrado (RN38) |
| Área com id desconhecido no JSON | A área é ignorada (com log) |
| Área sem lições | O cartão mostra "Em breve" e, ao tocar, a trilha exibe "Nenhuma lição disponível ainda" |
| Intent sem o extra obrigatório (`AREA_ID`, `LICAO_ID`) ou com id inexistente | A Activity mostra o Toast "Lição não encontrada" e chama `finish()` |
| Id do caderno de erros que não existe mais | Removido do caderno ao carregar a aba Revisão (RN31) |
| Relógio do celular atrasado (`ultimaRecarga` no futuro) | `ultimaRecarga = agora` (nenhuma vida é ganha nem perdida) |
| Data de estudo no futuro | A ofensiva não é alterada (RN27) |
| Valor estranho no SharedPreferences (ex.: vidas = 9, meta = 37) | Os getters corrigem: vidas limitadas entre 0 e 5; meta inválida volta para 50 |
| Aluno fecha o app no meio da lição | A lição é descartada (RN12). Vidas perdidas e erros já foram salvos |
| Clique duplo em botões que abrem telas | Os botões de ação são desabilitados logo depois do clique (VERIFICAR, CONTINUAR no final da lição, SALVAR) |
| Texto muito longo na questão ou na explicação | ScrollView no conteúdo e altura máxima com rolagem na explicação |
| Exceção inesperada durante o desenvolvimento | Ler o **Logcat** filtrando pela tag da classe. Nunca usar `catch` vazio: sempre ao menos um `Log.e` |

> **Conceito: Logcat.** É o painel do Android Studio que mostra as mensagens do app em execução. Use `Log.d("Tag", "mensagem")` para acompanhar valores e `Log.e` para erros. É a principal ferramenta para descobrir por que algo não funciona.

---

## 13. Recursos visuais

| Recurso | Uso | Alternativa gratuita |
|---|---|---|
| Ícones da barra inferior (casa, caderno, pessoa), engrenagem, fechar, cadeado, check | BottomNavigationView, toolbars e trilha | **Material Icons** pelo Android Studio: *File → New → Vector Asset → Clip Art*. São gratuitos (licença Apache 2.0) e vetoriais |
| Ícones das áreas | Cartões do Início | Material Icons: `menu_book` (Linguagens), `public` (Humanas), `science` (Natureza), `calculate` (Matemática) |
| Ícone do app (launcher) | Tela do celular | **Image Asset Studio**: *File → New → Image Asset*. Pode usar um Clip Art (ex.: `school`) com fundo verde `#58CC02`, ou um texto "E!" |
| Logo na tela de Onboarding | Boas-vindas | O próprio ícone do app em tamanho grande, ou um emoji 🦉📚 num TextView de 64sp |
| 🔥 ⭐ ❤️ 🏆 🎯 🎉 🔒 | Status, conquistas e mensagens | **Emojis** em TextView: não precisam de arquivo nenhum e ficam coloridos em qualquer Android recente |
| Fonte Nunito | Todos os textos | **Google Fonts** (licença OFL). Pelo Android Studio: no layout, selecione um TextView → `fontFamily` → *More Fonts...* → Nunito. Isso cria `res/font` sozinho |
| Fundos arredondados (círculos da trilha, etiquetas, avatar) | Trilha, caderno e perfil | `drawable` em XML com `<shape>`: não precisa de imagem |
| Ilustração do mascote (opcional) | Onboarding e estado vazio | **unDraw** (undraw.co, gratuito, sem atribuição obrigatória) ou **Storyset** (gratuito com atribuição). Baixe em SVG e importe como Vector Asset |
| Animação de comemoração (futuro) | Resultado | LottieFiles (gratuitas), mas exige a biblioteca Lottie: fica para versão futura |

**Cuidado com marca:** a inspiração é o Duolingo, mas o app **não deve usar** o nome, o logo, o mascote (a coruja) nem a fonte proprietária do Duolingo. As cores verdes e o estilo arredondado são só referência.

---

## 14. Estrutura do projeto Android

**Criação do projeto:** Android Studio → *New Project* → **Empty Views Activity** → Name: `ENEMigo` → Package: `com.seunome.enemigo` → Language: **Java** → Minimum SDK: **API 26 (Android 8.0)**.

> O template "Empty Views Activity" já inclui AppCompat, Material Components (BottomNavigationView, MaterialCardView, TextInputLayout...) e ConstraintLayout. **Não é necessário adicionar dependências.** A RecyclerView já vem junto com o Material Components.

```text
app/
├── src/main/
│   ├── AndroidManifest.xml
│   ├── assets/
│   │   └── questoes.json
│   ├── java/com/seunome/enemigo/
│   │   ├── model/
│   │   │   ├── Area.java                 (enum: 4 áreas com nome, sigla, cor, ícone)
│   │   │   ├── Licao.java
│   │   │   ├── Questao.java
│   │   │   ├── Conquista.java            (enum: 6 conquistas)
│   │   │   └── ResultadoSessao.java
│   │   ├── data/
│   │   │   ├── QuestaoRepository.java    (lê o JSON, valida, guarda em memória)
│   │   │   └── ProgressoManager.java     (SharedPreferences + regras de progresso)
│   │   ├── ui/
│   │   │   ├── MainActivity.java
│   │   │   ├── OnboardingActivity.java
│   │   │   ├── TrilhaActivity.java
│   │   │   ├── QuizActivity.java
│   │   │   ├── ResultadoActivity.java
│   │   │   ├── ConfiguracoesActivity.java
│   │   │   ├── InicioFragment.java
│   │   │   ├── RevisaoFragment.java
│   │   │   ├── PerfilFragment.java
│   │   │   └── adapter/
│   │   │       ├── LicaoAdapter.java
│   │   │       ├── CadernoAdapter.java
│   │   │       └── ConquistaAdapter.java
│   │   └── util/
│   │       ├── Constantes.java           (chaves, extras, valores de XP/vidas)
│   │       ├── Gamificacao.java          (cálculos puros: nível, vidas, ofensiva, 60%)
│   │       ├── TempoEstudo.java          (tempo estimado e tempo real, puros)
│   │       ├── Migracao.java             (progresso da versão 1 -> lições atuais)
│   │       └── DataUtil.java             (hoje, ontem, conversão de datas)
│   └── res/
│       ├── layout/
│       │   ├── activity_main.xml
│       │   ├── activity_onboarding.xml
│       │   ├── activity_trilha.xml
│       │   ├── activity_quiz.xml
│       │   ├── activity_resultado.xml
│       │   ├── activity_configuracoes.xml
│       │   ├── fragment_inicio.xml
│       │   ├── fragment_revisao.xml
│       │   ├── fragment_perfil.xml
│       │   ├── layout_status.xml
│       │   ├── item_alternativa.xml
│       │   ├── item_licao.xml
│       │   ├── item_caderno.xml
│       │   └── item_conquista.xml
│       ├── menu/
│       │   ├── menu_bottom_nav.xml
│       │   └── menu_perfil.xml           (item engrenagem)
│       ├── drawable/
│       │   ├── ic_home.xml, ic_revisao.xml, ic_perfil.xml, ic_settings.xml, ic_close.xml
│       │   ├── ic_area_linguagens.xml, ic_area_humanas.xml, ic_area_natureza.xml, ic_area_matematica.xml
│       │   ├── bg_circulo.xml            (shape oval, cor definida no Java)
│       │   ├── bg_etiqueta.xml           (shape retângulo arredondado)
│       │   └── bg_avatar.xml
│       ├── font/
│       │   └── nunito.xml                (gerado pelo Android Studio)
│       ├── mipmap-*/                     (ícone do app gerado pelo Image Asset)
│       └── values/
│           ├── colors.xml
│           ├── strings.xml
│           ├── themes.xml
│           └── dimens.xml
└── build.gradle(.kts)                    (minSdk 26; sem dependências extras)
```

**AndroidManifest.xml (o que precisa ter):**

- As 6 Activities declaradas, todas com `android:screenOrientation="portrait"`.
- `MainActivity` com o `intent-filter` MAIN/LAUNCHER.
- `TrilhaActivity`, `QuizActivity`, `ResultadoActivity` e `ConfiguracoesActivity` com `android:exported="false"`.
- Permissão `android.permission.VIBRATE` **somente** se implementar a vibração (C10). É uma permissão normal, sem pedido na tela.
- **Não** precisa da permissão de Internet.

**Extras das Intents (constantes em `Constantes`):**

| Extra | Tipo | Usado em |
|---|---|---|
| `EXTRA_AREA_ID` | String | Início → Trilha |
| `EXTRA_LICAO_ID` | String | Trilha/Início/Resultado → Quiz |
| `EXTRA_MODO` | String (`LICAO`/`REVISAO`) | → Quiz, Quiz → Resultado |
| `EXTRA_ACERTOS`, `EXTRA_TOTAL`, `EXTRA_XP`, `EXTRA_BONUS`, `EXTRA_OFENSIVA`, `EXTRA_META_BATIDA`, `EXTRA_DESBLOQUEOU`, `EXTRA_VIDAS_RECUPERADAS`, `EXTRA_CONQUISTAS` (ArrayList\<String\>) | vários | Quiz → Resultado |

> **Conceito: Intent extras.** Para mandar dados de uma Activity para outra, coloque na Intent com `putExtra("CHAVE", valor)` e leia na outra com `getIntent().getStringExtra("CHAVE")`. Passe **só o id** da lição, e não o objeto inteiro. A outra tela busca a lição no repositório.

---

## 15. Arquitetura

### Visão geral

Arquitetura **em camadas simples**, sem MVVM, sem injeção de dependência e sem bibliotecas extras. Cada tela conversa direto com duas classes centrais de dados.

```text
┌──────────────────────── ui ────────────────────────┐
│ Activities · Fragments · Adapters                   │
│ (mostram dados, capturam cliques, navegam)          │
└──────────────┬──────────────────────┬──────────────┘
               │ lê conteúdo           │ lê/grava progresso
               ▼                       ▼
     ┌──────────────────┐   ┌────────────────────────┐
     │ QuestaoRepository│   │    ProgressoManager    │
     │ (assets/JSON)    │   │   (SharedPreferences)  │
     └────────┬─────────┘   └───────────┬────────────┘
              │ cria                     │ usa
              ▼                          ▼
     ┌──────────────────┐   ┌────────────────────────┐
     │      model       │   │   util (Gamificacao,   │
     │ Area, Licao,     │   │   DataUtil, Constantes)│
     │ Questao, ...     │   └────────────────────────┘
     └──────────────────┘
```

**Regra de ouro:** as telas (`ui`) **não** acessam o SharedPreferences nem o JSON diretamente. Sempre passam pelo `ProgressoManager` ou pelo `QuestaoRepository`. Assim, se um dia o armazenamento mudar para Room ou para uma API, só essas duas classes mudam.

### Responsabilidade das classes

**model**

- `Area` (enum): as 4 áreas e seus dados visuais. Tem o método estático `fromId(String)`, que devolve `null` se o id não existir.
- `Questao`: dados de uma questão, com o método `isCorreta(int indice)`.
- `Licao`: dados de uma lição e sua lista de questões.
- `Conquista` (enum): as 6 conquistas com título, descrição e emoji.
- `ResultadoSessao`: "pacote" com o resumo de uma sessão terminada.

**data**

- `QuestaoRepository` (**singleton**: uma instância só, criada na primeira chamada de `getInstance(context)`):
  - `carregar()`: lê o JSON uma única vez, valida, converte a letra da `correta` em índice e monta as listas;
  - `getLicoes(Area)`, `getLicao(String id)`, `getQuestao(String id)`, `getLicaoSeguinte(Licao)`, `getTotalLicoes(Area)`;
  - `isCarregadoComSucesso()`.
- `ProgressoManager` (singleton):
  - perfil: `getNome()`, `setNome()`, `getMetaDiaria()`, `setMetaDiaria()`, `isOnboardingConcluido()`, `concluirOnboarding(nome, meta)`;
  - XP: `getXpTotal()`, `getXpHoje()`, `getNivel()`;
  - vidas: `getVidas()` (já aplica a recuperação), `perderVida()`, `ganharVida()`, `getMinutosParaProximaVida()`;
  - ofensiva: `getOfensivaAtual()`, `getMaiorOfensiva()`, `estudouHoje()`;
  - lições: `getMelhor(licaoId)`, `getEstado(Licao)` (CONCLUIDA/DISPONIVEL/BLOQUEADA), `getLicoesConcluidas(Area)`, `getMinutosEstimados(Licao)`;
  - caderno: `getCaderno()`, `adicionarAoCaderno(id)`, `removerDoCaderno(id)`;
  - tempo: `registrarTempoQuestao(Questao, ms)`, `getTempoMedioSegundos()`, `getMinutosHoje()`, `getFatorRitmo()`;
  - migração: `migrarProgresso()`, privado, chamado no construtor (RN37);
  - conquistas: `getConquistas()`;
  - sessões: **`registrarLicao(Licao, int acertos)`** e **`registrarRevisao(int acertos, int respondidas)`**, que aplicam todas as regras (XP, meta, ofensiva, melhor e conquistas) e devolvem um `ResultadoSessao`;
  - `resetarTudo()`.

**util**

- `Constantes`: nomes das chaves, dos extras e os números do jogo (`XP_ACERTO = 10`, `XP_BONUS_PERFEITA = 5`, `XP_ACERTO_REVISAO = 5`, `VIDAS_MAX = 5`, `MINUTOS_RECARGA = 30`, `PERCENTUAL_PARA_CONCLUIR = 60`, `MIN_QUESTOES_POR_LICAO = 3`, `MAX_QUESTOES_POR_LICAO = 5`, `TEMPO_MAX_QUESTAO_MS` = 10 min, `QUESTOES_PARA_RITMO = 10`). Mudar a regra do jogo = mudar um número aqui. As constantes da estimativa de tempo ficam no `tools/montar_questoes.py` (ver 9.4).
- `Gamificacao`: **funções estáticas puras**, que só recebem valores e devolvem resultados, sem Android, sem Context e sem SharedPreferences:
  - `calcularNivel(int xp)`;
  - `calcularXpLicao(int acertos)`;
  - `acertosParaConcluir(int total)` e `concluiu(int acertos, int total)` (60%, RN06);
  - `calcularNovaOfensiva(LocalDate ultima, LocalDate hoje, int ofensivaAtual)`;
  - `calcularVidasRecuperadas(int vidas, long ultimaRecarga, long agora)`.

  Por serem puras, dá para testar com **JUnit** (pasta `test/`) sem abrir o emulador. É opcional, mas é um ótimo extra para a apresentação.
- `TempoEstudo`: funções puras do tempo (minutos da trilha, tempo válido, média, fator de ritmo, tempo de hoje), testadas em `TempoEstudoTest`.
- `Migracao`: funções puras da migração do progresso da versão 1 (RN37), testadas em `MigracaoTest`.
- `DataUtil`: `hoje()`, `ontem()`, `paraTexto(LocalDate)` e `deTexto(String)` (devolve `null` se vazio ou inválido).

**ui**

- `MainActivity`: verifica o onboarding e troca os fragments pela BottomNavigationView.
- `InicioFragment`, `RevisaoFragment`, `PerfilFragment`: leem os dados no `onResume` e preenchem a tela.
- `TrilhaActivity` + `LicaoAdapter`: lista as lições com o estado de cada uma.
- `QuizActivity`: controla a sessão (índice atual, alternativa escolhida, acertos, se já verificou) e, no fim, chama `ProgressoManager.registrar...()` e abre o Resultado.
- `ResultadoActivity`: só exibe o que recebeu nos extras.
- `ConfiguracoesActivity`: edita o perfil e faz o reset.
- Adapters: convertem listas em itens da RecyclerView. Cada adapter recebe uma **interface de clique** (ex.: `OnLicaoClickListener`) para avisar a Activity.

> **Conceito: Singleton.** Padrão em que a classe tem uma única instância, acessada por `getInstance(context)`. Use `context.getApplicationContext()` dentro dele para não "prender" uma Activity na memória.

> **Conceito: `onResume`.** Método do ciclo de vida chamado toda vez que a tela volta a aparecer, inclusive quando o aluno volta do Quiz. Por isso as telas que mostram progresso atualizam os dados no `onResume`, e não só no `onCreate`.

> **Conceito: RecyclerView + Adapter.** A RecyclerView desenha só os itens visíveis e reaproveita as "caixinhas" ao rolar. O Adapter diz quantos itens existem (`getItemCount`), cria a caixinha (`onCreateViewHolder`, que infla o `item_*.xml`) e preenche cada uma (`onBindViewHolder`).

---

## 16. Plano de desenvolvimento em 4 aulas

> **Nota:** as seções 16 e 17 são o roteiro original das aulas, de quando cada lição tinha 5 questões e liberava a próxima com 3 acertos. As regras atuais (lições de 3 a 5 questões, 60% de acertos, tempo estimado e medido) estão nas seções 9.3, 9.4 e 10.

Cada aula termina com o app **compilando e rodando** no emulador ou no celular. Faça commits no Git (ou uma cópia do projeto zipada) ao fim de cada aula.

### Aula 1 — Estrutura e interface

**Meta:** o app abre, navega entre as abas e as telas principais existem com dados de exemplo.

1. Criar o projeto (Empty Views Activity, Java, minSdk 26, pacote `com.seunome.enemigo`).
2. Trocar o tema para `Theme.Material3.Light.NoActionBar` e travar o retrato no Manifest.
3. Preencher `colors.xml` (paleta da seção 7.2), `strings.xml` (textos fixos) e `dimens.xml`.
4. Criar os pacotes `model`, `data`, `ui`, `ui/adapter` e `util` e mover a MainActivity para `ui`.
5. Criar os modelos `Area`, `Questao`, `Licao` e a classe `Constantes`.
6. Criar os ícones pelo Vector Asset (casa, caderno, pessoa, engrenagem, fechar e os 4 das áreas).
7. `MainActivity` com BottomNavigationView e os 3 fragments (Revisão e Perfil ainda com um texto "Em construção").
8. `fragment_inicio.xml` completo com barra de status, cartão da meta e os 4 cartões de área (valores fixos).
9. `TrilhaActivity` + `item_licao.xml` + `LicaoAdapter` com uma **lista falsa** de 3 lições criada no código.
10. `activity_quiz.xml` + `item_alternativa.xml` montados, mostrando **uma questão fixa** do código, com a seleção visual das alternativas funcionando (sem corrigir ainda).

**Entregável:** app abrindo, com as 3 abas; tocar numa área abre a trilha; tocar numa lição abre o quiz com uma questão de exemplo.

**Se sobrar tempo:** gerar o ícone do app no Image Asset e aplicar a fonte Nunito.

### Aula 2 — Funcionalidades

**Meta:** dá para jogar lições de verdade, com questões do JSON, correção e resultado (ainda sem salvar nada).

1. Criar `assets/questoes.json` com pelo menos **2 lições por área (40 questões)**, no formato da seção 9.3.
2. Implementar o `QuestaoRepository` (leitura com org.json, validação e cache).
3. Início: cartões mostram o total de lições de cada área; tocar abre a Trilha com o `EXTRA_AREA_ID`.
4. Trilha: lista as lições reais da área (por enquanto todas "disponíveis").
5. Quiz completo no modo LICAO: carregar a lição pelo `EXTRA_LICAO_ID`, selecionar, VERIFICAR, cores de certo e errado, painel de feedback com explicação, CONTINUAR, barra de progresso, contador "Questão X de 5" e diálogo ao sair.
6. `ResultadoActivity`: título conforme os acertos, "Você acertou X de 5" e XP calculado (+10 por acerto, +5 com 5/5), com os botões CONTINUAR e REFAZER.

**Entregável:** fluxo completo Início → Trilha → Quiz (5 questões) → Resultado → Trilha, com questões reais.

**Se faltar tempo:** comece com 1 lição por área e complete o JSON em casa.

### Aula 3 — Dados e recursos

**Meta:** o progresso fica salvo e as regras do jogo funcionam.

1. `DataUtil` e `Gamificacao` (cálculos puros).
2. `ProgressoManager` com todas as chaves da seção 9.5 e as regras RN05 a RN33.
3. `OnboardingActivity` com validações, e a MainActivity redirecionando no primeiro acesso.
4. Barra de status real (🔥 ⭐ ❤️) no Início e na Trilha; cartão da meta diária real; progresso "X/Y lições" em cada área.
5. Trilha com estados reais (concluída, disponível, bloqueada) e o bloqueio por vidas.
6. Quiz: perder vida no erro, caderno de erros e fim da lição quando as vidas acabam.
7. Quiz chamando `registrarLicao()` no fim e passando o `ResultadoSessao` para o Resultado (ofensiva, meta batida, desbloqueio).
8. Aba **Revisão** com a lista do caderno, o estado vazio e o modo REVISAO no Quiz (+1 vida, +5 XP, sai do caderno).
9. Aba **Perfil** básica: nome, nível, barra de XP, estatísticas.

**Entregável:** fechar e abrir o app mantém tudo; a ofensiva, as vidas e o desbloqueio funcionam; os erros aparecem na Revisão.

**Dica para testar o tempo:** mude `MINUTOS_RECARGA` para 1 enquanto testa as vidas (volte para 30 depois!). Para a ofensiva, altere a data do celular ou do emulador (Configurações → Sistema → Data e hora).

### Aula 4 — Finalização

**Meta:** app completo, bonito, testado e pronto para apresentar.

1. `Conquista` + verificação em `registrarLicao()`/`registrarRevisao()` + exibição no Resultado e no Perfil (`ConquistaAdapter`).
2. `ConfiguracoesActivity`: editar nome e meta, "Sobre" e resetar o progresso com confirmação.
3. Complementares que couberem: contagem da próxima vida, cartão Continuar, fogo aceso ou apagado, vibração.
4. Tratamento de erros da seção 12 (JSON inválido, extras ausentes, caderno com ids antigos).
5. Completar o JSON para **3 lições por área (60 questões)** e revisar os gabaritos.
6. Acabamento visual: fonte, espaçamentos, cores das áreas, ícone do app, textos todos em `strings.xml`.
7. Rodar a lista de testes da seção 18 e corrigir o que falhar.
8. Gerar o APK (*Build → Build App Bundle(s) / APK(s) → Build APK(s)*) e instalar num celular real.
9. Preparar a apresentação: roteiro de demonstração (onboarding → lição com um erro → resultado → revisão → perfil) e **deixar o progresso resetado** para mostrar o primeiro acesso.

**Entregável:** versão final funcional, apresentável e com APK instalável.

---

## 17. Prompts para o GitHub Copilot

### Como usar

- Use o **Copilot Chat** do Android Studio, com os arquivos citados **abertos** no editor (o Copilot usa os arquivos abertos como contexto).
- Faça **um prompt por vez**, na ordem. Depois de cada um: rode o app, teste e só então passe para o próximo.
- **Leia o código gerado** antes de aceitar. Se algo não compilar, cole o erro do Build/Logcat no chat e peça a correção.
- Os prompts são independentes: todos começam com o contexto do projeto. Se o Copilot "esquecer" algo, cole também a seção relevante deste documento.
- Troque `com.seunome.enemigo` pelo seu pacote.

---

### Aula 1

#### Prompt 01 — Configuração base

```text
Projeto Android "ENEMigo" (Java + XML, minSdk 26, pacote com.seunome.enemigo, template Empty Views Activity).

OBJETIVO: configurar tema, cores, textos e orientação.

ARQUIVOS: res/values/colors.xml, res/values/themes.xml, res/values/strings.xml, res/values/dimens.xml, AndroidManifest.xml.

IMPLEMENTAÇÃO:
- colors.xml com: verde_primario #58CC02, verde_escuro #58A700, verde_claro #D7FFB8, vermelho #FF4B4B, vermelho_escuro #EA2B2B, vermelho_claro #FFDFE0, azul_selecao #1CB0F6, azul_claro #DDF4FF, cinza_borda #E5E5E5, cinza_bloqueado #AFAFAF, texto_principal #3C3C3C, texto_secundario #777777, fundo #FFFFFF, laranja_fogo #FF9600, amarelo_xp #FFC800, area_linguagens #1CB0F6, area_humanas #FF9600, area_natureza #58CC02, area_matematica #CE82FF.
- themes.xml: tema do app herdando de Theme.Material3.Light.NoActionBar, colorPrimary = verde_primario, android:windowBackground = fundo. Se existir values-night/themes.xml, deixe-o igual ao claro.
- dimens.xml: margem_tela 16dp, espaco_cards 12dp, raio_card 16dp, raio_botao 12dp, altura_botao 56dp.
- strings.xml: app_name "ENEMigo", nomes das abas (Início, Revisão, Perfil), nomes das 4 áreas, textos dos botões (VERIFICAR, CONTINUAR, COMEÇAR, SALVAR, REFAZER LIÇÃO, REVISAR AGORA, RESETAR PROGRESSO).
- Manifest: android:screenOrientation="portrait" na MainActivity.

REGRAS: não adicionar dependências no Gradle. Não criar nenhuma Activity nova ainda.

COMPORTAMENTO ESPERADO: o app compila, abre com fundo branco, sem ActionBar, e não gira a tela.
```

#### Prompt 02 — Modelos e constantes

```text
Projeto Android "ENEMigo" (Java, minSdk 26, pacote com.seunome.enemigo).

OBJETIVO: criar as classes de modelo e as constantes.

ARQUIVOS: model/Area.java, model/Questao.java, model/Licao.java, util/Constantes.java.

IMPLEMENTAÇÃO:
- Area (enum): LINGUAGENS("Linguagens","LIN",R.color.area_linguagens,R.drawable.ic_area_linguagens), HUMANAS("Ciências Humanas","HUM",...), NATUREZA("Ciências da Natureza","NAT",...), MATEMATICA("Matemática","MAT",...). Campos: nome, sigla, corRes, iconeRes. Getters. Método estático fromId(String id) que retorna o enum pelo name() ou null se não existir.
- Questao: id, area (Area), licaoId, texto, enunciado, alternativas (String[5]), correta (int 0 a 4), explicacao, fonte. Construtor com todos os campos, getters, método isCorreta(int indice) e getLetraCorreta() que devolve "A".."E".
- Licao: id, area, ordem (int), titulo, questoes (List<Questao>). Construtor e getters.
- Constantes (final, construtor privado): PREFS_NOME = "enemigo_prefs"; extras EXTRA_AREA_ID, EXTRA_LICAO_ID, EXTRA_MODO, MODO_LICAO = "LICAO", MODO_REVISAO = "REVISAO"; regras XP_ACERTO = 10, XP_BONUS_PERFEITA = 5, XP_ACERTO_REVISAO = 5, VIDAS_MAX = 5, MINUTOS_RECARGA = 30, ACERTOS_PARA_DESBLOQUEAR = 3, QUESTOES_POR_LICAO = 5, XP_POR_NIVEL = 100.

REGRAS: se os drawables ic_area_* ainda não existirem, crie-os como Vector Asset (Material Icons menu_book, public, science, calculate). Sem lógica de tela nessas classes.

COMPORTAMENTO ESPERADO: o projeto compila sem erros.
```

#### Prompt 03 — MainActivity com BottomNavigationView

```text
Projeto Android "ENEMigo" (Java + XML, minSdk 26, pacote com.seunome.enemigo). Pacotes: model, data, ui, util.

OBJETIVO: criar a navegação principal com 3 abas.

ARQUIVOS: ui/MainActivity.java, res/layout/activity_main.xml, res/menu/menu_bottom_nav.xml, ui/InicioFragment.java, ui/RevisaoFragment.java, ui/PerfilFragment.java, res/layout/fragment_inicio.xml, fragment_revisao.xml, fragment_perfil.xml, AndroidManifest.xml.

IMPLEMENTAÇÃO:
- activity_main.xml: ConstraintLayout com FrameLayout (id fragment_container) ocupando tudo acima de uma BottomNavigationView (id bottom_nav) presa embaixo.
- menu_bottom_nav.xml: itens nav_inicio, nav_revisao, nav_perfil com ícones Vector Asset (home, menu_book ou library_books, person) e títulos de strings.xml.
- MainActivity: ao criar, mostra InicioFragment (só se savedInstanceState == null). setOnItemSelectedListener troca o fragment com getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit().
- Os 3 fragments: por enquanto cada layout tem só um TextView central com o nome da aba.
- Mover a MainActivity para o pacote ui e atualizar o Manifest (mantendo o LAUNCHER e screenOrientation portrait).

REGRAS: usar androidx.fragment.app.Fragment. Não usar Navigation Component.

COMPORTAMENTO ESPERADO: ao tocar em cada aba o conteúdo muda; o item selecionado fica destacado; Voltar fecha o app.
```

#### Prompt 04 — Layout da tela Início

```text
Projeto Android "ENEMigo" (Java + XML). Cores e dimens já existem em colors.xml e dimens.xml.

OBJETIVO: montar a interface da aba Início com dados fixos.

ARQUIVOS: res/layout/layout_status.xml, res/layout/fragment_inicio.xml, ui/InicioFragment.java.

IMPLEMENTAÇÃO:
- layout_status.xml: LinearLayout horizontal com 3 TextViews de peso igual: txt_ofensiva ("🔥 0", cor laranja_fogo), txt_xp ("⭐ 0", cor amarelo_xp), txt_vidas ("❤️ 5", cor vermelho). Texto 18sp negrito, centralizado.
- fragment_inicio.xml: ScrollView > LinearLayout vertical com padding margem_tela contendo: <include layout="@layout/layout_status" android:id="@+id/status"/>; TextView txt_saudacao; MaterialCardView da meta com TextView "Meta diária", TextView txt_meta ("0 / 50 XP") e LinearProgressIndicator progress_meta; TextView "Áreas do ENEM"; grade 2x2 (dois LinearLayouts horizontais) com 4 MaterialCardViews: card_linguagens, card_humanas, card_natureza, card_matematica. Cada card: ImageView do ícone da área (tint branco), TextView nome, TextView de progresso (ids: txt_prog_linguagens etc.). Fundo de cada card na cor da área, textos brancos, raio raio_card, altura ~140dp.
- InicioFragment: infla o layout e, no onViewCreated, preenche a saudação "Oi, estudante!" e deixa os cliques dos cards mostrando um Toast com o nome da área.

REGRAS: todos os textos fixos em strings.xml. Não usar RecyclerView aqui (as áreas são sempre 4).

COMPORTAMENTO ESPERADO: a aba Início mostra a barra 🔥⭐❤️, o cartão da meta e os 4 cartões coloridos; tocar num cartão mostra um Toast.
```

#### Prompt 05 — Trilha com lista de exemplo

```text
Projeto Android "ENEMigo" (Java + XML, minSdk 26). Existem model/Area, model/Licao, model/Questao e util/Constantes (com EXTRA_AREA_ID).

OBJETIVO: criar a tela da trilha de lições de uma área com uma lista de exemplo.

ARQUIVOS: ui/TrilhaActivity.java, res/layout/activity_trilha.xml, res/layout/item_licao.xml, ui/adapter/LicaoAdapter.java, res/drawable/bg_circulo.xml, ui/InicioFragment.java, AndroidManifest.xml.

IMPLEMENTAÇÃO:
- activity_trilha.xml: LinearLayout vertical com MaterialToolbar (seta de voltar), include de layout_status, TextView txt_resumo e RecyclerView rv_licoes.
- item_licao.xml: MaterialCardView > LinearLayout horizontal com TextView txt_circulo (56dp, fundo bg_circulo, texto centralizado branco) e um LinearLayout vertical com txt_numero ("Lição 1"), txt_titulo (negrito) e txt_detalhe.
- bg_circulo.xml: shape oval branca (a cor será trocada no código com setBackgroundTintList).
- LicaoAdapter: recebe List<Licao>, a cor da área e uma interface OnLicaoClickListener { void onLicaoClick(Licao licao); }. Por enquanto mostra todas como disponíveis: círculo na cor da área com o número (ordem + 1) e txt_detalhe "Começar".
- TrilhaActivity: lê EXTRA_AREA_ID da Intent, converte com Area.fromId; se null, Toast "Área não encontrada" e finish(). Toolbar com o nome da área e fundo na cor da área; seta volta (finish). Cria uma lista falsa de 3 Licao (sem questões) e mostra no RecyclerView com LinearLayoutManager. Clique numa lição: Toast com o título.
- InicioFragment: clique em cada card abre a TrilhaActivity com o EXTRA_AREA_ID = area.name().
- Manifest: declarar TrilhaActivity com exported=false e screenOrientation portrait.

REGRAS: nada de acesso a JSON ainda. Não passar objetos pela Intent, só o id da área.

COMPORTAMENTO ESPERADO: tocar em "Matemática" abre a trilha roxa com 3 lições; a seta volta para o Início.
```

#### Prompt 06 — Layout do Quiz com seleção de alternativa

```text
Projeto Android "ENEMigo" (Java + XML, minSdk 26). Existem model/Questao e util/Constantes.

OBJETIVO: montar a tela do quiz exibindo uma questão fixa e a seleção visual das alternativas.

ARQUIVOS: ui/QuizActivity.java, res/layout/activity_quiz.xml, res/layout/item_alternativa.xml, res/drawable/ic_close.xml, ui/TrilhaActivity.java, AndroidManifest.xml.

IMPLEMENTAÇÃO:
- item_alternativa.xml: MaterialCardView clicável (strokeWidth 2dp, strokeColor cinza_borda, raio 12dp, margem vertical 6dp) > LinearLayout horizontal (padding 12dp) com TextView txt_letra (32dp, centralizado, negrito) e TextView txt_texto (peso 1, 16sp).
- activity_quiz.xml (ConstraintLayout): topo com ImageButton btn_fechar (ic_close), LinearProgressIndicator progress_quiz (max 5) e TextView txt_vidas_quiz; ScrollView no meio com LinearLayout contendo txt_contador ("Questão 1 de 5"), MaterialCardView card_texto_base com txt_texto_base, txt_enunciado (17sp), txt_fonte (12sp, cinza) e 5 includes de item_alternativa com ids alt_a, alt_b, alt_c, alt_d, alt_e; embaixo, MaterialButton btn_verificar (largura total, altura_botao, desabilitado) e um LinearLayout painel_feedback (visibility gone) com txt_feedback_titulo, ScrollView de altura máxima 160dp com txt_explicacao e MaterialButton btn_continuar.
- QuizActivity: cria uma Questao fixa no código (use a questão de porcentagem: tênis de R$ 250,00 com 20% + 10% de desconto, correta B = R$ 180,00). Preenche os textos; esconde card_texto_base se o texto for vazio. Guarda as 5 views de alternativa num array. Ao tocar numa alternativa: guarda o índice selecionado, pinta a selecionada (strokeColor azul_selecao, fundo azul_claro), volta as outras ao normal (cinza_borda, branco) e habilita btn_verificar.
- TrilhaActivity: clique numa lição abre a QuizActivity.
- Manifest: declarar QuizActivity (exported=false, portrait).

REGRAS: criar um método privado aplicarEstilo(MaterialCardView card, int corBorda, int corFundo) para não repetir código. Não corrigir a resposta ainda.

COMPORTAMENTO ESPERADO: a questão aparece com as letras A a E; só uma alternativa fica azul por vez; VERIFICAR só habilita depois de escolher.
```

---

### Aula 2

#### Prompt 07 — Arquivo de questões e QuestaoRepository

```text
Projeto Android "ENEMigo" (Java, minSdk 26, pacote com.seunome.enemigo). Existem model/Area (com fromId), model/Questao, model/Licao e util/Constantes.

OBJETIVO: carregar as questões de assets/questoes.json.

ARQUIVOS: app/src/main/assets/questoes.json, data/QuestaoRepository.java.

FORMATO DO JSON:
{ "versao": 1, "areas": [ { "id": "MATEMATICA", "licoes": [ { "id": "MAT-01", "titulo": "Porcentagem", "questoes": [ { "id": "MAT-01-01", "texto": "", "enunciado": "...", "alternativas": ["...","...","...","...","..."], "correta": "B", "explicacao": "...", "fonte": "ENEM 2019" } ] } ] } ] }

IMPLEMENTAÇÃO:
- QuestaoRepository singleton: getInstance(Context) usando getApplicationContext().
- carregar() (chamado uma vez no construtor): lê o arquivo com context.getAssets().open("questoes.json") e BufferedReader com StandardCharsets.UTF_8; converte com JSONObject/JSONArray (org.json).
- Para cada área: Area.fromId(id); se null, Log.w e ignora.
- Para cada lição: ordem = posição no array; monta as questões válidas; se não tiver exatamente Constantes.QUESTOES_POR_LICAO questões válidas, Log.w e ignora a lição.
- Questão válida: enunciado não vazio, exatamente 5 alternativas, correta entre "A" e "E" (converter: letra.charAt(0) - 'A'), id não repetido (usar um HashSet de ids já vistos). "texto" é opcional (optString com "").
- Guardar: Map<Area, List<Licao>> e Map<String, Licao> e Map<String, Questao> para busca por id.
- Métodos públicos: getLicoes(Area), getLicao(String id), getQuestao(String id), getLicaoSeguinte(Licao) (ou null), getTotalLicoes(Area), isCarregadoComSucesso().
- Em IOException ou JSONException: Log.e, mapas ficam vazios e isCarregadoComSucesso() retorna false.
- Criar o questoes.json com pelo menos 1 lição (5 questões) por área para teste.

REGRAS: não usar Gson nem outra biblioteca. Nunca deixar catch vazio. Nunca retornar null em getLicoes (retornar lista vazia).

COMPORTAMENTO ESPERADO: getLicoes(Area.MATEMATICA) retorna as lições do JSON na ordem; um JSON quebrado não fecha o app.
```

#### Prompt 08 — Trilha e Início com dados reais

```text
Projeto Android "ENEMigo" (Java + XML). Existe data/QuestaoRepository (singleton com getLicoes(Area), getTotalLicoes(Area), isCarregadoComSucesso()). Existem TrilhaActivity, LicaoAdapter e InicioFragment.

OBJETIVO: trocar os dados falsos pelos dados do JSON.

ARQUIVOS: ui/TrilhaActivity.java, ui/adapter/LicaoAdapter.java, ui/InicioFragment.java, ui/QuizActivity.java, res/layout/fragment_inicio.xml.

IMPLEMENTAÇÃO:
- TrilhaActivity: usa QuestaoRepository.getInstance(this).getLicoes(area). Se a lista estiver vazia, mostra no txt_resumo "Nenhuma lição disponível ainda". Clique numa lição abre a QuizActivity com Constantes.EXTRA_LICAO_ID = licao.getId() e EXTRA_MODO = MODO_LICAO.
- LicaoAdapter: mostra "Lição N" (ordem + 1) e o título real.
- InicioFragment: cada card mostra "N lições" com getTotalLicoes(area); se for 0, mostra "Em breve". Se isCarregadoComSucesso() for false, esconde a grade de áreas e mostra um TextView de erro "Não foi possível carregar as questões."
- QuizActivity: por enquanto, só leia o EXTRA_LICAO_ID e mostre a primeira questão da lição (se a lição não existir: Toast "Lição não encontrada" e finish()).

REGRAS: não passar objetos Licao/Questao pela Intent, só ids.

COMPORTAMENTO ESPERADO: as trilhas mostram as lições do JSON; tocar numa lição abre o quiz com a primeira questão real dela.
```

#### Prompt 09 — Lógica completa do Quiz

```text
Projeto Android "ENEMigo" (Java + XML, minSdk 26). A QuizActivity já tem o layout (alt_a..alt_e, btn_verificar, painel_feedback, txt_feedback_titulo, txt_explicacao, btn_continuar, progress_quiz, txt_contador, btn_fechar) e recebe EXTRA_LICAO_ID. QuestaoRepository existe.

OBJETIVO: implementar o fluxo de responder as 5 questões.

ARQUIVOS: ui/QuizActivity.java, res/values/strings.xml.

IMPLEMENTAÇÃO:
- Estado da tela: List<Questao> questoes, int indiceAtual, int selecionada (-1 = nenhuma), int acertos, boolean verificada.
- mostrarQuestao(): preenche os textos da questão atual, "Questão X de N", progress_quiz com o índice, reseta todas as alternativas para o estilo normal e clicáveis, esconde o painel e mostra btn_verificar desabilitado.
- Clique numa alternativa (só se !verificada): seleciona (azul) e habilita VERIFICAR.
- VERIFICAR: desabilita o botão na hora (evita clique duplo), marca verificada = true, trava as alternativas. Se acertou: alternativa verde (verde_primario/verde_claro), acertos++, painel com fundo verde_claro, título "Mandou bem! 🎉" em verde_escuro, btn_continuar verde. Se errou: selecionada vermelha (vermelho/vermelho_claro), correta verde, painel vermelho_claro, título "Resposta correta: " + letra em vermelho_escuro, btn_continuar vermelho. Mostra a explicação. Esconde btn_verificar e mostra painel_feedback.
- CONTINUAR: se houver próxima questão, indiceAtual++ e mostrarQuestao(); se era a última, por enquanto mostre um Toast com os acertos e finish().
- btn_fechar e Voltar do sistema (getOnBackPressedDispatcher().addCallback): MaterialAlertDialogBuilder "Sair da lição?" / "Você vai perder o progresso desta lição." com botões "Sair" (finish) e "Continuar estudando" (fecha o diálogo).

REGRAS: não embaralhar as alternativas. Não salvar nada ainda. Usar ContextCompat.getColor para cores. Textos em strings.xml.

COMPORTAMENTO ESPERADO: dá para responder as 5 questões, ver certo/errado com explicação e a barra de progresso avança; o X pede confirmação.
```

#### Prompt 10 — Tela de Resultado

```text
Projeto Android "ENEMigo" (Java + XML). A QuizActivity conta os acertos. Constantes tem XP_ACERTO = 10, XP_BONUS_PERFEITA = 5, EXTRA_LICAO_ID, EXTRA_MODO.

OBJETIVO: mostrar o resultado ao terminar a lição.

ARQUIVOS: ui/ResultadoActivity.java, res/layout/activity_resultado.xml, ui/QuizActivity.java, util/Constantes.java, AndroidManifest.xml.

IMPLEMENTAÇÃO:
- Constantes: adicionar EXTRA_ACERTOS, EXTRA_TOTAL, EXTRA_XP, EXTRA_BONUS.
- QuizActivity: ao tocar CONTINUAR na última questão, calcula xp = acertos * XP_ACERTO e bonus = (acertos == total) ? XP_BONUS_PERFEITA : 0; abre ResultadoActivity com os extras (incluindo EXTRA_LICAO_ID e EXTRA_MODO) e chama finish().
- activity_resultado.xml: LinearLayout vertical centralizado com txt_emoji (64sp), txt_titulo (26sp negrito), txt_acertos, dois MaterialCardViews lado a lado (txt_xp_ganho e txt_ofensiva_resultado, este último GONE por enquanto), txt_mensagens, btn_continuar e btn_refazer (estilo Widget.Material3.Button.OutlinedButton).
- ResultadoActivity: 5/5 → "🏆" + "Perfeito!"; 3 ou 4 → "✅" + "Lição concluída!"; 0 a 2 → "💪" + "Quase lá!". "Você acertou X de Y". "+XP XP" e, se houver bônus, "+5 bônus de lição perfeita". btn_continuar e Voltar → finish(). btn_refazer → abre QuizActivity com o mesmo EXTRA_LICAO_ID e finish().
- Manifest: declarar ResultadoActivity (exported=false, portrait).

REGRAS: nada é salvo ainda.

COMPORTAMENTO ESPERADO: ao terminar a lição aparece o resultado correto; CONTINUAR volta para a trilha (não para o quiz); REFAZER reinicia a mesma lição.
```

---

### Aula 3

#### Prompt 11 — Utilitários de data e gamificação

```text
Projeto Android "ENEMigo" (Java, minSdk 26 — pode usar java.time.LocalDate). Constantes tem XP_ACERTO, XP_BONUS_PERFEITA, VIDAS_MAX, MINUTOS_RECARGA, XP_POR_NIVEL, QUESTOES_POR_LICAO.

OBJETIVO: criar funções puras (sem Android) para as regras do jogo.

ARQUIVOS: util/DataUtil.java, util/Gamificacao.java, app/src/test/java/.../GamificacaoTest.java (opcional).

IMPLEMENTAÇÃO:
- DataUtil: hoje() → LocalDate.now(); paraTexto(LocalDate) → toString(); deTexto(String) → LocalDate.parse ou null se vazio/inválido (capturar DateTimeParseException).
- Gamificacao (métodos static):
  - calcularNivel(int xp) = xp / XP_POR_NIVEL + 1.
  - calcularXpLicao(int acertos, int total) = acertos * XP_ACERTO (sem bônus); calcularBonus(int acertos, int total) = XP_BONUS_PERFEITA se acertos == total, senão 0.
  - calcularNovaOfensiva(LocalDate ultima, LocalDate hoje, int ofensivaAtual): ultima null → 1; igual a hoje → ofensivaAtual; ultima.plusDays(1) igual a hoje → ofensivaAtual + 1; ultima depois de hoje → ofensivaAtual; senão → 1.
  - ofensivaParaExibir(LocalDate ultima, LocalDate hoje, int ofensiva): se ultima for hoje ou ontem retorna ofensiva, senão 0.
  - vidasRecuperadas(long ultimaRecarga, long agora): quantas vidas inteiras de MINUTOS_RECARGA couberam em (agora - ultimaRecarga); se negativo, 0.
  - minutosParaProximaVida(long ultimaRecarga, long agora): MINUTOS_RECARGA - (minutos passados % MINUTOS_RECARGA).
- Opcional: testes JUnit para calcularNivel (0→1, 99→1, 100→2), calcularNovaOfensiva (ontem, hoje, anteontem, null, futuro) e vidasRecuperadas (29 min → 0, 30 min → 1, 95 min → 3).

REGRAS: nenhuma classe do Android (Context, SharedPreferences) nesses arquivos.

COMPORTAMENTO ESPERADO: compila; se criou os testes, todos passam em "Run Tests".
```

#### Prompt 12 — ProgressoManager (SharedPreferences)

```text
Projeto Android "ENEMigo" (Java, minSdk 26). Existem util/Constantes, util/DataUtil, util/Gamificacao, data/QuestaoRepository, model/Licao, model/Area.

OBJETIVO: centralizar todo o progresso do aluno no SharedPreferences.

ARQUIVOS: data/ProgressoManager.java, model/ResultadoSessao.java, util/Constantes.java.

IMPLEMENTAÇÃO:
- Constantes: chaves nome, meta_diaria, onboarding_ok, xp_total, xp_hoje, data_xp_hoje, ofensiva, maior_ofensiva, ultima_data_estudo, vidas, ultima_recarga_vida, prefixo "melhor_", caderno_erros, conquistas, licoes_perfeitas, total_respondidas, total_acertos, ultima_area, vibracao.
- ResultadoSessao: modo, acertos, total, xpGanho, bonus, ofensiva, metaBatidaAgora, desbloqueouProxima, vidasRecuperadas, novasConquistas (ArrayList<String>). Getters e setters.
- ProgressoManager singleton (getInstance(Context), usa getApplicationContext, arquivo Constantes.PREFS_NOME, MODE_PRIVATE). Sempre gravar com apply().
  - Perfil: getNome, setNome, getMetaDiaria (se não for 20/50/100 retorna 50), setMetaDiaria, isOnboardingConcluido, concluirOnboarding(nome, meta).
  - XP: getXpTotal, getNivel (Gamificacao), getXpHoje (se data_xp_hoje != hoje, retorna 0), metodo privado adicionarXp(int) que zera xp_hoje se mudou o dia e soma em xp_total e xp_hoje.
  - Vidas: getVidas() aplica a recuperação: se vidas < 5, ganhas = Gamificacao.vidasRecuperadas(ultimaRecarga, agora); se ultimaRecarga > agora, ultimaRecarga = agora; se ganhas > 0, vidas = min(5, vidas + ganhas) e ultimaRecarga += ganhas * 30 min; salva e retorna (limitado entre 0 e 5). perderVida(): chama getVidas(); se era 5, ultimaRecarga = agora; vidas - 1 (mín. 0). ganharVida(): +1 (máx. 5). getMinutosParaProximaVida().
  - Ofensiva: getOfensivaAtual() (usa ofensivaParaExibir e salva 0 se zerou), getMaiorOfensiva(), estudouHoje().
  - Lições: getMelhor(licaoId) (padrão -1); getEstado(Licao) retorna "CONCLUIDA" se melhor >= 3, "DISPONIVEL" se ordem == 0 ou a lição anterior (QuestaoRepository.getLicoes(area).get(ordem - 1)) tiver melhor >= 3, senão "BLOQUEADA"; getLicoesConcluidas(Area).
  - Caderno: getCaderno() retorna CÓPIA (new HashSet<>(prefs.getStringSet(...))); adicionarAoCaderno(id) e removerDoCaderno(id) alteram uma cópia e salvam a cópia.
  - registrarLicao(Licao licao, int acertos): calcula xp e bônus; guarda metaAntes = getXpHoje() < meta; adicionarXp; atualiza melhor se maior; desbloqueouProxima = melhorAnterior < 3 && acertos >= 3 && existe lição seguinte; atualiza ofensiva (Gamificacao.calcularNovaOfensiva), ultima_data_estudo = hoje, maior_ofensiva; soma total_respondidas/total_acertos; se 5/5, licoes_perfeitas++; ultima_area = licao.getArea().name(); metaBatidaAgora = metaAntes && getXpHoje() >= meta. Retorna ResultadoSessao (novasConquistas vazia por enquanto).
  - registrarRevisao(int acertos, int respondidas, int vidasRecuperadas): XP = acertos * XP_ACERTO_REVISAO, mesma lógica de meta, ofensiva e totais. Retorna ResultadoSessao com modo REVISAO.
  - resetarTudo(): edit().clear().apply().

REGRAS: nenhuma tela acessa SharedPreferences diretamente, só esta classe. Nunca modificar o Set devolvido por getStringSet.

COMPORTAMENTO ESPERADO: compila; os valores persistem depois de fechar e abrir o app.
```

#### Prompt 13 — Onboarding

```text
Projeto Android "ENEMigo" (Java + XML). Existe data/ProgressoManager com isOnboardingConcluido() e concluirOnboarding(nome, meta).

OBJETIVO: tela de primeiro acesso.

ARQUIVOS: ui/OnboardingActivity.java, res/layout/activity_onboarding.xml, ui/MainActivity.java, res/values/strings.xml, AndroidManifest.xml.

IMPLEMENTAÇÃO:
- Layout (ScrollView > LinearLayout centralizado, padding 24dp): ImageView do logo (use o ícone do app), txt "Oi! Eu sou o ENEMigo 👋", subtítulo "Vou te ajudar a estudar pro ENEM um pouquinho todo dia.", TextInputLayout (id til_nome, hint "Como você quer ser chamado?", counterEnabled, counterMaxLength 20) com TextInputEditText (id edt_nome, inputType textPersonName|textCapWords, maxLength 20), TextView "Qual a sua meta diária?", RadioGroup rg_meta com rb_casual ("Casual — 20 XP por dia"), rb_regular ("Regular — 50 XP por dia", marcado), rb_intenso ("Intenso — 100 XP por dia"), MaterialButton btn_comecar.
- Validação ao clicar em COMEÇAR (nome com trim): vazio → "Digite seu nome"; menos de 2 → "O nome precisa ter pelo menos 2 letras"; mais de 20 → "Use no máximo 20 caracteres"; não casar com ^[\p{L} ]+$ → "Use apenas letras". Mostrar com til_nome.setError; limpar o erro quando o texto mudar. Meta: se nenhum RadioButton marcado → Toast "Escolha uma meta diária".
- Válido: concluirOnboarding(nome, meta), abrir MainActivity e finish().
- MainActivity.onCreate: antes de setContentView, se !isOnboardingConcluido(): startActivity(Onboarding), finish(), return.
- Manifest: declarar OnboardingActivity (exported=false, portrait).

REGRAS: mensagens em strings.xml.

COMPORTAMENTO ESPERADO: na primeira vez abre o onboarding; depois de preencher, abre o Início e o onboarding não aparece mais; Voltar no Início fecha o app (não volta ao onboarding).
```

#### Prompt 14 — Integrar o progresso nas telas

```text
Projeto Android "ENEMigo" (Java + XML). Existem ProgressoManager (getNome, getVidas, getOfensivaAtual, estudouHoje, getXpTotal, getXpHoje, getMetaDiaria, getEstado, getMelhor, getLicoesConcluidas, perderVida, adicionarAoCaderno, registrarLicao, getMinutosParaProximaVida) e QuestaoRepository.

OBJETIVO: ligar as telas ao progresso salvo.

ARQUIVOS: ui/InicioFragment.java, ui/TrilhaActivity.java, ui/adapter/LicaoAdapter.java, ui/QuizActivity.java, ui/ResultadoActivity.java, util/Constantes.java.

IMPLEMENTAÇÃO:
- Criar em cada tela com barra de status um método atualizarStatus(): "🔥 N" (laranja_fogo se estudouHoje(), cinza_bloqueado se não), "⭐ XP", "❤️ vidas".
- InicioFragment.onResume: saudação "Oi, {nome}!", meta "xpHoje / meta XP" com progress (max = meta; se batida: "Meta batida! 🎉"), em cada card "concluídas/total lições".
- TrilhaActivity.onResume: recarrega a lista e txt_resumo "X de Y lições concluídas". LicaoAdapter usa getEstado: CONCLUIDA → círculo cor da área com "✓" e "Melhor: N/5"; DISPONIVEL → cor da área com número e "Começar"; BLOQUEADA → cinza_bloqueado com "🔒" e "Faça 3 acertos na lição anterior". Clique: BLOQUEADA → Toast "Complete a lição anterior com pelo menos 3 acertos"; getVidas() == 0 → diálogo "Sem vidas 💔" com "Próxima vida em X min" e botões "Ir para Revisão" (abre MainActivity na aba Revisão via extra) e "OK"; senão abre o Quiz.
- QuizActivity (modo LICAO): mostrar vidas no topo; ao errar: perderVida(), adicionarAoCaderno(questao.getId()), atualizar o contador. No CONTINUAR, se getVidas() == 0 e não for a última questão: diálogo "Suas vidas acabaram 💔" (Ir para Revisão / Sair) sem registrar a lição. Na última questão: ResultadoSessao r = registrarLicao(licao, acertos) e passar todos os campos como extras (adicione as constantes EXTRA_OFENSIVA, EXTRA_META_BATIDA, EXTRA_DESBLOQUEOU, EXTRA_VIDAS_RECUPERADAS, EXTRA_CONQUISTAS).
- ResultadoActivity: mostrar "🔥 Ofensiva: N dias", e em txt_mensagens (uma por linha): "Meta diária batida! 🎯" se metaBatida; "Próxima lição desbloqueada! 🔓" se desbloqueou; se acertos < 3: "Faça 3 acertos para liberar a próxima lição". REFAZER: se getVidas() == 0, mostrar o diálogo Sem vidas em vez de abrir.

REGRAS: todas as telas atualizam no onResume. O XP só é somado dentro de registrarLicao (não somar na QuizActivity).

COMPORTAMENTO ESPERADO: a Lição 2 só abre depois de 3+ acertos na Lição 1; errar tira vida na hora; fechar o app mantém XP, vidas, ofensiva e desbloqueios.
```

#### Prompt 15 — Aba Revisão e modo REVISAO

```text
Projeto Android "ENEMigo" (Java + XML). ProgressoManager tem getCaderno() (cópia), removerDoCaderno(id), ganharVida(), getVidas(), registrarRevisao(acertos, respondidas, vidasRecuperadas). QuestaoRepository tem getQuestao(id). QuizActivity funciona no modo LICAO.

OBJETIVO: caderno de erros e revisão.

ARQUIVOS: ui/RevisaoFragment.java, res/layout/fragment_revisao.xml, res/layout/item_caderno.xml, res/drawable/bg_etiqueta.xml, ui/adapter/CadernoAdapter.java, ui/QuizActivity.java, ui/ResultadoActivity.java, ui/MainActivity.java.

IMPLEMENTAÇÃO:
- fragment_revisao.xml: título "Caderno de erros", txt_contador, texto "Acerte na revisão e ganhe ❤️ +1", btn_revisar ("REVISAR AGORA"), RecyclerView rv_caderno e layout_vazio (emoji 🎉 + "Nenhum erro por aqui! Continue fazendo lições.").
- item_caderno.xml: MaterialCardView com etiqueta da área (bg_etiqueta com tint na cor da área, texto branco), enunciado (maxLines 2, ellipsize end) e fonte.
- RevisaoFragment.onResume: para cada id de getCaderno(), busca getQuestao(id); ids inexistentes são removidos com removerDoCaderno. Lista vazia → esconde lista, mostra layout_vazio, desabilita btn_revisar. Contador "Você tem N questões para revisar".
- btn_revisar abre QuizActivity com EXTRA_MODO = MODO_REVISAO (sem EXTRA_LICAO_ID).
- QuizActivity no modo REVISAO: monta a lista com até 5 questões sorteadas do caderno (Collections.shuffle numa ArrayList). Esconde txt_vidas_quiz. Errar NÃO tira vida nem mexe no caderno. Acertar: removerDoCaderno(id); se getVidas() < 5, ganharVida() e vidasRecuperadas++. No fim: registrarRevisao(acertos, total, vidasRecuperadas) e abre o Resultado com os extras.
- ResultadoActivity no modo REVISAO: esconde btn_refazer; mensagem "Você tirou N questões do caderno e recuperou M vidas"; CONTINUAR → finish().
- MainActivity: se receber o extra "ABRIR_REVISAO" = true, selecionar a aba Revisão (bottom_nav.setSelectedItemId).

REGRAS: nunca alterar o Set do SharedPreferences diretamente.

COMPORTAMENTO ESPERADO: errar numa lição adiciona a questão na Revisão; acertá-la na revisão remove da lista e devolve 1 vida; caderno vazio mostra a mensagem de parabéns.
```

---

### Aula 4

#### Prompt 16 — Conquistas e Perfil

```text
Projeto Android "ENEMigo" (Java + XML). ProgressoManager tem registrarLicao e registrarRevisao que retornam ResultadoSessao (com novasConquistas vazia), além de getXpTotal, getNivel, getOfensivaAtual, getMaiorOfensiva, getLicoesConcluidas(Area).

OBJETIVO: conquistas e tela de Perfil completa.

ARQUIVOS: model/Conquista.java, data/ProgressoManager.java, ui/PerfilFragment.java, res/layout/fragment_perfil.xml, res/layout/item_conquista.xml, res/menu/menu_perfil.xml, res/drawable/bg_avatar.xml, ui/adapter/ConquistaAdapter.java, ui/ResultadoActivity.java.

IMPLEMENTAÇÃO:
- Conquista (enum): PRIMEIRA_LICAO("🎯","Primeiro passo","Conclua sua primeira lição"), OFENSIVA_3("🔥","Pegando fogo","3 dias seguidos"), OFENSIVA_7("🌋","Semana imbatível","7 dias seguidos"), XP_100("⭐","Cem na conta","Alcance 100 XP"), XP_500("🌟","Estudante dedicado","Alcance 500 XP"), LICAO_PERFEITA("🏆","Gabaritou!","Acerte 5 de 5 numa lição").
- ProgressoManager: getConquistas() (cópia do Set); método privado verificarConquistas() que testa cada condição (PRIMEIRA_LICAO: total_respondidas >= 5; OFENSIVA_3/7: ofensiva >= 3/7; XP_100/500; LICAO_PERFEITA: licoes_perfeitas >= 1), adiciona as novas ao Set salvo e retorna a lista das novas. Chamar no fim de registrarLicao e registrarRevisao (depois de XP e ofensiva) e colocar em resultado.novasConquistas. Adicionar getTotalRespondidas, getTotalAcertos, getTotalLicoesConcluidas.
- ResultadoActivity: para cada id em EXTRA_CONQUISTAS, adicionar a linha "🏅 Conquista desbloqueada: {título}".
- fragment_perfil.xml: MaterialToolbar "Perfil" com menu_perfil (item engrenagem, showAsAction always); avatar (TextView 80dp com bg_avatar oval verde e a inicial do nome); nome; "Nível N"; LinearProgressIndicator (max 100, progresso xp % 100); "X / 100 XP para o nível N+1"; grade 2 colunas com 6 cards (XP total, Ofensiva atual, Maior ofensiva, Lições concluídas, Questões respondidas, Taxa de acerto — mostrar "—" se respondidas == 0); "Conquistas (N/6)"; RecyclerView com GridLayoutManager(3) e nestedScrollingEnabled false.
- ConquistaAdapter: emoji, título, descrição; bloqueada com alpha 0.3 e "🔒" antes do título.
- PerfilFragment.onResume preenche tudo; o item engrenagem abre ConfiguracoesActivity (criar a classe vazia se ainda não existir).

REGRAS: uma conquista nunca é perdida (só no reset).

COMPORTAMENTO ESPERADO: a primeira lição mostra "Conquista desbloqueada: Primeiro passo"; o Perfil mostra nível, estatísticas corretas e conquistas acesas/apagadas.
```

#### Prompt 17 — Configurações e reset

```text
Projeto Android "ENEMigo" (Java + XML). ProgressoManager tem getNome, setNome, getMetaDiaria, setMetaDiaria, resetarTudo. As validações de nome estão na OnboardingActivity.

OBJETIVO: tela de Configurações.

ARQUIVOS: ui/ConfiguracoesActivity.java, res/layout/activity_configuracoes.xml, util/ValidacaoUtil.java (novo), ui/OnboardingActivity.java, AndroidManifest.xml.

IMPLEMENTAÇÃO:
- ValidacaoUtil.validarNome(String nome): retorna null se válido ou a mensagem de erro (mesmas regras do onboarding). Refatorar a OnboardingActivity para usar este método.
- Layout: MaterialToolbar "Configurações" com seta de voltar; TextInputLayout + TextInputEditText do nome; RadioGroup da meta (20/50/100); SwitchMaterial "Vibrar ao errar" (opcional); MaterialButton "SALVAR"; divisor; MaterialButton "RESETAR PROGRESSO" com backgroundTint vermelho; TextView Sobre: "ENEMigo v1.0 — Questões retiradas das provas oficiais do ENEM, disponíveis publicamente no site do INEP. App educacional sem vínculo com o INEP."
- Ao abrir: preencher nome e meta atuais.
- SALVAR: validar; se ok, setNome, setMetaDiaria, Toast "Configurações salvas" e finish().
- RESETAR: MaterialAlertDialogBuilder com título "Apagar todo o progresso?", mensagem "XP, ofensiva, lições e conquistas serão perdidos. Essa ação não pode ser desfeita.", botões "Cancelar" e "Apagar tudo". Confirmado: resetarTudo(); Intent para OnboardingActivity com FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK; startActivity.
- Manifest: declarar ConfiguracoesActivity (exported=false, portrait).

REGRAS: sair sem salvar não altera nada.

COMPORTAMENTO ESPERADO: mudar a meta reflete no cartão da tela Início; o reset volta ao onboarding e o Voltar não retorna às telas antigas.
```

#### Prompt 18 — Complementares e acabamento

```text
Projeto Android "ENEMigo" (Java + XML). Tudo da versão principal funciona.

OBJETIVO: implementar os complementares e revisar o visual.

ARQUIVOS: ui/InicioFragment.java, res/layout/fragment_inicio.xml, data/ProgressoManager.java, ui/QuizActivity.java, AndroidManifest.xml, res/values/strings.xml, layouts em geral.

IMPLEMENTAÇÃO:
1) Próxima vida: na barra de status, se vidas < 5, mostrar abaixo um TextView pequeno "Próxima ❤️ em X min".
2) Cartão Continuar: ProgressoManager.getLicaoSugerida() retorna a primeira lição DISPONIVEL e não CONCLUIDA da ultima_area; se não houver, procura em todas as áreas na ordem do enum; se tudo concluído, null. No Início, mostrar o cartão "Continuar: {área} · Lição N — {título}" (GONE se null); clique abre o Quiz (verificar vidas antes).
3) Vibração (opcional): permissão VIBRATE no Manifest; na QuizActivity, ao errar, se a vibração estiver ativa nas configurações, vibrar 150 ms (VibrationEffect.createOneShot).
4) Acabamento: aplicar a fonte Nunito (res/font) no tema; revisar que TODOS os textos visíveis estão em strings.xml; margens de 16dp; botões com altura 56dp e cantos 12dp; conferir que nenhum texto fica cortado em tela pequena (emulador 5").

REGRAS: não mudar as regras de negócio. Não adicionar dependências.

COMPORTAMENTO ESPERADO: o Início sugere a próxima lição com um toque; com menos de 5 vidas o tempo da próxima aparece; o app tem visual consistente.
```

#### Prompt 19 — Revisão final de robustez

```text
Projeto Android "ENEMigo" (Java + XML, minSdk 26). O app está completo.

OBJETIVO: revisar o código em busca de falhas antes da apresentação.

ARQUIVOS: todos os arquivos dos pacotes data, ui e util.

VERIFIQUE E CORRIJA:
- Algum lugar modifica diretamente o Set retornado por getStringSet? (deve sempre copiar com new HashSet<>)
- Todas as Activities que leem extras tratam extra ausente/id inexistente com Toast "Lição não encontrada" + finish()?
- Existe algum catch vazio? (trocar por Log.e com tag da classe)
- Os botões VERIFICAR, CONTINUAR (última questão) e SALVAR são desabilitados após o clique para evitar clique duplo?
- As telas que mostram progresso (InicioFragment, RevisaoFragment, PerfilFragment, TrilhaActivity) atualizam no onResume?
- getVidas() corrige ultimaRecarga no futuro e limita vidas entre 0 e 5? getMetaDiaria() devolve 50 para valores inválidos?
- A QuizActivity chama finish() ao abrir o Resultado?
- Algum texto visível está fixo no Java/XML em vez de strings.xml?
- Existem imports ou métodos não usados?

REGRAS: explique cada problema encontrado antes de corrigir. Não mudar as regras de negócio (XP 10/5, vidas 5/30 min, desbloqueio com 3 acertos).

COMPORTAMENTO ESPERADO: lista dos problemas encontrados e as correções aplicadas; o app continua compilando e passando nos testes da seção 18.
```

---

## 18. Testes

Faça os testes num emulador (ex.: Pixel com API 34 ou 35) **e**, se possível, num celular real. Marque cada teste como OK ou anote o problema.

> **Dicas para testar tempo:** (1) mude temporariamente `MINUTOS_RECARGA` para 1 para ver as vidas voltando; (2) para a ofensiva, altere a data do emulador em Configurações → Sistema → Data e hora (desative "Data automática"). Lembre de desfazer as duas coisas depois.
>
> **Para começar do zero:** Configurações do app → Resetar progresso, ou Configurações do Android → Apps → ENEMigo → Armazenamento → Limpar dados.

### 18.1 Testes das telas

| ID | Teste | Resultado esperado |
|---|---|---|
| T01 | Abrir o app pela primeira vez | Aparece o Onboarding |
| T02 | Abrir o app depois do onboarding | Abre direto na aba Início |
| T03 | Ver a tela Início | Status 🔥⭐❤️, saudação com o nome, meta diária e 4 cartões de área |
| T04 | Abrir a trilha de cada uma das 4 áreas | Toolbar com o nome e a cor da área; lições na ordem do JSON |
| T05 | Abrir uma lição | "Questão 1 de N" (N = questões da lição), barra de progresso vazia, vidas no topo |
| T06 | Questão sem texto-base | O cartão do texto-base não aparece |
| T07 | Questão com texto longo | A tela rola e nada fica cortado |
| T08 | Abrir a aba Revisão com o caderno vazio | Mensagem "Nenhum erro por aqui! 🎉" e botão desabilitado |
| T09 | Abrir o Perfil | Nome, nível, barra de XP, 6 estatísticas e 6 conquistas |
| T10 | Abrir Configurações | Nome e meta atuais já preenchidos |

### 18.2 Testes dos botões

| ID | Teste | Resultado esperado |
|---|---|---|
| T11 | Tocar em cada aba da barra inferior | Troca de conteúdo e aba destacada |
| T12 | VERIFICAR sem escolher alternativa | Botão desabilitado, nada acontece |
| T13 | Tocar duas vezes rápido em VERIFICAR | Corrige uma única vez (não perde 2 vidas) |
| T14 | Tocar em outra alternativa depois de verificar | Nada muda (alternativas travadas) |
| T15 | CONTINUAR na última questão | Abre o Resultado |
| T16 | Voltar do sistema no Resultado | Volta para a Trilha, e não para o Quiz |
| T17 | REFAZER LIÇÃO | Reinicia a mesma lição na questão 1 |
| T18 | X no Quiz → "Continuar estudando" | Continua na mesma questão |
| T19 | X no Quiz → "Sair" | Volta para a Trilha sem registrar a lição |
| T20 | Engrenagem no Perfil | Abre Configurações |

### 18.3 Testes dos campos

| ID | Teste | Resultado esperado |
|---|---|---|
| T21 | Nome vazio no Onboarding | "Digite seu nome" no campo |
| T22 | Nome só com espaços | "Digite seu nome" |
| T23 | Nome "A" | "O nome precisa ter pelo menos 2 letras" |
| T24 | Nome com 21 ou mais caracteres | O campo não aceita passar de 20 (contador 20/20) |
| T25 | Nome "Ana123" ou "@na" | "Use apenas letras" |
| T26 | Nome "José Antônio" | Aceito (acentos e espaço são permitidos) |
| T27 | Nome "  Ana  " | Salvo como "Ana" |
| T28 | Mudar o nome nas Configurações e salvar | Saudação e Perfil mostram o novo nome |

### 18.4 Testes das regras

| ID | Teste | Resultado esperado |
|---|---|---|
| T29 | Acertar 2 de 3 (ou 3 de 4) | +20 XP (ou +30), "Lição concluída!", próxima lição desbloqueada |
| T30 | Acertar todas de uma lição de 3 | +30 XP +5 bônus = 35 XP, "Perfeito!", conquista "Gabaritou!" |
| T31 | Acertar 1 de 3 (ou 2 de 4) | +10 XP (ou +20), "Quase lá!", "Para liberar a próxima lição, acerte 2 de 3", próxima continua bloqueada |
| T32 | Tocar numa lição bloqueada | Toast "Complete a lição anterior com pelo menos 60% de acertos" |
| T33 | Refazer uma lição com nota menor que a anterior | O "Melhor" continua com a nota maior |
| T34 | Errar uma questão numa lição | Vidas −1 na hora e a questão aparece na Revisão |
| T35 | Errar a mesma questão duas vezes (em duas tentativas) | Aparece só uma vez no caderno |
| T36 | Ficar com 0 vidas no meio da lição | Diálogo "Suas vidas acabaram"; a lição não é registrada |
| T37 | Tentar abrir uma lição com 0 vidas | Diálogo "Sem vidas" com o tempo para a próxima vida |
| T38 | Esperar 30 min (ou 1 min no modo teste) com menos de 5 vidas | +1 vida |
| T39 | Acertar uma questão na Revisão | Sai do caderno, ❤️ +1 (até 5), +5 XP |
| T40 | Errar uma questão na Revisão | Vidas não mudam e a questão continua no caderno |
| T41 | Terminar a primeira lição do dia | Ofensiva +1 (ou 1) e fogo colorido |
| T42 | Terminar outra lição no mesmo dia | Ofensiva não muda |
| T43 | Mudar a data para amanhã e fazer uma lição | Ofensiva +1 |
| T44 | Mudar a data para 2 dias depois e abrir o app | Ofensiva mostra 0; o recorde continua no Perfil |
| T45 | Atingir a meta diária | "Meta diária batida! 🎯" no Resultado e "Meta batida" no Início |
| T46 | Mudar a data para amanhã | "XP hoje" volta para 0 |
| T47 | Chegar a 100 XP | Nível 2 e conquista "Cem na conta" |
| T48 | Trocar a meta para 20 nas Configurações | O cartão da meta mostra "/ 20 XP" |
| T68 | Abrir a trilha de cada área | Cada nó mostra "LIÇÃO N · ~X MIN"; Linguagens, Humanas e Natureza entre ~3 e ~6 min, Matemática entre ~6 e ~8 |
| T69 | Responder uma questão, mandar o app para segundo plano por 2 min e voltar | O tempo médio do Perfil não conta esses 2 min |
| T70 | Fazer uma lição e abrir o Perfil | "Tempo médio por questão" com valor em segundos e "Minutos estudados hoje" maior que 0 |
| T71 | Responder 10 questões bem mais devagar que a estimativa | Os tempos da trilha sobem (até o dobro) |

### 18.5 Testes de dados

| ID | Teste | Resultado esperado |
|---|---|---|
| T49 | Fazer lições, fechar o app (tirar dos recentes) e abrir de novo | XP, vidas, ofensiva, desbloqueios, caderno e conquistas mantidos |
| T50 | Reiniciar o emulador | Mesmo resultado do T49 |
| T51 | Resetar o progresso | Volta para o Onboarding; depois dele, tudo zerado (XP 0, 5 vidas, lições bloqueadas) |
| T52 | Conferir o gabarito de 10 questões do JSON com o gabarito oficial do INEP | Todos batem |
| T53 | Conferir se todas as lições têm de 3 a 5 questões (`python tools/validar_questoes.py` e o Logcat com a tag QuestaoRepository) | Validador OK e nenhum aviso de "lição ignorada" |
| T72 | Com lições concluídas na versão antiga (4 lições de 5), instalar a nova por cima | As lições novas formadas só por questões de lições concluídas aparecem concluídas, em sequência; o caderno continua com as mesmas questões; XP e ofensiva iguais |

### 18.6 Testes de situações inválidas

| ID | Teste | Resultado esperado |
|---|---|---|
| T54 | Colocar uma vírgula a mais no JSON (cópia de teste) e rodar | O app não fecha; o Início mostra "Não foi possível carregar as questões." |
| T55 | Questão com `"correta": "F"` | Questão ignorada, aviso no Logcat |
| T56 | Lição com 2 (ou 6) questões | Lição ignorada, aviso no Logcat |
| T57 | Remover do JSON uma questão que está no caderno de erros | Ela some da Revisão sem travar |
| T58 | Voltar a data do celular para o passado | Nenhuma vida extra e a ofensiva não aumenta |
| T59 | Girar o celular em qualquer tela | A tela não gira |
| T60 | Modo escuro do Android ligado | O app continua claro e legível |

### 18.7 Testes de navegação

| ID | Teste | Resultado esperado |
|---|---|---|
| T61 | Voltar no Início | Fecha o app |
| T62 | Voltar na Trilha | Volta para o Início com os dados atualizados |
| T63 | Início → Trilha → Quiz → Resultado → Continuar | Volta para a Trilha com a lição concluída e a próxima liberada |
| T64 | "Ir para Revisão" no diálogo Sem vidas | Abre a MainActivity na aba Revisão |
| T65 | Revisão → Revisar agora → Resultado → Continuar | Volta para a aba Revisão com a lista atualizada |
| T66 | Depois do reset, apertar Voltar no Onboarding | Fecha o app (não volta ao Perfil antigo) |
| T67 | Cartão Continuar no Início | Abre direto o Quiz da lição sugerida |

---

## 19. Checklist final

**Funcionamento**

- [ ] O app instala e abre sem travar no emulador **e** num celular real.
- [ ] Onboarding aparece só no primeiro acesso e valida o nome.
- [ ] As 4 áreas têm lições de 3 a 5 questões, de uns 4 minutos (80 questões; `validar_questoes.py` OK).
- [ ] Quiz: seleção, VERIFICAR, feedback verde/vermelho com explicação, CONTINUAR e barra de progresso.
- [ ] Resultado mostra acertos, XP, bônus, ofensiva, meta, desbloqueio e conquistas.
- [ ] Desbloqueio sequencial com 60% de acertos funcionando.
- [ ] Vidas: perde no erro, bloqueia sem vida e recupera com o tempo (`MINUTOS_RECARGA` voltou para **30**).
- [ ] Ofensiva e meta diária corretas.
- [ ] Caderno de erros e revisão funcionando (+1 vida no acerto).
- [ ] Perfil com nível, estatísticas e conquistas.
- [ ] Configurações salvam e o reset pede confirmação.
- [ ] Progresso mantido depois de fechar e reabrir.

**Conteúdo**

- [ ] Todos os gabaritos conferidos com o gabarito oficial do INEP.
- [ ] Todas as questões com `fonte` preenchida.
- [ ] Explicações revisadas (sem erro de conteúdo nem de português).
- [ ] Letras corretas bem distribuídas entre A e E.

**Qualidade do código**

- [ ] Pacotes `model`, `data`, `ui` e `util` organizados.
- [ ] Nenhum texto visível fixo no código (tudo em `strings.xml`).
- [ ] Nenhum `catch` vazio.
- [ ] Nenhum aviso de "lição/questão ignorada" no Logcat.
- [ ] Código sem trechos comentados sobrando ou métodos não usados.
- [ ] Nomes de variáveis e métodos em português, claros e consistentes.

**Visual**

- [ ] Ícone do app personalizado.
- [ ] Cores das áreas consistentes em cartões, trilha e etiquetas.
- [ ] Textos legíveis, nada cortado em tela pequena.
- [ ] Tela travada em retrato.
- [ ] Sem logo, nome ou mascote do Duolingo.

**Apresentação**

- [ ] APK gerado (*Build → Build APK(s)*) e instalado no celular.
- [ ] Progresso **resetado** antes de apresentar (para mostrar o onboarding).
- [ ] Roteiro da demonstração: onboarding → lição com 1 erro → resultado → trilha desbloqueada → revisão (recuperar vida) → perfil com conquista.
- [ ] Saber explicar: por que SharedPreferences + JSON (e não Room), como funciona o cálculo da ofensiva e das vidas, e o papel do `ProgressoManager` e do `QuestaoRepository`.
- [ ] Este documento (`PROJETO_APP_ANDROID.md`) entregue junto com o projeto.

---

## 20. Possíveis melhorias

**Conteúdo**

- Questões com imagem também fora de Matemática (mapas, charges), usando os mesmos marcadores `[[imagem:...]]` e os arquivos em `assets/imagens/`.
- Mais lições por área, organizadas por assunto (ex.: Matemática → Porcentagem, Funções, Geometria, Estatística).
- Filtro por ano da prova ("só ENEM 2023").
- Treino de redação: temas antigos, repertório e checklist das 5 competências.
- Banco de questões online (API REST ou Firebase), atualizado sem publicar nova versão do app.

**Engajamento**

- "Congelar ofensiva": item comprado com XP que protege a ofensiva por um dia.
- Baús de recompensa a cada 5 lições.
- Animações de comemoração (Lottie) e sons de acerto e erro (SoundPool).
- Widget na tela inicial do Android com a ofensiva e a meta do dia.
- Compartilhar conquistas e ofensiva nas redes sociais (Intent de compartilhamento).

**Social**

- Login (Firebase Authentication) e sincronização do progresso entre aparelhos.
- Ligas semanais e ranking entre amigos ou colegas de turma.
- Desafio entre amigos: a mesma lição, quem acerta mais.

**Estudo**

- Simulado cronometrado (ex.: 45 questões em 2h15, no ritmo do ENEM real).
- Estatísticas por área e por habilidade da matriz de referência do ENEM, com gráfico de evolução.
- Histórico de respostas com Room, o que permite relatórios como "sua taxa de acerto em Natureza subiu 15% este mês".
- Repetição espaçada no caderno de erros (a questão volta depois de 1, 3 e 7 dias).
- Estimativa de nota com base no desempenho (lembrando que o ENEM usa a TRI, então seria só uma aproximação).

**Técnica e acessibilidade**

- Modo escuro (`values-night/colors.xml`).
- Suporte a tablets e orientação paisagem (salvando o estado do quiz em `onSaveInstanceState`).
- Acessibilidade: `contentDescription` em todos os ícones, teste com o TalkBack e tamanho de fonte ajustável.
- Migração para a arquitetura MVVM (ViewModel + LiveData) quando o app crescer.
- Testes automatizados de interface com Espresso.
