# Use a multi-stage build to reduce the final image size
# Stage 1: Build the application
FROM gradle:7-jdk17 AS build
COPY src /home/app/src
COPY build.gradle /home/app
COPY settings.gradle /home/app
WORKDIR /home/app
RUN gradle build -x test

# Stage 2: Run the application
FROM openjdk:17-jdk-alpine
VOLUME /tmp
COPY --from=build /home/app/build/libs/demoapi-0.0.1.jar /demoapi-0.0.1.jar
ENTRYPOINT ["java","-jar","/demoapi-0.0.1.jar"]