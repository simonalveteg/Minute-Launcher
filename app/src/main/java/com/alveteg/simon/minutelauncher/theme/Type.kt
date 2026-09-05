package com.alveteg.simon.minutelauncher.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
  displayLarge = TextStyle(
    fontFamily = archivoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 57.sp,
    lineHeight = 64.sp,
    letterSpacing = (-0.25).sp
  ),
  displayLargeEmphasized = TextStyle(
    fontFamily = archivoBlackFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 57.sp,
    lineHeight = 64.sp,
    letterSpacing = (-0.25).sp
  ),
  displayMedium = TextStyle(
    fontFamily = archivoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 45.sp,
    lineHeight = 52.sp,
    letterSpacing = 0.sp
  ),
  displayMediumEmphasized = TextStyle(
    fontFamily = archivoBlackFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 45.sp,
    lineHeight = 52.sp,
    letterSpacing = 0.sp
  ),
  displaySmall = TextStyle(
    fontFamily = archivoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    letterSpacing = 0.sp
  ),
  displaySmallEmphasized = TextStyle(
    fontFamily = archivoBlackFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    letterSpacing = 0.sp
  ),
  headlineLarge = TextStyle(
    fontFamily = archivoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp
  ),
  headlineLargeEmphasized = TextStyle(
    fontFamily = archivoBlackFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp
  ),
  headlineMedium = TextStyle(
    fontFamily = archivoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.sp
  ),
  headlineMediumEmphasized = TextStyle(
    fontFamily = archivoBlackFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.sp
  ),
  headlineSmall = TextStyle(
    fontFamily = archivoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp
  ),
  headlineSmallEmphasized = TextStyle(
    fontFamily = archivoBlackFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp
  ),
  titleLarge = TextStyle(
    fontFamily = archivoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
  ),
  titleLargeEmphasized = TextStyle(
    fontFamily = archivoBlackFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
  ),
  titleMedium = TextStyle(
    fontFamily = archivoFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp
  ),
  titleMediumEmphasized = TextStyle(
    fontFamily = archivoBlackFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp
  ),
  titleSmall = TextStyle(
    fontFamily = archivoFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
  ),
  titleSmallEmphasized = TextStyle(
    fontFamily = archivoBlackFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = archivoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
  ),
  bodyLargeEmphasized = TextStyle(
    fontFamily = archivoBlackFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = archivoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp
  ),
  bodyMediumEmphasized = TextStyle(
    fontFamily = archivoBlackFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp
  ),
  bodySmall = TextStyle(
    fontFamily = archivoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp
  ),
  bodySmallEmphasized = TextStyle(
    fontFamily = archivoBlackFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp
  ),
  labelLarge = TextStyle(
    fontFamily = archivoFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
  ),
  labelLargeEmphasized = TextStyle(
    fontFamily = archivoBlackFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
  ),
  labelMedium = TextStyle(
    fontFamily = archivoFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
  ),
  labelMediumEmphasized = TextStyle(
    fontFamily = archivoBlackFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
  ),
  labelSmall = TextStyle(
    fontFamily = archivoFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
  ),
  labelSmallEmphasized = TextStyle(
    fontFamily = archivoBlackFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
  )
)

@Preview(showBackground = true)
@Composable
fun TypographyPreview() {
  MinuteLauncherTheme {
    Surface {
      Column(
        modifier = Modifier
          .padding(16.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        TypographyRow("Display L", MaterialTheme.typography.displayLarge, MaterialTheme.typography.displayLargeEmphasized)
        TypographyRow("Display M", MaterialTheme.typography.displayMedium, MaterialTheme.typography.displayMediumEmphasized)
        TypographyRow("Display S", MaterialTheme.typography.displaySmall, MaterialTheme.typography.displaySmallEmphasized)
        TypographyRow("Headline L", MaterialTheme.typography.headlineLarge, MaterialTheme.typography.headlineLargeEmphasized)
        TypographyRow("Headline M", MaterialTheme.typography.headlineMedium, MaterialTheme.typography.headlineMediumEmphasized)
        TypographyRow("Headline S", MaterialTheme.typography.headlineSmall, MaterialTheme.typography.headlineSmallEmphasized)
        TypographyRow("Title L", MaterialTheme.typography.titleLarge, MaterialTheme.typography.titleLargeEmphasized)
        TypographyRow("Title M", MaterialTheme.typography.titleMedium, MaterialTheme.typography.titleMediumEmphasized)
        TypographyRow("Title S", MaterialTheme.typography.titleSmall, MaterialTheme.typography.titleSmallEmphasized)
        TypographyRow("Body L", MaterialTheme.typography.bodyLarge, MaterialTheme.typography.bodyLargeEmphasized)
        TypographyRow("Body M", MaterialTheme.typography.bodyMedium, MaterialTheme.typography.bodyMediumEmphasized)
        TypographyRow("Body S", MaterialTheme.typography.bodySmall, MaterialTheme.typography.bodySmallEmphasized)
        TypographyRow("Label L", MaterialTheme.typography.labelLarge, MaterialTheme.typography.labelLargeEmphasized)
        TypographyRow("Label M", MaterialTheme.typography.labelMedium, MaterialTheme.typography.labelMediumEmphasized)
        TypographyRow("Label S", MaterialTheme.typography.labelSmall, MaterialTheme.typography.labelSmallEmphasized)
      }
    }
  }
}

@Composable
private fun TypographyRow(label: String, style: TextStyle, emphasizedStyle: TextStyle) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = style,
      modifier = Modifier.weight(1f)
    )
    Text(
      text = label,
      style = emphasizedStyle,
      modifier = Modifier.weight(1f)
    )
  }
}