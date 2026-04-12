# Étape 1 : Build avec Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /build

# Copier tout le projet Maven (le parent + les modules)
COPY alteronix-back ./alteronix-back

# Copier le pom.xml du module parent
COPY pom.xml .

# Etape 2 : Installation des dépendances
RUN mvn -f alteronix-back/pom.xml clean package -DskipTests

# Étape 3 : Image d'exécution légère
FROM eclipse-temurin:23-jdk-alpine

WORKDIR /app

# Même argument pour le nom du jar
ARG JAR_NAME=alteronix-back

# Copier le JAR compilé depuis l'étape précédente
COPY --from=build /build/${JAR_NAME}/target/${JAR_NAME}-*.jar app.jar

# Expose the ports for HTTP
EXPOSE 9080

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]