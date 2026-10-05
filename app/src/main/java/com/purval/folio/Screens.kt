package com.purval.folio

import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle as JTextStyle
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/* ---------- shared bits ---------- */

@Composable
fun Masthead(title: String, line: String, font: FontFamily = Fonts.fraktur) {
    val ink = LocalInk.current
    Column(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 6.dp)) {
        Text(title, fontFamily = font, fontSize = 46.sp, lineHeight = 52.sp, color = ink.ink)
        Text(line, fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 16.sp, color = ink.faded)
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(3.dp).border(0.6.dp, ink.ink.copy(alpha = .6f)))
    }
}

@Composable
fun Label(text: String, color: Color = LocalInk.current.rubric, size: TextUnit = 12.sp) =
    Text(text.uppercase(), fontFamily = Fonts.fellSc, fontSize = size, letterSpacing = 2.sp, color = color)

@Composable
fun Panel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val ink = LocalInk.current
    Box(modifier.fillMaxWidth().paper(ink.paper, ink).doubleRule(ink).padding(16.dp)) { content() }
}

@Composable
fun Empty(glyph: String, line: String) {
    val ink = LocalInk.current
    Column(Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(glyph, fontSize = 48.sp, color = ink.rubric.copy(alpha = .7f))
        Spacer(Modifier.height(8.dp))
        Text(line, fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 17.sp, color = ink.faded, textAlign = TextAlign.Center)
    }
}

@Composable
fun Pill(text: String, filled: Boolean = true, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val ink = LocalInk.current
    Box(
        modifier.clip(RoundedCornerShape(50)).background(if (filled) ink.ink else Color.Transparent)
            .border(1.dp, ink.ink, RoundedCornerShape(50)).clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, fontFamily = Fonts.fellSc, fontSize = 16.sp, letterSpacing = 1.sp, color = if (filled) ink.paper else ink.ink) }
}

@Composable
fun Ring(fraction: Float, size: Dp, content: @Composable () -> Unit) {
    val ink = LocalInk.current
    val anim = remember { Animatable(0f) }
    LaunchedEffect(fraction) { anim.animateTo(fraction.coerceIn(0f, 1f), tween(900, easing = FastOutSlowInEasing)) }
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val s = 5.dp.toPx()
            drawArc(ink.rule, 0f, 360f, false, topLeft = Offset(s, s), size = Size(this.size.width - 2 * s, this.size.height - 2 * s), style = Stroke(s * .5f))
            drawArc(ink.rubric, -90f, 360f * anim.value, false, topLeft = Offset(s, s),
                size = Size(this.size.width - 2 * s, this.size.height - 2 * s), style = Stroke(s, cap = StrokeCap.Round))
            // twelve tick marks, like a clock face on an old dial
            for (k in 0 until 12) rotate(k * 30f) {
                drawLine(ink.faded.copy(alpha = .5f), Offset(center.x, 0f), Offset(center.x, s * .8f), 1f)
            }
        }
        content()
    }
}

