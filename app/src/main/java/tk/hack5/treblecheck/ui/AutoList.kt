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

import androidx.compose.animation.core.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp


sealed class AutoListEntry {
    class PlainEntry(val content: @Composable () -> ClickableEntry) : AutoListEntry()
    class ExpandableEntry(val content: List<AutoListEntry>) : AutoListEntry()
}

val AutoListEntry.collapsed: @Composable () -> ClickableEntry get() {
    var entry = this
    while (entry is AutoListEntry.ExpandableEntry) {
        entry = entry.content.first()
    }
    return (entry as AutoListEntry.PlainEntry).content
}

class AutoListScope {
    val contents = mutableListOf<AutoListEntry>()


    fun plain(content: @Composable () -> ClickableEntry) {
        contents.add(AutoListEntry.PlainEntry(content))
    }

    fun expandable(content: AutoListScope.() -> Unit) {
        contents.add(AutoListEntry.ExpandableEntry(AutoListScope().also(content).contents))
    }
}

/**
 * Sometimes we don't want a real transition, this lets us declare that we won't animate it.
 */
@Composable
inline fun <reified T>createFixedTransition(value: T) = updateTransition(value, label = "dummy")


@Composable
fun AutoList(
    modifier: Modifier,
    entryPadding: Dp,
    separator: Dp,
    rootExpandedIndexTransition: Transition<Int>,
    setRootExpandedIndex: (Int) -> Unit,
    specs: TransitionSpecs,
    content: AutoListScope.() -> Unit,
) {
    val contents = AutoListScope().also(content).contents


    AutoListExpandableEntry(modifier, rootExpandedIndexTransition, setRootExpandedIndex, contents, entryPadding, separator, createFixedTransition(false), createFixedTransition(true), null, true, specs)
}
@OptIn(ExperimentalTransitionApi::class)
@Composable
private fun AutoListExpandableEntry(
    modifier: Modifier,
    expandedIndexTransition: Transition<Int>,
    setExpandedIndex: (Int) -> Unit,
    contents: List<AutoListEntry>,
    entryPadding: Dp,
    separator: Dp,
    firstChildTitle: Transition<Boolean>,
    expanded: Transition<Boolean>,
    onClick: (() -> Unit)?,
    scrollable: Boolean,
    specs: TransitionSpecs
) {
    var initialHeightsOffsets: List<Pair<Int, Int>>? by remember { mutableStateOf(null) }
    val transitions = contents.indices.map { i -> expandedIndexTransition.createChildTransition(label = "Expandable $i") { it == i } }

    AutoListExpandableEntryContent(modifier, initialHeightsOffsets, { initialHeightsOffsets = it }, firstInitialHeight,
        { firstInitialHeight = it }, rememberScrollState(), transitions, expandedIndexTransition, setExpandedIndex, contents, entryPadding, separator, firstChildTitle, expanded, onClick, scrollable, specs)


}
@Composable
private fun AutoListExpandableEntryContent(
    modifier: Modifier,
    initialHeightsOffsets: List<Pair<Int, Int>>?,
    setInitialHeightsOffsets: (List<Pair<Int, Int>>) -> Unit,
    scrollState: ScrollState,
    transitions: List<Transition<Boolean>>,
    expandedIndexTransition: Transition<Int>,
    setExpandedIndex: (Int) -> Unit,
    contents: List<AutoListEntry>,
    entryPadding: Dp,
    separator: Dp,
    firstChildTitle: Transition<Boolean>,
    expanded: Transition<Boolean>,
    onClick: (() -> Unit)?,
    scrollable: Boolean,
    specs: TransitionSpecs
) {
    BoxWithConstraints(modifier) {
        val maxHeight = if (constraints.hasBoundedHeight) constraints.maxHeight else null

        // create transitions
        val heightTopStates = initialHeightsOffsets?.let { initialHeightsOffsets ->
            transitions.mapIndexed { i, transition ->
                transition.animateInt(
                    label = "Height",
                    transitionSpec = { specs.height }
                ) { if (it) maxHeight!! else initialHeightsOffsets[i].first } to transition.animateInt(
                    label = "Top",
                    transitionSpec = { specs.top }
                ) { if (it) scrollState.value else initialHeightsOffsets[i].second }
            }
        }

        Surface {
            Box {
                // always display first entry
                Layout(
                    content = {
                        val firstEntry = contents.first().collapsed()
                        DisplayedEntry(
                            entry = firstEntry.entry,
                            isTitle = firstChildTitle,
                            specs = specs,
                            onClick = when {
                                expanded.isRunning -> {
                                    { }
                                }
                                expanded.targetState -> firstEntry.onClick ?: { }
                                else -> onClick ?: firstEntry.onClick ?: { }
                            }
                        )
                    }
                ) { measurables, constraints ->
                    val placeable = measurables.single().measure(constraints)

                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, 0)
                    }
                }

                Layout(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    content = { /* TODO */ },
                ) { measurables, constraints ->
                    // calculate initial heights
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

                    //


                    layout(0, 0) {

                    }
                }
            }
        }

    }
    /*
    ExpandableColumn(
        modifier
            .fillMaxSize(),
        scrollable = scrollable,
        separator = separator,
        expandedIndex = expandedIndexTransition,
        expandIndex = setExpandedIndex,
        heightSpec = specs.height,
        topSpec = specs.top
    ) {
        contents.forEachIndexed { i, entry ->
            when (entry) {
                is AutoListEntry.PlainEntry -> plain {
                    val isTitle = i == 0 && firstChildTitle.targetState
                    val entryContent = entry.content()
                    DisplayedEntry(
                        Modifier.padding(horizontal = if (isTitle) 0.dp else entryPadding),
                        entryContent.entry,
                        if (i == 0) firstChildTitle else createFixedTransition(false),
                        specs,
                        entryContent.onClick ?: onClick ?: { }
                    )
                }
                is AutoListEntry.ExpandableEntry -> expandable {
                    if (!expanded) {
                        val entryContent = entry.collapsed()

                        DisplayedEntry(
                            Modifier.padding(horizontal = entryPadding),
                            entryContent.entry,
                            createFixedTransition(false),
                            specs,
                            entryContent.onClick ?: onClick ?: { }
                        )
                    } else {
                        var innerExpandedIndex: Int by rememberSaveable { mutableStateOf(-1) }
                        val innerExpandedIndexTransition = updateTransition(innerExpandedIndex, label = "moreInfoExpandedIndex")

                        Surface {
                            AutoListExpandableEntry(
                                modifier = Modifier,
                                expandedIndexTransition = innerExpandedIndexTransition,
                                setExpandedIndex = { innerExpandedIndex = it },
                                contents = entry.content,
                                entryPadding = entryPadding,
                                separator = separator,
                                firstChildTitle = transition,
                                expanded = transition.currentState || transition.targetState,
                                onClick = if (!transition.currentState && !transition.targetState) this.onClick else null,
                                scrollable = transition.currentState && transition.targetState,
                                specs = specs
                            )
                        }
                    }

                }
            }
        }
    }
    */
}

