# Using alpine-based images to try and make them more lightweight
FROM gradle:7.6.0-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle bootJar

# Using corretto instead of openjdk due to deprecation notice
FROM amazoncorretto:17-alpine
COPY --from=build /home/gradle/src/build/libs/*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "./app.jar"]
