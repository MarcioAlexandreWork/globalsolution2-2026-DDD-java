# ZeladoriaCidadã

> **Global Solution 2026.2 · FIAP · CitizenTech · Domain Driven Design - Java**

## Ficha da CitizenTech

| Campo | Resposta |
|---|---|
| **Nome da CitizenTech** | ZeladoriaCidadã |
| **Cidade e bairro** | São Paulo, Santana (Zona Norte) |
| **Cidadão beneficiado** | Moradores de Santana, com foco em idosos e pessoas com mobilidade reduzida, e agentes da subprefeitura responsáveis pelo atendimento |
| **Problema, em uma frase** | Moradores de Santana convivem com buracos, postes apagados e lixo acumulado, mas não sabem se o problema foi registrado, quando será resolvido nem como acompanhar. |
| **Como funciona** | O morador registra a ocorrência, a solução prioriza e encaminha, a equipe atua e o cidadão acompanha o status até a resolução. |
| **O que cada parte faz** | **Java:** API REST de ocorrências · **Database:** modelo relacional e scripts SQL · **Data Science:** previsão do tempo de resolução e do risco de atraso · **Dynamic Programming:** rotas e priorização · **Network:** rede do posto de atendimento · **Agile:** personas, backlog, protótipo e pitch · **AR-VR:** vídeo 3D |
| **Link do repositório** | `Repostiório com todos os outros projetos: https://github.com/MarcioAlexandreWork/GS-2-2026-Zeladoria/` |
| **Indicador de impacto** | Tempo médio de resolução das ocorrências (dias) e % resolvidas dentro do prazo |
| **ODS conectados** | ODS 11 (principal), ODS 10 e ODS 9 |

**Integrantes:**

| Nome completo | RM |
|---|---|
| Fabio Apolinario | 561828 |
| Leonardo Lopes Bellim | 562171 |
| Marcio Alexandre | 566238 |

---

## Arquitetura

API RESTful em **Java 21 + Spring Boot**, em camadas, com banco **Oracle (FIAP)** acessado por **JDBC** (DAO):

```
Controller  →  BO (regras de negócio)  →  DAO (JDBC / Oracle)
     ↓
GlobalExceptionHandler (tratamento de exceções)
```

| Pacote | Responsabilidade |
|---|---|
| `model` | Classes de domínio e enums (`Cidadao`, `Categoria`, `Regiao`, `Ocorrencia`, `Acompanhamento`) com Bean Validation |
| `dao` | Acesso ao Oracle com `PreparedStatement` (uma classe por tabela) |
| `bo` | Regras de negócio e orquestração |
| `controller` | Endpoints REST (GET, POST, PUT, DELETE) |
| `exception` | Exceções de domínio e `@RestControllerAdvice` com resposta de erro padronizada |
| `resources/static` | Front-end responsivo (`index.html`, `css/app.css`, `js/app.js`) servido pela própria aplicação |

### Regras de negócio
- Toda ocorrência nasce **ABERTA**, com data de abertura automática e prioridade **MEDIA** quando não informada.
- Cidadão, categoria e região informados precisam existir (senão, `422`).
- Ocorrência **RESOLVIDA** não pode ser reaberta (`422`); ao resolver, a `dataResolucao` é preenchida.
- Registrar um **acompanhamento** com novo status atualiza o status da ocorrência.
- E-mail de cidadão e nomes de categoria/região são únicos.
- Não é possível excluir cidadão, categoria ou região que possuam ocorrências vinculadas (`422`).
- Excluir uma ocorrência remove seu histórico (`ON DELETE CASCADE`).

## Endpoints

Base: `/api` · Interface web (front-end): `/` · Documentação interativa (Swagger): `/swagger-ui.html`

| Recurso | GET (lista) | GET (um) | POST | PUT | DELETE |
|---|---|---|---|---|---|
| Cidadãos | `/cidadaos` | `/cidadaos/{id}` | `/cidadaos` | `/cidadaos/{id}` | `/cidadaos/{id}` |
| Categorias | `/categorias` | `/categorias/{id}` | `/categorias` | `/categorias/{id}` | `/categorias/{id}` |
| Regiões | `/regioes` | `/regioes/{id}` | `/regioes` | `/regioes/{id}` | `/regioes/{id}` |
| Ocorrências | `/ocorrencias?status=&idCategoria=&idRegiao=` | `/ocorrencias/{id}` | `/ocorrencias` | `/ocorrencias/{id}` | `/ocorrencias/{id}` |
| Acompanhamentos | `/ocorrencias/{id}/acompanhamentos` | `.../acompanhamentos/{idAcomp}` | `.../acompanhamentos` | `.../acompanhamentos/{idAcomp}` (só o comentário) | `.../acompanhamentos/{idAcomp}` |

