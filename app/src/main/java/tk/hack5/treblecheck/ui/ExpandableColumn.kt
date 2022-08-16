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
import kotlin.math.max


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
                is ExpandableColumnRow.ExpandableColumnExpandableRow -> {
                    c.content(
                        ExpandableColumnExpandableRowScope(
                            transitions[i]
                        ) { onClick(indices[i]!!) }
                    )
                }
                is ExpandableColumnRow.ExpandableColumnPlainRow -> {

                    c.content()
                }
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

/*
@Composable
fun ExpandableColumn2(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    separator: Dp,
    expandedIndex: Transition<Int>,
    expandIndex: (Int) -> Unit,
    heightSpec: FiniteAnimationSpec<Int>,
    sizeSpec: FiniteAnimationSpec<IntSize>,
    content: ExpandableColumnScope.() -> Unit
) {
    val data = ExpandableColumnScope().also(content).also { it.finalize() }
    val scrollingEnabled = expandedIndex.targetState == -1 // TODO remember scroll state when disabling; it is forgotten

    BoxWithConstraints(modifier) {
        Layout(
            modifier = Modifier.verticalScroll(scrollState, scrollingEnabled),
            content = {
                var expandableIndex = 0
                var yOffset = 0.dp

                data.contents.forEachIndexed { i, c ->
                    when (c) {
                        is ExpandableColumnRow.ExpandableColumnExpandableRow -> {
                            var boxModifier = Modifier.animateContentSize()

                            if (expandedIndex.targetState == expandableIndex++) {
                                boxModifier = boxModifier.height(this@BoxWithConstraints.maxHeight)
                            }

                            yOffset +=

                            Box(boxModifier.offset(y = yOffset)) {
                                c.content(
                                    ExpandableColumnExpandableRowScope(
                                        transitions[i]
                                    ) { onClick(indices[i]!!) }
                                )
                            }
                        }
                        is ExpandableColumnRow.ExpandableColumnPlainRow -> Box {
                            c.content()
                        }
                    }
                }
            }
        ) {
            layout()
        }
    }

}*/

@OptIn(ExperimentalTransitionApi::class)
@Composable
fun ExpandableColumn(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    scrollable: Boolean,
    separator: Dp,
    expandedIndex: Transition<Int>,
    expandIndex: (Int) -> Unit,
    heightSpec: FiniteAnimationSpec<Int>,
    topSpec: FiniteAnimationSpec<Int>,
    content: ExpandableColumnScope.() -> Unit
) {
    val data = ExpandableColumnScope().also(content).also { it.finalize() }
    val transitions =
        data.indices.map { i -> expandedIndex.createChildTransition(label = "Expandable $i") { if (i == null) false else it == i } }

    var initialHeightsOffsets: List<Pair<Int, Int>>? by remember { mutableStateOf(null) }

    val scrollingEnabled =
        expandedIndex.targetState == -1 // TODO remember scroll state when disabling; it is forgotten

    ExpandableColumnContent(
        modifier,
        scrollState,
        scrollable,
        separator,
        expandedIndex,
        expandIndex,
        heightSpec,
        topSpec,
        data,
        transitions,
        initialHeightsOffsets,
        { initialHeightsOffsets = it },
        scrollingEnabled
    )
}

@Composable
fun ExpandableColumnContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    scrollable: Boolean,
    separator: Dp,
    expandedIndex: Transition<Int>,
    expandIndex: (Int) -> Unit,
    heightSpec: FiniteAnimationSpec<Int>,
    topSpec: FiniteAnimationSpec<Int>,
    data: ExpandableColumnScope,
    transitions: List<Transition<Boolean>>,
    initialHeightsOffsets: List<Pair<Int, Int>>?,
    setInitialHeightsOffsets: (List<Pair<Int, Int>>) -> Unit,
    scrollingEnabled: Boolean
) {
    BoxWithConstraints(modifier) {
        val maxHeight = if (constraints.hasBoundedHeight) constraints.maxHeight else null

        val heightTopStates = initialHeightsOffsets?.let { initialHeightsOffsets ->
            transitions.mapIndexed { i, transition ->
                transition.animateInt(
                    label = "Height",
                    transitionSpec = { heightSpec }
                ) { if (it) maxHeight!! else initialHeightsOffsets[i].first } to transition.animateInt(
                    label = "Top",
                    transitionSpec = { topSpec }
                ) { if (it) scrollState.value else initialHeightsOffsets[i].second }
            }
        }

        Layout(
            modifier = if (scrollable) Modifier.verticalScroll(scrollState, enabled = scrollingEnabled) else Modifier,
            content = { data.content(transitions = transitions, onClick = expandIndex) }
        ) { measurables, constraints ->
            if (!expandedIndex.isRunning && expandedIndex.currentState != -1) {
                val measurable = measurables[data.reverseIndices[expandedIndex.currentState]]
                val placeable = measurable.measure(
                    maxHeight?.let { constraints.copy(minHeight = it, maxHeight = it) } ?: constraints
                )
                return@Layout layout(constraints.maxWidth, placeable.height /* TODO */) {
                    placeable.placeRelative(0, scrollState.value)
                }
            }



            val (placeables, offsets) = if (initialHeightsOffsets == null) {
                val placeables = measurables.map { measurable ->
                    measurable.measure(constraints)
                }
                val heights = placeables.map { it.height }
                val offsets = heights.scan(0) {
                        y, h -> y + separator.roundToPx() + h
                }
                setInitialHeightsOffsets(heights.zip(offsets))

                placeables to offsets
            } else {
                measurables.mapIndexed { i, measurable ->
                    val height = heightTopStates!![i].first.value
                    val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
                    placeable
                } to heightTopStates!!.map { it.second.value }
            }

            //val totalHeight = initialHeights!!.sum() + separator.roundToPx() * (initialHeights!!.size - 1)
//            assert(totalHeight == offsets.last())

            layout(constraints.maxWidth, max(maxHeight ?: 0, offsets.last() + placeables.last().height)) {
                // Track the y co-ord we have placed children up to
                //var yPosition = 0
                Log.d(tag, scrollState.value.toString())
                Log.d(tag, scrollState.maxValue.toString())

                // Place children in the parent layout
                placeables.forEachIndexed { i, placeable ->
                    val isElevated = transitions[i].currentState || transitions[i].targetState
                    placeable.placeRelative(x = 0, y = offsets[i], if (isElevated) 1f else 0f)

                    // Record the y co-ord placed up to
                    //yPosition += initialHeights!![i] + separator.roundToPx()
                }
            }
        }
    }
}



private const val tag = "ExpandableColumn"