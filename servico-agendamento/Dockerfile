FROM gradle:8.14-jdk21 AS build

WORKDIR /workspace
COPY . .
RUN gradle :servico-agendamento:bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --create-home appuser

WORKDIR /app
COPY --from=build /workspace/servico-agendamento/build/libs/app.jar app.jar
USER appuser

EXPOSE 8080
EXPOSE 6565
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
