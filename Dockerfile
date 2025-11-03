# Multi-stage Dockerfile: build with Maven + JDK 17, run with slim JRE
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven files first to leverage layer caching
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw

# Copy sources
COPY src ./src

# Build package (skip tests for speed)
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/url-validator-1.0.0.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
