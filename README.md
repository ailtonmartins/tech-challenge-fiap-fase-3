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
`PACIENTE`. Nesta etapa, usuários devem ser cadastrados diretamente no banco;
a massa de demonstração será adicionada em história posterior.

O endpoint `GET /api/usuarios/me` permite validar a autenticação e requer
credenciais HTTP Basic. Credenciais ausentes ou inválidas retornam `401` sem
expor senha, hash ou detalhes internos. O health check permanece público.

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
