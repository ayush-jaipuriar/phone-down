plugins {
    id("phonedown.android.compose.library")
}

android {
    namespace = "phonedown.feature.account"
}

dependencies {
    implementation(project(":core:auth"))
    implementation(project(":core:backup"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
}
