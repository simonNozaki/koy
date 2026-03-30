plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":"))

    implementation("io.ktor:ktor-server-netty:3.4.2")
    implementation("io.ktor:ktor-server-content-negotiation:3.4.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.2")
    implementation("io.ktor:ktor-server-cors:3.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("ch.qos.logback:logback-classic:1.5.32")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:3.4.2")
}

tasks.test {
    useJUnitPlatform()
    dependsOn(":shadowJar")
}

tasks.named("startScripts") {
    dependsOn(":shadowJar")
}

application {
    mainClass.set("PlaygroundServerKt")
}
