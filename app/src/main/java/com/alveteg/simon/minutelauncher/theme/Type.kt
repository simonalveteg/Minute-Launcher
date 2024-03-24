package com.alveteg.simon.minutelauncher.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.alveteg.simon.minutelauncher.R

val provider = GoogleFont.Provider(
  providerAuthority = "com.google.android.gms.fonts",
  providerPackage = "com.google.android.gms",
  certificates = R.array.com_google_android_gms_fonts_certs
)

val archivo = GoogleFont("Archivo")
val archivoBlack = GoogleFont("Archivo Black")

val archivoFamily = FontFamily(
  Font(
    googleFont = archivo,
    fontProvider = provider,
    weight = FontWeight.Normal,
    style = FontStyle.Normal
  )
)
val archivoBlackFamily = FontFamily(
  Font(
    googleFont = archivoBlack,
    fontProvider = provider,
    weight = FontWeight.Normal,
    style = FontStyle.Normal
  )
)

// Set of Material typography styles to start with
val Typography = Typography(
  bodyLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
  )
  /* Other default text styles to override
  titleLarge = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 22.sp,
      lineHeight = 28.sp,
      letterSpacing = 0.sp
  ),
  labelSmall = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Medium,
      fontSize = 11.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.5.sp
  )
  */
)

@Preview
@Composable
fun TypographyPreview() {
  Text(
    text = "Display",
    style = MaterialTheme.typography.displayLarge
  )
  Text(
    text = "Display",
    style = MaterialTheme.typography.displayMedium
  )
  Text(
    text = "Display",
    style = MaterialTheme.typography.displaySmall
  )
  Text(
    text = "Headline",
    style = MaterialTheme.typography.headlineLarge
  )
  Text(
    text = "Headline",
    style = MaterialTheme.typography.headlineMedium
  )
  Text(
    text = "Headline",
    style = MaterialTheme.typography.headlineSmall
  )
  Text(
    text = "Title",
    style = MaterialTheme.typography.titleLarge
  )
  Text(
    text = "Title",
    style = MaterialTheme.typography.titleMedium
  )
  Text(
    text = "Title",
    style = MaterialTheme.typography.titleSmall
  )
  Text(
    text = "Display",
    style = MaterialTheme.typography.displayLarge
  )
  Text(
    text = "Display",
    style = MaterialTheme.typography.displayMedium
  )
  Text(
    text = "Display",
    style = MaterialTheme.typography.displaySmall
  )
}