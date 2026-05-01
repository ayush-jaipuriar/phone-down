plugins {
    id("phonedown.android.library")
}

android {
    namespace = "phonedown.core.datastore"
}

dependencies {
    implementation(project(":core:model"))
}
