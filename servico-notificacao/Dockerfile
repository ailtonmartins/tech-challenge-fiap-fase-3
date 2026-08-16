FROM gradle:8.14-jdk21 AS build

WORKDIR /workspace
COPY . .
RUN gradle :servico-notificacao:bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --create-home appuser

WORKDIR /app
COPY --from=build /workspace/servico-notificacao/build/libs/app.jar app.jar
USER appuser

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
