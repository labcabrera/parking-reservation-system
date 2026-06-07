# Stage 1: Build
FROM gradle:8-jdk21 AS builder
WORKDIR /workspace
COPY gradle gradle
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY prs-ms-users/build.gradle prs-ms-users/build.gradle
COPY prs-ms-users/src prs-ms-users/src
RUN ./gradlew :prs-ms-users:bootJar --no-daemon -q
RUN mkdir -p /otel && curl -fsSL -o /otel/opentelemetry-javaagent.jar \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.5.0/opentelemetry-javaagent.jar

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S parking && adduser -S parking -G parking
COPY --from=builder /workspace/prs-ms-users/build/libs/*.jar app.jar
COPY --from=builder /otel/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS="-javaagent:/otel/opentelemetry-javaagent.jar"
USER parking
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
