# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw ./
RUN chmod +x mvnw
COPY media-service/pom.xml media-service/pom.xml
COPY media-service/src media-service/src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -f media-service/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/media-service/target/media-service-0.0.1-SNAPSHOT.jar app.jar
ENV PORT=8082
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
