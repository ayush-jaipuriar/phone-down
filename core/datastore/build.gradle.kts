plugins {
    id("phonedown.android.library")
    id("phonedown.hilt")
}

android {
    namespace = "phonedown.core.datastore"
}

dependencies {
    implementation(project(":core:model"))
    api(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
