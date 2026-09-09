# Tech Challenge FIAP — Fase 3

## Guia passo a passo para elaboração do trabalho

Este documento apresenta um roteiro completo para construir o Tech Challenge da Fase 3: um backend hospitalar seguro, modular e orientado a eventos, capaz de agendar consultas, consultar o histórico de pacientes e enviar notificações assíncronas.

> **Estado da implementação:** todas as histórias de usuário foram
> implementadas. O fluxo de notificação consulta os dados atuais do paciente
> no serviço de agendamento por gRPC depois de consumir o evento Kafka.

O projeto será desenvolvido com Java e Spring Boot e aplicará os conteúdos estudados no curso:

- APIs e interfaces de comunicação;
- autenticação, autorização, criptografia, Spring Security e hardening;
- GraphQL;
- arquitetura de sistemas distribuídos;
- padrões de resiliência, monitoramento e escalabilidade;
- comunicação assíncrona com Kafka.

---

## 1. Entender o problema e delimitar o escopo

O sistema deve atender três perfis de usuário:

| Perfil | Permissões |
|---|---|
| Médico | Consultar o histórico, criar consultas e editar consultas existentes |
| Enfermeiro | Consultar o histórico, registrar consultas e editar consultas existentes |
| Paciente | Visualizar somente as próprias consultas |

O escopo mínimo da solução será:

1. autenticar usuários com Spring Security;
2. autorizar operações conforme o perfil;
3. criar e editar consultas;
4. consultar o histórico completo ou apenas consultas futuras com GraphQL;
5. publicar um evento quando uma consulta for criada ou alterada;
6. consumir o evento em outro serviço e gerar um lembrete;
7. executar toda a infraestrutura localmente com Docker Compose;
8. fornecer testes, documentação e collection para validação.

### Decisões de escopo

- Será usada autenticação HTTP Basic, pois é o requisito explícito do enunciado. Em uma evolução para produção, recomenda-se OAuth 2.0/OpenID Connect e JWT.
- O histórico será um serviço independente, responsável por materializar as alterações de consultas e disponibilizá-las por GraphQL.
- Será usado Apache Kafka, com tópicos versionados, grupos de consumidores, retentativas e tópicos de dead letter (DLT).
- O envio de lembrete poderá ser inicialmente simulado por log e persistido no banco. Como evolução, poderá ser integrado ao MailHog ou a um provedor de e-mail.
- gRPC é usado somente para comunicação síncrona interna: após receber um evento Kafka, o serviço de notificações consulta o serviço de agendamento para obter os dados atuais do paciente. A API GraphQL continua destinada aos clientes, e Kafka continua sendo a integração assíncrona.

### Critérios de pronto

- Um médico ou enfermeiro consegue criar e editar uma consulta.
- A criação ou edição publica um evento no Kafka.
- Os serviços de histórico e notificações consomem o evento de forma idempotente.
- Um paciente não consegue consultar dados de outro paciente.
- Usuários sem o perfil correto recebem `403 Forbidden`.
- Credenciais inválidas recebem `401 Unauthorized`.
- Consultas GraphQL retornam histórico completo e consultas futuras.
- Os serviços possuem health check e logs úteis.
- Os fluxos principais são cobertos por testes automatizados.

---

## 2. Definir a arquitetura

Serão criados três microsserviços. No ambiente local, eles usam uma única
instância PostgreSQL com schemas isolados (`agendamento`, `historico` e
`notificacao`); cada serviço acessa somente o seu schema. Em produção, esses
schemas podem ser separados em instâncias físicas conforme a necessidade.

```text
Cliente/Postman -- HTTP Basic --> servico-agendamento :8080
                                  |      |
                                  |      +-- gRPC interno :6565 (dados do paciente)
                                  |                              ^
                                  |                              |
                                  v                              |
Kafka <--- consulta.criada.v1 / consulta.atualizada.v1 --- servico-notificacao :8082
  |                                                          |
  +------------------------------> servico-historico :8081   +-- cliente gRPC
                                     (GraphQL)
```

### Responsabilidades

#### Serviço de agendamento

- autenticar usuários;
- aplicar as regras de autorização;
- cadastrar pacientes e consultas;
- alterar consultas;
- publicar eventos no Kafka após alterações confirmadas no banco;
- não disponibilizar o histórico diretamente: ele pertence ao serviço de histórico.

#### Serviço de histórico

- consumir eventos de consultas criadas e atualizadas;
- manter uma projeção própria do histórico de consultas;
- disponibilizar consultas e consultas futuras por GraphQL;
- aplicar autorização e garantir que pacientes leiam apenas a própria projeção.

#### Serviço de notificações

- consumir eventos de consulta criada ou atualizada pelo Kafka;
- consultar o serviço de agendamento por gRPC para obter os dados atuais do paciente;
- validar e processar a mensagem;
- impedir notificações duplicadas;
- registrar o lembrete enviado;
- direcionar mensagens inválidas para uma dead-letter queue.

### Portas sugeridas

| Componente | Porta |
|---|---:|
| Serviço de agendamento | 8080 |
| Serviço de histórico (GraphQL) | 8081 |
| Serviço de notificações | 8082 |
| PostgreSQL (schemas `agendamento`, `historico` e `notificacao`) | 5432 |
| Kafka | 9092 |
| Kafka UI (opcional) | 8085 |
| gRPC interno do serviço de agendamento | 6565 |

---

## 3. Escolher a stack

### Aplicações

- Java 21 LTS;
- Spring Boot 3.x;
- Gradle multi-module com Gradle Wrapper;
- Spring Security;
- Spring Data JPA;
- Spring for Apache Kafka;
- gRPC com Protobuf e starter Spring Boot compatível;
- Bean Validation;
- Spring Boot Actuator;
- PostgreSQL;
- Flyway;
- Testcontainers;
- JUnit 5, Mockito e Spring Security Test.

