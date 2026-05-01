plugins {
    id("phonedown.android.compose.library")
    alias(libs.plugins.paparazzi)
}

android {
    namespace = "phonedown.feature.focus"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":domain:session"))
}

tasks.withType<Test>().configureEach {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        },
    )
}
