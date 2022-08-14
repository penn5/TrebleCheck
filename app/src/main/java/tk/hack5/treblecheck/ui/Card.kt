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
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickableIconCard(modifier: Modifier, icon: Painter, iconTint: Color, onClick: () -> Unit, content: @Composable () -> Unit) {
    val newModifier = modifier
        .padding(cardOuterPadding)
        .fillMaxWidth()
    val newContent: @Composable () -> Unit = {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(cardInnerPadding),
            Arrangement.Start,
            Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, Modifier.size(cardIconSize), iconTint)
            Spacer(Modifier.width(cardIconSpacerWidth))
            content()
        }
    }

    OutlinedCard(
        onClick = onClick,
        newModifier,
        content = { newContent() }
    )
}

data class AnimationParameters(val floatSpec: FiniteAnimationSpec<Float>, val intSizeSpec: FiniteAnimationSpec<IntSize>) {
    val enterTransition = fadeIn(floatSpec) + expandVertically(intSizeSpec)
    val exitTransition = shrinkVertically(intSizeSpec) + fadeOut(floatSpec)

    companion object {
        val DEFAULT = AnimationParameters(spring(), spring())
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TextCardContent(title: String, htmlBody: String, detail: String, expanded: Boolean, animationParameters: AnimationParameters, icon: Painter? = null) {
    Column(
        Modifier.fillMaxWidth()
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val iconSize = 24.dp
                Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.width(this@BoxWithConstraints.maxWidth - iconSize))
                if (icon != null) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(iconSize))
                } else {
                    val upDown by animateFloatAsState(
                        if (expanded) 1f else -1f,
                        animationParameters.floatSpec
                    )
                    Chevron(upDown, modifier = Modifier.size(iconSize))
                }
            }
        }
        HtmlText(htmlBody)
        Spacer(Modifier.height(explanationSpacing))
        AnimatedVisibility(expanded, enter = animationParameters.enterTransition, exit = animationParameters.exitTransition) {
            HtmlText(detail)
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

