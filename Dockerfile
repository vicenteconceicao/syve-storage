# Dockerfile for a Java application using Maven
# This Dockerfile builds a Java application using Maven and packages it into a Docker image.
FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Build the final image using a smaller base image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN useradd -ms /bin/bash quarkus
USER quarkus

COPY --from=build /build/target/quarkus-app/ ./

EXPOSE 8081

ENV JAVA_OPTS="-XX:+ExitOnOutOfMemoryError"
ENV TZ=America/Sao_Paulo

# Ponto de entrada
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]