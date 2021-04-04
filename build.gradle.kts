import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
//sourceSets.main.resources.srcDirs = ["../android/assets"]
//project.ext.assetsDir = new File("../android/assets")
plugins {
    kotlin("jvm") version "1.4.31"
}

group = "me.professional"
version = "1.0-SNAPSHOT"

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
    api( "io.github.libktx:ktx-assets:1.9.12-b1")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}