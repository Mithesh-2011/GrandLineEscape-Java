FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY src ./src
RUN mkdir -p out && javac --release 11 -d out src/*.java

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/out ./out
COPY web ./web
ENV PORT=8080
ENV OPEN_BROWSER=false
EXPOSE 8080
CMD ["java", "-cp", "out", "Main"]
