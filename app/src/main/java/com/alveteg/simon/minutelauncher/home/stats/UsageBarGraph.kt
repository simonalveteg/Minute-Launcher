package com.alveteg.simon.minutelauncher.home.stats

import android.graphics.Typeface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.data.UsageStatistics
import com.alveteg.simon.minutelauncher.data.toTimeUsed
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEndAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.VerticalPosition
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun UsageBarGraph(
  usageStatistics: List<UsageStatistics>
) {
  val modelProducer = remember { CartesianChartModelProducer() }
  val maxDuration = usageStatistics.maxOfOrNull { it.usageDuration } ?: 0L
  val dailyAverage = usageStatistics.sumOf { it.usageDuration }.div(7)

  val dateValueFormatter = CartesianValueFormatter { value, _, _ ->
    val date = LocalDate.now().minusDays(7.minus(value).toLong())
    date.format(DateTimeFormatter.ofPattern("EEE"))
  }
  val usageValueFormatter = CartesianValueFormatter { value, _, _ ->
    value.toLong().toTimeUsed()
  }

  val style = MaterialTheme.typography.bodySmall
  val resolver = LocalFontFamilyResolver.current
  val typeface = remember(resolver, style) {
    resolver.resolve(
      fontFamily = archivoFamily,
      fontWeight = style.fontWeight ?: FontWeight.Normal,
      fontStyle = style.fontStyle ?: FontStyle.Normal,
      fontSynthesis = style.fontSynthesis ?: FontSynthesis.All,
    )
  }.value as Typeface

  LaunchedEffect(Unit) {
    withContext(Dispatchers.Default) {
      while (isActive) {
        modelProducer.runTransaction {
          columnSeries {
            val dates =
              usageStatistics.map { it.usageDate.toEpochDay() - LocalDate.now().toEpochDay() + 7 }
            val durations = usageStatistics.map { it.usageDuration }
            series(y = durations, x = dates)
          }
        }
        delay(60000L)
      }
    }
  }
  Surface(
    modifier = Modifier
      .heightIn(min = 220.dp, max = 600.dp)
      .fillMaxWidth(),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    shape = MaterialTheme.shapes.large,
  ) {
    if (maxDuration > 0L) {
      Column {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 8.dp)
        ) {
          Text(
            text = "Daily usage",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = archivoBlackFamily
          )
          Text(
            text = "~${dailyAverage.toTimeUsed(expanded = true)}/day",
            style = MaterialTheme.typography.titleSmall,
            fontFamily = archivoFamily
          )
        }
        HorizontalDivider()
        CartesianChartHost(
          modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 12.dp),
          getXStep = { 1f },
          chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
              columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                rememberLineComponent(
                  color = MaterialTheme.colorScheme.primary,
                  thickness = 40.dp,
                  shape = remember { Shape.rounded(8f) }
                )
              ),
              spacing = 4.dp,
              axisValueOverrider = AxisValueOverrider.fixed(
                minY = 0f,
                maxY = maxDuration * 1.15f,
                minX = 1f,
                maxX = 7f
              ),
              dataLabel = rememberTextComponent(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                padding = Dimensions(horizontalDp = 0f, verticalDp = 1f),
              ),
              dataLabelVerticalPosition = VerticalPosition.Top,
              dataLabelValueFormatter = usageValueFormatter
            ),
            bottomAxis = rememberBottomAxis(
              valueFormatter = dateValueFormatter,
              guideline = null,
              tick = rememberLineComponent(color = MaterialTheme.colorScheme.outlineVariant),
              axis = rememberLineComponent(color = MaterialTheme.colorScheme.outlineVariant),
              label = rememberTextComponent(
                typeface = typeface,
                color = MaterialTheme.colorScheme.onSurface
              )
            ),
            endAxis = rememberEndAxis(
              valueFormatter = { value, _, _ ->
                value.toLong().toTimeUsed()
              },
              guideline = rememberAxisGuidelineComponent(
                color = MaterialTheme.colorScheme.background,
                shape = Shape.Rectangle,
                margins = Dimensions(topDp = 8f, bottomDp = 8f, startDp = 0f, endDp = 0f)
              ),
              axis = rememberLineComponent(thickness = 0.dp),
              tick = rememberLineComponent(thickness = 0.dp),
              label = null
            )
          ),
          modelProducer = modelProducer,
          zoomState = rememberVicoZoomState(zoomEnabled = false),
          scrollState = rememberVicoScrollState(scrollEnabled = false)
        )
      }
    } else {
      Text(
        text = "No recent usage found.",
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
          .padding(bottom = 8.dp),
        textAlign = TextAlign.Center,
      )
    }
  }
}
