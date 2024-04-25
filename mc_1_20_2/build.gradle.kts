plugins {
    id("io.papermc.paperweight.userdev") version "1.5.15"
}

dependencies {
    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:1.20.2-R0.1-SNAPSHOT")
    compileOnly(project(":common"))
}