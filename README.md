# Tech Challenge FIAP — Fase 3

Backend hospitalar modular para agendamento de consultas, consulta de histórico
e envio de lembretes. A solução usa Java 21, Spring Boot, PostgreSQL e Kafka.

## Estado atual

A infraestrutura local da **US-01** está disponível: três aplicações Spring
Boot com Actuator, um PostgreSQL com schemas isolados por serviço, Kafka em
modo KRaft e Kafka UI. As regras de negócio, segurança, GraphQL, eventos e
gRPC serão implementados nas próximas histórias.

## Arquitetura inicial

```text
Cliente
  ├─ servico-agendamento  :8080 ── PostgreSQL :5432 / schema agendamento
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

Com o perfil `dev` ativo, o Flyway executa a migration de massa de
desenvolvimento. O `compose.yaml` já ativa esse perfil; nos perfis `test` e
`prod`, essa migration não faz parte dos locais de migration do Flyway.

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

A migration Flyway é aplicada uma única vez por banco; as cláusulas
`ON CONFLICT` também protegem os IDs fixos caso os dados já existam. Por ser
uma carga SQL direta, ela não publica eventos Kafka. Para gerar eventos de
integração, crie ou altere consultas pela API após a inicialização.

## Autorização

As regras de autorização são aplicadas na camada de serviço por
`ConsultaAuthorizationService`, para que não dependam apenas de uma rota HTTP:

- `MEDICO` e `ENFERMEIRO` podem criar e alterar consultas, além de consultar
  históricos;
- `PACIENTE` não pode gerenciar consultas e só pode consultar dados vinculados
  ao seu próprio `pacienteId`;
- uma autorização insuficiente retorna `403` com uma resposta segura.

As operações REST de consulta e as queries GraphQL que invocarão essas regras
serão entregues nas histórias de agendamento e histórico.

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

## Estrutura

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
