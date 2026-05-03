# syntax=docker/dockerfile:1

FROM maven:3.9.9-amazoncorretto-21 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn -q -e -B dependency:go-offline

COPY src ./src
RUN mvn -q -e -B package -DskipTests

FROM amazoncorretto:21-alpine
WORKDIR /app

RUN apk add --no-cache libc6-compat

COPY --from=builder /app/target/financial-ai-*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
