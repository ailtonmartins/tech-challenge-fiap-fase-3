# Histórias de Usuário — Tech Challenge FIAP Fase 3

## Objetivo

Este backlog orienta o desenvolvimento do sistema hospitalar composto por serviço de agendamento, serviço de histórico e serviço de notificações. As histórias estão escritas no formato:

> Como **[perfil]**, quero **[ação]**, para **[benefício]**.

As prioridades seguem esta convenção:

| Prioridade | Significado |
|---|---|
| P0 | Essencial para a entrega mínima do Tech Challenge |
| P1 | Importante para qualidade, segurança e demonstração |
| P2 | Evolução desejável após a entrega principal |

---

## Épico 1 — Infraestrutura e acesso seguro

### US-01 — Executar o ambiente local

**Prioridade:** P0  
**Como** desenvolvedor, **quero** iniciar bancos de dados, Kafka e serviços com Docker Compose, **para** executar o projeto de forma padronizada em qualquer máquina.

**Critérios de aceite:**

- [x] Existe um arquivo `compose.yaml` na raiz do repositório.
- [x] Existe um Gradle Wrapper único na raiz do repositório.
- [x] O comando `docker compose up --build` inicia Kafka, um PostgreSQL com schemas isolados e os três serviços.
- [x] Cada serviço possui health check disponível pelo Actuator.
- [x] O projeto possui `.env.example` sem senhas reais.
- [x] O README explica os pré-requisitos e o comando de inicialização.

---

### US-01B — Organizar o projeto como Gradle multi-module

**Prioridade:** P0  
**Como** desenvolvedor, **quero** organizar os serviços em módulos Gradle no mesmo repositório, **para** executar builds e testes de forma centralizada sem perder a independência de cada serviço.

**Critérios de aceite:**

- [x] A raiz possui `settings.gradle`, `build.gradle` e um único Gradle Wrapper.
- [x] O `settings.gradle` inclui `servico-agendamento`, `servico-historico`, `servico-notificacao` e `contratos-grpc`.
- [x] Cada serviço possui seu próprio `build.gradle`, Dockerfile, configuração e schema de banco isolado.
- [x] O módulo `contratos-grpc` contém o arquivo `.proto` e gera os stubs usados por histórico e notificações.
- [x] Nenhuma entidade JPA ou regra de negócio é compartilhada entre os serviços.
- [x] `./gradlew test` executa os testes de todos os módulos.
- [x] É possível iniciar individualmente cada serviço com `./gradlew :nome-do-modulo:bootRun`.

---

### US-02 — Autenticar usuários

**Prioridade:** P0  
**Como** médico, enfermeiro ou paciente, **quero** autenticar-me com usuário e senha, **para** acessar somente os recursos permitidos ao meu perfil.

**Critérios de aceite:**

- [x] A autenticação usa HTTP Basic.
- [x] Senhas são armazenadas com hash BCrypt.
- [x] Credenciais ausentes ou inválidas retornam `401 Unauthorized`.
- [x] Usuários possuem os perfis `MEDICO`, `ENFERMEIRO` ou `PACIENTE`.
- [x] Não há senha, hash ou cabeçalho `Authorization` nos logs.

---

### US-01A — Criar massa de dados de desenvolvimento

**Prioridade:** P0  
**Como** desenvolvedor, **quero** carregar usuários, pacientes e consultas previsíveis no perfil de desenvolvimento, **para** testar os cenários da API, GraphQL, Kafka e gRPC sem cadastrar todos os dados manualmente.

**Critérios de aceite:**

- [x] A massa é executada apenas com o perfil `dev`, pela migration `db/dev-migration/V4__criar_massa_de_desenvolvimento.sql`.
- [x] A migration cria médico, enfermeiro, dois pacientes e três consultas com identificadores fixos documentados.
- [x] Uma consulta é futura para Maria, uma é passada para Maria e uma é futura para João.
- [x] Os usuários pacientes estão vinculados aos respectivos pacientes.
- [x] A execução é idempotente: o histórico do Flyway executa a migration uma vez e os `INSERT`s usam `ON CONFLICT`.
- [x] Senhas de demonstração são armazenadas como hash BCrypt; senhas em texto puro não são persistidas.
- [x] Por ser carga SQL direta, a migration não publica eventos Kafka; cenários de integração devem criar ou alterar consultas pela API.
- [x] A migration de massa não é carregada nos perfis `test` e `prod`.

---

### US-03 — Restringir operações por perfil

