# Schoolbot-kt
![GitHub issues](https://img.shields.io/github/issues/day-mon/Schoolbot-kt.svg)
![GitHub](https://img.shields.io/github/license/day-mon/Schoolbot-kt.svg)
![GitHub last commit](https://img.shields.io/github/last-commit/day-mon/Schoolbot-kt.svg)
![GitHub top language](https://img.shields.io/github/languages/top/day-mon/Schoolbot-kt.svg)

Schoolbot-kt is a Kotlin rewrite of [Schoolbot](https://github.com/day-mon/School-Bot-Remastered), a Discord bot that provides helps college students manage their school life.

## Features
- [x] Class schedule
  - Classes can be automatically added 
    - Only for [University of Pittsburgh](https://www.pitt.edu/) or any of its [branch campuses](https://www.pitt.edu/about/regional-campuses#:~:text=Pitt's%20regional%20campuses%20in%20Bradford,of%20a%20major%20research%20university)
- [x] Class reminders
- [x] Class cancellations
- [x] Class changes
- [x] Class assignments
- [x] Laundry room availability
  - Only for [University of Pittsburgh](https://pitt.edu/) or any of [branch campuses](https://www.pitt.edu/about/regional-campuses#:~:text=Pitt's%20regional%20campuses%20in%20Bradford,of%20a%20major%20research%20universityg)


## Installation
### Requirements
- Java 17 or higher
- Gradle 4.10.2 or higher
- A Discord bot token
- Any Database supported by [JDBI](http://jdbi.org/)

### Setup (Non-Docker)
1. Clone the repository
2. Create a file called `schoolbot_cfg.json` in the root directory. Example below:
```json
{
    "token" : "YOUR_TOKEN_HERE",
    "developerIds" : [ "" ],
    "logLevel": "INFO"
}
```
3. Create a file called `application.yml` in the root directory. Example below:
```properties
spring.datasource.hikari.pool-name=Schoolbot Connection Pool
spring.datasource.hikari.connection-timeout=
spring.datasource.hikari.maximum-pool-size=
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=
spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=
spring.jpa.properties.hibernate.jdbc.batch_size=100
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```
4. Run `gradle build` to build the project
5. Run `gradle run` to run the project
6. Invite the bot to your server

### Setup (Docker)
1. Download the docker-compose.yml file from the repository
2. Create a file called `schoolbot_cfg.json` in the root directory. Example below:
```json
{
    "token" : "YOUR_TOKEN_HERE",
    "developerIds" : [ "" ],
    "logLevel": "INFO"
}
```
3. Create a file called `application.yml` in the root directory. Example below:
```properties
spring.datasource.hikari.pool-name=Schoolbot Connection Pool
spring.datasource.hikari.connection-timeout=
spring.datasource.hikari.maximum-pool-size=
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=
spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=
spring.jpa.properties.hibernate.jdbc.batch_size=100
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```
4. Run `docker-compose up -d` to start the bot
5. Invite the bot to your server

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[Apache 2.0](https://choosealicense.com/licenses/apache-2.0/)
