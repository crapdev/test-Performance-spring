# ---- Etapa 1 (build): Maven + JDK 21 ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primero solo el pom: Docker cachea las dependencias y no las vuelve a bajar si solo cambia el código
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B package -DskipTests

# ---- Etapa 2 (run): solo JRE ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario sin privilegios de root
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/*.jar app.jar
USER app

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
