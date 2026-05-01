plugins {
    id("phonedown.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

dependencies {
    add("implementation", platform("androidx.compose:compose-bom:2026.04.01"))
    add("implementation", "androidx.compose.material3:material3")
    add("implementation", "androidx.compose.runtime:runtime")
    add("implementation", "androidx.compose.ui:ui")
}
