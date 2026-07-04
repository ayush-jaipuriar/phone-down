import java.util.Properties

plugins {
    id("phonedown.android.application")
    id("com.google.gms.google-services")
    id("phonedown.hilt")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties =
    Properties().apply {
        if (keystorePropertiesFile.exists()) {
            keystorePropertiesFile.inputStream().use(::load)
        }
    }

fun signingValue(
    propertyName: String,
    environmentName: String,
): String? =
    keystoreProperties.getProperty(propertyName)
        ?: providers.environmentVariable(environmentName).orNull

val releaseStoreFile = signingValue("storeFile", "PHONE_DOWN_STORE_FILE")
val releaseStorePassword = signingValue("storePassword", "PHONE_DOWN_STORE_PASSWORD")
val releaseKeyAlias = signingValue("keyAlias", "PHONE_DOWN_KEY_ALIAS")
val releaseKeyPassword = signingValue("keyPassword", "PHONE_DOWN_KEY_PASSWORD")
val hasReleaseSigningConfig =
    listOf(
        releaseStoreFile,
        releaseStorePassword,
        releaseKeyAlias,
        releaseKeyPassword,
    ).all { !it.isNullOrBlank() }

android {
    namespace = "phonedown.app"

    defaultConfig {
        applicationId = "phonedown.app"
        versionCode = 4
        versionName = "1.0.3"
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigningConfig) {
                storeFile = file(requireNotNull(releaseStoreFile))
                storePassword = requireNotNull(releaseStorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

tasks
    .matching { task ->
        task.name in setOf(
            "assembleRelease",
            "bundleRelease",
            "packageReleaseBundle",
            "signReleaseBundle",
        )
    }.configureEach {
        doFirst {
            check(hasReleaseSigningConfig) {
                """
                Release signing is not configured.

                Copy keystore.properties.example to keystore.properties and fill it with the upload
                keystore path/passwords, or provide PHONE_DOWN_STORE_FILE, PHONE_DOWN_STORE_PASSWORD,
                PHONE_DOWN_KEY_ALIAS, and PHONE_DOWN_KEY_PASSWORD as environment variables.
                """.trimIndent()
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
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
