import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id ("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "io.github.simonnozaki"
version = "0.0.1"

tasks.withType(Jar::class.java) {
    manifest {
        attributes["Main-Class"] = "io.github.simonnozaki.koy.KoyLang"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.javafp:parsecj:0.6")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}