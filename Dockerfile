FROM maven:3.5-jdk-8-alpine AS build
WORKDIR /app
COPY . /app/
RUN mvn clean package

MAINTAINER jess<jesswalters53@gmail.com>

FROM openjdk:latest
WORKDIR /app
COPY --from=build /app/target/ /app
CMD java -jar discord-bot-1.0-SNAPSHOT-jar-with-dependencies.jar
