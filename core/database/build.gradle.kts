plugins {
    id("phonedown.android.library")
}

android {
    namespace = "phonedown.core.database"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
}
