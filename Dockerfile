# Use an official Java runtime as a parent image
FROM amazoncorretto:17-alpine

# Set the working directory
WORKDIR /app

# Copy the Spring fat JAR into the container
COPY build/libs/service-actor-management-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

#Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]