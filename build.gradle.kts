plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("com.diffplug.spotless") version "6.24.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.28")
    implementation("org.projectlombok:lombok:1.18.28")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("com.gurkab.fellowshipinterviewscheduler.Application")
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "com.gurkab.fellowshipinterviewscheduler.Application")
    }
}

tasks.test {
    useJUnitPlatform()
}

spotless {
    java {
        target("src/main/java/**/*.java")
        prettier(mapOf("prettier" to "2.8.1", "prettier-plugin-java" to "2.0.0")).config(mapOf("parser" to "java", "tabWidth" to 4, "printWidth" to 140))
        removeUnusedImports()
    }
}