plugins {
    id("io.papermc.paperweight.userdev") version "1.7.4"
}

dependencies {
    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:1.20.1-R0.1-SNAPSHOT")
    compileOnly(project(":common"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}