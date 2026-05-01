import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    id("org.jetbrains.kotlin.jvm")
}

extensions.configure<KotlinJvmProjectExtension> {
    jvmToolchain(17)
}

dependencies {
    add("testImplementation", "junit:junit:4.13.2")
}