### Infraestrutura e entrega

- Docker e Docker Compose;
- Apache Kafka;
- Postman ou Bruno;
- Git e GitHub/GitLab;
- Mermaid para diagramas no README.

O repositório usará Gradle multi-module: um único Wrapper e arquivos de configuração na raiz, com módulos independentes para cada serviço. O `build.gradle` raiz centraliza versões e convenções; cada submódulo declara apenas as dependências que utiliza. Registrar no README a versão estável do Spring Boot escolhida e manter a mesma família de versões em todos os módulos.

---

## 4. Planejar o repositório

Estrutura sugerida:

```text
tech-challenge-fase-3/
├── settings.gradle                  # declara os submódulos
├── build.gradle                     # versões e convenções compartilhadas
├── gradlew
├── gradlew.bat
├── gradle/wrapper/
├── servico-agendamento/
│   ├── src/main/java/...
│   ├── src/main/resources/
│   ├── src/test/java/...
│   ├── Dockerfile
│   └── build.gradle
├── servico-historico/
│   ├── src/main/java/...
│   ├── src/main/resources/
│   ├── src/test/java/...
│   ├── Dockerfile
│   └── build.gradle
├── servico-notificacao/
│   ├── src/main/java/...
│   ├── src/main/resources/
│   ├── src/test/java/...
│   ├── Dockerfile
│   └── build.gradle
├── contratos-grpc/
│   ├── src/main/proto/
│   │   └── pacientes.proto
│   └── build.gradle
├── postman/
│   ├── Tech-Challenge-Fase-3.postman_collection.json
│   └── Local.postman_environment.json
├── docs/
│   ├── arquitetura.md
│   ├── seguranca.md
│   └── evidencias/
├── compose.yaml
├── .env.example
├── .gitignore
└── README.md
```

O `settings.gradle` deve incluir: `servico-agendamento`, `servico-historico`, `servico-notificacao` e `contratos-grpc`. Os serviços continuam sendo aplicações separadas, com banco, porta, Dockerfile e deploy próprios; o multi-module simplifica apenas o build e o compartilhamento do contrato Protobuf.

Dependências entre módulos:

```text
servico-agendamento   -> independente dos demais módulos
servico-historico     -> implementation(project(":contratos-grpc"))
servico-notificacao   -> implementation(project(":contratos-grpc"))
```

Não criar um módulo compartilhado de entidades JPA ou regras de domínio. Somente o contrato gRPC será compartilhado, para manter os serviços desacoplados.

### Modelo de camadas simples

```text
controller  -> recebe requisições REST, GraphQL ou gRPC e devolve respostas
service     -> concentra regras de negócio e coordena as operações
repository  -> acessa o banco de dados com JPA
model       -> entidades e enums persistidos
dto         -> objetos de entrada e saída da API/eventos
config      -> segurança, Kafka, GraphQL, gRPC e configurações gerais
```

O controller deve ser fino: validar o formato do dado, chamar um serviço e retornar a resposta. Regras de negócio, autorização complementar e publicação/consumo de eventos ficam em `service`.

### Estrutura Java do serviço de agendamento

```text
servico-agendamento/
└── src/main/java/br/com/fiap/techchallenge/agendamento/
    ├── AgendamentoApplication.java
    ├── controller/
    │   └── ConsultaController.java
    ├── service/
    │   ├── ConsultaService.java
    │   ├── PacienteService.java
    │   └── KafkaProducerService.java
    ├── repository/
    │   ├── ConsultaRepository.java
    │   ├── PacienteRepository.java
    │   └── UsuarioRepository.java
    ├── model/
    │   ├── Consulta.java
    │   ├── Paciente.java
    │   ├── Usuario.java
    │   └── StatusConsulta.java
    ├── dto/
    │   ├── CriarConsultaRequest.java
    │   ├── AtualizarConsultaRequest.java
    │   ├── ConsultaResponse.java
    │   └── ConsultaEvento.java
    ├── config/
    │   ├── SecurityConfig.java
    │   └── KafkaConfig.java
    └── exception/
        └── GlobalExceptionHandler.java
```

### Estrutura Java do serviço de histórico

```text
servico-historico/
├── src/main/java/br/com/fiap/techchallenge/historico/
│   ├── HistoricoApplication.java
│   ├── controller/
│   │   └── HistoricoGraphqlController.java
│   ├── service/
│   │   ├── HistoricoService.java
│   │   └── KafkaConsumerService.java
│   ├── repository/
│   │   └── ConsultaHistoricoRepository.java
│   ├── model/
│   │   ├── ConsultaHistorico.java
│   │   └── StatusConsulta.java
│   ├── dto/
│   │   └── ConsultaHistoricoResponse.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   └── KafkaConfig.java
│   └── exception/
│       └── GraphqlExceptionHandler.java
```

### Estrutura Java do serviço de notificações

```text
servico-notificacao/
└── src/main/java/br/com/fiap/techchallenge/notificacao/
    ├── NotificacaoApplication.java
    ├── controller/
    │   └── NotificacaoAdminController.java
    ├── service/
    │   ├── NotificacaoService.java
    │   ├── KafkaConsumerService.java
    │   └── PacienteGrpcClient.java
    ├── repository/
    │   └── NotificacaoRepository.java
    ├── model/
    │   ├── Notificacao.java
    │   └── StatusNotificacao.java
    ├── dto/
    │   ├── ConsultaEvento.java
    │   └── NotificacaoResponse.java
    ├── config/
    │   ├── KafkaConfig.java
    │   └── GrpcClientConfig.java
    └── exception/
        └── GlobalExceptionHandler.java
```

