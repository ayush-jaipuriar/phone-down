plugins {
    id("phonedown.android.compose.library")
}

android {
    namespace = "phonedown.feature.insights"
}

dependencies {
    implementation(project(":core:charts"))
    implementation(project(":core:designsystem"))
    implementation(project(":domain:insights"))
}
