plugins {
    id("phonedown.android.compose.library")
}

android {
    namespace = "phonedown.core.designsystem"
}

dependencies {
    implementation(project(":core:model"))
}
