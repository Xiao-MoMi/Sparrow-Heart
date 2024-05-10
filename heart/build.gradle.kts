dependencies {
    implementation(project(":common"))
    implementation(project(":mojmap_R1"))
    implementation(project(mapOf("path" to ":reobf_1_20_R4", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_20_R3", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_20_R2", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_20_R1", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_19_R3", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_19_R2", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_19_R1", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_18_R2", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_18_R1", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_17_R1", "configuration" to "reobf")))
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