**Prioridade:** P0  
**Como** administrador do sistema, **quero** aplicar autorização por perfil e por propriedade do dado, **para** proteger os dados médicos dos pacientes.

**Critérios de aceite:**

- [x] Médico e enfermeiro podem criar e alterar consultas.
- [x] Paciente não pode criar ou alterar consultas.
- [ ] Médico e enfermeiro podem consultar histórico de pacientes — depende da API GraphQL da US-09.
- [ ] Paciente consulta somente as próprias consultas — depende da API GraphQL da US-10.
- [x] Um acesso fora dessas regras retorna `403 Forbidden`.
- [x] A autorização é aplicada na camada de serviço ou resolver, e não apenas na rota HTTP.

---

## Épico 2 — Agendamento de consultas

### US-04 — Cadastrar paciente

**Prioridade:** P0  
**Como** médico ou enfermeiro, **quero** cadastrar os dados básicos de um paciente, **para** associar consultas a uma pessoa identificada.

**Critérios de aceite:**

- [x] O paciente possui identificador único, nome, e-mail, telefone e data de nascimento.
- [x] Campos obrigatórios são validados.
- [x] E-mail possui formato válido.
- [x] O sistema não permite pacientes duplicados conforme a regra definida pelo grupo (por exemplo, e-mail único).
- [x] Apenas profissionais autorizados realizam o cadastro.

---

### US-05 — Criar consulta

**Prioridade:** P0  
**Como** médico ou enfermeiro, **quero** criar uma consulta para um paciente, **para** registrar seu atendimento futuro.

**Critérios de aceite:**

- [x] A operação é disponibilizada em `POST /api/consultas` no serviço de agendamento.
- [x] São obrigatórios: paciente, médico, especialidade e data/hora com fuso horário.
- [x] O paciente informado deve existir.
- [x] A consulta inicia com status `AGENDADA`.
- [x] Não é permitido agendar duas consultas para o mesmo paciente no mesmo horário.
- [x] A resposta retorna `201 Created` e os dados essenciais da consulta.
- [x] Após a persistência, é criado um evento de consulta para publicação no Kafka.

---

### US-06 — Alterar consulta

**Prioridade:** P0  
**Como** médico ou enfermeiro, **quero** alterar os dados de uma consulta, **para** corrigir informações ou reagendar o atendimento.

**Critérios de aceite:**

- [x] A operação é disponibilizada em `PUT /api/consultas/{id}`.
- [x] A consulta deve existir para ser alterada.
- [x] Não é permitida alteração de consulta cancelada ou realizada, salvo regra explicitamente documentada.
- [x] O sistema valida conflito de horário após o reagendamento.
- [x] O controle de versão impede perda silenciosa de alterações concorrentes.
- [x] A resposta retorna `200 OK` com a consulta atualizada.
- [x] Após a persistência, é criado um evento de atualização para publicação no Kafka.

---

### US-07 — Publicar evento de consulta

**Prioridade:** P0  
**Como** sistema de agendamento, **quero** publicar um evento Kafka quando uma consulta for criada ou alterada, **para** manter outros serviços atualizados sem acoplamento síncrono.

**Critérios de aceite:**

- [x] Eventos de criação são publicados no tópico `consulta.criada.v1`.
- [x] Eventos de atualização são publicados no tópico `consulta.atualizada.v1`.
- [x] O evento possui `eventId`, tipo, versão, data de ocorrência e `consultaId`.
- [x] A chave da mensagem é o `consultaId`, preservando a ordem dos eventos da mesma consulta.
- [x] O produtor utiliza confirmação de entrega do Kafka.
- [x] Eventos não enviados permanecem pendentes para nova tentativa pelo padrão Outbox, ou a limitação da primeira versão está documentada.
- [x] O evento não contém observações médicas desnecessárias.

---

## Épico 3 — Histórico e GraphQL

### US-08 — Materializar o histórico a partir do Kafka

**Prioridade:** P0  
**Como** serviço de histórico, **quero** consumir eventos de consulta, **para** manter uma projeção de leitura independente e disponível para consultas GraphQL.

**Critérios de aceite:**

- [x] O serviço usa o consumer group `historico-consumer-v1`.
- [x] Eventos criados e atualizados criam ou atualizam uma `ConsultaHistorico`.
- [x] A projeção possui schema de banco próprio (`historico`).
- [x] Eventos duplicados não geram dados duplicados.
- [x] Eventos antigos não sobrescrevem a projeção mais recente.
- [x] O offset é confirmado somente após a persistência bem-sucedida.
- [x] O README informa que a projeção possui consistência eventual.

