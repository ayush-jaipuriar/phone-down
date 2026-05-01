package phonedown.domain.session

data class SessionRuleConfig(
    val armingDurationMillis: Long = 3_000L,
    val interruptionGracePeriodMillis: Long = 5_000L,
    val brokenInterruptionDurationMillis: Long = 60_000L,
    val penaltySecondsIncrement: Long = 60L,
    val brokenPenaltyInterruptions: Int = 3,
)
