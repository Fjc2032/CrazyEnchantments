plugins {
    `config-java`
}

project.group = "${rootProject.group}.common"

dependencies {
    implementation(libs.fusion.core)
}