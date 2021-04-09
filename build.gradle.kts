import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("application")
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
}

group = "me.professional"
version = "1.0-SNAPSHOT"
val ktor_version = "1.5.3"

tasks.withType<Jar> {
    manifest {
        attributes("Main-Class" to "io.ktor.server.netty.EngineMain")
    }
//    from { configurations.compile.fileCollection { if (it.isDirectory()) it else zipTree(it) }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit"))
    implementation("com.badlogicgames.gdx:gdx:1.9.12")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl:1.9.12")
    implementation("com.badlogicgames.gdx:gdx-platform:1.9.12:natives-desktop")
    implementation("io.github.libktx:ktx-app:1.9.12-b1")
    implementation("io.github.libktx:ktx-graphics:1.9.12-b1")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("io.ktor:ktor-websockets:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-gson:$ktor_version")
    implementation("io.ktor:ktor-gson:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-client-apache:$ktor_version")
    implementation("com.squareup.okhttp3:mockwebserver:4.9.0")
    implementation("org.slf4j:slf4j-nop:1.7.30")
//    implementation("ch.qos.logback:logback-classic:$logback_version"
    implementation("io.ktor:ktor-serialization:$ktor_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    api("io.github.libktx:ktx-assets:1.9.12-b1")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}