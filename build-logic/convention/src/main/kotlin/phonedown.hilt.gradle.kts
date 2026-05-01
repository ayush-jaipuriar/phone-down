plugins {
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

dependencies {
    add("implementation", "com.google.dagger:hilt-android:2.57.2")
    add("ksp", "com.google.dagger:hilt-compiler:2.57.2")
}
