package com.example.android.minutelauncher.home

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope
import com.example.android.minutelauncher.Event
import com.example.android.minutelauncher.ScreenState
import com.example.android.minutelauncher.clearFocusOnKeyboardDismiss

@Composable
fun ConstraintLayoutScope.SearchBar(
  screenState: ScreenState,
  text: String,
  constraintReference: ConstrainedLayoutReference,
  constraints: ConstrainScope.() -> Unit,
  onEvent: (Event) -> Unit,
  onSearch: KeyboardActionScope.() -> Unit
) {
  val focusRequester = remember { FocusRequester() }
  var searchHeight by remember { mutableFloatStateOf(0f) }
  val fastFloatSpec: AnimationSpec<Float> = tween(durationMillis = 500)
  val slowFloatSpec: AnimationSpec<Float> = tween(durationMillis = 1000)
  val superFastDpSpec: AnimationSpec<Dp> = tween(durationMillis = 200)
  val slowDpSpec: AnimationSpec<Dp> = tween(durationMillis = 1000)
  val searchBarAlpha by animateFloatAsState(
    targetValue = if (screenState.hasSearch()) 1f else 0f,
    label = "",
    animationSpec = if (screenState.hasSearch()) fastFloatSpec else slowFloatSpec
  )
  val searchBarOffset by animateDpAsState(
    targetValue = if (screenState.hasSearch()) 0.dp else searchHeight.dp,
    label = "",
    animationSpec = if (screenState.hasSearch()) superFastDpSpec else slowDpSpec
  )

  Surface(
    shape = MaterialTheme.shapes.large,
    tonalElevation = 1.dp,
    modifier = Modifier
      .constrainAs(constraintReference) {
        constraints()
      }
      .graphicsLayer {
        alpha = searchBarAlpha
      }
      .padding(horizontal = 32.dp)
      .padding(bottom = 32.dp)
      .onGloballyPositioned {
        searchHeight = it.size.height.toFloat()
      }
      .offset(y = searchBarOffset)
  ) {
    TextField(
      value = text,
      onValueChange = { onEvent(Event.UpdateSearch(it)) },
      modifier = Modifier
        .fillMaxWidth()
        .focusRequester(focusRequester)
        .clearFocusOnKeyboardDismiss(),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
      keyboardActions = KeyboardActions(onSearch = onSearch),
      colors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color.Transparent,
        focusedBorderColor = Color.Transparent
      ),
      placeholder = {
        Text(
          text = "search",
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      },
      textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
    )
  }

}