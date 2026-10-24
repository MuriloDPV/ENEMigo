# ENEMigo

Um app Android no estilo Duolingo que, em vez de ensinar idiomas, treina para o ENEM com **questões reais de provas anteriores**, em lições curtas e diárias.

Feito como projeto da disciplina de Programação do meu curso técnico, em Java + XML no Android Studio.

## Como funciona

- O conteúdo é dividido nas áreas do ENEM, cada uma com uma **trilha de lições**.
- Cada lição tem **5 questões** (A a E). Você responde, aperta **VERIFICAR** e vê na hora se acertou, com uma explicação curta.
- A próxima lição só abre quando você acerta pelo menos 3 de 5.
- Tudo funciona **offline**: as questões ficam dentro do app e o progresso fica salvo no celular.

## Funcionalidades

- Primeiro acesso com nome e meta diária de XP (20, 50 ou 100)
- Trilhas por área com lições concluídas, disponíveis e bloqueadas
- Quiz com feedback imediato e explicação de cada questão
- Figuras e fórmulas nas questões de Matemática, com zoom ao tocar na figura
- XP e níveis (+10 XP por acerto, +5 de bônus na lição perfeita)
- 5 vidas: cada erro tira uma, e uma volta a cada 30 minutos
- Ofensiva diária (dias seguidos estudando) e meta diária
- Caderno de erros: as questões que você errou vão para a aba Revisão, e acertar lá devolve uma vida
- Perfil com nível, XP, ofensiva, recorde, lições concluídas e taxa de acerto

## Questões

São **80 questões oficiais do ENEM (2019 a 2023)**, distribuídas em Linguagens, Ciências Humanas, Ciências da Natureza e Matemática (4 lições de 5 questões em cada área). Cada questão mostra a fonte, por exemplo "ENEM 2019 – Questão 29". O gabarito é sempre o oficial.

Em Matemática, as figuras (gráficos, tabelas, plantas) ficam dentro do app, em `app/src/main/assets/imagens/`, então tudo continua funcionando offline. A API entrega as fórmulas como imagens muito pequenas, que ficariam borradas no celular; por isso as alternativas que são fórmula foram redigitadas em LaTeX e renderizadas em alta resolução pelo script de montagem.

As questões vêm da API pública [enem.dev](https://enem.dev), e os scripts que montam o banco ficam em [`tools/`](tools):

- `baixar_enem.py`: baixa as provas da API para `tools/enem_raw/` (essa pasta não vai para o repositório)
- `filtrar_enem.py`: separa as questões aproveitáveis (100% texto nas três primeiras áreas; em Matemática, também as com figura ou fórmula)
- `montar_questoes.py`: gera o `app/src/main/assets/questoes.json`, baixa as figuras e renderiza as fórmulas
- `validar_questoes.py`: confere o JSON e as imagens contra a especificação e contra o original da API

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

## Estrutura

```text
app/src/main/java/com/murilo/enemigo/
├── data/    # leitura do questoes.json e progresso salvo (SharedPreferences)
├── model/   # Area, Licao, Questao, ResultadoSessao
├── ui/      # Activities, Fragments e Adapters
└── util/    # regras de gamificação (XP, vidas, ofensiva) e constantes
```

As regras de gamificação ficam em `Gamificacao.java` e têm testes em `app/src/test`.

A especificação completa do app (telas, regras de negócio, plano das aulas) está em [`docs/PROJETO_APP_ANDROID.md`](docs/PROJETO_APP_ANDROID.md).

## Próximos passos

- Conquistas
- Tela de configurações (editar nome, trocar meta, resetar progresso)
- Lembrete diário por notificação

## Créditos e licença

- Questões: provas do ENEM, elaboradas e publicadas pelo **INEP**. Obtidas pela API [enem.dev](https://enem.dev).
- O código do app está sob a licença MIT (veja [LICENSE](LICENSE)). A licença não se aplica às questões do ENEM, que continuam pertencendo ao INEP.
