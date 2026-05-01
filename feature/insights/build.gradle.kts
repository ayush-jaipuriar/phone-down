plugins {
    id("phonedown.android.compose.library")
    alias(libs.plugins.paparazzi)
}

android {
    namespace = "phonedown.feature.insights"
}

dependencies {
    implementation(project(":core:charts"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":domain:insights"))
}

tasks.withType<Test>().configureEach {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        },
    )
}
