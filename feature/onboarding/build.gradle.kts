plugins {
    id("phonedown.android.compose.library")
}

android {
    namespace = "phonedown.feature.onboarding"
}

dependencies {
    implementation(project(":core:designsystem"))
}
