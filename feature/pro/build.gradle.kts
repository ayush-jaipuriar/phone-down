plugins {
    id("phonedown.android.compose.library")
}

android {
    namespace = "phonedown.feature.pro"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
}
