plugins {
    id("io.papermc.paperweight.userdev") version "1.5.15"
}

dependencies {
    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:1.19.4-R0.1-SNAPSHOT")
    compileOnly(project(":common"))
}