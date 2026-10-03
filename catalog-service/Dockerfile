# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw ./
RUN chmod +x mvnw
COPY catalog-service/pom.xml catalog-service/pom.xml
COPY catalog-service/src catalog-service/src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -f catalog-service/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/catalog-service/target/catalog-service-0.0.1-SNAPSHOT.jar app.jar
ENV PORT=8083
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
