FROM gradle:8.7-jdk21 AS builder

WORKDIR /build

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src

RUN gradle clean build -x test

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
    bash curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar
RUN mkdir -p /app/logs /app/keys

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
CMD curl -f http://localhost:9090/health || exit 1

EXPOSE 9090

ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"

ENTRYPOINT ["java"]
CMD ["-server", "-XX:+UseG1GC", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
