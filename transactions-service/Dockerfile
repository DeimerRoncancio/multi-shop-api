# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw ./
RUN chmod +x mvnw
COPY transactions-service/pom.xml transactions-service/pom.xml
COPY transactions-service/src transactions-service/src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -f transactions-service/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/transactions-service/target/transactions-service-0.0.1-SNAPSHOT.jar app.jar
ENV PORT=8085
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
