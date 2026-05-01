plugins {
    id("phonedown.android.library")
}

android {
    namespace = "phonedown.core.billing"
}

dependencies {
    implementation(project(":core:model"))
}
