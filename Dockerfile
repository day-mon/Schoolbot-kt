FROM gradle:jdk17 as build
WORKDIR /home/schoolbot

COPY . .
RUN gradle clean compileJava

FROM adoptopenjdk/openjdk16-openj9:alpine
WORKDIR /home/schoolbot

COPY --from=build /home/schoolbot/target/*.jar schoolbot.jar

ENTRYPOINT java -server -Xmx10G -Dnogui=true -jar schoolbot.jar
