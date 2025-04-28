# Use an official Java runtime as a parent image
FROM openjdk:17.0.2-jdk-slim

# Set the working directory
WORKDIR /app

ARG APP_VERSION

# Copy the Spring fat JAR into the container
COPY build/libs/service-actor-management-${APP_VERSION}.jar app.jar

# Expose the application port
EXPOSE 8080

#Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]