**Extra:** `POST /api/cidadaos/identificar` recebe `nome`, `email`, `telefone` e `tipo`; reaproveita o cadastro pelo e-mail ou cria um novo cidadão sempre como `MORADOR`.

Códigos: `200` OK · `201` criado (com `Location`) · `204` excluído · `400` dados inválidos · `404` não encontrado · `422` regra de negócio · `500` erro inesperado.

## Front-end

Interface web moderna e acessível, servida em `/` (sem dependências externas):

- **Painel:** indicadores em tempo real, gráficos por situação, tipo e local, % dentro do prazo e tempo médio de resolução.
- **Ocorrências:** busca, filtros, ordenação, visão em cartões ou tabela, barra de prazo e exportação em CSV.
- **Registrar:** a própria pessoa digita nome, e-mail e telefone; escolhe tipo e local; recebe um protocolo (ex.: `ZC-00009`).
- **Detalhe:** histórico, andamento, edição, exclusão e link direto (`#ocorrencia/ID`).
- **Cadastros:** CRUD de categorias (com prazo padrão) e de regiões.
- **Personalização e acessibilidade:** tema escuro/claro, alto contraste, tamanho do texto, navegação por teclado e "lembrar meus dados" (salvos só no navegador).
- **Privacidade:** o nome de quem registrou aparece abreviado (ex.: *Beatriz P.*).

### Exemplo: registrar uma ocorrência
`POST /api/ocorrencias`
```json
{
  "titulo": "Poste apagado na esquina",
  "descricao": "Trecho escuro à noite.",
  "prioridade": "ALTA",
  "idCidadao": 1,
  "idCategoria": 2,
  "idRegiao": 2
}
```

### Exemplo: resposta de erro padronizada
```json
{
  "timestamp": "2026-11-05T20:15:00",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos na requisição.",
  "caminho": "/api/ocorrencias",
  "detalhes": ["titulo: O título é obrigatório."]
}
```

---

## Como executar o projeto

### Ferramentas e versões
- IntelliJ IDEA (ou outra IDE Java)
- **Java JDK 21**
- **Maven 3.9+**
- **Banco Oracle da FIAP** (Oracle 12c ou superior, por causa das colunas `IDENTITY`)
- Git

### Passo a passo (local)
1. Clone o repositório: `git clone https://github.com/MarcioAlexandreWork/globalsolution2-2026-DDD-java/`
2. No Oracle da FIAP (SQL Developer), execute em ordem os scripts da pasta `database/`:
   `01_ddl.sql` → `02_dml.sql` (opcional, `00_drop.sql` recria do zero e `03_dql.sql` traz consultas de exemplo).
3. Configure as variáveis de ambiente (veja `.env.example`):
   - `DB_URL` (padrão: `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL`)
   - `DB_USER` (seu RM)
   - `DB_PASSWORD`
4. Rode: `mvn spring-boot:run` (ou execute `ZeladoriaCidadaApplication` na IDE).
5. Acesse `http://localhost:8080` (interface web) e `http://localhost:8080/swagger-ui.html` (teste dos endpoints).

> **Segurança:** usuário e senha do banco nunca ficam no código nem no GitHub, somente em variáveis de ambiente.

### Deploy (Render)
- **Link do deploy:** `https://globalsolution2-2026-ddd-java.onrender.com/`
1. No GitHub, suba o projeto (o `Dockerfile` já está na raiz).
2. No [render.com](https://render.com): **New → Web Service** → conecte o repositório → *Runtime:* **Docker**.
3. Em **Environment**, cadastre `DB_URL`, `DB_USER` e `DB_PASSWORD`.
4. (Opcional) *Health Check Path:* `/v3/api-docs`.
5. Clique em **Deploy**. Ao terminar, teste `https://SEU-APP.onrender.com/swagger-ui.html`.

> No plano gratuito do Render o serviço "dorme" após inatividade; a primeira chamada pode levar cerca de um minuto.

## Dados e privacidade
Todos os dados de exemplo são **fictícios** (nomes, e-mails, telefones e CEPs ilustrativos). Nenhum dado pessoal real é utilizado.

## Uso de IA
Ferramentas de IA foram usadas como copiloto para revisão e apoio ao desenvolvimento. Os prompts utilizados estão registrados em `prompts.md`.
