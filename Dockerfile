FROM openjdk:15-alpine
WORKDIR /home/monkebot/
COPY build/libs/MonkeKotlin-all.jar MonkeKotlin.jar
ENTRYPOINT java -jar MonkeKotlin.jar
