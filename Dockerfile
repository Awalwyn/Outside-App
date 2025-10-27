# Stage 1: Build
FROM maven:3.9.0-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only pom.xml first for better caching
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN mvn dependency:go-offline -B

# Copy ONLY source code (Java files) - NO config files needed
COPY src/main/java ./src/main/java

# Build the application (Spring Boot will use environment variables)
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/outside-api-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
# Environment variables will be read directly from system
ENTRYPOINT ["java", \
    "-Dspring.datasource.url=${DB_URL}", \
    "-Dspring.datasource.username=${DB_USERNAME}", \
    "-Dspring.datasource.password=${DB_PASSWORD}", \
    "-Dspring.datasource.driver-class-name=org.postgresql.Driver", \
    "-Dspring.jpa.hibernate.ddl-auto=update", \
    "-Dspring.jpa.show-sql=false", \
    "-Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect", \
    "-jar", "app.jar"]
