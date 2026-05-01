plugins {
    id("phonedown.android.compose.library")
}

android {
    namespace = "phonedown.feature.settings"
}

dependencies {
    implementation(project(":core:designsystem"))
}
