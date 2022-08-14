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

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.min
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.ceil
import kotlin.math.min

class GroupScope

interface NewAdaptiveGridScope {
    val maxColumns: Int

    fun group(content: @Composable (GroupScope.() -> Unit))
}

private class NewAdaptiveGridScopeImpl(override val maxColumns: Int) : NewAdaptiveGridScope {
    val groups = mutableListOf<@Composable (GroupScope.() -> Unit)>()

    override fun group(content: @Composable (GroupScope.() -> Unit)) {
        groups += content
    }
}

class AdaptiveGridScope(val numColumns: Int)


/**
 * @param minColumnWidth The minimum width of each column
 * @param maxColumnWidth The maximum width of each column. Must be at least double [minColumnWidth]
 * @param modifier The modifier for the Grid
 * @param content The content of the grid, grouped in [NewAdaptiveGridScope.group]s
 */
@ExperimentalContracts
@Composable
fun Grid2(
    minColumnWidth: Dp,
    maxColumnWidth: Dp,
    modifier: Modifier = Modifier,
    content: (NewAdaptiveGridScope.() -> Unit)
) {
    contract {
        callsInPlace(content, InvocationKind.EXACTLY_ONCE)
    }
    // maxWidth=6, minColumnWidth=2, maxColumnCount=3, maxColumnWidth=4, columnCount=2
    assert(maxColumnWidth >= minColumnWidth * 2)
    BoxWithConstraints(modifier) {
        require(constraints.hasBoundedWidth)
        val maxColumnCount = (maxWidth / minColumnWidth).toInt()
        val groups = NewAdaptiveGridScopeImpl(maxColumnCount).also(content).groups
        val columnCount = min(maxColumnCount, groups.size)
        val columnWidth = min(maxWidth / columnCount, maxColumnWidth)
        val columns = List(columnCount) { mutableListOf<@Composable (GroupScope.() -> Unit)>() }
        groups.forEachIndexed { i, groupContent ->
            columns[i % columnCount] += groupContent
        }
        Row {
            columns.forEach { column ->
                Column(Modifier.width(columnWidth)) {
                    column.forEach {
                        it(GroupScope())
                    }
                }
            }
        }
    }
}


@Composable
fun AdaptiveGrid2(
    minColumnWidth: Dp,
    modifier: Modifier = Modifier,
    content: @Composable (AdaptiveGridScope.() -> Unit)
) {
    BoxWithConstraints(modifier) {
        require(constraints.hasBoundedWidth)
        val columns = (maxWidth / minColumnWidth).toInt()

        if (columns <= 1) {
            Column {
                content(AdaptiveGridScope(1))
            }
        } else {
            Layout(
                content = { content(AdaptiveGridScope(columns)) }
            ) { measurables, constraints ->
                check(constraints.maxWidth == this@BoxWithConstraints.constraints.maxWidth) { "$constraints.maxWidth != ${this@BoxWithConstraints.constraints}.maxWidth" }

                val rows = Array(ceil(measurables.size.toFloat() / columns).toInt()) { i ->
                    measurables.subList(i * columns, min((i + 1) * columns, measurables.size))
                }

                val rowHeights = IntArray(rows.size) { i ->
                    val row = rows[i]
                    val width = maxWidth / row.size
                    rows[i].maxOf { it.minIntrinsicHeight(width.toPx().toInt()) }
                }


                layout(constraints.maxWidth, rowHeights.sum()) {
                    var y = 0
                    rows.forEachIndexed { i, row ->
                        val width = maxWidth / row.size
                        val height = rowHeights[i]
                        row.forEachIndexed { j, it ->
                            val placeable = it.measure(Constraints.fixed(width.toPx().toInt(), height))
                            placeable.placeRelative(IntOffset((width * j).roundToPx(), y))
                        }
                        y += height
                    }
                }
            }
        }

    }
}

@Composable
fun AdaptiveGrid(
    minColumnWidth: Dp,
    modifier: Modifier = Modifier,
    content: @Composable AdaptiveGridScope.() -> Unit
) {
    BoxWithConstraints(modifier) {
        require(constraints.hasBoundedWidth)
        //require(!constraints.hasBoundedHeight)
        val columns = min((maxWidth / minColumnWidth).toInt(), 1)
        val normalColumnWidth = maxWidth / columns

        Layout(
            content = { content(AdaptiveGridScope(columns)) }
        ) { measurables, constraints ->
            check(constraints.maxWidth == this@BoxWithConstraints.constraints.maxWidth) { "$constraints.maxWidth != ${this@BoxWithConstraints.constraints}.maxWidth" }

            val rows = Array(ceil(measurables.size.toFloat() / columns).toInt()) { i ->
                measurables.subList(i * columns, min((i + 1) * columns, measurables.size))
            }

            val rowHeights = IntArray(rows.size) { i ->
                val row = rows[i]
                val width = maxWidth / row.size
                rows[i].maxOf { it.minIntrinsicHeight(width.toPx().toInt()) }
            }


            layout(constraints.maxWidth, rowHeights.sum()) {
                var y = 0
                rows.forEachIndexed { i, row ->
                    val width = maxWidth / row.size
                    val height = rowHeights[i]
                    row.forEachIndexed { j, it ->
                        val placeable = it.measure(Constraints.fixed(width.toPx().toInt(), height))
                        placeable.placeRelative(IntOffset((width * j).roundToPx(), y))
                    }
                    y += height
                }
            }
        }
    }
}
