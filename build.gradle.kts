plugins {
    kotlin("jvm") version "2.2.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    // Source: https://mvnrepository.com/artifact/org.json/json
    implementation("org.json:json:20200518")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}