plugins {
    id("java")
    id("application")
}

application {
    mainClass.set("terrain_generator.Main")
}

group = "org.example"
version = "1.0-SNAPSHOT"

val joglVersion = "2.6.0"
val jomlVersion = "1.10.8"

repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots")
    maven("https://jogamp.org/deployment/maven")
}

dependencies {
    runtimeOnly("org.jogamp.jogl:jogl-all:2.6.0:natives-windows-amd64")
    runtimeOnly("org.jogamp.gluegen:gluegen-rt:2.6.0:natives-windows-amd64")
    implementation("org.jogamp.jogl:jogl-all:2.6.0")
    implementation("org.jogamp.gluegen:gluegen-rt:2.6.0")
    implementation("org.joml:joml:1.10.8")
}
