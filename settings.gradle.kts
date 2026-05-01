pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PhoneDown"

include(":app")

include(":core:common")
include(":core:model")
include(":core:designsystem")
include(":core:database")
include(":core:datastore")
include(":core:sensors")
include(":core:notifications")
include(":core:billing")
include(":core:auth")
include(":core:backup")
include(":core:charts")

include(":domain:session")
include(":domain:insights")

include(":feature:onboarding")
include(":feature:focus")
include(":feature:insights")
include(":feature:settings")
include(":feature:account")
include(":feature:pro")
