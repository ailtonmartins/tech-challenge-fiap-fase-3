# Tech Challenge FIAP — Fase 3

Backend hospitalar modular para agendamento de consultas, consulta de histórico
e envio de lembretes. A solução usa Java 21, Spring Boot, PostgreSQL e Kafka.

## Estado atual

As histórias de usuário estão implementadas. A solução reúne agendamento REST,
histórico GraphQL, eventos Kafka e notificações. Quando recebe um evento de
consulta, o serviço de notificações consulta os dados atuais do paciente via
gRPC no serviço de agendamento antes de acionar os canais de notificação.

## Arquitetura inicial

```text
Cliente
  ├─ servico-agendamento  :8080 (gRPC interno :6565) ── PostgreSQL :5432 / schema agendamento
  ├─ servico-historico    :8081 ──── PostgreSQL :5432 / schema historico
  └─ servico-notificacao  :8082 ─── PostgreSQL :5432 / schema notificacao

Todos os serviços compartilham o Kafka :9092 pela rede interna Docker. O
Kafka UI fica disponível em :8085.
```

## Pré-requisitos

- Docker Engine 28+ com Docker Compose v2;
- Java 21, apenas para executar os serviços fora do Docker.

## Como executar

1. Crie o arquivo local de variáveis:

   ```bash
   cp .env.example .env
   ```

2. Inicie toda a plataforma:

   ```bash
   docker compose up --build
   ```

3. Em outro terminal, confirme a saúde dos containers:

   ```bash
   docker compose ps
   ```

Os health checks dos serviços estão disponíveis em:

- `http://localhost:8080/actuator/health` — agendamento;
- `http://localhost:8081/actuator/health` — histórico;
- `http://localhost:8082/actuator/health` — notificações.

As mensagens, tópicos e consumer groups podem ser inspecionados em
`http://localhost:8085` pelo Kafka UI.

Para parar o ambiente, use `docker compose down`. Os dados dos bancos e do
Kafka são preservados em volumes nomeados. Para removê-los deliberadamente,
use `docker compose down --volumes`.

## Configuração

O arquivo [`.env.example`](.env.example) contém apenas valores de
desenvolvimento. Copie-o para `.env` e altere as senhas quando necessário.
O arquivo `.env` é ignorado pelo Git e não deve conter credenciais reais de
produção.

## Autenticação

O serviço de agendamento usa HTTP Basic com senhas armazenadas exclusivamente
como hash BCrypt. Os usuários podem ter os perfis `MEDICO`, `ENFERMEIRO` ou
`PACIENTE`.

O endpoint `GET /api/usuarios/me` permite validar a autenticação e requer
credenciais HTTP Basic. Credenciais ausentes ou inválidas retornam `401` sem
expor senha, hash ou detalhes internos. O health check permanece público.

## Massa de desenvolvimento

Com o perfil `dev` ativo, o Flyway executa as migrations de massa de
desenvolvimento nos três serviços. O `compose.yaml` já ativa esse perfil; nos
perfis `test` e `prod`, essas migrations não fazem parte dos locais de
migration do Flyway.

As senhas de demonstração são `fiap-dev-2026` e são gravadas somente como hash
BCrypt. Os identificadores fixos para testes manuais são:

| Dado | Identificador / usuário |
|---|---|
| Médico | `medico.demo` — `10000000-0000-0000-0000-000000000001` |
| Enfermeiro | `enfermeiro.demo` — `10000000-0000-0000-0000-000000000002` |
| Paciente Maria | `maria.demo` — `20000000-0000-0000-0000-000000000001` |
| Paciente João | `joao.demo` — `20000000-0000-0000-0000-000000000002` |
| Consulta futura da Maria | `30000000-0000-0000-0000-000000000001` |
| Consulta passada da Maria | `30000000-0000-0000-0000-000000000002` |
| Consulta futura do João | `30000000-0000-0000-0000-000000000003` |

O histórico recebe uma projeção das três consultas acima. O serviço de
notificação recebe três resultados demonstrativos: `ENVIADA`, `NAO_ENVIADA` e
`FALHA`, identificados respectivamente pelos `eventId`s terminados em `001`,
`002` e `003` na faixa `50000000-...`.

A migration Flyway é aplicada uma única vez por banco; as cláusulas
`ON CONFLICT` também protegem os IDs fixos caso os dados já existam. Por ser
uma carga SQL direta, ela não publica eventos Kafka. Para gerar eventos de
integração, crie ou altere consultas pela API após a inicialização.

## Autorização

As regras de autorização são aplicadas na camada de serviço por
`ConsultaAuthorizationService`, para que não dependam apenas de uma rota HTTP:

- `MEDICO` e `ENFERMEIRO` podem criar e alterar consultas, além de consultar
  históricos por GraphQL;
- `PACIENTE` não pode gerenciar consultas e, pela query GraphQL
  `minhasConsultas`, só consulta dados vinculados ao seu usuário autenticado;
- uma autorização insuficiente retorna `403` com uma resposta segura.

As regras também são verificadas na camada de serviço ou resolver, sem depender
somente da proteção da rota HTTP.

## Cadastro de pacientes

`POST /api/pacientes` cadastra pacientes e é restrito a `MEDICO` e
`ENFERMEIRO`.

```json
{
  "nome": "Maria Souza",
  "email": "maria@example.com",
  "telefone": "+55 11 99999-9999",
  "dataNascimento": "1990-05-20"
}
```

O endpoint retorna `201 Created`. Nome, e-mail, telefone e data de nascimento
são obrigatórios; o e-mail é validado e único sem diferenciar maiúsculas de
minúsculas. Pacientes sem autorização recebem `403 Forbidden`.

## Criação de consultas

`POST /api/consultas` cria uma consulta para um paciente existente e é
restrito a `MEDICO` e `ENFERMEIRO`.

```json
{
  "pacienteId": "UUID_DO_PACIENTE",
  "medico": "Dra. Ana Silva",
  "especialidade": "Cardiologia",
  "dataHora": "2026-09-10T14:00:00-03:00",
  "observacoes": "Consulta de acompanhamento"
}
```

`dataHora` deve ter offset de fuso horário e estar no futuro. A consulta é
criada com status `AGENDADA`; não é permitido repetir paciente e horário. Após
o commit, o serviço publica um evento no tópico Kafka `consulta.criada.v1`,
sem incluir observações clínicas.

Para alterar uma consulta, use `PUT /api/consultas/{id}` com os dados
atualizados e a versão atual retornada pela criação ou última alteração:

```json
{
  "medico": "Dra. Ana Silva",
  "especialidade": "Cardiologia",
  "dataHora": "2026-09-11T14:00:00-03:00",
  "observacoes": "Horário reagendado",
  "version": 0
}
```

Consultas `CANCELADA` ou `REALIZADA` não podem ser alteradas. A versão evita
sobrescrever alterações concorrentes e uma atualização bem-sucedida publica o
evento `CONSULTA_ATUALIZADA` no tópico `consulta.atualizada.v1`.

Para mudar o ciclo de vida, use `PATCH /api/consultas/{id}/status` com a versão
atual. São permitidas as transições `AGENDADA → CONFIRMADA/CANCELADA` e
`CONFIRMADA → REALIZADA/CANCELADA`.

O paciente também pode confirmar ou cancelar a própria consulta por
`PATCH /api/consultas/{id}/confirmar`. Nesse endpoint, os únicos status
aceitos são `CONFIRMADA` e `CANCELADA`; `REALIZADA` é uma ação de profissional
executada pelo endpoint `/status`. O paciente só pode operar consultas
vinculadas ao seu próprio usuário autenticado.

```json
{
  "status": "CONFIRMADA",
  "version": 1
}
```

Cada transição incrementa a versão, retorna a nova versão e publica
`CONSULTA_ATUALIZADA`. Consultas `REALIZADA` e `CANCELADA` são estados finais.