Em `src/test/java`, repetir a mesma organização de pacotes do código principal. Essa estrutura é suficiente para o desafio, fácil de explicar na apresentação e evita complexidade arquitetural desnecessária.

---

## 5. Preparar a infraestrutura local

Criar um `compose.yaml` contendo:

- uma instância PostgreSQL com schemas isolados para agendamento, histórico e notificações;
- Apache Kafka (em modo KRaft, sem ZooKeeper);
- Kafka UI opcional para inspecionar tópicos e consumidores;
- health checks para todos os componentes;
- volumes nomeados para persistência;
- rede interna compartilhada pelos serviços.

Variáveis esperadas no `.env.example`:

```dotenv
POSTGRES_DB=hospital
POSTGRES_USER=hospital_app
POSTGRES_PASSWORD=trocar_em_ambiente_real
KAFKA_BOOTSTRAP_SERVERS=kafka:29092
SPRING_PROFILES_ACTIVE=dev
```

Não versionar o arquivo `.env` real nem credenciais de produção. O `.env.example` deve conter somente valores de desenvolvimento claramente identificados.

Validar a infraestrutura antes de programar:

```bash
docker compose up -d
docker compose ps
```

Caso seja incluído, o Kafka UI deverá estar acessível em `http://localhost:8085`.

---

## 6. Criar o serviço de agendamento

Gerar um projeto Gradle com as dependências:

- Spring Web;
- Spring Security;
- Spring Data JPA;
- PostgreSQL Driver;
- Flyway Migration;
- Validation;
- Spring for Apache Kafka;
- Actuator;
- Testcontainers nos testes.

### Configurações essenciais

No `application.yml`, configurar:

- conexão com PostgreSQL;
- Flyway habilitado;
- `ddl-auto: validate`, evitando alterações automáticas do schema;
- Kafka;
- Actuator expondo apenas `health`, `info`, `metrics` e, se utilizado, `prometheus`;
- logs sem dados médicos, senhas ou cabeçalhos de autorização.

Criar perfis:

- `application.yml`: configuração comum;
- `application-dev.yml`: ambiente local;
- `application-test.yml`: testes;
- `application-prod.yml`: valores recebidos por variáveis de ambiente.

---

## 7. Modelar os dados do agendamento

### Entidade `Usuario`

Campos mínimos:

- `id: UUID`;
- `username: String`, único;
- `passwordHash: String`;
- `role: MEDICO | ENFERMEIRO | PACIENTE`;
- `enabled: boolean`;
- `pacienteId: UUID`, preenchido somente para usuários pacientes;
- datas de criação e alteração.

### Entidade `Paciente`

Campos mínimos:

- `id: UUID`;
- `nome: String`;
- `email: String`;
- `telefone: String`;
- data de nascimento;
- datas de criação e alteração.

### Entidade `Consulta`

Campos mínimos:

- `id: UUID`;
- `pacienteId: UUID`;
- `medico: String` ou referência para o usuário médico;
- `especialidade: String`;
- `dataHora: OffsetDateTime`;
- `status: AGENDADA | CONFIRMADA | REALIZADA | CANCELADA`;
- `observacoes: String`;
- `version: Long` com `@Version` para controle de concorrência;
- datas de criação e alteração.

### Regras de negócio

- a data e a hora devem ser informadas com fuso horário;
- não criar consulta para paciente inexistente;
- não permitir edição de consulta realizada ou cancelada, salvo regra explicitamente documentada;
- impedir agendamentos duplicados para o mesmo paciente e horário;
- aplicar limite de tamanho nas observações;
- paciente só acessa registros associados ao seu `pacienteId`;
- toda alteração relevante gera evento.

Criar as tabelas com migrations Flyway versionadas, por exemplo:

```text
V1__criar_tabela_usuario.sql
V1__criar_tabela_paciente.sql
V2__criar_tabela_consulta.sql
```

### Seed para desenvolvimento e cenários de teste manual

No ambiente local, a massa é uma migration Flyway separada, carregada apenas
no perfil `dev`. Isso permite que mais de uma réplica inicie sem criar dados
duplicados: o Flyway usa sua tabela de histórico e bloqueio de migration; os
`INSERT`s também usam `ON CONFLICT` como proteção adicional.

Estrutura implementada:

```text
servico-agendamento/
└── src/main/resources/
    ├── application.yml
    ├── application-dev.yml
    ├── db/migration/             # schema, carregado em todos os perfis
    └── db/dev-migration/
        └── V4__criar_massa_de_desenvolvimento.sql
```

Configuração implementada em `application-dev.yml`:

```yaml
spring:
  flyway:
    locations: classpath:db/migration,classpath:db/dev-migration
```

Nos perfis padrão, `test` e `prod`, o Flyway lê somente
`classpath:db/migration`. Testes automatizados devem criar somente os dados
necessários dentro de cada cenário, sem depender da massa de desenvolvimento.

Massa de desenvolvimento implementada:

| Tipo | Identificador fixo | Usuário | Perfil | Finalidade |
|---|---|---|---|---|
| Profissional | `10000000-0000-0000-0000-000000000001` | `medico.demo` | `MEDICO` | Criar/editar consultas |
| Profissional | `10000000-0000-0000-0000-000000000002` | `enfermeiro.demo` | `ENFERMEIRO` | Criar/editar consultas |
| Paciente | `20000000-0000-0000-0000-000000000001` | `maria.demo` | `PACIENTE` | Cenários da Maria |
| Paciente | `20000000-0000-0000-0000-000000000002` | `joao.demo` | `PACIENTE` | Cenários do João |
| Consulta futura | `30000000-0000-0000-0000-000000000001` | — | — | Consulta da Maria |
| Consulta passada | `30000000-0000-0000-0000-000000000002` | — | — | Histórico da Maria |
| Consulta futura | `30000000-0000-0000-0000-000000000003` | — | — | Consulta do João |

