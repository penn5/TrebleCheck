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

import android.graphics.Typeface
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import androidx.annotation.FloatRange
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlin.math.roundToLong

data class TransitionSpecs(
    val crossfade: FiniteAnimationSpec<Float>,
    val expandShrink: FiniteAnimationSpec<IntSize>,
    val fade: FiniteAnimationSpec<Float>,
    val padding: FiniteAnimationSpec<Float>,
    val height: FiniteAnimationSpec<Float>,
    val top: FiniteAnimationSpec<Float>,
) {
    /**
     * Scale all transitions by the given factor.
     * @param factor how many times longer the animations should be. Frames may be changed if not a power of 2.
     */
    fun scale(factor: Float): TransitionSpecs {
        return TransitionSpecs(
            ScaledFiniteAnimationSpec(crossfade, factor),
            ScaledFiniteAnimationSpec(expandShrink, factor),
            ScaledFiniteAnimationSpec(fade, factor),
            ScaledFiniteAnimationSpec(padding, factor),
            ScaledFiniteAnimationSpec(height, factor),
            ScaledFiniteAnimationSpec(top, factor),
        )
    }

    class ScaledFiniteAnimationSpec<T>(val base: FiniteAnimationSpec<T>, val factor: Float) : FiniteAnimationSpec<T> {
        override fun <V : AnimationVector> vectorize(converter: TwoWayConverter<T, V>): VectorizedFiniteAnimationSpec<V> {
            return ScaledVectorizedFiniteAnimationSpec(base.vectorize(converter), factor)
        }
    }

    class ScaledVectorizedFiniteAnimationSpec<V : AnimationVector>(val base: VectorizedFiniteAnimationSpec<V>, val factor: Float) : VectorizedFiniteAnimationSpec<V> {
        override fun getDurationNanos(initialValue: V, targetValue: V, initialVelocity: V): Long {
            return (base.getDurationNanos(initialValue, targetValue, initialVelocity) * factor).roundToLong()
        }

        override fun getValueFromNanos(
            playTimeNanos: Long,
            initialValue: V,
            targetValue: V,
            initialVelocity: V
        ): V {
            return base.getValueFromNanos((playTimeNanos / factor).roundToLong(), initialValue, targetValue, initialVelocity)
        }

        override fun getVelocityFromNanos(
            playTimeNanos: Long,
            initialValue: V,
            targetValue: V,
            initialVelocity: V
        ): V {
            return base.getVelocityFromNanos((playTimeNanos / factor).roundToLong(), initialValue, targetValue, initialVelocity)
        }
    }
}



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TextCardContent(title: String, htmlBody: String, detail: String?, icon: Painter, iconTint: Color, titleOnly: Transition<Boolean>, specs: TransitionSpecs) {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(cardInnerPadding)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val iconSize = cardIconSize
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.requiredWidth(this@BoxWithConstraints.maxWidth - iconSize)
                )
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = iconTint
                )
            }
            titleOnly.AnimatedVisibility(
                visible = { !it },
                enter = expandVertically(specs.expandShrink, Alignment.Top) + fadeIn(specs.fade),
                exit = shrinkVertically(specs.expandShrink, Alignment.Top) + fadeOut(specs.fade)
            ) {
                Column {
                    HtmlText(htmlBody)
                    Spacer(Modifier.height(explanationSpacing))
                    detail?.let {
                        HtmlText(detail)
                    }
                }
            }
        }
    }
}

@Composable
fun Chevron(@FloatRange(from = -1.0, to = 1.0) upDown: Float, modifier: Modifier = Modifier) {
    val midY = 12 + upDown * 4
    Canvas(
        modifier
    ) {
        drawLine(Color.Black, Offset(7f.dp.toPx(), 12f.dp.toPx()), Offset(12f.dp.toPx(), midY.dp.toPx()), 2f.dp.toPx(), StrokeCap.Square)
        drawLine(Color.Black, Offset(12f.dp.toPx(), midY.dp.toPx()), Offset(17f.dp.toPx(), 12f.dp.toPx()), 2f.dp.toPx(), StrokeCap.Square)
    }
}

@Composable
fun HtmlText(
    htmlText: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current
) {
    val parsed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(htmlText)
    }
    val annotatedBody = convertSpanned(parsed)

    Text(
        annotatedBody,
        modifier,
        color,
        fontSize,
        fontStyle,
        fontWeight,
        fontFamily,
        letterSpacing,
        textDecoration,
        textAlign,
        lineHeight,
        overflow,
        softWrap,
        maxLines,
        style = style
    )
}

fun convertSpanned(body: Spanned): AnnotatedString {
    val annotatedString = buildAnnotatedString {
        append(body.toString())
        for (span in body.getSpans(0, body.length, CharacterStyle::class.java)) {
            val style = convertStyle(span)
            val start = body.getSpanStart(span)
            val end = body.getSpanEnd(span)
            addStyle(style, start, end)
        }
    }
    return annotatedString
}

fun convertStyle(span: CharacterStyle): SpanStyle {
    return when (span) {
        is StyleSpan -> when (span.style) {
            Typeface.NORMAL -> SpanStyle()
            Typeface.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
            Typeface.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
            Typeface.BOLD_ITALIC -> SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
            else -> error("Invalid Typeface ${span.style}")
        }
        else -> TODO("Cannot convert $span")
    }
}

