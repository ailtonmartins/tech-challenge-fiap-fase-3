# Collection de testes

Importe apenas `Tech-Challenge-Fase-3.postman_collection.json` no Postman. Ela
reúne os health checks, os fluxos REST do agendamento e as queries GraphQL.
Execute a pasta **Agendamento - Fluxo principal** na ordem indicada.

## Teste gRPC — consulta de paciente

O contrato canônico é
[`contratos-grpc/src/main/proto/pacientes.proto`](../contratos-grpc/src/main/proto/pacientes.proto).
Não há cópia dele nesta pasta, para impedir divergência entre o contrato
executado pela aplicação e o usado no Postman.

Com o ambiente Docker em execução, siga estes passos:

1. Selecione **New > gRPC** no Postman.
2. Informe o servidor `127.0.0.1:6565`.
3. Importe o arquivo `contratos-grpc/src/main/proto/pacientes.proto` pela tela
   da requisição.
4. Selecione `BuscaPacienteById > ObterDadosDoPaciente`.
5. Informe um dos payloads abaixo no editor **Message** e clique em
   **Invoke**.
6. Use **Save As** para armazenar cada cenário em uma collection
   multiprotocolo do Postman.

> A collection JSON `Tech-Challenge-Fase-3.postman_collection.json` contém
> somente HTTP e GraphQL. Requests gRPC nativos precisam ser salvos pelo
> Postman em uma collection multiprotocolo.

### Sucesso

```json
{
  "paciente_id": "20000000-0000-0000-0000-000000000001"
}
```

Resultado esperado: `pacienteId`, `pacienteNome`, `pacienteEmail` e
`pacienteTelefone` de Maria Souza.

### Identificador inválido

```json
{
  "paciente_id": "nao-e-uuid"
}
```

Resultado esperado: status `INVALID_ARGUMENT`.

### Paciente inexistente

```json
{
  "paciente_id": "00000000-0000-0000-0000-000000000099"
}
```

Resultado esperado: status `NOT_FOUND`.

O Compose publica a porta somente em `127.0.0.1:6565` no ambiente local; entre
os containers, a comunicação continua em `servico-agendamento:6565`.