Para fins acadêmicos, o README documenta a senha de demonstração
`fiap-dev-2026`; a migration persiste somente o hash BCrypt. Nunca usar essas
credenciais fora do ambiente local.

Como é uma carga SQL direta, essa migration não publica eventos Kafka. Para
testar a cadeia Kafka, crie ou altere uma consulta pela API após a
inicialização do ambiente.

---

## 8. Implementar a segurança

### Autenticação

Configurar Spring Security com:

- HTTP Basic;
- `PasswordEncoder` usando BCrypt;
- usuários carregados do banco;
- aplicação stateless;
- respostas padronizadas para `401` e `403`;
- CSRF desabilitado somente porque a API será stateless e autenticada pelo cabeçalho `Authorization`;
- method security com `@EnableMethodSecurity`.

### Autorização

No serviço de agendamento, aplicar autorização nos métodos de serviço REST. No serviço de histórico, como todas as operações GraphQL normalmente usam `POST /graphql`, proteger apenas a URL não é suficiente: aplicar também autorização no resolver ou serviço.

```java
@PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
public Consulta criarConsulta(CriarConsultaRequest input) { ... }
```

```java
@PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
public List<ConsultaHistorico> listarConsultas(...) { ... }
```

Além do perfil, validar a propriedade do dado:

```text
Se o usuário for PACIENTE:
    paciente solicitado deve ser o paciente ligado ao usuário autenticado
Senão:
    permitir conforme o perfil
```

Não confiar em um `pacienteId` enviado pelo paciente. O identificador deve ser obtido do usuário autenticado ou comparado obrigatoriamente com ele.

### Hardening

- não expor stack traces ao cliente;
- padronizar erros sem revelar detalhes internos;
- validar todos os inputs;
- não registrar senha, token, dados sensíveis ou histórico clínico completo;
- limitar tamanho de query e profundidade do GraphQL;
- limitar paginação para impedir consultas enormes;
- desabilitar GraphiQL e introspection em produção, caso isso seja compatível com a estratégia operacional;
- usar usuário de banco com privilégios mínimos;
- executar containers com usuário não root;
- verificar dependências com OWASP Dependency-Check ou ferramenta equivalente;
- usar HTTPS no ambiente publicado.

---

## 9. Criar o serviço de histórico e a API GraphQL

Gerar o `servico-historico` com Spring Web, Spring for GraphQL, Spring Security, Spring Data JPA, PostgreSQL Driver, Flyway, Spring for Apache Kafka, Validation, Actuator e Testcontainers.

O serviço de histórico mantém uma **projeção de leitura**: não é consultado pelo serviço de agendamento e não altera consultas. Ele consome os mesmos eventos publicados pelo agendamento e persiste os dados necessários para consultas rápidas. Essa separação aplica CQRS de forma simples: o agendamento é o lado de escrita e o histórico é o lado de leitura.

O GraphQL deve existir somente neste serviço, em `src/main/resources/graphql/schema.graphqls`, e ficar disponível em `http://localhost:8081/graphql`.

### Projeção de histórico e consumo de eventos

Criar a entidade de leitura `ConsultaHistorico`, com `consultaId` único, dados do paciente, profissional, especialidade, data/hora, status e o instante do último evento aplicado. O consumidor do grupo `historico-consumer-v1` deve:

1. receber um evento Kafka;
2. verificar se o `eventId` já foi processado;
3. localizar a projeção pelo `consultaId`;
4. criar ou atualizar a projeção somente se `occurredAt` for mais recente;
5. persistir a projeção e o `eventId` processado na mesma transação;
6. confirmar o offset somente após o listener retornar com a persistência concluída.

Esse fluxo torna o histórico eventual-consistente: depois de criar ou alterar uma consulta, a projeção GraphQL pode levar alguns instantes para refletir a mudança. Essa característica deve ser descrita no README e demonstrada na apresentação.

O serviço de histórico precisa conhecer os usuários e os vínculos `usuario -> paciente` para aplicar autorização. Para manter a autonomia entre bancos, incluir esses dados necessários no evento de consulta ou publicar também eventos de usuário/paciente. Não realizar consulta síncrona ao banco do serviço de agendamento.

### Contrato gRPC interno

O contrato Protobuf está versionado em `contratos-grpc/src/main/proto/pacientes.proto`. Os módulos `servico-agendamento` e `servico-notificacao` dependem de `contratos-grpc` e usam os stubs gerados durante o build Gradle. O endpoint gRPC do agendamento usa a porta interna `6565`; ele não deve ser exposto ao cliente final nem substituir REST ou GraphQL.

```proto
syntax = "proto3";

package fiap.techchallenge.historico.v1;

service BuscaPacienteById {
  rpc ObterDadosDoPaciente(ObterDadosDoPacienteRequest)
      returns (DadosDoPacienteResponse);
}

message ObterDadosDoPacienteRequest {
  string paciente_id = 1;
}

message DadosDoPacienteResponse {
  string paciente_id = 1;
  string paciente_nome = 2;
  string paciente_email = 3;
  string paciente_telefone = 4;
}
```

O servidor retorna somente os dados do paciente necessários à notificação. Validar `paciente_id` e manter a comunicação na rede interna entre serviços; não reutilizar a autenticação HTTP Basic do usuário final nesse canal.

### Tipos sugeridos

