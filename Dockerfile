# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -ntp clean package

FROM eclipse-temurin:21-jre-jammy
RUN groupadd --system twibo && useradd --system --gid twibo --home-dir /app twibo
WORKDIR /app

COPY --from=build --chown=twibo:twibo /workspace/target/twibo-*.jar app.jar

USER twibo
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=production

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/urandom", "-jar", "/app/app.jar"]
