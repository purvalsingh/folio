package com.purval.folio

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs

/** A volume on a shelf: either on this phone ([book]) or waiting in the online library ([entry]). */
data class Volume(val id: String, val title: String, val author: String, val year: String, val era: Era,
                  val book: Book?, val entry: Library.Entry?, val blurb: String)

data class ShelfInfo(val name: String, val note: String, val coming: List<String>)

/** Default shelf order, used until the online catalog says otherwise. */
val DEFAULT_SHELVES = listOf(
    ShelfInfo("Power & Strategy", "Rulers, generals and the craft of influence", emptyList()),
    ShelfInfo("War & Statecraft", "Duels, republics and the art of ruling well", emptyList()),
    ShelfInfo("Persuasion & Rhetoric", "Speaking, charming and winning people over", emptyList()),
    ShelfInfo("Stoic Wisdom", "Calm in a world you cannot control", emptyList()),
    ShelfInfo("Eastern Paths", "The Way, the Gita and the Buddha's verses", emptyList()),
    ShelfInfo("Indian Wisdom", "Fables, couplets and counsel from old India", emptyList()),
    ShelfInfo("Mind & the Crowd", "How thought shapes us, and crowds shape thought", emptyList()),
    ShelfInfo("The Human Mind", "Habits, dreams, emotions and the unconscious", emptyList()),
    ShelfInfo("Love & Human Nature", "Romance, friendship and trusting yourself", emptyList()),
    ShelfInfo("Courtly Wisdom", "Maxims for surviving the courts of kings", emptyList()),
)
const val BOUND_SHELF = "Your Bound Books"

/** Assembles shelves from the phone's books plus the catalog's books not yet downloaded. */
fun App.shelves(): List<Pair<ShelfInfo, List<Volume>>> {
    val infos = catalog?.shelves?.takeIf { it.isNotEmpty() } ?: DEFAULT_SHELVES
    val local = books.filter { !it.imported }.map { Volume(it.id, it.title, it.author, it.year, it.era, it, null, it.blurb) }
    val remote = available().map { Volume(it.id, it.title, it.author, it.year, it.era, null, it, it.blurb) }
    val shelfOf = (books.associate { it.id to it.shelf } + catalog?.books.orEmpty().associate { it.id to it.shelf })
    val all = local + remote
    val named = infos.map { s -> s to all.filter { (shelfOf[it.id] ?: "") == s.name } }
    val homeless = all.filter { v -> infos.none { it.name == shelfOf[v.id] } }
    val bound = books.filter { it.imported }.map { Volume(it.id, it.title, it.author, it.year, it.era, it, null, it.blurb) }
    return named.map { (s, v) -> if (s === infos.first()) s to (v + homeless) else s to v } +
        (ShelfInfo(BOUND_SHELF, "Your own PDFs, bound as flashcards", emptyList()) to bound)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookcaseScreen(app: App, open: (Book, Int) -> Unit, bind: () -> Unit, recall: () -> Unit = {}, journey: (Journey) -> Unit = {}, desk: () -> Unit = {}) {
    val ink = LocalInk.current
    val shelves = app.shelves()
    val list = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var picked by remember { mutableStateOf<Volume?>(null) }
    val headerItems = 2 // masthead block + sticky tabs

    LazyColumn(Modifier.fillMaxSize(), state = list, contentPadding = PaddingValues(bottom = 28.dp)) {
        item { Column(Modifier.padding(horizontal = 20.dp)) { LibraryTop(app, open, recall, journey, desk) } }
        stickyHeader {
            Column(Modifier.fillMaxWidth().paper(ink.page, ink)) {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val open = app.desk().count { !it.finished }
                    if (open > 0) item {
                        Box(
                            Modifier.clip(RoundedCornerShape(50)).background(ink.rubric).clickable(onClick = desk)
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                        ) { Text("Reading now · $open", fontFamily = Fonts.fellSc, fontSize = 13.sp, letterSpacing = .5.sp, color = ink.paper) }
                    }
                    items(shelves.size) { i ->
                        val (s, vols) = shelves[i]
                        Box(
                            Modifier.clip(RoundedCornerShape(50)).border(1.dp, ink.ink.copy(alpha = .6f), RoundedCornerShape(50))
                                .clickable { scope.launch { list.animateScrollToItem(headerItems + i) } }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                        ) {
                            Text("${s.name} · ${vols.size}", fontFamily = Fonts.fellSc, fontSize = 13.sp, letterSpacing = .5.sp, color = ink.ink)
                        }
                    }
                    item {
                        Box(
                            Modifier.clip(RoundedCornerShape(50)).border(1.dp, ink.faded.copy(alpha = .5f), RoundedCornerShape(50))
                                .clickable { scope.launch { list.animateScrollToItem(headerItems + shelves.size) } }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                        ) { Text("In the bindery ⋯", fontFamily = Fonts.fellSc, fontSize = 13.sp, color = ink.faded) }
                    }
                }
                Box(Modifier.fillMaxWidth().height(0.6.dp).background(ink.rule))
            }
        }
        items(shelves.size) { i ->
            val (s, vols) = shelves[i]
            Shelf(app, s, vols, picked?.id, onPick = { picked = it }, bind = bind.takeIf { s.name == BOUND_SHELF })
        }
        item {
            // shelves still in the bindery: announced, not yet written
            val coming = app.catalog?.coming.orEmpty().ifEmpty { DEFAULT_COMING }
            coming.forEach { s -> Shelf(app, s, emptyList(), null, onPick = {}, bind = null) }
        }
    }

    picked?.let { v ->
        BookSheet(app, v, onDismiss = { picked = null }, onRead = { b -> picked = null; open(b, app.store.position[b.id] ?: 0) })
    }
}

