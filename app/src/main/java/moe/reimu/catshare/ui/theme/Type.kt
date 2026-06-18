package moe.reimu.catshare.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val GoogleSans = try {
    FontFamily(Font(DeviceFontFamilyName("google-sans")))
} catch (_: Throwable) {
    FontFamily.Default
}

private fun googleSansStyle(
    fontWeight: FontWeight = FontWeight.Normal,
    fontSize: Int,
    lineHeight: Int,
): TextStyle {
    return TextStyle(
        fontFamily = GoogleSans,
        fontWeight = fontWeight,
        fontSize = fontSize.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = 0.sp,
    )
}

val Typography = Typography(
    displaySmall = googleSansStyle(FontWeight.Normal, 36, 44),
    headlineSmall = googleSansStyle(FontWeight.Normal, 24, 32),
    titleLarge = googleSansStyle(FontWeight.Normal, 22, 28),
    titleMedium = googleSansStyle(FontWeight.Medium, 16, 24),
    titleSmall = googleSansStyle(FontWeight.Medium, 14, 20),
    bodyLarge = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = googleSansStyle(FontWeight.Normal, 14, 20),
    bodySmall = googleSansStyle(FontWeight.Normal, 12, 16),
    labelLarge = googleSansStyle(FontWeight.Medium, 14, 20),
    labelMedium = googleSansStyle(FontWeight.Medium, 12, 16),
    labelSmall = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
)
