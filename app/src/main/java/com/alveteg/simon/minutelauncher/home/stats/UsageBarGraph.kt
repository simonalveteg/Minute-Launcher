package com.alveteg.simon.minutelauncher.home.stats

import android.graphics.Typeface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.data.UsageStatistics
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed
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
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.max

@Composable
fun UsageBarGraph(
  usageStatistics: List<UsageStatistics>
) {
  val sortedStats = remember(usageStatistics) {
    usageStatistics.sortedBy { it.usageDate }
  }
  val maxDuration = remember(sortedStats) {
    sortedStats.maxOfOrNull { it.usageDuration } ?: 0L
  }
  val yAxisValues = remember { mutableListOf<Float>() }
  var yMaxValue by remember { mutableLongStateOf(0L) }
  val modelProducer = remember { CartesianChartModelProducer() }
  val dates =
    sortedStats.map { it.usageDate.toEpochDay() - LocalDate.now().toEpochDay() + 7 }
  val durations = sortedStats.map { it.usageDuration }

  LaunchedEffect(usageStatistics) {
    if (usageStatistics.isEmpty()) return@LaunchedEffect
    val timeStep =
      if (maxDuration < 5) 1 else if (maxDuration < 10) 2 else if (maxDuration < 50) 10 else 60
    yMaxValue = maxDuration.plus(maxDuration.mod(timeStep)).plus(timeStep)
    (0..yMaxValue step timeStep.toLong()).forEach {
      yAxisValues.add(it.toFloat())
    }
    Timber.d("Calculating graph values: \n -- MaxValue: $yMaxValue, timeStep: $timeStep, values: $yAxisValues")
  }

  Surface(
    modifier = Modifier
      .height(220.dp)
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    color = MaterialTheme.colorScheme.background,
    shape = MaterialTheme.shapes.large,
    tonalElevation = 8.dp
  ) {
    if (usageStatistics.isNotEmpty()) {
      UsageColumnChart(
        modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 6.dp, bottom = 8.dp),
        modelProducer = modelProducer,
        maxValue = yMaxValue.toFloat(),
        yAxisValues = yAxisValues,
        xValues = dates,
        yValues = durations
      )
    } else {
      Text(
        text = "No recent usage found.",
        modifier = Modifier
          .fillMaxSize()
          .wrapContentHeight()
          .padding(bottom = 8.dp),
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
fun UsageColumnChart(
  modifier: Modifier,
  modelProducer: CartesianChartModelProducer,
  maxValue: Float,
  yAxisValues: List<Float>,
  xValues: List<Long>,
  yValues: List<Long>
) {
  LaunchedEffect(maxValue) {
    if (maxValue == 0f) return@LaunchedEffect
    withContext(Dispatchers.Default) {
      while (isActive) {
        Timber.d("Running transaction with maxValue: $maxValue \n -- yAxisValues: $yAxisValues \n -- durations: $yValues, \n -- dates: $xValues")
        modelProducer.runTransaction {
          columnSeries {
            series(xValues, yValues)
          }
        }
        delay(TimeUnit.MINUTES.toMillis(1))
      }
    }
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
  CartesianChartHost(
    modifier = modifier,
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
          maxY = maxValue,
          minX = 1f,
          maxX = 7f
        ),
      ),
      bottomAxis = rememberBottomAxis(
        valueFormatter = { value, _, _ ->
          val date = LocalDate.now().minusDays(7.minus(value).toLong())
          date.format(DateTimeFormatter.ofPattern("EEE"))
        },
        guideline = null,
        tick = null,
        axis = rememberLineComponent(color = MaterialTheme.colorScheme.background),
        label = rememberTextComponent(
          typeface = typeface,
          color = LocalContentColor.current
        )
      ),
      endAxis = rememberEndAxis(
        valueFormatter = { value, _, _ ->
          value.toLong().toTimeUsed()
        },
        itemPlacer = remember { VerticalPlacer(yAxisValues) },
        guideline = rememberAxisGuidelineComponent(
          color = MaterialTheme.colorScheme.background,
          shape = Shape.Rectangle
        ),
        axis = rememberLineComponent(thickness = 0.dp),
        tick = rememberLineComponent(thickness = 0.dp),
        label = rememberTextComponent(
          typeface = typeface,
          color = LocalContentColor.current
        )
      )
    ),
    modelProducer = modelProducer,
    zoomState = rememberVicoZoomState(zoomEnabled = false),
    scrollState = rememberVicoScrollState(scrollEnabled = false)
  )
}

class VerticalPlacer(
  private val values: List<Float>,
  private val shiftTopLines: Boolean = false
) : AxisItemPlacer.Vertical {

  override fun getBottomVerticalAxisInset(
    context: CartesianMeasureContext,
    verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
    maxLabelHeight: Float,
    maxLineThickness: Float
  ): Float = when (verticalLabelPosition) {
    VerticalAxis.VerticalLabelPosition.Top -> maxLineThickness
    VerticalAxis.VerticalLabelPosition.Center ->
      (maxOf(maxLabelHeight, maxLineThickness) + maxLineThickness).div(2)

    else -> maxLabelHeight + maxLineThickness.div(2)
  }

  override fun getHeightMeasurementLabelValues(
    context: CartesianMeasureContext,
    position: AxisPosition.Vertical
  ): List<Float> = values

  override fun getLabelValues(
    context: CartesianDrawContext,
    axisHeight: Float,
    maxLabelHeight: Float,
    position: AxisPosition.Vertical
  ): List<Float> = values

  override fun getTopVerticalAxisInset(
    context: CartesianMeasureContext,
    verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
    maxLabelHeight: Float,
    maxLineThickness: Float
  ): Float = when (verticalLabelPosition) {
    VerticalAxis.VerticalLabelPosition.Top ->
      maxLabelHeight + (if (shiftTopLines) maxLineThickness else -maxLineThickness).div(2)

    VerticalAxis.VerticalLabelPosition.Center ->
      (max(maxLabelHeight, maxLineThickness) +
          if (shiftTopLines) maxLineThickness else -maxLineThickness)
        .div(2)

    else -> if (shiftTopLines) maxLineThickness else 0f
  }

  override fun getWidthMeasurementLabelValues(
    context: CartesianMeasureContext,
    axisHeight: Float,
    maxLabelHeight: Float,
    position: AxisPosition.Vertical
  ): List<Float> = values
}
