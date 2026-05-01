package phonedown.core.model

enum class ThemeMode {
    System,
    Light,
    Dark,
}

fun ThemeMode.shouldUseDarkTheme(systemInDarkTheme: Boolean): Boolean =
    when (this) {
        ThemeMode.System -> systemInDarkTheme
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
