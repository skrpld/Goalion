package com.skrpld.goalion.ui.screens.main

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

@Composable
fun rememberDragDropState(lazyListState: LazyListState, onMove: (Int, Int) -> Unit): DragDropState {
    val scope = rememberCoroutineScope()
    val state = remember(lazyListState) {
        DragDropState(state = lazyListState, onMove = onMove, scope = scope)
    }
    return state
}

class DragDropState(
    private val state: LazyListState,
    private val onMove: (Int, Int) -> Unit,
    private val scope: kotlinx.coroutines.CoroutineScope
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    var draggingItemOffset by mutableStateOf(0f)
        private set

    internal val dragGestureModifier = Modifier.pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                state.layoutInfo.visibleItemsInfo
                    .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
                    ?.also {
                        draggingItemIndex = it.index
                        draggingItemOffset = 0f
                    }
            },
            onDrag = { change, dragAmount ->
                change.consume()
                draggingItemOffset += dragAmount.y

                val current = draggingItemIndex ?: return@detectDragGesturesAfterLongPress
                val currentInfo = state.layoutInfo.visibleItemsInfo.find { it.index == current } ?: return@detectDragGesturesAfterLongPress

                val distFromTop = change.position.y
                val distFromBottom = state.layoutInfo.viewportSize.height - change.position.y

                if (distFromTop < 150f) {
                    scope.launch { state.scrollBy(-10f) }
                } else if (distFromBottom < 150f) {
                    scope.launch { state.scrollBy(10f) }
                }

                val startOffset = currentInfo.offset + draggingItemOffset
                val middleOffset = startOffset + (currentInfo.size / 2f)

                val targetItem = state.layoutInfo.visibleItemsInfo.find { item ->
                    middleOffset.toInt() in item.offset..(item.offset + item.size) && item.index != current
                }

                if (targetItem != null) {
                    onMove(current, targetItem.index)
                    draggingItemIndex = targetItem.index
                    draggingItemOffset += (currentInfo.offset - targetItem.offset)
                }
            },
            onDragEnd = {
                draggingItemIndex = null
                draggingItemOffset = 0f
            },
            onDragCancel = {
                draggingItemIndex = null
                draggingItemOffset = 0f
            }
        )
    }
}

fun Modifier.dragDropItem(
    index: Int,
    dragDropState: DragDropState
): Modifier = this.then(
    if (index == dragDropState.draggingItemIndex) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationY = dragDropState.draggingItemOffset
            }
    } else {
        Modifier.zIndex(0f)
    }
)