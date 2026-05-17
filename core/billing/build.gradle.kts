plugins {
    id("phonedown.android.library")
}

android {
    namespace = "phonedown.core.billing"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.google.play.billing.ktx)
    implementation(libs.kotlinx.coroutines.core)
}
