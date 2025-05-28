# Java Application Dockerfile
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven/Gradle files first for better caching
# For Maven:
COPY pom.xml .
COPY src ./src

# For Gradle (uncomment if using Gradle instead):
# COPY build.gradle .
# COPY gradle.properties .
# COPY gradle ./gradle
# COPY gradlew .
# COPY src ./src

# Install Maven (if not using Gradle)
RUN apt-get update && apt-get install -y maven

# Build the application
# For Maven:
RUN mvn clean package -DskipTests

# For Gradle (uncomment if using Gradle):
# RUN ./gradlew build -x test

# Copy the built JAR file
# Adjust the path based on your actual JAR file location
COPY target/*.jar app.jar

# For Gradle:
# COPY build/libs/*.jar app.jar

# Expose the port your application runs on
EXPOSE 8080

# Create a non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]