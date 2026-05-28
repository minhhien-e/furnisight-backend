FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

ARG GITHUB_ACTOR
ENV GITHUB_ACTOR=${GITHUB_ACTOR}

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]