plugins {
    id("java")
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass = "org.example.Main"; // <- For Java, not MainKt
}

// 👇 Define your 'server' task
tasks.register<JavaExec>("server") {
    group = "application"
    description = "Runs the server"
    mainClass.set("server.Server")
    classpath = sourceSets.main.get().runtimeClasspath
    standardInput = System.`in`
}

// 👇 Define your 'client' task
tasks.register<JavaExec>("client") {
    group = "application"
    description = "Runs the client"
    mainClass.set("client.Client")
    classpath = sourceSets.main.get().runtimeClasspath
    standardInput = System.`in`
}

tasks.test {
    useJUnitPlatform()
}