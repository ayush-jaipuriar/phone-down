plugins {
    id("phonedown.android.library")
}

android {
    namespace = "phonedown.core.auth"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
}
