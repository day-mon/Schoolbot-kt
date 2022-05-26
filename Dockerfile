# Downloads gradle w/ java
FROM gradle:7.4.2-jdk18  as cache
# Creates cache home
RUN mkdir -p /home/gradle/cache_home

# Sets GRADLE_USER_HOME to here so it can write the dependencies here instead of normal gradle home
ENV GRADLE_USER_HOME /home/gradle/cache_home

# Copies build file
COPY build.gradle.kts /home/gradle/java-code/

# Sets Workdirectory moves
WORKDIR /home/gradle/java-code

# Builds project
RUN gradle clean build -i --stacktrace


# Downloads gradle w/ java
FROM gradle:7.4.2-jdk18  as builder
# Copies depenencies to actual GRADLE_USER_HOME so it doesnt need to redownload here...
# Normal GRADLE_USER_HOME = /home/gradle/.gradle. Its changed so you wont need to have gradle installed for it to persist
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle

# Copies all files from project into /home/schoolbot-kt
COPY . /home/schoolbot-kt

RUN ls

# Sets Workdirectory moves
WORKDIR /home/schoolbot-kt

RUN ls -l
# Jars project
RUN gradle bootJar -i --stacktrace

# Downloads java
FROM  openjdk:18-slim

# Sets user to root
USER root

# Sets Workdirectory moves
WORKDIR /home/schoolbot-kt

# Copies jar from builder build steps
COPY --from=builder /home/schoolbot-kt/build/libs/SchoolbotKt.jar ./app.jar


# Copies all of the files jsut because why not
COPY --from=builder /home/schoolbot-kt/. .


# Runs.. :)
ENTRYPOINT java -jar -Xmx2G app.jar