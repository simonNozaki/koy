import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.3.20"
    id ("com.gradleup.shadow") version "8.3.6"
    jacoco
}

group = "io.github.simonnozaki"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

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
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}