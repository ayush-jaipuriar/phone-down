plugins {
    id("phonedown.android.library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:model"))
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}

android {
    namespace = "phonedown.core.backup"
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
}
