plugins {
    id("phonedown.android.library")
}

android {
    namespace = "phonedown.core.sensors"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
}
