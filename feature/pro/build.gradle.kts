plugins {
    id("phonedown.android.compose.library")
}

android {
    namespace = "phonedown.feature.pro"
}

dependencies {
    implementation(project(":core:billing"))
    implementation(project(":core:designsystem"))
}
