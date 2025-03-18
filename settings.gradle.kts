rootProject.name = "Sparrow-Heart"
include(":heart")
include(":common")
include(":plugin")
include(":reobf_1_21_R3")
include(":reobf_1_21_R2")
include(":reobf_1_21_R1")
include(":reobf_1_20_R4")
include(":reobf_1_20_R3")
include(":reobf_1_20_R2")
include(":reobf_1_20_R1")
include(":reobf_1_19_R3")
include(":reobf_1_19_R2")
include(":reobf_1_19_R1")
include(":reobf_1_18_R2")
include(":reobf_1_18_R1")
include(":reobf_1_17_R1")
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}