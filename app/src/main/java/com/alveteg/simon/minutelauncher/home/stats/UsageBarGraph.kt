package com.alveteg.simon.minutelauncher.home.stats

import android.graphics.Typeface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMaxOfOrNull
import androidx.compose.ui.util.fastSumBy
import com.alveteg.simon.minutelauncher.data.UsageStatistics
import com.alveteg.simon.minutelauncher.data.toTimeUsed
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.shape.toVicoShape
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer.ColumnProvider.Companion.series
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun UsageBarGraph(
  usageStatistics: List<UsageStatistics>,
  modifier: Modifier = Modifier
) {
  val modelProducer = remember { CartesianChartModelProducer() }
  val maxDuration = remember(usageStatistics) {
    usageStatistics.fastMaxOfOrNull { it.usageDuration } ?: 0L
  }
  val dailyAverage = remember(usageStatistics) {
    usageStatistics.fastSumBy { it.usageDuration.toInt() }.div(7).toLong()
  }

  val dateValueFormatter = CartesianValueFormatter { _, value, _ ->
    val date = LocalDate.now().minusDays(7.minus(value).toLong())
    date.format(DateTimeFormatter.ofPattern("EEE"))
  }
  val usageValueFormatter = CartesianValueFormatter { _, value, _ ->
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
    modifier = modifier
      .height(240.dp)
      .fillMaxWidth(),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    shape = MaterialTheme.shapes.large,
  ) {
    if (maxDuration > 0L) {
      Column {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 8.dp)
        ) {
          Text(
            text = "Daily usage",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = archivoBlackFamily,
            modifier = Modifier.alignByBaseline()
          )
          Text(
            text = "~${dailyAverage.toTimeUsed(expanded = true)}/day",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = archivoFamily,
            modifier = Modifier.alignByBaseline()
          )
        }
        HorizontalDivider()
        CartesianChartHost(
          modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 12.dp),
          chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
              columnProvider = series(
                rememberLineComponent(
                  fill = Fill(MaterialTheme.colorScheme.primary.toArgb()),
                  thickness = 40.dp,
                  shape = RoundedCornerShape(8.dp).toVicoShape()
                )
              ),
              columnCollectionSpacing = 4.dp,
              rangeProvider = CartesianLayerRangeProvider.fixed(
                minY = 0.0,
                maxY = maxDuration * 1.15,
                minX = 1.0,
                maxX = 7.0
              ),
              dataLabel = rememberTextComponent(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                padding = Insets(horizontalDp = 0f, verticalDp = 1f),
              ),
              dataLabelPosition = Position.Vertical.Top,
              dataLabelValueFormatter = usageValueFormatter
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
              valueFormatter = dateValueFormatter,
              guideline = null,
              tick = rememberLineComponent(fill = Fill(MaterialTheme.colorScheme.outlineVariant.toArgb())),
              line = rememberLineComponent(fill = Fill(MaterialTheme.colorScheme.outlineVariant.toArgb())),
              label = rememberTextComponent(
                typeface = typeface,
                color = MaterialTheme.colorScheme.onSurface
              )
            ),
            endAxis = VerticalAxis.rememberEnd(
              valueFormatter = { _, value, _ ->
                value.toLong().toTimeUsed()
              },
              guideline = rememberAxisGuidelineComponent(
                fill = Fill(MaterialTheme.colorScheme.background.toArgb()),
                shape = Shape.Rectangle,
                margins = Insets(topDp = 8f, bottomDp = 8f, startDp = 0f, endDp = 0f)
              ),
              line = rememberLineComponent(thickness = 0.dp),
              tick = rememberLineComponent(thickness = 0.dp),
              label = null
            )
          ),
          modelProducer = modelProducer,
          zoomState = rememberVicoZoomState(zoomEnabled = false),
          scrollState = rememberVicoScrollState(scrollEnabled = false),
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
