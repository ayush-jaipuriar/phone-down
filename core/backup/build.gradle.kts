plugins {
    id("phonedown.android.library")
}

android {
    namespace = "phonedown.core.backup"
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
}
