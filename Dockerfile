# Stage 1: Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Step 1: Copy pom.xml and download dependencies (Caching layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Step 2: Copy source and build the application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Be explicit about the JAR file to avoid conflicts with .original files
COPY --from=build /app/target/result-system-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the application with the production profile
ENTRYPOINT ["java", "-Dspring.profiles.active=production", "-jar", "app.jar"]