Quando uma validação falha, a API responde `400` com `code` igual a
`VALIDATION_ERROR` e o motivo de cada campo em `details`. Exemplo:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Dados de entrada inválidos",
  "details": {
    "dataHora": "dataHora deve estar no futuro",
    "version": "version é obrigatória"
  }
}
```

## Eventos Kafka

Os eventos `CONSULTA_CRIADA` e `CONSULTA_ATUALIZADA` têm `eventId`, tipo,
versão do contrato, data de ocorrência e `consultaId`. A chave Kafka é o
`consultaId`, preservando a ordem das alterações de uma mesma consulta. O
produtor usa `acks=all`, idempotência e callback de confirmação de entrega.

Nesta versão, a publicação ocorre após o commit da transação da consulta, mas
ainda não utiliza Transactional Outbox. Portanto, uma indisponibilidade do
Kafka depois do commit pode exigir intervenção manual; a persistência de
eventos pendentes e a retentativa serão introduzidas como evolução de
resiliência.

## Projeção de histórico

O serviço de histórico consome `consulta.criada.v1` e
`consulta.atualizada.v1` com o consumer group `historico-consumer-v1`. Ele
materializa os dados no schema PostgreSQL próprio, `historico`, sem consultar
o banco do serviço de agendamento.

O `eventId` evita o reprocessamento de mensagens duplicadas e o horário de
ocorrência impede que uma mensagem antiga substitua uma projeção mais recente.
O offset Kafka só é confirmado depois que a transação de persistência termina
com sucesso. Como consequência desse fluxo assíncrono, o histórico tem
**consistência eventual**: após criar ou alterar uma consulta, sua leitura no
histórico pode levar alguns instantes para refletir o novo estado.

## Consultas GraphQL do histórico

O serviço de histórico expõe `POST http://localhost:8081/graphql`. As queries
`historicoDoPaciente` e `consultasFuturas` exigem HTTP Basic de `MEDICO` ou
`ENFERMEIRO` e aceitam `pacienteId`, `pagina` e `tamanho`; o tamanho máximo é
50. Erros de execução GraphQL retornam um código estável em
`errors[].extensions.code`: `FORBIDDEN`, `NOT_FOUND` ou `VALIDATION_ERROR`.
A query `minhasConsultas`, exclusiva do perfil `PACIENTE`, determina o paciente
vinculado ao usuário autenticado e não aceita `pacienteId`. Ela aceita o filtro
`somenteFuturas`; sua resposta não expõe o identificador do paciente.

## Testes manuais com Postman

Importe [`postman/Tech-Challenge-Fase-3.postman_collection.json`](postman/Tech-Challenge-Fase-3.postman_collection.json)
no Postman. A collection reúne health checks, o fluxo REST de agendamento e as
queries GraphQL. Execute a pasta **Agendamento - Fluxo principal** na ordem
indicada.

As requisições de mudança de status são fixas e atualizam automaticamente a
variável `consultaVersao` com a versão devolvida pela API:

- **4 - Confirmar consulta** — muda para `CONFIRMADA`;
- **5A - Realizar consulta (após confirmar)** — muda para `REALIZADA`;
- **5B - Cancelar consulta (após confirmar; alternativa ao 5A)** — muda para
  `CANCELADA`.

Execute somente **5A** ou **5B** para uma mesma consulta, pois os estados
`REALIZADA` e `CANCELADA` são finais.

### Teste gRPC — consulta de paciente

O contrato canônico é
[`contratos-grpc/src/main/proto/pacientes.proto`](contratos-grpc/src/main/proto/pacientes.proto).
Não há cópia dele na pasta `postman/`, evitando divergência entre o contrato
executado pela aplicação e o usado no teste.

Com o ambiente Docker em execução:

1. Selecione **New > gRPC** no Postman.
2. Informe o servidor `127.0.0.1:6565`.
3. Importe `contratos-grpc/src/main/proto/pacientes.proto` na tela da
   requisição.
4. Selecione `BuscaPacienteById > ObterDadosDoPaciente`.
5. Informe um dos payloads a seguir no editor **Message** e clique em
   **Invoke**.
6. Use **Save As** para manter cada cenário em uma collection multiprotocolo.

> A collection JSON contém somente HTTP e GraphQL. Requests gRPC nativos devem
> ser salvos pelo Postman em uma collection multiprotocolo.

Sucesso:

```json
{
  "paciente_id": "20000000-0000-0000-0000-000000000001"
}
```

Resultado esperado: `pacienteId`, `pacienteNome`, `pacienteEmail` e
`pacienteTelefone` de Maria Souza.

Identificador inválido:

```json
{
  "paciente_id": "nao-e-uuid"
}
```

Resultado esperado: status `INVALID_ARGUMENT`.

Paciente inexistente:

```json
{
  "paciente_id": "00000000-0000-0000-0000-000000000099"
}
```

Resultado esperado: status `NOT_FOUND`.

O Compose publica a porta somente em `127.0.0.1:6565` no ambiente local; entre
os containers, a comunicação continua em `servico-agendamento:6565`.

## Observabilidade

O ambiente local inclui Prometheus e Grafana. Após `docker compose up --build`,
acesse:

