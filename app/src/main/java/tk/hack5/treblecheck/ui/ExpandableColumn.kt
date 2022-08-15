/*
 * Sub-licenses:
 *         https://github.com/google/material-design-icons/blob/master/LICENSE
 *         https://github.com/Templarian/MaterialDesign/blob/master/LICENSE
 *         https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt
 * This project:
 *         Copyright (C) 2022 Penn Mackintosh
 *         Licensed under https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package tk.hack5.treblecheck.ui

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt


sealed class ExpandableColumnRow {
    class ExpandableColumnExpandableRow(val content: @Composable ExpandableColumnExpandableRowScope.() -> Unit) : ExpandableColumnRow()
    class ExpandableColumnPlainRow(val content: @Composable () -> Unit) : ExpandableColumnRow()
}

class ExpandableColumnScope {
    private val contents = mutableListOf<ExpandableColumnRow>()
    val indices = mutableListOf<Int?>()
    val reverseIndices = mutableListOf<Int>()
    private var finalized = false

    @Composable
    internal fun content(transitions: List<Transition<Boolean>>, onClick: (Int) -> Unit) {
        require(finalized)
        contents.forEachIndexed { i, c ->
            when (c) {
                is ExpandableColumnRow.ExpandableColumnExpandableRow -> c.content(
                    ExpandableColumnExpandableRowScope(
                        transitions[i]
                    ) { onClick(indices[i]!!) }
                )
                is ExpandableColumnRow.ExpandableColumnPlainRow -> c.content()
            }
        }
    }

    fun expandable(content: @Composable (ExpandableColumnExpandableRowScope.() -> Unit)) {
        require(!finalized)
        contents.add(ExpandableColumnRow.ExpandableColumnExpandableRow(content))
        reverseIndices.add(contents.lastIndex)
        indices.add(reverseIndices.lastIndex)
    }

    fun plain(content: @Composable () -> Unit) {
        require(!finalized)
        contents.add(ExpandableColumnRow.ExpandableColumnPlainRow(content))
        indices.add(null)
    }

    internal fun finalize() {
        finalized = true
    }
}

class ExpandableColumnExpandableRowScope(val transition: Transition<Boolean>, val onClick: () -> Unit)



@OptIn(ExperimentalTransitionApi::class)
@Composable
fun ExpandableColumn(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    separator: Dp,
    expandedIndex: Transition<Int>,
    expandIndex: (Int) -> Unit,
    heightSpec: FiniteAnimationSpec<Float>,
    topSpec: FiniteAnimationSpec<Float>,
    content: ExpandableColumnScope.() -> Unit
) {
    val data = ExpandableColumnScope().also(content).also { it.finalize() }
    val transitions = data.indices.map { i -> expandedIndex.createChildTransition(label = "Expandable $i") { if (i == null) false else it == i } }

    val heightStates = transitions.mapIndexed { i, transition -> transition.animateFloat(label = "Height", transitionSpec = { heightSpec }) { if (it) 1f else 0f } }
    val topStates = transitions.mapIndexed { i, transition -> transition.animateFloat(label = "Top", transitionSpec = { topSpec }) { if (it) 1f else 0f } }
    val scrollingEnabled = expandedIndex.targetState == -1 // TODO remember scroll state when disabling; it is forgotten
    var initialHeights: List<Int> by remember { mutableStateOf(emptyList()) }
    BoxWithConstraints(modifier) {
        Layout(
            modifier = Modifier.verticalScroll(scrollState, enabled = scrollingEnabled),
            content = { data.content(transitions = transitions, onClick = expandIndex) }
        ) { measurables, constraints ->
            if (!expandedIndex.isRunning && expandedIndex.currentState != -1) {
                val measurable = measurables[data.reverseIndices[expandedIndex.currentState]]
                val height = this@BoxWithConstraints.constraints.maxHeight
                val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
                return@Layout layout(constraints.maxWidth, placeable.height) {
                    placeable.placeRelative(0, scrollState.value)
                }
            }
            val placeables = if (initialHeights.isEmpty()) {
                measurables.map { measurable ->
                    measurable.measure(constraints)
                }.also { placeables ->
                    initialHeights = placeables.map { it.height }
                }
            } else {
                measurables.mapIndexed { i, measurable ->
                    val height = initialHeights[i] + ((this@BoxWithConstraints.constraints.maxHeight - initialHeights[i]) * heightStates[i].value).roundToInt()
                    val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
                    placeable
                }
            }

            val totalHeight = initialHeights.sum() + separator.roundToPx() * (initialHeights.size - 1)

            layout(constraints.maxWidth, totalHeight) {
                // Track the y co-ord we have placed children up to
                var yPosition = 0
                Log.d(tag, scrollState.value.toString())
                Log.d(tag, scrollState.maxValue.toString())

                // Place children in the parent layout
                placeables.forEachIndexed { i, placeable ->
                    val isElevated = transitions[i].currentState || transitions[i].targetState
                    val absoluteBasePosition = yPosition - scrollState.value
                    val newAbsolutePosition = absoluteBasePosition * (1 - topStates[i].value)
                    val newPosition = newAbsolutePosition.roundToInt() + scrollState.value
                    placeable.placeRelative(x = 0, y = newPosition, if (isElevated) 1f else 0f)

                    // Record the y co-ord placed up to
                    yPosition += initialHeights[i] + separator.roundToPx()
                }
            }
        }
    }
}



private const val tag = "ExpandableColumn"