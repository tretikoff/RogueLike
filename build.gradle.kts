import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.jvm.tasks.Jar

plugins {
    id("application")
    id("org.jetbrains.kotlin.jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
}

group = "me.professional"
version = "1.0-SNAPSHOT"
val ktor_version = "1.5.3"

tasks {
    "build" {
        dependsOn(fatJar)
    }
}


repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
    google()
    maven(url= "https://oss.sonatype.org/content/groups/public")
    maven(url ="https://jitpack.io")
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
    implementation("org.slf4j:slf4j-nop:1.7.30")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    implementation(kotlin("stdlib"))
    api("io.github.libktx:ktx-assets:1.9.12-b1")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "com.bomjRogue.Server"
    }
}

val fatJar = task("fatJar", type = Jar::class) {
    manifest {
        attributes["Main-Class"] = "com.bomjRogue.Server"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}


tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}