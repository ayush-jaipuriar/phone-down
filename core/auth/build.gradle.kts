plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "phonedown.core.auth"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }
}

kotlin {
    jvmToolchain(17)
}
