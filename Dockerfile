FROM maven:3.6.3-jdk-11-slim AS build
WORKDIR /app
COPY . /app/
RUN mvn clean package

FROM openjdk:latest
WORKDIR /app
COPY --from=build /app/target/ /app
COPY --from=build /app/res/ /app/res
RUN apt install wget
RUN wget -O dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'
CMD java -javaagent:"/app/dd-java-agent.jar" -Ddd.profiling.enabled=true -Ddd.logs.injection=true -Ddd.trace.sample.rate=1 -Ddd.service=vinny-main -Ddd.env=prod -jar discord-bot-1.0-SNAPSHOT-jar-with-dependencies.jar