/* ---------- Library ---------- */

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(app: App, open: (Book, Int) -> Unit, bind: () -> Unit) {
    val ink = LocalInk.current
    val store = app.store
    val tick = store.tick
    val today = LocalDate.now()
    var doomed by remember { mutableStateOf<Book?>(null) }
    val hour = LocalTime.now().hour
    val greet = when (hour) { in 5..11 -> "Good morrow, reader."; in 12..17 -> "Good afternoon, reader."; else -> "Good evening, reader." }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
        item {
            Masthead("Folio", greet)
            Label("${today.dayOfWeek.getDisplayName(JTextStyle.FULL, Locale.ENGLISH)} · ${roman(today.dayOfMonth)} ${today.month.getDisplayName(JTextStyle.FULL, Locale.ENGLISH)} ${roman(today.year)}", ink.faded, 11.sp)
            Spacer(Modifier.height(16.dp))
            UpdateBanner(app)
        }
        item {
            val read = remember(tick) { store.readToday() }
            val streak = remember(tick) { store.streak() }
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Ring(read / store.goal.toFloat(), 92.dp) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$read", fontFamily = Fonts.cinzel, fontSize = 26.sp, color = ink.ink)
                            Text("of ${store.goal}", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 12.sp, color = ink.faded)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Label("Today's quota")
                        Text(
                            when {
                                read >= store.goal -> "Quota kept. Anything more is glory."
                                read == 0 -> "${store.goal} folios await. One takes a minute."
                                else -> "${store.goal - read} more folio${if (store.goal - read == 1) "" else "s"} to keep the quota."
                            },
                            fontFamily = Fonts.fell, fontSize = 17.sp, lineHeight = 22.sp, color = ink.ink,
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            remember(tick) { store.week() }.forEach { (d, n) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 6.dp)) {
                                    Box(Modifier.size(12.dp).clip(CircleShape).background(if (n > 0) ink.rubric else Color.Transparent)
                                        .border(1.dp, if (n > 0) ink.rubric else ink.faded.copy(alpha = .5f), CircleShape))
                                    Text(d.dayOfWeek.getDisplayName(JTextStyle.NARROW, Locale.ENGLISH), fontSize = 10.sp,
                                        fontFamily = Fonts.fellSc, color = ink.faded)
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            Text("$streak-day streak", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = ink.faded)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            LevelBar(app)
            Spacer(Modifier.height(20.dp))
        }
        val last = store.lastBook?.let { app.book(it) }
        if (last != null) item {
            Label("Continue reading")
            Spacer(Modifier.height(8.dp))
            val pos = store.position[last.id] ?: 0
            Column(Modifier.fillMaxWidth().clickable { open(last, pos) }) {
                Plate(last, last.cards.getOrNull(pos)?.img ?: last.cover, ratio = 16f / 9f)
                Spacer(Modifier.height(8.dp))
                Text(last.cards.getOrNull(pos)?.title ?: last.title, fontFamily = last.era.display, fontSize = 26.sp, color = ink.ink)
                Text("${last.title} · resume at folio ${pos + 1} of ${last.cards.size}", fontFamily = Fonts.fell,
                    fontStyle = FontStyle.Italic, fontSize = 15.sp, color = ink.faded)
            }
            Spacer(Modifier.height(24.dp))
        }
        item { Label("The shelf"); Spacer(Modifier.height(8.dp)) }
        items(app.books, key = { it.id }) { b ->
            val done = remember(tick, b.id) { store.sealedCount(b.id) }
            Row(
                Modifier.fillMaxWidth().combinedClickable(
                    onClick = { open(b, store.position[b.id] ?: 0) },
                    onLongClick = { if (b.imported) doomed = b },
                ).padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.width(78.dp)) { Plate(b, b.cover, ratio = 3f / 4f) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(b.title, fontFamily = b.era.display, fontSize = 25.sp, lineHeight = 28.sp, color = ink.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(listOf(b.author, b.year).filter { it.isNotBlank() }.joinToString(" · "), fontFamily = Fonts.fell,
                        fontStyle = FontStyle.Italic, fontSize = 14.sp, color = ink.faded)
                    Text("${b.era.label} · ${b.cards.size} folios", fontFamily = Fonts.fellSc, fontSize = 12.sp, color = ink.faded)
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(progress = { if (b.cards.isEmpty()) 0f else done / b.cards.size.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(3.dp), color = ink.rubric, trackColor = ink.rule,
                        drawStopIndicator = {}, gapSize = 0.dp)
                    Text(if (done == b.cards.size && done > 0) "Finis — completed" else "$done sealed", fontFamily = Fonts.fell,
                        fontSize = 12.sp, color = if (done == b.cards.size && done > 0) ink.rubric else ink.faded)
                }
            }
        }
        item { OnlineShelf(app) }
        item {
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(3.dp)).border(1.dp, ink.faded.copy(alpha = .5f), RoundedCornerShape(3.dp))
                    .clickable(onClick = bind).padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✒", fontSize = 28.sp, color = ink.rubric)
                    Text("Bind a new book", fontFamily = Fonts.fraktur, fontSize = 26.sp, color = ink.ink)
                    Text("Give Folio any PDF — it becomes flashcards.", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic,
                        fontSize = 14.sp, color = ink.faded, textAlign = TextAlign.Center)
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
    doomed?.let { b ->
        AlertDialog(
            onDismissRequest = { doomed = null }, containerColor = ink.paper,
            title = { Text("Unbind “${b.title}”?", fontFamily = Fonts.fell, color = ink.ink) },
            text = { Text("The cards, plates and your progress in this book will be removed. Saved words stay in your Lexicon.", fontFamily = Fonts.fell, color = ink.ink) },
            confirmButton = { TextButton(onClick = { app.removeBook(b); doomed = null }) { Text("Unbind", color = ink.rubric) } },
            dismissButton = { TextButton(onClick = { doomed = null }) { Text("Keep", color = ink.ink) } },
        )
    }
}

@Composable
fun LevelBar(app: App) {
    val ink = LocalInk.current
    val xp = app.store.xp
    val lv = levelOf(xp)
    val from = xpFor(lv); val to = xpFor(lv + 1)
    Row(verticalAlignment = Alignment.CenterVertically) {
        WaxSeal(roman(lv), 44.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row {
                Text(rankOf(lv), fontFamily = Fonts.fraktur, fontSize = 22.sp, color = ink.ink, modifier = Modifier.weight(1f))
                Text("${xp - from} / ${to - from} xp", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = ink.faded)
            }
            LinearProgressIndicator(progress = { (xp - from) / (to - from).toFloat() }, modifier = Modifier.fillMaxWidth().height(3.dp),
                color = ink.ink, trackColor = ink.rule, drawStopIndicator = {}, gapSize = 0.dp)
            Text("Next rank: ${rankOf(lv + 1)}", fontFamily = Fonts.fell, fontSize = 12.sp, color = ink.faded)
        }
    }
}

/* ---------- Lexicon ---------- */

@Composable
fun LexiconScreen(app: App) {
    val ink = LocalInk.current
    val store = app.store
    var q by rememberSaveable { mutableStateOf("") }
    var test by rememberSaveable { mutableStateOf(false) }
    val shown = store.lexicon.filter { q.isBlank() || it.word.contains(q, true) || it.meaning.contains(q, true) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
        item {
            Masthead("Lexicon", "Your dictionary of rare words — ${store.lexicon.size} collected.")
            if (store.lexicon.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(q, { q = it }, Modifier.weight(1f), placeholder = { Text("Search words", fontFamily = Fonts.fell) },
                        singleLine = true, colors = fieldColors(), textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Fonts.fell, fontSize = 17.sp))
                    Spacer(Modifier.width(10.dp))
                    Pill(if (test) "Show all" else "Test me", filled = test) { test = !test }
                }
                if (test) Text("Tap a word to reveal its meaning.", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic,
                    color = ink.faded, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(8.dp))
            }
        }
        if (store.lexicon.isEmpty()) item { Empty("✒", "Tap any red word while reading\nto learn it and keep it here.") }
        items(shown, key = { it.word }) { w ->
            var open by remember(test) { mutableStateOf(!test) }
            Column(Modifier.fillMaxWidth().clickable { open = !open }.padding(vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(w.word, fontFamily = Fonts.fraktur, fontSize = 28.sp, color = ink.ink, modifier = Modifier.weight(1f))
                    IconButton(onClick = { store.lexicon.remove(w); store.persist() }) { Icon(Icons.Outlined.Close, "Remove", tint = ink.faded) }
                }
                if (open) {
                    Text(w.meaning, fontFamily = Fonts.fell, fontSize = 17.sp, lineHeight = 23.sp, color = ink.ink)
                    if (w.example.isNotBlank()) Text("“${w.example}”", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 15.sp,
                        lineHeight = 21.sp, color = ink.faded, modifier = Modifier.padding(top = 4.dp))
                    app.book(w.bookId)?.let { Text("from ${it.title}", fontFamily = Fonts.fellSc, fontSize = 11.sp, color = ink.rubric, modifier = Modifier.padding(top = 4.dp)) }
                } else Text("· · ·", color = ink.faded, fontSize = 18.sp)
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth().height(0.6.dp).background(ink.rule))
            }
        }
    }
}