```graphql
scalar DateTime

type Paciente {
  id: ID!
  nome: String!
  email: String!
}

type Consulta {
  id: ID!
  paciente: Paciente!
  medico: String!
  especialidade: String!
  dataHora: DateTime!
  status: StatusConsulta!
  observacoes: String
}

enum StatusConsulta {
  AGENDADA
  CONFIRMADA
  REALIZADA
  CANCELADA
}

type Query {
  consulta(id: ID!): Consulta
  historicoDoPaciente(pacienteId: ID!, pagina: Int = 0, tamanho: Int = 20): [Consulta!]!
  consultasFuturas(pacienteId: ID!, pagina: Int = 0, tamanho: Int = 20): [Consulta!]!
  minhasConsultas(futurasSomente: Boolean = false, pagina: Int = 0, tamanho: Int = 20): [Consulta!]!
}
```

### Exemplos para validação

Criar consulta como médico ou enfermeiro no **serviço de agendamento**:

```http
POST /api/consultas
Content-Type: application/json

{
  "pacienteId": "UUID_DO_PACIENTE",
  "medico": "Dra. Ana Silva",
  "especialidade": "Cardiologia",
  "dataHora": "2026-09-10T14:00:00-03:00",
  "observacoes": "Consulta de acompanhamento"
}
```

Consultar somente as próprias consultas como paciente:

```graphql
query MinhasConsultas {
  minhasConsultas(futurasSomente: true) {
    id
    medico
    especialidade
    dataHora
    status
  }
}
```

Consultar histórico como profissional:

```graphql
query Historico($pacienteId: ID!) {
  historicoDoPaciente(pacienteId: $pacienteId) {
    id
    dataHora
    medico
    especialidade
    status
  }
}
```

---

## 10. Implementar a regra de aplicação

Para cada operação, seguir o fluxo:

1. receber e validar o input REST no agendamento ou GraphQL no histórico;
2. obter o usuário autenticado;
3. verificar o perfil e a propriedade do recurso;
4. executar regras do domínio;
5. salvar a alteração em transação;
6. preparar o evento de integração;
7. publicar o evento somente após a persistência bem-sucedida;
8. retornar um DTO REST ou GraphQL, sem expor diretamente a entidade JPA.

Criar exceções específicas, por exemplo:

- `PacienteNaoEncontradoException`;
- `ConsultaNaoEncontradaException`;
- `ConsultaNaoPodeSerAlteradaException`;
- `AcessoNegadoAoPacienteException`;
- `ConflitoDeAgendamentoException`.

Mapear essas exceções para erros GraphQL com códigos estáveis, como `NOT_FOUND`, `FORBIDDEN`, `VALIDATION_ERROR` e `CONFLICT`.

### Endpoints REST do serviço de agendamento

| Método | Rota | Perfis | Finalidade |
|---|---|---|---|
| `POST` | `/api/consultas` | MÉDICO, ENFERMEIRO | Criar consulta e evento Kafka |
| `PUT` | `/api/consultas/{id}` | MÉDICO, ENFERMEIRO | Alterar consulta e evento Kafka |
| `GET` | `/actuator/health` | Público | Verificar saúde do serviço |

As consultas de leitura permanecem no serviço de histórico via GraphQL; o agendamento não deve expor endpoints de leitura do histórico.

---

## 11. Definir a mensageria Kafka

### Topologia

Usar os seguintes nomes, centralizados em constantes ou configuração. Criar os tópicos no Compose, por `KafkaAdmin`, ou por script de inicialização:

```text
Tópicos de domínio:
- consulta.criada.v1
- consulta.atualizada.v1

Consumer groups:
- historico-consumer-v1
- notificacao-consumer-v1

Tópicos de dead letter:
- consulta.criada.v1.DLT
- consulta.atualizada.v1.DLT
```

Os dois serviços devem consumir os tópicos em grupos diferentes. Assim, cada serviço recebe todos os eventos, mas múltiplas instâncias do mesmo serviço dividem as partições entre si. Usar a chave `consultaId` na publicação para preservar a ordenação dos eventos de uma mesma consulta.

### Contrato do evento

```json
{
  "eventId": "d73983ea-8a43-4ed4-9b31-340079540896",
  "eventType": "CONSULTA_CRIADA",
  "eventVersion": 1,
  "occurredAt": "2026-08-15T10:30:00-03:00",
  "consultaId": "4e295274-4160-49e7-9755-cb241de04568",
  "pacienteId": "8ee742fc-e678-45fb-871d-250a31274f72",
  "pacienteNome": "Maria Souza",
  "pacienteEmail": "maria@example.com",
  "medico": "Dra. Ana Silva",
  "especialidade": "Cardiologia",
  "dataHora": "2026-09-10T14:00:00-03:00",
  "status": "AGENDADA"
}
```

Regras importantes:

- todo evento possui `eventId` único;
- o contrato possui versão;
- datas usam ISO 8601 com offset;
- não enviar observações clínicas desnecessárias na mensagem;
- usar `acks=all`, idempotência do produtor e confirmação de entrega pelo Kafka;
- configurar `enable-auto-commit=false` e confirmar o offset somente após o processamento bem-sucedido;
- configurar serialização JSON explícita e headers com versão/correlation ID;
- definir retenção dos tópicos de acordo com o ambiente e a política de dados.

### Consistência entre banco e mensageria

O cenário ideal é adotar o padrão Transactional Outbox:

1. salvar a consulta e um registro de evento na tabela `outbox_event` na mesma transação;
2. um publicador lê eventos pendentes;
3. publica no Kafka usando a chave `consultaId`;
4. marca o evento como publicado após a confirmação do broker;
5. eventos com falha permanecem disponíveis para nova tentativa.

