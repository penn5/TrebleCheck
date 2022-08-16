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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


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

    AutoListExpandableEntry(modifier, rootExpandedIndexTransition, setRootExpandedIndex, contents, entryPadding, separator, createFixedTransition(false), true, specs)


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
    scrollable: Boolean,
    specs: TransitionSpecs
) {
    Surface {
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
                            entryContent.onClick ?: { }
                        )
                    }
                    is AutoListEntry.ExpandableEntry -> expandable {
                        if (transition.isRunning) {
                            Surface(Modifier.fillMaxSize()) {
                                Column(
                                    //Modifier.wrapContentHeight(align = Alignment.Top, unbounded = true),
                                    verticalArrangement = Arrangement.spacedBy(separator)
                                ) {
                                    val firstChildPadding by transition.animateDp(label = "Padding for the first child", transitionSpec = { specs.padding }) { if (it) 0.dp else entryPadding }
                                    val fixedFalse = createFixedTransition(false)

                                    entry.content.map(AutoListEntry::collapsed).forEachIndexed { childIndex, child ->
                                        DisplayedEntry(
                                            Modifier.padding(horizontal = if (childIndex == 0) firstChildPadding else entryPadding),
                                            child().entry,
                                            if (childIndex == 0) transition else fixedFalse,
                                            specs,
                                            onClick
                                        )
                                    }
                                }
                            }
                        } else if (transition.currentState) {
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
                                    firstChildTitle = transition.createChildTransition(label = "First child is title") { it },
                                    scrollable = transition.currentState && transition.targetState,
                                    specs = specs
                                )
                            }
                        } else {
                            val content = (entry.content[0] as AutoListEntry.PlainEntry).content

                            DisplayedEntry(Modifier.padding(horizontal = entryPadding), content().entry, transition, specs, onClick)
                        }
                    }
                }
            }
        }
    }
}

