

application {
    mainClass.set("me.damon.schoolbot.Schoolbot")
}


plugins {
    id("com.github.johnrengelman.shadow") version "7.1.1"
    kotlin("jvm") version "1.6.10"
    application


    id("org.jetbrains.kotlin.plugin.noarg") version ("1.6.10")
    id("org.jetbrains.kotlin.plugin.serialization") version("1.4.30")
    id("org.springframework.boot") version "2.6.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

project.setProperty("mainClassName", "me.damon.schoolbot.Schoolbot")
group = "com.github.day-mon"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
    maven("https://repo.spring.io/milestone")
    maven("https://repo.spring.io/snapshot")

}

noArg {
    annotation("javax.persistence.Embeddable")
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
}

/**
 * GAV coordinates GroupId:Artifact:Version
 * It is worth knowing that specifying a version means that the component must be at least that version, not exactly that version.
 * You can use platforms, and it's a package full of already configured dependencies with versions

implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

this platform has org.jetbrains.kotlin:kotlin-stdlib-jdk8 within it and a specified version
 */

dependencies {

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("ru.gildor.coroutines:kotlin-coroutines-okhttp:1.0")

    // Discord
    implementation("net.dv8tion:JDA:5.0.0-alpha.5")
    implementation("com.github.minndevelopment:jda-ktx:6f70da7")

    // Spring
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.jsoup:jsoup:1.14.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Database remove soon
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:42.3.1")

    // Utils
    implementation("org.reflections:reflections:0.10.2")

    // Misc
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    implementation("ch.qos.logback:logback-classic:1.2.10")
    implementation("com.yahoofinance-api:YahooFinanceAPI:3.15.0")
}