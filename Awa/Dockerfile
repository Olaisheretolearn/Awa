FROM eclipse-temurin:17-jdk AS builder

WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN chmod +x gradlew

COPY src src

RUN ./gradlew clean bootJar -x test --no-daemon


FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /workspace/build/libs/Awa-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