---

### US-09 — Consultar histórico por GraphQL

**Prioridade:** P0  
**Como** médico ou enfermeiro, **quero** consultar o histórico de consultas de um paciente por GraphQL, **para** obter somente os dados necessários para o atendimento.

**Critérios de aceite:**

- [x] A API GraphQL está disponível em `POST /graphql` no serviço de histórico.
- [x] A query `historicoDoPaciente` retorna consultas paginadas do paciente solicitado.
- [x] A query `consultasFuturas` retorna somente consultas com data/hora futura.
- [x] O retorno inclui identificador, profissional, especialidade, data/hora e status.
- [x] Médico e enfermeiro recebem `200 OK` para consultas autorizadas.
- [x] Parâmetros de paginação possuem limites máximos.
- [x] Erros possuem códigos claros, como `NOT_FOUND`, `FORBIDDEN` e `VALIDATION_ERROR`.

---

### US-10 — Consultar as próprias consultas

**Prioridade:** P0  
**Como** paciente, **quero** consultar minhas consultas por GraphQL, **para** acompanhar meus atendimentos sem expor dados de outras pessoas.

**Critérios de aceite:**

- [ ] A query `minhasConsultas` usa o paciente vinculado ao usuário autenticado.
- [ ] O paciente pode filtrar somente consultas futuras.
- [ ] O paciente não envia ou escolhe livremente outro `pacienteId` para acessar dados.
- [ ] Uma tentativa de acessar consulta de outro paciente retorna `403 Forbidden`.
- [ ] O retorno não expõe dados além do necessário para o paciente.

---

## Épico 4 — Notificações e gRPC

### US-11 — Consumir eventos para notificação

**Prioridade:** P0  
**Como** serviço de notificações, **quero** consumir eventos de consulta do Kafka, **para** gerar lembretes após criações e alterações de consultas.

**Critérios de aceite:**

- [ ] O serviço usa o consumer group `notificacao-consumer-v1`.
- [ ] O consumidor processa eventos de criação e atualização.
- [ ] Cada evento processado gera um registro de notificação com `eventId` único.
- [ ] Um evento duplicado não envia ou registra o lembrete novamente.
- [ ] O offset é confirmado somente após o processamento terminar.
- [ ] Falhas permanentes são encaminhadas ao tópico DLT correspondente.

---

### US-12 — Obter dados atualizados por gRPC

**Prioridade:** P0  
**Como** serviço de notificações, **quero** buscar os dados atuais da consulta no serviço de histórico via gRPC, **para** montar o lembrete com informações consistentes e atualizadas.

**Critérios de aceite:**

- [ ] O serviço de histórico expõe `HistoricoNotificacaoService.ObterDadosDoLembrete` na porta `9090`.
- [ ] O contrato está versionado em `contratos-grpc/src/main/proto`.
- [ ] O serviço de notificações usa o stub gerado pelo Protobuf.
- [ ] A chamada recebe `consultaId` e retorna apenas dados necessários ao lembrete.
- [ ] O cliente gRPC usa deadline/timeout configurável.
- [ ] Indisponibilidade do histórico provoca retentativa limitada e circuit breaker, sem confirmar prematuramente o offset Kafka.
- [ ] A comunicação interna possui autenticação adequada

---

### US-13 — Gerar lembrete ao paciente

**Prioridade:** P0  
**Como** paciente, **quero** receber um lembrete da minha consulta, **para** reduzir o risco de esquecer o atendimento.

**Critérios de aceite:**

- [ ] O lembrete contém nome do paciente, profissional, especialidade e data/hora.
- [ ] O envio pode ser simulado por log e persistido no banco na primeira versão.
- [ ] A notificação possui status `RECEBIDA`, `ENVIADA` ou `FALHA`.
- [ ] O sistema não inclui observações médicas no texto do lembrete.
- [ ] O fuso horário apresentado é o configurado para a aplicação.

---

## Épico 5 — Resiliência, observabilidade e qualidade

### US-14 — Tratar falhas de mensageria

**Prioridade:** P1  
**Como** operador, **quero** que eventos com falha sejam retentados e encaminhados para tópicos de dead letter, **para** que não sejam perdidos silenciosamente.

**Critérios de aceite:**

- [ ] Falhas transitórias usam retentativa com backoff.
- [ ] Falhas de validação não são retentadas indefinidamente.
- [ ] Após o limite de tentativas, a mensagem é publicada no tópico `.DLT` correspondente.
- [ ] O erro e o `eventId` podem ser rastreados.
- [ ] Existe procedimento documentado para inspeção e reprocessamento de DLT.

---

### US-15 — Monitorar a saúde dos serviços

**Prioridade:** P1  
**Como** operador, **quero** monitorar a saúde e métricas dos serviços, **para** identificar indisponibilidades e falhas de processamento.

**Critérios de aceite:**

- [ ] Todos os serviços expõem `GET /actuator/health`.
- [ ] Os endpoints administrativos não necessários são protegidos.
- [ ] Logs incluem `eventId`, `consultaId`, nome do serviço e resultado da operação quando aplicável.
- [ ] Existem métricas de eventos publicados, consumidos, falhas, retentativas e lag dos consumer groups.
- [ ] Dados sensíveis não são registrados nos logs.

---

### US-16 — Testar os fluxos críticos

**Prioridade:** P1  
**Como** desenvolvedor, **quero** testes automatizados dos fluxos principais, **para** entregar uma solução confiável e fácil de manter.

**Critérios de aceite:**

- [ ] Há testes unitários para regras de agendamento, autorização, idempotência e montagem de lembrete.
- [ ] Há testes de integração com PostgreSQL e Kafka via Testcontainers.
- [ ] Há teste de integração GraphQL para o serviço de histórico.
- [ ] Há teste de integração gRPC para o contrato de lembrete.
- [ ] Há testes para `401`, `403`, validação e tentativa de acesso de um paciente a dados de outro.
- [ ] O comando de testes está documentado e executa sem depender de serviços externos instalados manualmente.
- [ ] `./gradlew test` executa os testes de todos os módulos a partir da raiz.

---

## Épico 6 — Documentação e validação

### US-17 — Disponibilizar collection de testes

**Prioridade:** P1  
**Como** avaliador, **quero** importar uma collection Postman, **para** validar os principais fluxos sem construir requisições manualmente.

**Critérios de aceite:**

- [ ] A collection possui requests para health checks, agendamento, histórico GraphQL, segurança e evidências de mensageria.
- [ ] Há ambiente local com URLs e credenciais de demonstração.
- [ ] A collection contém exemplos para médico, enfermeiro e paciente.
- [ ] Há cenário que comprova acesso negado ao paciente indevido.
- [ ] As requisições possuem asserções básicas de status e erro.

---

### US-18 — Documentar a solução

**Prioridade:** P0  
**Como** avaliador, **quero** ler instruções claras de arquitetura e execução, **para** reproduzir e avaliar o projeto.

**Critérios de aceite:**

- [ ] O README contém arquitetura, tecnologias e pré-requisitos.
- [ ] O README explica como subir o ambiente e executar os testes.
- [ ] O contrato REST, GraphQL, Kafka e gRPC está documentado.
- [ ] A estratégia de autenticação, autorização, hardening e proteção de dados está documentada.
- [ ] A consistência eventual do histórico e as estratégias de resiliência estão documentadas.
- [ ] O repositório não contém segredos reais.

---

## Ordem sugerida de desenvolvimento

| Ordem | Histórias | Entrega resultante |
|---:|---|---|
| 1 | US-01, US-01B | Ambiente e build multi-module disponíveis |
| 2 | US-02, US-03 | Acesso seguro e perfis definidos |
| 3 | US-04, US-05, US-06, US-07 | Agendamento, eventos e massa de desenvolvimento |
| 4 | US-01A | Seed idempotente e cenários prontos para teste manual |
| 5 | US-08, US-09, US-10 | Histórico independente via GraphQL |
| 6 | US-11, US-12, US-13 | Notificações via Kafka e gRPC |
| 7 | US-14, US-15, US-16 | Resiliência, monitoramento e testes |
| 8 | US-17, US-18 | Collection, documentação e entrega |

## Definição de pronto para uma história

Uma história só deve ser considerada concluída quando:

- [ ] o código está organizado na estrutura em camadas definida no plano;
- [ ] a regra de negócio e os critérios de aceite foram implementados;
- [ ] há teste automatizado proporcional ao risco da mudança;
- [ ] não foram adicionadas credenciais ou dados sensíveis ao repositório;
- [ ] a documentação ou collection foi atualizada quando o contrato mudou;
- [ ] a funcionalidade foi validada no ambiente Docker Compose.
