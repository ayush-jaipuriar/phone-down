plugins {
    id("phonedown.android.application")
    id("com.google.gms.google-services")
    id("phonedown.hilt")
}

android {
    namespace = "phonedown.app"

    defaultConfig {
        applicationId = "phonedown.app"
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // Use debug signing for now. For production, configure signingConfigs.release
            // with your keystore and update this to signingConfig = signingConfigs.release
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(project(":core:auth"))
    implementation(project(":core:backup"))
    implementation(project(":core:billing"))
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:notifications"))
    implementation(project(":core:sensors"))
    implementation(project(":domain:insights"))
    implementation(project(":domain:session"))
    implementation(project(":feature:account"))
    implementation(project(":feature:focus"))
    implementation(project(":feature:insights"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:pro"))
    implementation(project(":feature:settings"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.googleid)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
