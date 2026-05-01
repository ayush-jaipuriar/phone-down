plugins {
    id("phonedown.kotlin.library")
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))

    testImplementation(libs.kotlinx.coroutines.test)
}
