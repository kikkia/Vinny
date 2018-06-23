FROM alpine/git as clone (1)
WORKDIR /app
RUN git clone https://github.com/JessWalters/Vinny-Redux.git

FROM maven:3.5-jdk-8-alpine as build (2)
WORKDIR /app
COPY --from=clone /app/Vinny-Redux /app (3)
RUN mvn install

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/discord-bot-1.0-SNAPSHOT.jar /app
CMD ["java -jar discord-bot-1.0-SNAPSHOT.jar"]