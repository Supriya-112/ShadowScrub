plugins {
    id("buildlogic.java-application-conventions")
}

dependencies {
    implementation(project(":shadowscrub-core"))
    implementation(libs.picocli)

    testImplementation(libs.assertj)
}

application {
    mainClass = "com.shadowscrub.cli.ShadowScrubCommand"
}
