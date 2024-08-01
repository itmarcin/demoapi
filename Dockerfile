# Stage 1: Build the application
FROM gradle:7-jdk17 AS build
COPY src /home/app/src
COPY build.gradle /home/app
COPY settings.gradle /home/app
WORKDIR /home/app
RUN gradle build -x test

# Stage 2: Run the application
FROM openjdk:17-jdk-alpine

# Create a non-root user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create a directory for the app and set permissions
RUN mkdir /app && chown appuser:appgroup /app

# Copy the JAR file from the build stage
COPY --from=build /home/app/build/libs/demoapi-0.0.1.jar /app/demoapi-0.0.1.jar

# Switch to the non-root user
USER appuser

# Define the working directory
WORKDIR /app

# Document the port the application will listen on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","/app/demoapi-0.0.1.jar"]