val DEFAULT_COMING = listOf(
    ShelfInfo("Fortune & Wealth", "In the bindery", listOf("The Wealth of Nations", "Walden", "The Richest Man in Babylon")),
    ShelfInfo("Poetry of Life", "In the bindery", listOf("Rubaiyat", "Gitanjali", "Leaves of Grass")),
    ShelfInfo("Myth & Hero", "In the bindery", listOf("The Odyssey", "Mahabharata Tales", "Beowulf")),
)

/* ---------- one shelf ---------- */

@Composable
private fun Shelf(app: App, info: ShelfInfo, vols: List<Volume>, pickedId: String?, onPick: (Volume) -> Unit, bind: (() -> Unit)?) {
    val ink = LocalInk.current
    val ghost = vols.isEmpty() && bind == null
    Column(Modifier.fillMaxWidth().padding(top = 22.dp)) {
        // engraved name plate
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("❧", color = ink.rubric, fontSize = 16.sp, modifier = Modifier.rotate(180f))
            Spacer(Modifier.width(8.dp))
            Column(
                Modifier.weight(1f).paper(ink.paper, ink).doubleRule(ink, 3.dp).padding(horizontal = 12.dp, vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(info.name.uppercase(), fontFamily = Fonts.fellSc, fontSize = 15.sp, letterSpacing = 2.sp,
                    color = if (ghost) ink.faded else ink.ink, textAlign = TextAlign.Center)
                Text(if (ghost) "In the bindery — coming soon" else "${info.note} · ${vols.size} volume${if (vols.size == 1) "" else "s"}",
                    fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 12.sp, color = ink.faded, textAlign = TextAlign.Center,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(8.dp))
            Text("❧", color = ink.rubric, fontSize = 16.sp)
        }
        Spacer(Modifier.height(10.dp))
        // the books, standing on a plank
        Box(Modifier.fillMaxWidth().height(206.dp)) {
            LazyRow(
                Modifier.fillMaxWidth().height(200.dp),
                contentPadding = PaddingValues(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                if (ghost) items(info.coming.ifEmpty { listOf("", "", "") }) { t -> GhostSpine(t) }
                else items(vols, key = { it.id }) { v -> Spine(app, v, lifted = v.id == pickedId) { onPick(v) } }
                if (bind != null) item { BindSpine(bind) }
                if (!ghost && vols.size < 4) items(4 - vols.size) { GhostSpine("") }
            }
            Plank(Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun Plank(modifier: Modifier) {
    val ink = LocalInk.current
    Box(modifier.fillMaxWidth().height(10.dp).padding(horizontal = 10.dp).drawBehind {
        drawRect(if (ink.dark) Color(0xFF4A4037) else ink.ink.copy(alpha = .82f)) // dark oak at night, not a pale bar
        // wood grain: fine hairlines along the plank
        for (k in 1..3) drawLine(ink.paper.copy(alpha = .14f), Offset(0f, size.height * k / 4f), Offset(size.width, size.height * k / 4f), .8f)
        // brackets
        listOf(.12f, .88f).forEach { x ->
            drawLine(ink.ink.copy(alpha = .6f), Offset(size.width * x, size.height), Offset(size.width * x - 10f, size.height + 18f), 3f)
        }
    })
}

/* ---------- spines ---------- */

private data class SpineStyle(val bg: Color, val fg: Color, val band: Color, val outline: Boolean)

/**
 * Bindings keep their own colours day and night, like real books: dark leather carries gilt or cream
 * lettering, pale vellum and stone carry ink. Night only dims the pale ones, it never inverts them.
 */
@Composable
private fun styleFor(era: Era, seed: Int): SpineStyle {
    val dark = LocalInk.current.dark
    val cream = Color(0xFFF3E7CF); val gilt = Color(0xFFE2C98F); val red = Color(0xFFA8382A); val inkC = Color(0xFF1C1915)
    val shade = (seed % 3) * .045f // neighbouring volumes differ a little, like a real shelf
    fun cloth(c: Long) = lerp(Color(c), Color.Black, shade)
    fun pale(day: Long, night: Long) = lerp(Color(if (dark) night else day), Color.Black, shade * .5f)
    return when (era) {
        Era.RENAISSANCE, Era.GERMANIC -> SpineStyle(cloth(0xFF221E1A), gilt, red, dark)
        Era.INDIC -> SpineStyle(cloth(0xFF84281B), cream, gilt.copy(alpha = .85f), false)
        Era.EASTERN -> SpineStyle(cloth(0xFF2F2925), cream, red, dark)
        Era.BAROQUE -> SpineStyle(pale(0xFFEDE3CF, 0xFFC9BFAB), inkC, red, true)
        Era.ANCIENT -> SpineStyle(pale(0xFFD8CDB8, 0xFFB9AE9A), inkC, Color(0xFF5E564A), true)
        Era.ENLIGHTENMENT -> SpineStyle(cloth(0xFF34414B), cream, gilt.copy(alpha = .8f), false)
        Era.VICTORIAN -> SpineStyle(cloth(0xFF2F3C32), gilt, gilt.copy(alpha = .7f), false)
        Era.MODERN -> SpineStyle(cloth(0xFF4A423A), cream, cream.copy(alpha = .5f), false)
    }
}

private fun spineSize(id: String): Pair<Dp, Dp> {
    val h = abs(id.hashCode())
    return (50 + h % 16).dp to (160 + (h / 7) % 34).dp
}

@Composable
private fun Spine(app: App, v: Volume, lifted: Boolean, onTap: () -> Unit) {
    val ink = LocalInk.current
    val haptic = LocalHapticFeedback.current
    var pressed by remember { mutableStateOf(false) }
    val lift by animateDpAsState(if (lifted) (-22).dp else if (pressed) (-10).dp else 0.dp, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow), label = "lift")
    val tilt by animateFloatAsState(if (pressed) -3f else 0f, spring(Spring.DampingRatioMediumBouncy), label = "tilt")
    val (w, h) = spineSize(v.id)
    val st = styleFor(v.era, abs(v.id.hashCode()))
    val onShelf = v.book != null
    val done = remember(app.store.tick, v.id) { v.book?.let { app.store.sealedCount(it.id) } ?: 0 }
    val total = v.book?.cards?.size ?: v.entry?.cards ?: 0
    val reading = onShelf && done in 1 until total
    val finished = onShelf && total > 0 && done >= total

    Box(
        Modifier.offset(y = lift).rotate(tilt).size(w, h)
            .pointerInput(v.id) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onTap() },
                )
            }
            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomStart = 1.dp, bottomEnd = 1.dp))
            .background(st.bg)
            .then(if (st.outline) Modifier.border(1.dp, (if (ink.dark) Color.Black else ink.ink).copy(alpha = .55f), RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)) else Modifier)
            .drawBehind {
                // raised bands, like a hand-bound spine
                listOf(.08f, .12f, .86f, .9f).forEach { y -> drawLine(st.band, Offset(5f, size.height * y), Offset(size.width - 5f, size.height * y), 2.2f) }
                // a soft highlight on the curve of the spine
                drawRect(Color.White.copy(alpha = .07f), topLeft = Offset(size.width * .18f, 0f), size = Size(size.width * .12f, size.height))
                // still in the online library: a light veil over the cloth only, so the title stays crisp
                if (!onShelf) drawRect(ink.page.copy(alpha = .28f))
            },
        contentAlignment = Alignment.Center,
    ) {
        // long titles wrap onto two lines down the spine instead of being cut off
        // short titles in the era's display face; long ones in its text face, which stays legible at small sizes
        val n = v.title.length
        val long = n > 15
        val size = when { n <= 11 -> 16 + (if (v.era == Era.BAROQUE) 3 else 0); n > 24 -> 13; else -> 14 }
        Text(
            v.title, fontFamily = if (long) v.era.body else v.era.display, fontSize = size.sp, lineHeight = (size + 2).sp, color = st.fg,
            maxLines = if (n > 11) 2 else 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
            modifier = Modifier.requiredWidth(h * .7f).rotate(-90f),
        )
        Text(if (onShelf) v.era.fleuron else "⇣", color = if (onShelf) st.band else st.fg, fontSize = if (onShelf) 11.sp else 15.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp))
        if (reading) Box(Modifier.align(Alignment.TopEnd).padding(end = 6.dp).width(5.dp).height(h * .32f).background(ink.rubric)) // ribbon
        if (finished) WaxSeal("✓", 22.dp, modifier = Modifier.align(Alignment.TopCenter).padding(top = h * .14f))
    }
}

