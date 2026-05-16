# Build stage
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

COPY . .

RUN ./gradlew assemble --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/build/libs/taskmanager-*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
  CMD java -cp app.jar org.springframework.boot.loader.JarLauncher --version || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
