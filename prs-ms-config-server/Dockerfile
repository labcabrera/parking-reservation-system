# Stage 1: Build
FROM gradle:8-jdk21 AS builder
WORKDIR /workspace
COPY gradle gradle
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY prs-ms-config-server/build.gradle prs-ms-config-server/build.gradle
COPY prs-ms-config-server/src prs-ms-config-server/src
RUN ./gradlew :prs-ms-config-server:bootJar --no-daemon -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S parking && adduser -S parking -G parking
COPY --from=builder /workspace/prs-ms-config-server/build/libs/*.jar app.jar
USER parking
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
