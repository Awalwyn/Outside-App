# Stage 1: Build
FROM maven:3.9.0-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only pom.xml first for better caching
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN mvn dependency:go-offline -B

# Copy ONLY source code (Java files)
COPY src/main/java ./src/main/java
COPY src/main/resources/application-prod.properties ./src/main/resources/application-prod.properties

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/outside-api-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
# Render will pass environment variables at runtime
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
