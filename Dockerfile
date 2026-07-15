# Stage 1: build the JAR
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src
RUN ./mvnw package -DskipTests

# Stage 2: run the JAR
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/inventory-management-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]