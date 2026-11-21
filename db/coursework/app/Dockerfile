# Multi-stage build: Maven + JDK 21 build the jar, a JRE 21 image runs it.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /w
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline
COPY src src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre
ENV TZ=UTC
WORKDIR /app
COPY --from=build /w/target/micromobility.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=UTC", "-jar", "app.jar"]
