package com.alveteg.simon.minutelauncher.home.stats

import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMaxOfOrNull
import com.alveteg.simon.minutelauncher.data.UsageStatistics
import com.alveteg.simon.minutelauncher.data.sumOf
import com.alveteg.simon.minutelauncher.data.toTimeUsed
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberShowOnHover
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberShowOnPress
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.shape.toVicoShape
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer.MergeMode
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun UsageBarGraph(
  usageStatistics: List<UsageStatistics>,
  selectedDate: LocalDate? = null,
  onDateSelectionChange: (LocalDate?) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val modelProducer = remember { CartesianChartModelProducer() }
  val maxDuration = remember(usageStatistics) {
    usageStatistics.fastMaxOfOrNull { it.usageDuration } ?: 0.milliseconds
  }
  val dailyAverage = remember(usageStatistics) {
    usageStatistics.sumOf { it.usageDuration }.div(7)
  }

  val dateValueFormatter = CartesianValueFormatter { _, value, _ ->
    val date = LocalDate.now().minusDays(7.minus(value).toLong())
    date.format(DateTimeFormatter.ofPattern("EEE"))
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

  val labelTextComponent = rememberTextComponent(
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    padding = Insets(horizontalDp = 0f, verticalDp = 1f),
  )

  LaunchedEffect(usageStatistics) {
    withContext(Dispatchers.Default) {
      while (isActive) {
        modelProducer.runTransaction {
          columnSeries {
            series(
              x = usageStatistics.map {
                it.usageDate.toEpochDay() - LocalDate.now().toEpochDay() + 7
              },
              y = usageStatistics.map { it.usageDuration.inWholeMilliseconds }
            )
          }
        }
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
    if (maxDuration > 1.seconds) {
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
          modifier = Modifier
            .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 12.dp),
          chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
              columnProvider = rememberSelectionColumnProvider(selectedDate = selectedDate),
              columnCollectionSpacing = 4.dp,
              rangeProvider = CartesianLayerRangeProvider.fixed(
                minY = 0.0,
                maxY = maxDuration.inWholeMilliseconds * 1.15,
                minX = 1.0,
                maxX = 7.0
              ),
              mergeMode = { MergeMode.Stacked },
              dataLabel = labelTextComponent,
              dataLabelPosition = Position.Vertical.Top,
              dataLabelValueFormatter = CartesianValueFormatter { _, value, _ ->
                value.milliseconds.toTimeUsed()
              }
            ),
            getXStep = { 1.0 },
            bottomAxis = HorizontalAxis.rememberBottom(
              valueFormatter = dateValueFormatter,
              guideline = null,
              tick = rememberLineComponent(fill = Fill(MaterialTheme.colorScheme.outlineVariant.toArgb())),
              line = rememberLineComponent(fill = Fill(MaterialTheme.colorScheme.outlineVariant.toArgb())),
              label = rememberTextComponent(
                typeface = typeface,
                color = MaterialTheme.colorScheme.onSurface,
              )
            ),
            endAxis = VerticalAxis.rememberEnd(
              valueFormatter = { _, value, _ ->
                value.milliseconds.toTimeUsed()
              },
              guideline = rememberAxisGuidelineComponent(
                fill = Fill(MaterialTheme.colorScheme.background.toArgb()),
                shape = Shape.Rectangle,
                margins = Insets(topDp = 8f, bottomDp = 8f, startDp = 0f, endDp = 0f)
              ),
              line = rememberLineComponent(thickness = 0.dp),
              tick = rememberLineComponent(thickness = 0.dp),
              label = null
            ),
            marker = rememberDefaultCartesianMarker(
              label = labelTextComponent,
              labelPosition = DefaultCartesianMarker.LabelPosition.AroundPoint,
              valueFormatter = DefaultCartesianMarker.ValueFormatter { _, _ -> "" }
            ),
            markerVisibilityListener = object : CartesianMarkerVisibilityListener {
              override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                val x = targets.firstOrNull()?.x ?: return
                val date = LocalDate.now().minusDays(7 - x.toLong())
                onDateSelectionChange(date)
              }

              override fun onUpdated(
                marker: CartesianMarker,
                targets: List<CartesianMarker.Target>
              ) {}

              override fun onHidden(marker: CartesianMarker) {
                onDateSelectionChange(null)
              }
            },
            markerController = CartesianMarkerController.rememberShowOnPress()
          ),
          modelProducer = modelProducer,
          zoomState = rememberVicoZoomState(zoomEnabled = false),
          scrollState = rememberVicoScrollState(
            scrollEnabled = true,
            initialScroll = Scroll.Absolute.End
          ),
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
@Composable
private fun rememberSelectionColumnProvider(
  selectedDate: LocalDate?,
  thickness: Dp = 16.dp,
  shape: Shape = RoundedCornerShape(8.dp).toVicoShape(),
): ColumnCartesianLayer.ColumnProvider {
  val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
  val fadedColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

  val animatables = remember { List(7) { Animatable(1f) } }

  animatables.forEachIndexed { index, animatable ->
    val date = LocalDate.now().minusDays(7 - (index + 1).toLong())
    val target = if (selectedDate == null || date == selectedDate) 1f else 0f
    LaunchedEffect(target) {
      animatable.animateTo(target, animationSpec = spring())
    }
  }

  // Rebuild provider every frame by keying on current animated values
  val fractions = animatables.map { it.value }

  return remember(fractions) {
    object : ColumnCartesianLayer.ColumnProvider {
      override fun getColumn(
        entry: ColumnCartesianLayerModel.Entry,
        seriesIndex: Int,
        extraStore: ExtraStore,
      ): LineComponent {
        val index = entry.x.toInt() - 1  // x is 1..7
        val fraction = fractions.getOrElse(index) { 1f }
        val color = lerpArgb(fadedColor, primaryColor, fraction)
        return LineComponent(
          fill = Fill(color),
          thicknessDp = thickness.value,
          shape = shape,
        )
      }

      override fun getWidestSeriesColumn(
        seriesIndex: Int,
        extraStore: ExtraStore,
      ): LineComponent = LineComponent(
        fill = Fill(primaryColor),
        thicknessDp = thickness.value,
        shape = shape,
      )
    }
  }
}

private fun lerpArgb(from: Int, to: Int, fraction: Float): Int {
  fun channel(f: Int, t: Int) =
    ((f + (t - f) * fraction).roundToInt()).coerceIn(0, 255)
  return (channel((from ushr 24) and 0xFF, (to ushr 24) and 0xFF) shl 24) or
      (channel((from ushr 16) and 0xFF, (to ushr 16) and 0xFF) shl 16) or
      (channel((from ushr 8)  and 0xFF, (to ushr 8)  and 0xFF) shl 8)  or
      channel( from          and 0xFF,  to           and 0xFF)
}