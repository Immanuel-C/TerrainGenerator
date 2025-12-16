plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

val lwjglVersion = "3.3.0"
val jomlVersion = "1.10.8"
val `lwjgl3-awtVersion` = "0.1.8"

val lwjglNatives = Pair(
    System.getProperty("os.name")!!,
    System.getProperty("os.arch")!!
).let { (name, arch) ->
    when {
        arrayOf("Linux", "SunOS", "Unit").any { name.startsWith(it) } ->
            "natives-linux"
        arrayOf("Windows").any { name.startsWith(it) }                ->
            "natives-windows"
        else                                                                            ->
            throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
    }
}


repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots")
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-jawt")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation ("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    implementation ("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    implementation("org.joml", "joml", jomlVersion)
    implementation("org.lwjglx", "lwjgl3-awt", `lwjgl3-awtVersion`)
}
