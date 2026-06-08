# Stage 1: Build
FROM gradle:8-jdk21 AS builder
WORKDIR /workspace
COPY gradle gradle
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY prs-ms-pricing/build.gradle prs-ms-pricing/build.gradle
COPY prs-ms-pricing/src prs-ms-pricing/src
RUN gradle :prs-ms-pricing:bootJar --no-daemon -q
RUN mkdir -p /otel && curl -fsSL --retry 5 --retry-delay 2 --retry-all-errors -o /otel/opentelemetry-javaagent.jar \
    https://repo1.maven.org/maven2/io/opentelemetry/javaagent/opentelemetry-javaagent/2.5.0/opentelemetry-javaagent-2.5.0.jar

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S parking && adduser -S parking -G parking
COPY --from=builder /workspace/prs-ms-pricing/build/libs/*.jar app.jar
COPY --from=builder /otel/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS="-javaagent:/otel/opentelemetry-javaagent.jar"
USER parking
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
