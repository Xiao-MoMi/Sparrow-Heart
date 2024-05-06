//plugins {
//    id("io.papermc.paperweight.userdev") version "1.7.0"
//}

repositories {
    maven("https://libraries.minecraft.net/")
}

dependencies {
//    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
    compileOnly(files("libs/1.20.6.jar"))
    compileOnly(files("libs/datafixerupper-7.0.14.jar"))
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly(project(":common"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

//paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION