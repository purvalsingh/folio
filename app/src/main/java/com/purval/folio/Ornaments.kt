package com.purval.folio

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/* ---------- paper ---------- */

private val grain: ImageBitmap by lazy {
    val n = 160
    val r = Random(7)
    val px = IntArray(n * n) {
        val a = if (r.nextFloat() < 0.18f) r.nextInt(6, 22) else 0
        (a shl 24) or 0x2A2418
    }
    Bitmap.createBitmap(px, n, n, Bitmap.Config.ARGB_8888).asImageBitmap()
}

/** Flat colour plus a faint ink speckle, so surfaces read as printed paper rather than plastic. */
fun Modifier.paper(color: Color, ink: Ink) = drawBehind {
    drawRect(color)
    drawRect(ShaderBrush(ImageShader(grain, TileMode.Repeated, TileMode.Repeated)), alpha = if (ink.dark) 0.5f else 1f)
}

/** Two hairlines a few dp apart — the frame every old book plate had. */
fun Modifier.doubleRule(ink: Ink, gap: Dp = 4.dp) = drawBehind {
    val g = gap.toPx()
    drawRect(ink.ink.copy(alpha = 0.75f), style = Stroke(1.2.dp.toPx()))
    drawRect(ink.ink.copy(alpha = 0.45f), topLeft = Offset(g, g),
        size = size.copy(size.width - 2 * g, size.height - 2 * g), style = Stroke(0.6.dp.toPx()))
}

@Composable
fun Fleuron(modifier: Modifier = Modifier, glyph: String = "❦") {
    val ink = LocalInk.current
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Canvas(Modifier.weight(1f).height(1.dp)) { drawLine(Brush.horizontalGradient(listOf(Color.Transparent, ink.rule.copy(alpha = .5f))), Offset.Zero, Offset(size.width, 0f), 1.dp.toPx()) }
        Text(glyph, color = ink.rubric, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 12.dp))
        Canvas(Modifier.weight(1f).height(1.dp)) { drawLine(Brush.horizontalGradient(listOf(ink.rule.copy(alpha = .5f), Color.Transparent)), Offset.Zero, Offset(size.width, 0f), 1.dp.toPx()) }
    }
}

/* ---------- wax seal ---------- */

@Composable
fun WaxSeal(label: String, size: Dp, earned: Boolean = true, modifier: Modifier = Modifier) {
    val ink = LocalInk.current
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center
            val r = this.size.minDimension / 2f * 0.92f
            val blob = Path()
            for (k in 0..120) {
                val t = k / 120.0 * 2 * PI
                val rr = r * (1 + 0.045 * sin(t * 9) + 0.02 * sin(t * 23 + 1))
                val p = Offset(c.x + (rr * cos(t)).toFloat(), c.y + (rr * sin(t)).toFloat())
                if (k == 0) blob.moveTo(p.x, p.y) else blob.lineTo(p.x, p.y)
            }
            blob.close()
            if (earned) {
                drawPath(blob, Brush.radialGradient(listOf(ink.rubric.copy(alpha = 1f), ink.rubric.copy(red = ink.rubric.red * .7f)), c, r))
                drawCircle(Color.Black.copy(alpha = .18f), r * 0.72f, c, style = Stroke(r * 0.05f))
                drawCircle(Color.White.copy(alpha = .12f), r * 0.66f, c, style = Stroke(r * 0.02f))
            } else {
                drawPath(blob, ink.faded.copy(alpha = .5f), style = Stroke(1.2.dp.toPx()))
                drawCircle(ink.faded.copy(alpha = .35f), r * 0.7f, c, style = Stroke(0.8.dp.toPx()))
            }
        }
        val fs = with(LocalDensity.current) { (size * if (label.length > 3) 0.2f else 0.32f).toSp() }
        Text(label, fontFamily = Fonts.cinzel, fontSize = fs, color = if (earned) Color(0xFFF6EBDD) else ink.faded,
            textAlign = TextAlign.Center)
    }
}

/* ---------- images ---------- */

private val imgCache = LruCache<String, ImageBitmap>(20)

@Composable
fun rememberPlate(book: Book, name: String?): ImageBitmap? {
    val ctx = LocalContext.current
    val key = "${book.id}/${book.version}/$name"
    fun load(): ImageBitmap? = if (name == null) null else runCatching {
        val f = book.dir?.let { File(it, "$name.webp") }
        val bmp = if (f != null && f.exists()) BitmapFactory.decodeFile(f.path)
        else if (book.imported) null
        else ctx.assets.open("img/$name.webp").use { BitmapFactory.decodeStream(it) }
        bmp?.asImageBitmap()?.also { imgCache.put(key, it) }
    }.getOrNull()
    // previews and snapshots render a single frame, so they decode up front; the app decodes off the main thread
    val first = imgCache[key] ?: if (LocalInspectionMode.current) load() else null
    val v by produceState(first, key) {
        if (value == null && name != null) value = withContext(Dispatchers.IO) { load() }
    }
    return v
}

