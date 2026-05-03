plugins {
    id("phonedown.android.compose.library")
    alias(libs.plugins.paparazzi)
}

android {
    namespace = "phonedown.feature.onboarding"
}

dependencies {
    implementation(project(":core:designsystem"))
    testImplementation(project(":core:model"))
}

tasks.withType<Test>().configureEach {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        },
    )
}
