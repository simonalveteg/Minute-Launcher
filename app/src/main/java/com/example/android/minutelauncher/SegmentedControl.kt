package com.example.android.minutelauncher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import timber.log.Timber

/**
 * items : list of items to be render
 * defaultSelectedItemIndex : to highlight item by default (Optional)
 * useFixedWidth : set true if you want to set fix width to item (Optional)
 * itemWidth : Provide item width if useFixedWidth is set to true (Optional)
 * cornerRadius : To make control as rounded (Optional)
 * color : Set color to control (Optional)
 * onItemSelection : Get selected item index
 *
 * taken from https://medium.com/@manojbhadane/hello-everyone-558290eb632e
 */
@Composable
fun SegmentedControl(
  items: Set<Int>,
  selectedItem: Int,
  useFixedWidth: Boolean = false,
  itemWidth: Dp = 120.dp,
  color: Color = MaterialTheme.colorScheme.primary,
  selectedTextColor: Color = MaterialTheme.colorScheme.onPrimary,
  onItemSelection: (selectedItem: Int) -> Unit
) {
  val selectedIndex = remember { mutableIntStateOf(items.indexOf(selectedItem)) }

  LaunchedEffect(selectedIndex){
    Timber.d("SelectedIndex: ${selectedIndex.intValue}, selectedItem: $selectedItem")
  }

  Row(
    modifier = Modifier
  ) {
    items.forEachIndexed { index, item ->
      OutlinedButton(
        modifier = when (index) {
          0 -> {
            if (useFixedWidth) {
              Modifier
                .width(itemWidth)
                .offset(0.dp, 0.dp)
                .zIndex(if (selectedIndex.intValue == index) 1f else 0f)
            } else {
              Modifier
                .wrapContentSize()
                .offset(0.dp, 0.dp)
                .zIndex(if (selectedIndex.intValue == index) 1f else 0f)
            }
          } else -> {
            if (useFixedWidth)
              Modifier
                .width(itemWidth)
                .offset((-1 * index).dp, 0.dp)
                .zIndex(if (selectedIndex.intValue == index) 1f else 0f)
            else Modifier
              .wrapContentSize()
              .offset((-1 * index).dp, 0.dp)
              .zIndex(if (selectedIndex.intValue == index) 1f else 0f)
          }
        },
        onClick = {
          selectedIndex.intValue = index
          onItemSelection(items.elementAt(index))
        },
        shape = when (index) {
          /**
           * left outer button
           */
          0 -> MaterialTheme.shapes.small.copy(
            topEnd = CornerSize(0),
            bottomEnd = CornerSize(0)
          )
          /**
           * right outer button
           */
          items.size - 1 -> MaterialTheme.shapes.small.copy(
            topStart = CornerSize(0),
            bottomStart = CornerSize(0)
          )
          /**
           * middle button
           */
          else -> MaterialTheme.shapes.small.copy(
            topEnd = CornerSize(0),
            bottomEnd = CornerSize(0),
            topStart = CornerSize(0),
            bottomStart = CornerSize(0)
          )
        },
        border = BorderStroke(
          1.dp, if (selectedIndex.intValue == index) {
            color
          } else {
            color.copy(alpha = 0.75f)
          }
        ),
        colors = if (selectedIndex.intValue == index) {
          /**
           * selected colors
           */
          ButtonDefaults.outlinedButtonColors(
            containerColor = color
          )
        } else {
          /**
           * not selected colors
           */
          ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
        },
      ) {
        Text(
          text = item.toString(),
          fontWeight = FontWeight.Normal,
          color = if (selectedIndex.intValue == index) {
            selectedTextColor
          } else {
            color.copy(alpha = 0.9f)
          },
        )
      }
    }
  }
}