Esse padrão evita perder o evento caso o banco confirme a consulta e o Kafka esteja temporariamente indisponível. Para uma primeira versão acadêmica, a publicação após o commit pode ser aceita se a limitação estiver documentada, mas o Outbox demonstra melhor arquitetura distribuída e resiliência.

---

## 12. Criar o serviço de notificações

Gerar outro projeto Spring Boot com:

- Spring Web, apenas se forem expostos health checks ou endpoints administrativos;
- Spring Data JPA;
- PostgreSQL Driver;
- Flyway;
- Spring for Apache Kafka;
- gRPC com Protobuf e starter Spring Boot compatível;
- Validation;
- Actuator;
- Testcontainers.

### Modelo `Notificacao`

Campos sugeridos:

- `id: UUID`;
- `eventId: UUID`, único;
- `consultaId: UUID`;
- `pacienteId: UUID`;
- `destinatario: String`;
- `tipo: EMAIL | LOG`;
- `status: RECEBIDA | ENVIADA | FALHA`;
- `tentativas: Integer`;
- `mensagem: String`;
- datas de recebimento e envio.

### Fluxo do consumidor

1. receber a mensagem;
2. validar versão e campos obrigatórios;
3. consultar `eventId` no banco;
4. ignorar de forma segura se o evento já foi processado;
5. chamar `BuscaPacienteById.ObterDadosDoPaciente` por gRPC;
6. montar e direcionar o lembrete com os dados atuais do paciente;
7. enviar ou simular a notificação;
8. salvar o resultado;
9. confirmar o offset somente após o processamento;
10. em falha temporária, realizar novas tentativas com atraso;
11. após o limite, publicar no tópico DLT correspondente.

Exemplo de lembrete:

```text
Olá, Maria Souza. Sua consulta de Cardiologia com Dra. Ana Silva está agendada para 10/09/2026 às 14:00.
```

Tratar horários usando o fuso configurado para a aplicação e registrar explicitamente essa decisão na documentação.

---

## 13. Aplicar resiliência

Implementar e documentar:

- timeout em qualquer integração externa;
- retentativa com backoff para falhas transitórias;
- tópicos DLT para mensagens que não puderem ser processadas;
- idempotência pelo `eventId`;
- controle de concorrência otimista com `@Version`;
- health checks para banco e Kafka;
- deadline e retentativa limitada para a chamada gRPC;
- circuit breaker no cliente gRPC para impedir sobrecarga quando o agendamento estiver indisponível;
- graceful shutdown para o consumidor terminar mensagens em processamento;
- limites de pool de conexões e consumidores;
- circuit breaker com Resilience4j para proteger a chamada gRPC ao serviço de agendamento.

Não aplicar retentativas indiscriminadamente. Erros de validação são permanentes e devem ir para o tópico DLT; indisponibilidade temporária de banco ou provedor de e-mail pode ser retentada.

---

## 14. Adicionar observabilidade

### Logs

Usar logs estruturados e incluir quando possível:

- `correlationId`;
- `eventId`;
- `consultaId`;
- nome do serviço;
- resultado da operação;
- duração.

Nunca registrar:

- senha ou hash de senha;
- cabeçalho `Authorization`;
- observações médicas completas;
- dados pessoais além do necessário para diagnóstico.

### Métricas

Expor pelo Actuator/Micrometer:

- quantidade de consultas criadas e alteradas;
- eventos publicados;
- eventos consumidos;
- falhas e retentativas;
- lag dos consumer groups e volume dos tópicos DLT;
- tempo de processamento de mensagem;
- conexões com banco e broker.

### Health checks

Validar:

```text
GET http://localhost:8080/actuator/health
GET http://localhost:8081/actuator/health
```

O endpoint de health pode ser público e retornar apenas o estado necessário. Demais endpoints do Actuator devem ser protegidos.

---

## 15. Escrever os testes

### Testes unitários

No serviço de agendamento:

- criação de consulta válida;
- paciente inexistente;
- conflito de horário;
- alteração de consulta;
- bloqueio de consulta finalizada;
- validação de propriedade do paciente;
- montagem correta do evento.

No serviço de histórico:

- criação e atualização da projeção por evento Kafka;
- descarte de evento duplicado ou desatualizado;
- filtro de consultas futuras;
- autorização e propriedade da consulta GraphQL.

No serviço de notificações:

- montagem do lembrete;
- chamada gRPC bem-sucedida ao agendamento para obter os dados do paciente;
- timeout ou indisponibilidade gRPC, com retentativa e abertura do circuit breaker;
- processamento de evento válido;
- idempotência para evento duplicado;
- falha transitória e retentativa;
- mensagem inválida direcionada ao tópico DLT.

### Testes de integração

Usar Testcontainers com PostgreSQL e Kafka para testar:

- migrations Flyway;
- repositórios JPA;
- queries GraphQL do serviço de histórico;
- autenticação HTTP Basic;
- regras `401` e `403`;
- publicação e consumo real de uma mensagem;
- materialização do histórico após o consumo;
- persistência da notificação após consumo.

Usar teste de integração gRPC para validar o contrato Protobuf, o retorno dos dados mínimos do lembrete e a autenticação interna configurada para o ambiente de teste.

### Matriz mínima de segurança

| Cenário | Resultado esperado |
|---|---|
| Requisição sem credenciais | 401 |
| Credencial inválida | 401 |
| Médico cria consulta | Permitido |
| Enfermeiro cria consulta | Permitido |
| Paciente cria consulta | Negado |
| Médico consulta histórico | Permitido |
| Enfermeiro consulta histórico | Permitido |
| Paciente consulta suas consultas | Permitido |
| Paciente consulta outro paciente | Negado |
| Input inválido ou excessivamente grande | Erro de validação |

Executar:

```bash
./gradlew test
```

Executar somente um serviço durante o desenvolvimento:

