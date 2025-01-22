import io.papermc.paperweight.userdev.PaperweightUserDependenciesExtension

plugins {
    id("io.papermc.paperweight.userdev") version "1.7.4"
}

dependencies {
    the<PaperweightUserDependenciesExtension>().paperDevBundle("1.21.4-R0.1-20241215.095037-18")
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

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION