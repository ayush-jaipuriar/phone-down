package phonedown.app.navigation

sealed interface PhoneDownRoute {
    val path: String

    data object Onboarding : PhoneDownRoute {
        override val path = "onboarding"
    }

    data object Focus : PhoneDownRoute {
        override val path = "focus"
    }

    data object Insights : PhoneDownRoute {
        override val path = "insights"
    }

    data object Settings : PhoneDownRoute {
        override val path = "settings"
    }

    data object Account : PhoneDownRoute {
        override val path = "account"
    }

    data object Pro : PhoneDownRoute {
        override val path = "pro"
    }

    data object PrivacyPolicy : PhoneDownRoute {
        override val path = "privacy_policy"
    }
}
