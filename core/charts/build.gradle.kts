plugins {
    id("phonedown.android.compose.library")
}

android {
    namespace = "phonedown.core.charts"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":domain:insights"))
}
