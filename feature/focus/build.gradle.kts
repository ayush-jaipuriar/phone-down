plugins {
    id("phonedown.android.compose.library")
}

android {
    namespace = "phonedown.feature.focus"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":domain:session"))
}
