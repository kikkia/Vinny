FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app
COPY . /app/
RUN mvn clean package

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/ /app
COPY --from=build /app/res/ /app/res
CMD java -javaagent:"/app/res/dd-java-agent.jar" -Ddd.profiling.enabled=true -Ddd.logs.injection=true -Ddd.trace.sample.rate=1 -Ddd.trace.analytics.enabled=true -Ddd.service=vinny-main -Ddd.env=prod -jar discord-bot-1.0-SNAPSHOT-jar-with-dependencies.jar
