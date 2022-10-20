import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    id ("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "io.github.simonnozaki"
version = "0.0.1"


repositories {
    mavenCentral()
}

dependencies {
    implementation("org.javafp:parsecj:0.6")
    testImplementation(kotlin("test"))
}

tasks.withType(Jar::class.java) {
    // https://docs.gradle.org/7.3.3/dsl/org.gradle.api.tasks.Copy.html#org.gradle.api.tasks.Copy:duplicatesStrategy
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "io.github.simonnozaki.koy.KoyLangKt"
    }
    val archives = configurations.compileClasspath.get().map {
        if (it.isDirectory) {
            it
        } else {
            zipTree(it)
        }
    }
    from(archives)
    archiveFileName.set("koy.jar")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}