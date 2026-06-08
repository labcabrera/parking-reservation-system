# Stage 1: Build
FROM gradle:8-jdk21 AS builder
WORKDIR /workspace
COPY gradle gradle
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY prs-mock-ms-payment/build.gradle prs-mock-ms-payment/build.gradle
COPY prs-mock-ms-payment/src prs-mock-ms-payment/src
RUN gradle :prs-mock-ms-payment:bootJar --no-daemon -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S parking && adduser -S parking -G parking
COPY --from=builder /workspace/prs-mock-ms-payment/build/libs/*.jar app.jar
USER parking
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