@Composable
fun fieldColors() = LocalInk.current.let { ink ->
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ink.ink, unfocusedBorderColor = ink.faded.copy(alpha = .5f), cursorColor = ink.rubric,
        focusedTextColor = ink.ink, unfocusedTextColor = ink.ink, focusedLabelColor = ink.rubric, unfocusedLabelColor = ink.faded,
        focusedPlaceholderColor = ink.faded, unfocusedPlaceholderColor = ink.faded,
    )
}

/* ---------- Commonplace book ---------- */

@Composable
fun CommonplaceScreen(app: App, open: (Book, Int) -> Unit) {
    val ink = LocalInk.current
    val store = app.store
    val ctx = LocalContext.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
        item {
            Masthead("Commonplace", "Renaissance readers copied the lines worth keeping into a commonplace book. This is yours.")
            Spacer(Modifier.height(8.dp))
        }
        if (store.quotes.isEmpty()) item { Empty("❦", "Tap the quote mark under any card\nto keep a line here.") }
        items(store.quotes, key = { it.bookId + it.idx }) { q ->
            val b = app.book(q.bookId)
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Panel(Modifier.clickable { b?.let { open(it, q.idx) } }) {
                    Column {
                        Text("“", fontFamily = Fonts.fraktur, fontSize = 44.sp, lineHeight = 30.sp, color = ink.rubric)
                        Text(q.text, fontFamily = b?.era?.body ?: Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 19.sp, lineHeight = 27.sp, color = ink.ink)
                        b?.cards?.getOrNull(q.idx)?.takeIf { it.qMean.isNotBlank() }?.let { c ->
                            Text("In plain English: ${c.qMean}", fontFamily = Fonts.fell, fontSize = 15.sp, lineHeight = 21.sp,
                                color = ink.faded, modifier = Modifier.padding(top = 8.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(b?.cards?.getOrNull(q.idx)?.qBy?.ifBlank { null } ?: listOfNotNull(b?.author, b?.cards?.getOrNull(q.idx)?.ch).joinToString(" · "), fontFamily = Fonts.fellSc,
                                fontSize = 12.sp, color = ink.faded, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain")
                                    .putExtra(Intent.EXTRA_TEXT, "“${q.text}”\n— ${b?.cards?.getOrNull(q.idx)?.attribution(b) ?: ""}"), "Share quote"))
                            }) { Icon(Icons.Outlined.Share, "Share", tint = ink.faded) }
                            IconButton(onClick = { store.quotes.remove(q); store.persist() }) { Icon(Icons.Outlined.Delete, "Remove", tint = ink.faded) }
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Bookmarks ---------- */

@Composable
fun MarksScreen(app: App, open: (Book, Int) -> Unit) {
    val ink = LocalInk.current
    val store = app.store
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
        item { Masthead("Marks", "Pages you ribboned for later."); Spacer(Modifier.height(8.dp)) }
        if (store.bookmarks.isEmpty()) item { Empty("❧", "Tap the ribbon at the top of any card\nto mark it.") }
        items(store.bookmarks.toList(), key = { it }) { id ->
            val b = app.book(id.substringBefore('#')) ?: return@items
            val i = id.substringAfter('#').toIntOrNull() ?: return@items
            val c = b.cards.getOrNull(i) ?: return@items
            Row(Modifier.fillMaxWidth().clickable { open(b, i) }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(96.dp)) { Plate(b, c.img) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Label(c.ch, size = 11.sp)
                    Text(c.title, fontFamily = b.era.display, fontSize = 21.sp, lineHeight = 24.sp, color = ink.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("${b.title} · folio ${i + 1}", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = ink.faded)
                }
                IconButton(onClick = { store.toggleBookmark(id) }) { Icon(Icons.Outlined.Close, "Remove mark", tint = ink.faded) }
            }
        }
    }
}

/* ---------- Honours (levels, milestones, settings) ---------- */

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HonoursScreen(app: App, account: () -> Unit = {}) {
    val ink = LocalInk.current
    val store = app.store
    val stats = remember(store.tick, store.lexicon.size, store.quotes.size) { store.stats(app.books) }
    val lv = levelOf(store.xp)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp)) {
        Masthead("Honours", "Levels, seals and the rules of your reading.")
        Spacer(Modifier.height(12.dp))
        AccountPanel(app, account)
        Spacer(Modifier.height(16.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            WaxSeal(roman(lv), 120.dp)
            Spacer(Modifier.height(8.dp))
            Text(rankOf(lv), fontFamily = Fonts.fraktur, fontSize = 38.sp, color = ink.ink)
            Text("Level ${roman(lv)} · ${store.xp} xp", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 15.sp, color = ink.faded)
        }
        Spacer(Modifier.height(14.dp))
        LevelBar(app)
        Text("Earn xp: $XP_FOLIO per folio sealed · $XP_SAVE per word or quote kept · $XP_GOAL for a kept quota · $XP_BOOK per finished book.",
            fontFamily = Fonts.fell, fontSize = 13.sp, color = ink.faded, modifier = Modifier.padding(top = 6.dp))
        Spacer(Modifier.height(20.dp))

        Label("The ledger")
        Spacer(Modifier.height(8.dp))
        FlowRow(Modifier.fillMaxWidth(), maxItemsInEachRow = 3) {
            listOf("Folios" to stats.folios, "Books" to stats.books, "Streak" to stats.streak,
                "Words" to stats.words, "Quotes" to stats.quotes, "Quota days" to stats.goalDays).forEach { (k, v) ->
                Column(Modifier.weight(1f).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$v", fontFamily = Fonts.cinzel, fontSize = 28.sp, color = ink.ink)
                    Label(k, ink.faded, 11.sp)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Label("Seals of achievement")
        Spacer(Modifier.height(10.dp))
        FlowRow(Modifier.fillMaxWidth(), maxItemsInEachRow = 3) {
            MILESTONES.forEach { m ->
                val got = store.earned(m.id)
                Column(Modifier.weight(1f).padding(vertical = 10.dp, horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    WaxSeal(m.seal, 62.dp, earned = got)
                    Text(m.title, fontFamily = Fonts.fell, fontSize = 14.sp, color = if (got) ink.ink else ink.faded,
                        textAlign = TextAlign.Center, maxLines = 2)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Label("Daily quota")
        Text("How many folios a day? Each takes about a minute.", fontFamily = Fonts.fell, fontSize = 14.sp, color = ink.faded)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5, 10, 15, 20, 30).forEach { n -> Pill("$n", filled = store.goal == n) { store.goal = n; store.persist() } }
        }
        Spacer(Modifier.height(20.dp))
        Label("Reading light")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("System", filled = store.night == null) { store.night = null; store.persist() }
            Pill("Paper", filled = store.night == false) { store.night = false; store.persist() }
            Pill("Lamplight", filled = store.night == true) { store.night = true; store.persist() }
        }
        Spacer(Modifier.height(20.dp))
        Label("Plain-English helper (optional)")
        Text("Without a key, imported books are distilled offline using the author's own sentences, and word meanings come from a free online dictionary. Add a free Google Gemini API key (aistudio.google.com) and Folio will write plain-English summaries and simpler meanings instead. The key stays on this phone.",
            fontFamily = Fonts.fell, fontSize = 14.sp, lineHeight = 19.sp, color = ink.faded)
        Spacer(Modifier.height(8.dp))
        var key by remember { mutableStateOf(store.geminiKey) }
        OutlinedTextField(key, { key = it.trim(); store.geminiKey = key; store.persist() }, Modifier.fillMaxWidth(),
            placeholder = { Text("Gemini API key", fontFamily = Fonts.fell) }, singleLine = true, colors = fieldColors(),
            visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
        Spacer(Modifier.height(20.dp))
        Label("Edition")
        val ctx = LocalContext.current
        val scope = androidx.compose.runtime.rememberCoroutineScope()
        Text("Folio ${Library.installedVersionName(ctx)}" + (app.updateWaiting()?.let { " · version ${it.versionName} is ready in the Library" } ?: ""),
            fontFamily = Fonts.fell, fontSize = 15.sp, color = ink.ink)
        Spacer(Modifier.height(8.dp))
        Pill(if (app.checking) "Checking…" else "Check for new books & updates", filled = false) { scope.launch { app.refresh() } }
        Spacer(Modifier.height(24.dp))
        Text("Texts: The Prince (Marriott, 1908), The Art of War (Giles, 1910) and the Bhagavad Gita (Arnold, 1885) via Project Gutenberg; Gracián (Jacobs, 1892) via archive.org. The 48 Laws of Power (Greene, 1998) is in copyright: Folio carries original summaries only, with quotes from the public-domain classics behind each law. Summaries written for Folio. Plates: engravings made for Folio, and public-domain Renaissance prints and paintings via Wikimedia Commons, credited under each card. Type: UnifrakturMaguntia, IM Fell, Cinzel, Old Standard, Special Elite (SIL OFL / Apache).",
            fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 12.sp, color = ink.faded, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(28.dp))
    }
}

/* ---------- celebration ---------- */

@Composable
fun CelebrationOverlay(c: Celebration, onDone: () -> Unit) {
    val ink = LocalInk.current
    val haptic = LocalHapticFeedback.current
    val stamp = remember(c) { Animatable(2.4f) }
    val spin = remember(c) { Animatable(-40f) }
    val burst = remember(c) { Animatable(0f) }
    LaunchedEffect(c) {
        kotlinx.coroutines.delay(150)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        kotlinx.coroutines.coroutineScope {
            launch { spin.animateTo(-10f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) }
            launch { burst.animateTo(1f, tween(1100, easing = FastOutSlowInEasing)) }
            stamp.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow))
        }
    }
    Dialog(onDismissRequest = onDone, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CelebrationCard(c, stamp.value, spin.value, burst.value, onDone)
    }
}

@Composable
fun CelebrationCard(c: Celebration, stamp: Float, spin: Float, burst: Float, onDone: () -> Unit) {
    val ink = LocalInk.current
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .55f)).clickable(
            interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDone,
        ), contentAlignment = Alignment.Center) {
            Column(
                Modifier.padding(28.dp).fillMaxWidth().paper(ink.paper, ink).doubleRule(ink, 5.dp).padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.size(170.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        // ink flecks thrown out by the stamp
                        val t = burst
                        for (k in 0 until 22) {
                            val a = k / 22f * 6.283f + (k % 3) * .2f
                            val r = size.minDimension * (.25f + .3f * t) * (if (k % 2 == 0) 1f else .8f)
                            val p = Offset(center.x + r * cos(a), center.y + r * sin(a))
                            drawCircle((if (k % 3 == 0) ink.ink else ink.rubric).copy(alpha = (1f - t).coerceAtLeast(0f) * .9f),
                                (if (k % 4 == 0) 4.5f else 2.8f) * density, p)
                        }
                    }
                    WaxSeal(c.seal, 112.dp, modifier = Modifier.scale(stamp).rotate(spin))
                }
                Label("Huzzah!")
                Spacer(Modifier.height(4.dp))
                Text(c.title, fontFamily = Fonts.fraktur, fontSize = 36.sp, lineHeight = 40.sp, color = ink.ink, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(c.message, fontFamily = Fonts.fell, fontSize = 18.sp, lineHeight = 25.sp, color = ink.ink, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Pill("Onward  ❧", onClick = onDone)
            }
        }
}

/* ---------- online library: app updates + new books ---------- */

@Composable
fun UpdateBanner(app: App) {
    val ink = LocalInk.current
    val ctx = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val r = app.updateWaiting() ?: return
    var progress by remember { mutableStateOf<Float?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var apk by remember { mutableStateOf<java.io.File?>(null) }
    Panel {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WaxSeal("✦", 40.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Label("A new edition of Folio")
                    Text("Version ${r.versionName}" + if (r.sizeKb > 0) " · ${r.sizeKb / 1024} MB" else "",
                        fontFamily = Fonts.fell, fontSize = 16.sp, color = ink.ink)
                }
            }
            if (r.notes.isNotBlank()) Text(r.notes, fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 14.sp,
                lineHeight = 19.sp, color = ink.faded, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(10.dp))
            val p = progress
            if (p != null) {
                LinearProgressIndicator(progress = { p }, modifier = Modifier.fillMaxWidth().height(3.dp), color = ink.rubric,
                    trackColor = ink.rule, drawStopIndicator = {}, gapSize = 0.dp)
                Text("Fetching the new edition…", fontFamily = Fonts.fell, fontSize = 13.sp, color = ink.faded)
            } else {
                Pill(if (apk != null) "Install update" else "Update — your progress is kept", modifier = Modifier.fillMaxWidth()) {
                    error = null
                    val ready = apk
                    when {
                        !Library.canInstall(ctx) -> Library.askInstallPermission(ctx)
                        ready != null -> Library.launchInstaller(ctx, ready)
                        else -> scope.launch {
                            progress = 0f
                            runCatching { Library.downloadApk(ctx, r) { progress = it } }
                                .onSuccess { apk = it; progress = null; Library.launchInstaller(ctx, it) }
                                .onFailure { progress = null; error = it.message ?: "Download failed." }
                        }
                    }
                }
                if (!Library.canInstall(ctx)) Text("Android will ask once to allow Folio to install its own updates.",
                    fontFamily = Fonts.fell, fontSize = 12.sp, color = ink.faded, modifier = Modifier.padding(top = 6.dp))
            }
            error?.let { Text(it, fontFamily = Fonts.fell, fontSize = 13.sp, color = ink.rubric, modifier = Modifier.padding(top = 6.dp)) }
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
fun OnlineShelf(app: App) {
    val ink = LocalInk.current
    val ctx = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val list = app.available()
    if (list.isEmpty()) return
    Spacer(Modifier.height(18.dp))
    Label("From the online library")
    Spacer(Modifier.height(6.dp))
    list.forEach { e ->
        var progress by remember(e.id) { mutableStateOf<Float?>(null) }
        var error by remember(e.id) { mutableStateOf<String?>(null) }
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(e.title, fontFamily = e.era.display, fontSize = 23.sp, lineHeight = 26.sp, color = ink.ink)
                Text(listOf(e.author, e.year).filter { it.isNotBlank() }.joinToString(" · "), fontFamily = Fonts.fell,
                    fontStyle = FontStyle.Italic, fontSize = 14.sp, color = ink.faded)
                Text("${e.era.label} · ${e.cards} folios · ${maxOf(1, e.sizeKb / 1024)} MB", fontFamily = Fonts.fellSc, fontSize = 12.sp, color = ink.faded)
                if (e.blurb.isNotBlank()) Text(e.blurb, fontFamily = Fonts.fell, fontSize = 14.sp, lineHeight = 19.sp, color = ink.ink,
                    maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                val p = progress
                if (p != null) LinearProgressIndicator(progress = { p }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(3.dp),
                    color = ink.rubric, trackColor = ink.rule, drawStopIndicator = {}, gapSize = 0.dp)
                error?.let { Text(it, fontFamily = Fonts.fell, fontSize = 13.sp, color = ink.rubric) }
            }
            Spacer(Modifier.width(12.dp))
            if (progress == null) Pill("Fetch", filled = false) {
                error = null; progress = 0f
                scope.launch {
                    runCatching { Library.install(ctx, e) { progress = it } }
                        .onSuccess { app.put(it); progress = null }
                        .onFailure { progress = null; error = it.message ?: "Could not download." }
                }
            }
        }
    }
}

/* ---------- account ---------- */

@Composable
fun AccountPanel(app: App, open: () -> Unit) {
    val ink = LocalInk.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val s = app.session
    Panel {
        Column {
            if (s == null) {
                Label("Your account")
                Text("Sign in to keep your pages, words and quotes safe and in step across phones. Your library is encrypted on this phone before it is uploaded.",
                    fontFamily = Fonts.fell, fontSize = 15.sp, lineHeight = 21.sp, color = ink.ink)
                Spacer(Modifier.height(10.dp))
                Pill(if (CloudConfig.ready) "Sign in or create account" else "Accounts open soon", filled = CloudConfig.ready, modifier = Modifier.fillMaxWidth()) {
                    if (CloudConfig.ready) open()
                }
            } else {
                Label("Signed in")
                Text(s.email, fontFamily = Fonts.fell, fontSize = 18.sp, color = ink.ink)
                Text(app.syncNote ?: "Encrypted sync is on.", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 13.sp,
                    lineHeight = 18.sp, color = ink.faded)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill(if (app.syncing) "Syncing…" else "Sync now", filled = false) { scope.launch { app.sync() } }
                    Pill("Sign out", filled = false) { scope.launch { app.signOut() } }
                }
            }
        }
    }
}

@Composable
fun AccountScreen(app: App, done: () -> Unit) {
    val ink = LocalInk.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var mode by rememberSaveable { mutableStateOf("in") } // in | up | forgot | code
    var email by rememberSaveable { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    var pw2 by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf<String?>(null) }
    val emailOk = android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    fun go(block: suspend () -> Unit) {
        msg = null; busy = true
        scope.launch {
            runCatching { block() }.onFailure { msg = it.message ?: "Something went wrong." }
            busy = false
        }
    }
    val field = androidx.compose.ui.text.TextStyle(fontFamily = Fonts.fell, fontSize = 18.sp, color = ink.ink)
    Column(
        Modifier.fillMaxSize().statusBarsPadding().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = done) { Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ink.ink) }
        }
        Masthead(
            when (mode) { "up" -> "Join Folio"; "forgot", "code" -> "New Password"; else -> "Welcome back" },
            when (mode) {
                "up" -> "One account keeps your library on every phone you use."
                "forgot" -> "We'll email you a 6-digit code."
                "code" -> "Enter the code from your email and choose a new password."
                else -> "Sign in to sync your pages, words and quotes."
            },
        )
        Spacer(Modifier.height(16.dp))
        if (mode != "code") OutlinedTextField(email, { email = it.trim() }, Modifier.fillMaxWidth(), label = { Text("Email", fontFamily = Fonts.fell) },
            singleLine = true, colors = fieldColors(), textStyle = field,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        if (mode == "code") OutlinedTextField(code, { code = it.filter(Char::isDigit).take(8) }, Modifier.fillMaxWidth(),
            label = { Text("Code from email", fontFamily = Fonts.fell) }, singleLine = true, colors = fieldColors(), textStyle = field,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        if (mode != "forgot") {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(pw, { pw = it }, Modifier.fillMaxWidth(), label = { Text(if (mode == "code") "New password" else "Password", fontFamily = Fonts.fell) },
                singleLine = true, colors = fieldColors(), textStyle = field, visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
        }
        if (mode == "up" || mode == "code") {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(pw2, { pw2 = it }, Modifier.fillMaxWidth(), label = { Text("Repeat password", fontFamily = Fonts.fell) },
                singleLine = true, colors = fieldColors(), textStyle = field, visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            Text("At least 8 characters. Your password also locks your synced library — keep it safe.",
                fontFamily = Fonts.fell, fontSize = 13.sp, color = ink.faded, modifier = Modifier.padding(top = 4.dp))
        }
        msg?.let { Text(it, fontFamily = Fonts.fell, fontSize = 15.sp, color = ink.rubric, modifier = Modifier.padding(top = 10.dp)) }
        Spacer(Modifier.height(16.dp))
        val pwOk = pw.length >= 8 && (mode == "in" || pw == pw2)
        val ok = !busy && when (mode) { "forgot" -> emailOk; "code" -> code.length >= 6 && pwOk; else -> emailOk && pwOk }
        Pill(
            if (busy) "One moment…" else when (mode) { "up" -> "Create account"; "forgot" -> "Email me a code"; "code" -> "Set password & sign in"; else -> "Sign in" },
            filled = ok, modifier = Modifier.fillMaxWidth(),
        ) {
            if (!ok) {
                msg = when {
                    mode != "code" && !emailOk -> "Please enter a valid email."
                    pw.length < 8 -> "Password needs at least 8 characters."
                    else -> "The two passwords don't match."
                }
                return@Pill
            }
            when (mode) {
                "in" -> go { app.signedIn(Cloud.signIn(email, pw)); done() }
                "up" -> go { app.signedIn(Cloud.signUp(email, pw)); done() }
                "forgot" -> go { Cloud.sendResetCode(email); mode = "code"; msg = "Code sent to $email." }
                "code" -> go { app.signedIn(Cloud.resetPassword(email, code, pw)); done() }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { msg = null; mode = if (mode == "in") "up" else "in" }) {
                Text(if (mode == "in") "New here? Create an account" else "Have an account? Sign in", fontFamily = Fonts.fell, color = ink.ink)
            }
            if (mode == "in") TextButton(onClick = { msg = null; mode = "forgot" }) { Text("Forgot password?", fontFamily = Fonts.fell, color = ink.faded) }
        }
        Spacer(Modifier.height(18.dp))
        Text("How your data is protected: passwords are hashed by the server and never stored on this phone. Your library is encrypted here with a key made from your password (AES-256) before upload, so even the server sees only scrambled text. The sign-in token is sealed in this phone's secure hardware.",
            fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 13.sp, lineHeight = 18.sp, color = ink.faded)
        Spacer(Modifier.height(32.dp))
    }
}
