FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY web ./web
RUN mvn -B package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/grand-line-escape.jar ./app.jar
ENV OPEN_BROWSER=false
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
