plugins {
    // This allows us to share versions across modules
    java
}

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
        testImplementation("org.assertj:assertj-core:3.24.2")
    }

    tasks.test {
        useJUnitPlatform()
    }
}