| Recurso | Endereço | Finalidade |
|---|---|---|
| Prometheus | http://127.0.0.1:9090 | Consultar alvos e séries de métricas |
| Grafana | http://127.0.0.1:3000 | Criar e visualizar dashboards |
| Loki | http://127.0.0.1:3100 | API local de consulta de logs |
| Grafana Alloy | http://127.0.0.1:12345 | Diagnóstico do coletor de logs |
| Kafka Exporter | http://127.0.0.1:9308/metrics | Métricas de tópicos, grupos consumidores e lag |

O login inicial do Grafana é `admin` / `admin`, configurável por
`GRAFANA_ADMIN_USER` e `GRAFANA_ADMIN_PASSWORD`. O datasource Prometheus é
provisionado automaticamente, assim como o dashboard **Hospital - Visão dos
Serviços**. Os três serviços expõem métricas em suas redes
internas nos endpoints `/actuator/prometheus`; o Prometheus as coleta a cada
15 segundos. Esses endpoints não são publicados no host.

Os logs dos containers são coletados pelo Grafana Alloy e enviados ao Loki. No
Grafana, abra o dashboard **Hospital - Visão dos Serviços** para o painel
consolidado ou os dashboards **Hospital - Agendamento - Logs**, **Hospital -
Histórico - Logs** e **Hospital - Notificação - Logs** para consultar cada
serviço separadamente. Também é possível usar **Explore > Loki** com consultas
como:

```logql
{service_name=~".*servico-notificacao.*"}
```

```logql
{service_name=~".*servico-.*"} |= "eventId="
```

O Alloy recebe o socket Docker somente para leitura. Em produção, esse acesso
deve ser isolado em uma rede operacional e concedido ao menor número possível
de agentes.

O dashboard **Hospital - Mensageria Kafka** apresenta disponibilidade do
exporter, lag e membros dos grupos consumidores, offsets dos tópicos de consulta
e os logs Kafka correlatos no Loki.

No Compose de desenvolvimento, `GET /actuator/prometheus` não exige HTTP Basic
para que o Prometheus interno realize o scrape. Em produção, mantenha a rota
em rede operacional privada ou proteja-a com autenticação compatível com o
coletor; não exponha endpoints de gerenciamento na internet.

Consultas úteis no Prometheus:

```promql
up
sum by (job) (rate(http_server_requests_seconds_count[5m]))
sum by (job) (jvm_memory_used_bytes)
```

## Consulta de paciente por gRPC

O serviço de notificações consome eventos de consulta e, durante o
processamento, chama o serviço interno
`BuscaPacienteById.ObterDadosDoPaciente` do agendamento. O contrato está em
[`contratos-grpc/src/main/proto/pacientes.proto`](contratos-grpc/src/main/proto/pacientes.proto)
e recebe `pacienteId`, retornando apenas identificador, nome, e-mail e telefone
do paciente. A porta gRPC `6565` é destinada exclusivamente à comunicação
entre serviços na rede Docker; ela não substitui APIs REST ou GraphQL e não é
exposta ao cliente final.

## Estrutura

O roteiro visual para navegar e demonstrar os fluxos disponíveis está em
[docs/ROTEIRO_NAVEGACAO.md](docs/ROTEIRO_NAVEGACAO.md).

```text
.
├── compose.yaml
├── contratos-grpc/                # contrato Protobuf e stubs gRPC gerados
├── servico-agendamento/
├── servico-historico/
└── servico-notificacao/
```

Cada serviço possui seu próprio Dockerfile e expõe somente endpoints
operacionais nesta etapa. O PostgreSQL usa schemas separados para manter os
dados de cada serviço organizados no ambiente local.

## Build multi-módulo

O projeto possui um único Gradle Wrapper na raiz. Os módulos
`servico-historico` e `servico-notificacao` dependem somente do módulo
`contratos-grpc`, que gera os stubs Java a partir do contrato Protobuf. Não há
entidades JPA ou regras de domínio compartilhadas entre os serviços.

Execute todos os testes a partir da raiz:

```bash
./gradlew test
```

Para gerar manualmente os stubs gRPC:

```bash
./gradlew :contratos-grpc:generateProto
```

Durante o desenvolvimento, cada serviço pode ser iniciado isoladamente:

```bash
./gradlew :servico-agendamento:bootRun
./gradlew :servico-historico:bootRun
./gradlew :servico-notificacao:bootRun
```
