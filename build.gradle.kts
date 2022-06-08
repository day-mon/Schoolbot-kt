// https://spring.io/guides/tutorials/spring-boot-kotlin/

application {
    mainClass.set("me.damon.schoolbot.SchoolbotKt")
}



plugins {
    // spring plugins
    kotlin("plugin.spring") version "1.6.21"
    // he default configuration declares the kotlin-spring plugin which automatically opens classes and methods (unlike in Java, the default qualifier is final in Kotlin) annotated or meta-annotated with Spring annotations.
    id("org.springframework.boot") version "2.6.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    id("com.github.johnrengelman.shadow") version "7.1.1"
    kotlin("jvm") version "1.6.21"
    application

    id("org.jetbrains.kotlin.plugin.noarg") version ("1.6.10")
    id("org.jetbrains.kotlin.plugin.serialization") version("1.4.30")

}

project.setProperty("mainClassName", "me.damon.schoolbot.SchoolbotKt")
group = "com.github.day-mon"

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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation(kotlin("stdlib-jdk8"))


    // Discord
    implementation("net.dv8tion:JDA:5.0.0-alpha.11")
    implementation("com.github.minndevelopment:jda-ktx:78e74bc")

    // Spring
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.jsoup:jsoup:1.14.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Database
    implementation("org.postgresql:postgresql:42.3.4")

    // Misc
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("com.yahoofinance-api:YahooFinanceAPI:3.16.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.9.3"))
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

}
