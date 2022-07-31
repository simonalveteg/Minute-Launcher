package com.example.android.minutelauncher

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlin.math.roundToInt

enum class States {
    EXPANDED,
    HIDDEN
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen() {
    val swipeableState = rememberSwipeableState(initialValue = States.HIDDEN)
    val lazyListState = rememberLazyListState()

    Surface {
        BoxWithConstraints {
            val constraintsScope = this
            val maxHeight = with(LocalDensity.current) {
                constraintsScope.maxHeight.toPx()
            }

            val connection = remember {
                object : NestedScrollConnection {
                    val scrolledToTop =
                        lazyListState.firstVisibleItemScrollOffset == 0 && lazyListState.firstVisibleItemIndex == 0

                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        val delta = available.y
                        return if (delta < 0) {
                            swipeableState.performDrag(delta).toOffset()
                        } else Offset.Zero
                    }

                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        return if (consumed.y == 0f) swipeableState.performDrag(available.y)
                            .toOffset() else Offset.Zero
                    }

                    override suspend fun onPreFling(available: Velocity): Velocity {
                        if (available.y > 0 && scrolledToTop) {
                            swipeableState.performFling(velocity = available.y)
                        }
                        return Velocity.Zero
                    }

                    override suspend fun onPostFling(
                        consumed: Velocity,
                        available: Velocity
                    ): Velocity {
                        swipeableState.performFling(velocity = available.y)
                        return super.onPostFling(consumed, available)
                    }

                    private fun Float.toOffset() = Offset(0f, this)
                }
            }
            Box(
                Modifier
                    .alpha(swipeableState.offset.value / maxHeight)
            ) {
                FavoriteApps()
            }
            Box(
                Modifier
                    .swipeable(
                        state = swipeableState,
                        orientation = Orientation.Vertical,
                        anchors = mapOf(
                            0f to States.EXPANDED,
                            maxHeight to States.HIDDEN,
                        )
                    )
                    .nestedScroll(connection)
                    .offset {
                        IntOffset(0, swipeableState.offset.value.roundToInt())
                    }
            ) {

                AppList(lazyListState)
            }
        }
    }
}
