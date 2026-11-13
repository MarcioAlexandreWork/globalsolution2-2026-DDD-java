# Estágio 1: compila o projeto
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# Estágio 2: imagem final enxuta
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/zeladoria-cidada.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