@Composable
private fun GhostSpine(title: String) {
    val ink = LocalInk.current
    val (w, h) = spineSize(title.ifBlank { "ghost${title.length}" } + title)
    Box(
        Modifier.size(w, h).drawBehind {
            drawRoundRect(ink.faded.copy(alpha = .55f), style = Stroke(1.2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f))
        },
        contentAlignment = Alignment.Center,
    ) {
        if (title.isNotBlank()) Text(title, fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 14.sp, color = ink.faded,
            maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.requiredWidth(h * .7f).rotate(-90f))
    }
}

@Composable
private fun BindSpine(bind: () -> Unit) {
    val ink = LocalInk.current
    Box(
        Modifier.size(54.dp, 170.dp).clip(RoundedCornerShape(3.dp)).border(1.2.dp, ink.rubric, RoundedCornerShape(3.dp)).clickable(onClick = bind),
        contentAlignment = Alignment.Center,
    ) {
        Text("+  Bind a PDF", fontFamily = Fonts.fellSc, fontSize = 14.sp, color = ink.rubric, maxLines = 1,
            modifier = Modifier.requiredWidth(150.dp).rotate(-90f), textAlign = TextAlign.Center)
    }
}

/* ---------- pulled-out book ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookSheet(app: App, v: Volume, onDismiss: () -> Unit, onRead: (Book) -> Unit) {
    val ink = LocalInk.current
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val progress = remember { mutableStateMapOf<String, Float>() }
    var error by remember { mutableStateOf<String?>(null) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ink.paper) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 30.dp)) {
            val b = v.book ?: app.book(v.id)
            if (b != null) Plate(b, b.cover ?: b.cards.firstOrNull()?.img, ratio = 16f / 9f)
            else Box(Modifier.fillMaxWidth().height(120.dp).doubleRule(ink), contentAlignment = Alignment.Center) {
                Text(v.era.fleuron, fontSize = 48.sp, color = ink.rubric)
            }
            Spacer(Modifier.height(12.dp))
            Text(v.title, fontFamily = v.era.display, fontSize = if (v.era == Era.BAROQUE) 38.sp else 30.sp, lineHeight = 40.sp, color = ink.ink)
            Text(listOf(v.author, v.year).filter { it.isNotBlank() }.joinToString(" · "), fontFamily = Fonts.fell, fontStyle = FontStyle.Italic,
                fontSize = 15.sp, color = ink.faded)
            Text("${v.era.label} · ${b?.cards?.size ?: v.entry?.cards ?: 0} folios" + (v.entry?.let { if (b == null) " · ${maxOf(1, it.sizeKb / 1024)} MB" else "" } ?: ""),
                fontFamily = Fonts.fellSc, fontSize = 12.sp, color = ink.faded)
            Spacer(Modifier.height(10.dp))
            Text(v.blurb, fontFamily = v.era.body, fontSize = 17.sp, lineHeight = 24.sp, color = ink.ink)
            Spacer(Modifier.height(16.dp))
            if (b != null) {
                val done = remember(app.store.tick) { app.store.sealedCount(b.id) }
                LinearProgressIndicator(progress = { if (b.cards.isEmpty()) 0f else done / b.cards.size.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(3.dp), color = ink.rubric, trackColor = ink.rule, drawStopIndicator = {}, gapSize = 0.dp)
                Text(if (done >= b.cards.size && done > 0) "Finis — completed" else "$done of ${b.cards.size} sealed",
                    fontFamily = Fonts.fell, fontSize = 13.sp, color = ink.faded, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(14.dp))
                val pos = app.store.position[b.id] ?: 0
                Pill(if (pos > 0) "Continue at folio ${pos + 1}" else "Open the book", modifier = Modifier.fillMaxWidth()) { onRead(b) }
            } else {
                val e = v.entry!!
                val p = progress[e.id]
                if (p != null) {
                    LinearProgressIndicator(progress = { p }, modifier = Modifier.fillMaxWidth().height(3.dp), color = ink.rubric,
                        trackColor = ink.rule, drawStopIndicator = {}, gapSize = 0.dp)
                    Text("Fetching the volume from the library…", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 13.sp,
                        color = ink.faded, modifier = Modifier.padding(top = 4.dp))
                } else Pill("Fetch from the library", modifier = Modifier.fillMaxWidth()) {
                    error = null; progress[e.id] = 0f
                    scope.launch {
                        runCatching { Library.install(ctx, e) { progress[e.id] = it } }
                            .onSuccess { nb -> app.put(nb); progress.remove(e.id); onRead(nb) }
                            .onFailure { progress.remove(e.id); error = it.message ?: "Could not download." }
                    }
                }
                error?.let { Text(it, fontFamily = Fonts.fell, fontSize = 13.sp, color = ink.rubric, modifier = Modifier.padding(top = 6.dp)) }
            }
            if (b != null && b.imported) {
                var sure by remember { mutableStateOf(false) }
                androidx.compose.material3.TextButton(onClick = { if (sure) { app.removeBook(b); onDismiss() } else sure = true }) {
                    Text(if (sure) "Tap again to unbind (removes its cards)" else "Unbind this book", fontFamily = Fonts.fell, color = ink.rubric)
                }
            }
            b?.translator?.takeIf { it.isNotBlank() }?.let {
                Text(it, fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 12.sp, lineHeight = 17.sp, color = ink.faded,
                    modifier = Modifier.padding(top = 14.dp))
            }
        }
    }
}