private val mono = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

/** A book plate: the engraving, desaturated, inside a double rule. Falls back to an ornament. */
@Composable
fun Plate(book: Book, name: String?, modifier: Modifier = Modifier, ratio: Float = 4f / 3f) {
    val ink = LocalInk.current
    val img = rememberPlate(book, name)
    Box(modifier.fillMaxWidth().aspectRatio(ratio).doubleRule(ink).padding(7.dp), contentAlignment = Alignment.Center) {
        if (img != null) {
            Image(img, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, colorFilter = mono,
                alpha = if (ink.dark) 0.82f else 1f)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("❦", fontSize = 54.sp, color = ink.rubric.copy(alpha = .8f))
                Text(book.title, fontFamily = book.era.display, fontSize = 18.sp, color = ink.faded, textAlign = TextAlign.Center)
            }
        }
    }
}

/* ---------- glossary links ---------- */

private val wordRx = Regex("[\\p{L}]+(?:['’][\\p{L}]+)?")

/** Marks every glossary word in [text] as a tappable link. */
fun glossed(text: String, glossary: Map<String, Gloss>, ink: Ink, onWord: (Gloss) -> Unit): AnnotatedString =
    buildAnnotatedString {
        append(text)
        val style = TextLinkStyles(SpanStyle(color = ink.rubric, textDecoration = TextDecoration.Underline))
        wordRx.findAll(text).forEach { m ->
            val g = glossary[m.value.lowercase()] ?: return@forEach
            addLink(LinkAnnotation.Clickable(m.value, style) { onWord(g) }, m.range.first, m.range.last + 1)
        }
    }

/* ---------- drop cap ---------- */

/**
 * Body text whose first letter is a large initial spanning [lines] lines, with the text wrapping
 * beside it and then running full width — measured, not faked with a fixed indent.
 */
@Composable
fun DropCapText(
    text: AnnotatedString, style: TextStyle, capFont: FontFamily, lines: Int, illuminated: Boolean,
    modifier: Modifier = Modifier, capScale: Float = 0.78f,
) {
    val ink = LocalInk.current
    if (text.isEmpty()) return
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val lineH = with(density) { style.lineHeight.toDp() }
        val capBox = lineH * lines - 4.dp
        val gap = 10.dp
        val full = constraints.maxWidth
        val narrow = with(density) { (maxWidth - capBox - gap).roundToPx() }.coerceAtLeast(1)
        val rest = text.subSequence(1, text.length)
        val split = remember(rest, narrow, style) {
            val l = measurer.measure(rest, style, constraints = Constraints(maxWidth = narrow))
            if (l.lineCount <= lines) rest.length else l.getLineEnd(lines - 1)
        }
        Column {
            Row {
                Box(
                    Modifier.size(capBox).padding(top = 2.dp).then(
                        if (illuminated) Modifier.drawBehind {
                            drawRect(ink.rubric)
                            val step = 5.dp.toPx()
                            var x = -size.height
                            while (x < size.width) {
                                drawLine(Color.Black.copy(alpha = .16f), Offset(x, size.height), Offset(x + size.height, 0f), 1f); x += step
                            }
                            drawRect(Color(0xFFF6EBDD).copy(alpha = .6f), topLeft = Offset(4f, 4f),
                                size = size.copy(size.width - 8f, size.height - 8f), style = Stroke(1.5f))
                        } else Modifier.border(0.8.dp, ink.rule)
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text.text.take(1), fontFamily = capFont, color = if (illuminated) Color(0xFFF6EBDD) else ink.rubric,
                        fontSize = with(density) { (capBox * capScale).toSp() }, lineHeight = with(density) { capBox.toSp() })
                }
                Spacer(Modifier.width(gap))
                Text(rest.subSequence(0, split), style = style)
            }
            if (split < rest.length) Text(rest.subSequence(split, rest.length).trimStartSpaces(), style = style,
                modifier = Modifier.width(with(density) { full.toDp() }))
        }
    }
}

private fun AnnotatedString.trimStartSpaces(): AnnotatedString {
    val n = text.indexOfFirst { !it.isWhitespace() }.coerceAtLeast(0)
    return subSequence(n, length)
}