```bash
./gradlew :servico-agendamento:bootRun
./gradlew :servico-historico:bootRun
./gradlew :servico-notificacao:bootRun
```

---

## 16. Criar a collection do Postman

A collection deve conter pastas:

```text
01 - Health Checks
02 - Médico
03 - Enfermeiro
04 - Paciente
05 - Casos de Segurança
06 - Mensageria e Evidências
```

Criar variáveis:

- `agendamentoUrl`;
- `historicoUrl`;
- `notificacaoUrl`;
- `medicoUsername` e `medicoPassword`;
- `enfermeiroUsername` e `enfermeiroPassword`;
- `pacienteUsername` e `pacientePassword`;
- `pacienteId`;
- `consultaId`.

As mutations REST de agendamento usam `agendamentoUrl`; as queries GraphQL usam `historicoUrl`:

```http
POST /graphql
Content-Type: application/json
Authorization: Basic <credenciais>
```

Corpo:

```json
{
  "query": "query MinhasConsultas { minhasConsultas { id medico dataHora status } }"
}
```

Adicionar scripts de teste que validem status HTTP, ausência de `errors` nas operações válidas e presença do código correto nos cenários negados.

---

## 17. Containerizar as aplicações

Criar um `Dockerfile` multi-stage para cada serviço:

1. estágio de build com Gradle e JDK;
2. estágio de runtime com JRE;
3. usuário não root;
4. somente o JAR final copiado;
5. health check ou integração com o health check do Compose;
6. parâmetros de memória configuráveis.

Adicionar os serviços Java ao `compose.yaml` e estabelecer dependências por health check, não apenas por ordem de inicialização.

Validar o ambiente completo:

```bash
docker compose up --build
docker compose ps
```

Depois, executar a collection e confirmar no Kafka UI que os consumer groups de histórico e notificações avançaram seus offsets e que os tópicos DLT permanecem sem mensagens nos cenários de sucesso.

---

## 18. Documentar o projeto

O `README.md` final deve conter:

1. título e descrição do problema;
2. integrantes do grupo e RM;
3. arquitetura e justificativas das decisões;
4. tecnologias e versões;
5. pré-requisitos;
6. instruções de configuração e execução;
7. credenciais exclusivas de demonstração;
8. contrato GraphQL com exemplos;
9. regras de autorização;
10. tópicos, consumer groups, estratégia de chave e contrato do Kafka;
11. contrato Protobuf, instruções para geração dos stubs e finalidade do gRPC;
12. estratégia de resiliência;
13. estratégia de segurança e hardening;
13. como executar testes;
14. como importar a collection;
15. limitações conhecidas e evoluções futuras;
16. evidências de funcionamento.

### Evoluções que podem ser citadas

- trocar HTTP Basic por OAuth 2.0/OpenID Connect;
- implementar notificações reais por e-mail/SMS;
- Kubernetes para escalabilidade e alta disponibilidade;
- OpenTelemetry, Prometheus e Grafana;
- gestão de segredos com Vault ou serviço de nuvem;
- auditoria imutável de acesso ao histórico médico.

---

## 19. Relacionar a solução às disciplinas da FIAP

| Conteúdo estudado | Aplicação no trabalho |
|---|---|
| APIs e interfaces | Queries GraphQL no histórico e eventos Kafka versionados |
| Fundamentos de segurança | Privilégio mínimo, redução de exposição e proteção de dados |
| Autenticação e autorização | HTTP Basic, BCrypt, perfis e method security |
| Criptografia | Hash de senha e recomendação de TLS |
| Spring Security | Security filter chain, user details e `@PreAuthorize` |
| Testes de segurança e hardening | Matriz 401/403, validação, limites e análise de dependências |
| GraphQL | Queries flexíveis de histórico e consultas futuras |
| gRPC | Consulta síncrona interna de dados atualizados para o lembrete |
| Arquitetura distribuída | Serviços e bancos independentes, contratos bem definidos |
| Resiliência | Outbox, retry, backoff, DLT, idempotência e health checks |
| Monitoramento e escalabilidade | Actuator, métricas, logs estruturados e consumidores escaláveis |
| Kafka | Tópicos, partições, chaves, produtor, consumer groups e DLT |

Essa relação deve aparecer na apresentação para mostrar que as decisões técnicas não foram aleatórias e correspondem ao conteúdo da fase.

---

## 20. Organizar a execução em etapas

### Etapa 1 — Planejamento

- validar requisitos;
- criar backlog;
- desenhar arquitetura;
- definir contrato GraphQL e contratos de eventos Kafka;
- definir critérios de aceite.

**Resultado:** arquitetura e contratos revisados antes do código.

### Etapa 2 — Infraestrutura

- criar Compose com bancos e Kafka;
- configurar migrations;
- validar conectividade e health checks.

**Resultado:** ambiente local reproduzível.

### Etapa 3 — Domínio de agendamento

- criar entidades, repositórios e serviços;
- implementar regras de consulta;
- criar testes unitários.

**Resultado:** domínio funcionando independentemente da interface.

### Etapa 4 — Segurança

- implementar usuários, BCrypt e HTTP Basic;
- aplicar autorização por perfil e propriedade;
- criar testes de segurança.

**Resultado:** acessos permitidos e negados conforme a matriz.

### Etapa 5 — Serviço de histórico e GraphQL

- criar schema;
- implementar somente queries de leitura;
- implementar o consumidor que materializa a projeção;
- padronizar erros;
- validar paginação e limites.

**Resultado:** operações de consulta e manutenção disponíveis pela API.

### Etapa 6 — Mensageria

- criar tópicos Kafka, consumer groups e tópicos DLT;
- implementar eventos versionados;
- implementar Outbox ou documentar a abordagem adotada;
- confirmar publicação de eventos.

