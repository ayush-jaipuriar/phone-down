plugins {
    id("phonedown.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

dependencies {
    add("implementation", platform("androidx.compose:compose-bom:2026.04.01"))
    add("implementation", "androidx.compose.material3:material3")
    add("implementation", "androidx.compose.runtime:runtime")
    add("implementation", "androidx.compose.ui:ui")
    add("implementation", "androidx.compose.ui:ui-tooling-preview")
    add("debugImplementation", "androidx.compose.ui:ui-test-manifest")
    add("androidTestImplementation", platform("androidx.compose:compose-bom:2026.04.01"))
    add("androidTestImplementation", "androidx.activity:activity:1.12.0")
    add("androidTestImplementation", "androidx.compose.ui:ui-test-junit4")
    add("androidTestImplementation", "androidx.test.ext:junit:1.3.0")
    add("androidTestImplementation", "androidx.test:runner:1.7.0")
}
