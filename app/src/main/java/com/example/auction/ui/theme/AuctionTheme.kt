package com.example.auction.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Stadium Pro Palette
val EmeraldPrimary = Color(0xFF00E676)
val EmeraldDark = Color(0xFF008947)
val EmeraldContainer = Color(0xFF0A3320)
val OnEmeraldContainer = Color(0xFFB9F6CA)

val GoldAccent = Color(0xFFFFD54F)
val GoldContainer = Color(0xFF3E2E00)
val OnGoldContainer = Color(0xFFFFE082)

val CrimsonError = Color(0xFFFF5252)
val CrimsonContainer = Color(0xFF450A0A)

val DarkSurface = Color(0xFF0F1720)
val DarkSurfaceElevated = Color(0xFF16222F)
val DarkSurfaceVariant = Color(0xFF1E2E3E)
val DarkBackground = Color(0xFF0A1017)
val DarkOutline = Color(0xFF283D52)

val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF4F7F5)
val LightSurfaceVariant = Color(0xFFE4EDE7)
val LightBackground = Color(0xFFF0F5F2)
val LightOutline = Color(0xFFCAD8D0)

val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF00391A),
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = GoldAccent,
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = GoldContainer,
    onSecondaryContainer = OnGoldContainer,
    tertiary = Color(0xFF40C4FF),
    onTertiary = Color(0xFF00344F),
    background = DarkBackground,
    onBackground = Color(0xFFE2EBF2),
    surface = DarkSurface,
    onSurface = Color(0xFFE2EBF2),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF9AB2C7),
    outline = DarkOutline,
    outlineVariant = Color(0xFF1C2C3A),
    error = CrimsonError,
    onError = Color(0xFF490003),
    errorContainer = CrimsonContainer,
    onErrorContainer = Color(0xFFFFB4AB)
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007A3D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7F3D6),
    onPrimaryContainer = Color(0xFF00210C),
    secondary = Color(0xFF8A6200),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE082),
    onSecondaryContainer = Color(0xFF291C00),
    tertiary = Color(0xFF00668B),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF142019),
    surface = LightSurface,
    onSurface = Color(0xFF142019),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF3E5045),
    outline = LightOutline,
    outlineVariant = Color(0xFFDFE9E2),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

val AuctionShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

val AuctionTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 16.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 9.sp,
        lineHeight = 12.sp
    )
)

@Composable
fun AuctionTheme(
    darkTheme: Boolean = true, // Default to sleek stadium dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = AuctionShapes,
        typography = AuctionTypography,
        content = content
    )
}