**Resultado:** criação e edição produzem mensagens confiáveis.

### Etapa 7 — Notificações

- criar consumidor;
- criar cliente gRPC com base no contrato Protobuf;
- persistir notificações;
- implementar idempotência, retry e DLT;
- testar eventos duplicados e inválidos.

**Resultado:** lembrete processado com segurança e resiliência.

### Etapa 8 — Observabilidade e hardening

- adicionar Actuator, métricas e logs;
- proteger endpoints administrativos;
- revisar exposição de dados;
- executar análise de dependências.

**Resultado:** serviços observáveis e endurecidos.

### Etapa 9 — Testes de ponta a ponta

- subir o ambiente pelo Compose;
- executar testes automatizados;
- executar a collection Postman;
- validar bancos, tópicos, consumidores, offsets e DLT;
- registrar evidências.

**Resultado:** fluxo completo comprovado.

### Etapa 10 — Entrega

- revisar README e diagramas;
- remover segredos e arquivos temporários;
- conferir scripts de inicialização;
- publicar o repositório;
- testar as instruções em ambiente limpo;
- preparar demonstração e apresentação.

**Resultado:** repositório público, reproduzível e pronto para avaliação.

---

## 21. Sugestão de backlog

| Prioridade | História |
|---:|---|
| P0 | Como usuário, quero me autenticar para acessar o sistema com segurança |
| P0 | Como médico/enfermeiro, quero cadastrar uma consulta |
| P0 | Como médico/enfermeiro, quero editar uma consulta |
| P0 | Como profissional, quero consultar o histórico de um paciente |
| P0 | Como paciente, quero ver somente minhas consultas |
| P0 | Como sistema, quero publicar eventos de consulta |
| P0 | Como sistema, quero consumir eventos e gerar lembretes |
| P1 | Como operador, quero reprocessar falhas transitórias sem perder mensagens |
| P1 | Como operador, quero observar a saúde e as métricas dos serviços |
| P1 | Como avaliador, quero executar os fluxos por uma collection |
| P2 | Como paciente, quero receber um e-mail real |
| P2 | Como operador, quero dashboards com métricas e tracing distribuído |

---

## 22. Roteiro de demonstração

A apresentação pode seguir esta ordem:

1. explicar o problema e a arquitetura;
2. mostrar containers, bancos e Kafka ativos;
3. autenticar como enfermeiro e criar uma consulta;
4. mostrar o endpoint REST de criação respondendo com sucesso;
5. mostrar o evento publicado no tópico Kafka e os dois consumer groups processando-o;
6. mostrar a chamada gRPC do serviço de notificações para o agendamento;
7. mostrar a notificação persistida ou enviada;
8. autenticar como médico e editar a consulta;
9. consultar o histórico via GraphQL;
10. autenticar como paciente e listar as próprias consultas;
11. tentar consultar outro paciente e demonstrar o bloqueio;
12. apresentar testes automatizados e health checks;
13. concluir relacionando a implementação aos tópicos da FIAP.

Preparar previamente IDs e credenciais de demonstração para não depender de ajustes manuais durante a apresentação.

---

## 23. Checklist final da entrega

### Funcionalidade

- [ ] Médico consulta e edita o histórico.
- [ ] Enfermeiro registra consultas e consulta o histórico.
- [ ] Paciente visualiza somente as próprias consultas.
- [ ] GraphQL retorna histórico completo e consultas futuras.
- [ ] Criação e edição publicam eventos.
- [ ] Serviço de notificações consome e gera lembretes.
- [ ] Serviço de notificações obtém dados do lembrete por gRPC.

### Segurança

- [ ] Senhas armazenadas com BCrypt.
- [ ] HTTP Basic configurado.
- [ ] Autorização aplicada nos métodos.
- [ ] Propriedade do paciente validada.
- [ ] Inputs limitados e validados.
- [ ] Logs não contêm credenciais ou dados médicos sensíveis.
- [ ] Actuator protegido.
- [ ] Dependências analisadas.

### Sistemas distribuídos

- [ ] Serviços possuem responsabilidades independentes.
- [ ] Cada serviço possui seu próprio banco.
- [ ] Contrato de evento está documentado e versionado.
- [ ] Consumidor é idempotente.
- [ ] Retry, backoff e DLT estão configurados.
- [ ] Falha do Kafka não causa perda silenciosa de eventos.
- [ ] Timeout e indisponibilidade do gRPC são tratados com resiliência.

### Qualidade

- [ ] Código organizado por domínio.
- [ ] Entidades JPA não são expostas diretamente.
- [ ] Migrations estão versionadas.
- [ ] Testes unitários e de integração passam.
- [ ] Health checks respondem corretamente.
- [ ] Docker Compose sobe o projeto completo.

### Entregáveis

- [ ] README completo.
- [ ] Diagrama de arquitetura.
- [ ] Collection e environment do Postman.
- [ ] Evidências dos fluxos principais.
- [ ] Repositório público acessível.
- [ ] Instruções testadas em ambiente limpo.
- [ ] Nenhum segredo foi versionado.

---

## 24. Definição final de sucesso

O trabalho estará pronto quando um avaliador conseguir clonar o repositório, iniciar a infraestrutura com as instruções do README, autenticar-se com os três perfis, executar as operações GraphQL, observar a mensagem sendo processada pelo serviço de notificações e confirmar os controles de segurança por meio da collection e dos testes automatizados.

O foco deve permanecer na correção do fluxo principal, na clareza da arquitetura, na segurança dos acessos e na confiabilidade da comunicação assíncrona. Funcionalidades extras só devem ser adicionadas depois que esses pontos estiverem concluídos e documentados.
