# Stage 1: Build
FROM gradle:8-jdk21 AS builder
WORKDIR /workspace
COPY gradle gradle
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY catalog-service/build.gradle catalog-service/build.gradle
COPY catalog-service/src catalog-service/src
RUN ./gradlew :catalog-service:bootJar --no-daemon -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S parking && adduser -S parking -G parking
COPY --from=builder /workspace/catalog-service/build/libs/*.jar app.jar
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.5.0/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS="-javaagent:/otel/opentelemetry-javaagent.jar"
USER parking
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
