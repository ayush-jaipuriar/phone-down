plugins {
    id("phonedown.android.library")
}

android {
    namespace = "phonedown.core.notifications"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.core.ktx)
}
