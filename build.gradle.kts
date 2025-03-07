plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
}

group = "com.suryadigital.training.codereviewtool"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.typesafe:config:1.4.2")
    }
}

//application {
//    mainClass.set("com.suryadigital.training.application.ApplicationKt")
//}

sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
        kotlin {
            srcDirs("src/main/kotlin")
        }
        resources {
            srcDirs("src/main/resources")
        }
    }
    test {
        java {
            srcDirs("src/test/java")
        }
        kotlin {
            srcDirs("src/test/kotlin")
        }
        resources {
            srcDirs("src/test/resources")
        }
    }
}

dependencies {
    // Ktor dependencies
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // Koin dependency
    implementation("io.insert-koin:koin-core:3.5.3")

    // Typesafe Config dependency
    implementation("com.typesafe:config:1.4.2")

    // Kotlin Test dependencies
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:2.3.7")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}