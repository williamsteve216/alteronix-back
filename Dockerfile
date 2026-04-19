# Étape 1 : Build avec Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copier le pom.xml
COPY pom.xml .

# Etape 2 : Installation des dépendances
RUN mvn dependency:go-offline -B

# Ensuite on copie le code source et on compile
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 3 : Image d'exécution légère
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

# Copier le JAR compilé depuis l'étape précédente
COPY --from=build /app/target/*.jar app.jar

# Expose the ports for HTTP
EXPOSE 9080

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]