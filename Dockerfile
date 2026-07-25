FROM eclipse-temurin:25-jdk AS builder
WORKDIR /build

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies

COPY src ./src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=25s \
  CMD wget -qO- http://127.0.0.1:8080/comments >/dev/null || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
