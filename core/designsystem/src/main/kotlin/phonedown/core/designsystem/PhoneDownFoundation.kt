package phonedown.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object PhoneDownSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val screen = 20.dp
    val card = 16.dp
}

object PhoneDownSize {
    val minTouchTarget = 48.dp
    val timerRing = 190.dp
    val compactTimerRing = 168.dp
    val bottomBarHeight = 64.dp
}

val PhoneDownShapes =
    Shapes(
        extraSmall = RoundedCornerShape(6.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(8.dp),
        large = RoundedCornerShape(8.dp),
        extraLarge = RoundedCornerShape(8.dp),
    )

val PhoneDownButtonShape = RoundedCornerShape(percent = 50)

val PhoneDownTimerTextStyle =
    TextStyle(
        fontSize = 52.sp,
        lineHeight = 60.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = (-0.5).sp,
    )

val PhoneDownScreenTitleTextStyle =
    TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
    )

val PhoneDownSectionHeaderTextStyle =
    TextStyle(
        fontSize = 17.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
    )

val PhoneDownCardHeaderTextStyle =
    TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    )

val PhoneDownTypography =
    Typography(
        displayLarge =
            TextStyle(
                fontSize = 44.sp,
                lineHeight = 52.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.sp,
            ),
        headlineMedium =
            TextStyle(
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp,
            ),
        titleMedium =
            TextStyle(
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp,
            ),
        bodyLarge =
            TextStyle(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.sp,
            ),
        bodyMedium =
            TextStyle(
                fontSize = 13.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.sp,
            ),
        labelLarge =
            TextStyle(
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp,
            ),
        labelMedium =
            TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.sp,
            ),
        labelSmall =
            TextStyle(
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp,
            ),
    )
