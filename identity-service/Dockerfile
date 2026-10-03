# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw ./
RUN chmod +x mvnw
COPY identity-service/pom.xml identity-service/pom.xml
COPY identity-service/src identity-service/src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -f identity-service/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/identity-service/target/identity-service-0.0.1-SNAPSHOT.jar app.jar
ENV PORT=8084
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
