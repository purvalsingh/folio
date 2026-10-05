package com.purval.folio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** A book that has been opened: how far along it is and when it was last read. */
data class DeskBook(val book: Book, val done: Int, val pos: Int, val opened: Long) {
    val total get() = book.cards.size
    val finished get() = total > 0 && done >= total
    val percent get() = if (total == 0) 0 else done * 100 / total
}

/** Every opened book, most recently read first. */
fun App.desk(): List<DeskBook> = books.mapNotNull { b ->
    val done = store.sealedCount(b.id)
    val pos = store.position[b.id]
    if (done == 0 && pos == null) null else DeskBook(b, done, pos ?: 0, store.opened[b.id] ?: 0L)
}.sortedWith(compareByDescending<DeskBook> { it.opened }.thenByDescending { it.book.id == store.lastBook }.thenByDescending { it.done })

private fun ago(ms: Long): String {
    if (ms == 0L) return ""
    val d = ChronoUnit.DAYS.between(Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now())
    return when {
        d <= 0 -> "read today"
        d == 1L -> "read yesterday"
        d < 7 -> "read $d days ago"
        d < 14 -> "read a week ago"
        else -> "read ${d / 7} weeks ago"
    }
}

@Composable
private fun Progress(p: DeskBook) {
    val ink = LocalInk.current
    LinearProgressIndicator(progress = { p.done / p.total.coerceAtLeast(1).toFloat() }, modifier = Modifier.fillMaxWidth().height(3.dp),
        color = ink.rubric, trackColor = ink.rule, drawStopIndicator = {}, gapSize = 0.dp)
}

/* ---------- on the Library page ---------- */

@Composable
fun ReadingNowRow(app: App, open: (Book, Int) -> Unit, seeAll: () -> Unit) {
    val ink = LocalInk.current
    val desk = remember(app.store.tick, app.books.size, app.store.lastBook) { app.desk() }
    val reading = desk.filter { !it.finished }
    if (desk.isEmpty()) return
    Spacer(Modifier.height(18.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Label(if (reading.isEmpty()) "Reading now" else "Reading now · ${reading.size}", modifier = Modifier.weight(1f))
        Text("See all ›", fontFamily = Fonts.fellSc, fontSize = 14.sp, color = ink.rubric,
            modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = seeAll).padding(horizontal = 8.dp, vertical = 6.dp))
    }
    val first = reading.firstOrNull()
    if (first == null) {
        Text("Every book you opened is finished. Pull a new one from the bookcase.", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic,
            fontSize = 14.sp, color = ink.faded)
        return
    }
    Spacer(Modifier.height(6.dp))
    val b = first.book
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(3.dp)).clickable { open(b, first.pos) }.paper(ink.paper, ink).doubleRule(ink).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(96.dp)) { Plate(b, b.cards.getOrNull(first.pos)?.img ?: b.cover) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Label("Continue", size = 11.sp)
            Text(b.cards.getOrNull(first.pos)?.title ?: b.title, fontFamily = b.era.display, fontSize = 21.sp, lineHeight = 25.sp,
                color = ink.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("${b.title} · folio ${first.pos + 1} of ${first.total}", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic,
                fontSize = 13.sp, color = ink.faded, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Progress(first)
        }
    }
    val rest = reading.drop(1)
    if (rest.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(rest, key = { it.book.id }) { p ->
                Column(Modifier.width(150.dp).clip(RoundedCornerShape(3.dp)).clickable { open(p.book, p.pos) }.paper(ink.paper, ink).doubleRule(ink).padding(8.dp)) {
                    Plate(p.book, p.book.cards.getOrNull(p.pos)?.img ?: p.book.cover)
                    Spacer(Modifier.height(6.dp))
                    Text(p.book.title, fontFamily = p.book.era.display, fontSize = 16.sp, lineHeight = 19.sp, color = ink.ink,
                        maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.height(40.dp))
                    Progress(p)
                    Text("${p.percent}% · ${p.total - p.done} to go", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 12.sp,
                        color = ink.faded, modifier = Modifier.padding(top = 3.dp))
                }
            }
        }
    }
}

/* ---------- the dedicated page ---------- */

@Composable
fun ReadingNowScreen(app: App, open: (Book, Int) -> Unit, back: () -> Unit) {
    val ink = LocalInk.current
    val desk = remember(app.store.tick, app.books.size) { app.desk() }
    val reading = desk.filter { !it.finished }
    val finished = desk.filter { it.finished }
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
        item {
            IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ink.ink) }
            Masthead("Reading Now", "The books open on your desk, and the ones you have finished.")
            if (desk.isNotEmpty()) {
                val left = reading.sumOf { it.total - it.done }
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Stat("${reading.size}", "open")
                    Stat("${finished.size}", "finished")
                    Stat("$left", "folios to go")
                }
            }
        }
        if (desk.isEmpty()) item { Empty("❦", "No book is open yet.\nPull one from the bookcase to begin.") }
        if (reading.isNotEmpty()) {
            item { Label("On the desk", modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
            items(reading, key = { it.book.id }) { DeskRow(it, open) }
        }
        if (finished.isNotEmpty()) {
            item { Label("Finished", modifier = Modifier.padding(top = 18.dp, bottom = 4.dp)) }
            items(finished, key = { "done-" + it.book.id }) { DeskRow(it, open) }
        }
    }
}

@Composable
private fun Stat(n: String, label: String) {
    val ink = LocalInk.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(n, fontFamily = Fonts.fell, fontSize = 30.sp, color = ink.ink)
        Text(label.uppercase(), fontFamily = Fonts.fellSc, fontSize = 11.sp, letterSpacing = 1.5.sp, color = ink.faded)
    }
}

@Composable
private fun DeskRow(p: DeskBook, open: (Book, Int) -> Unit) {
    val ink = LocalInk.current
    val b = p.book
    val at = if (p.finished) 0 else p.pos
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(3.dp)).clickable { open(b, at) }
        .paper(ink.paper, ink).doubleRule(ink).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(92.dp)) {
            Plate(b, b.cover ?: b.cards.firstOrNull()?.img, ratio = 3f / 4f)
            if (p.finished) WaxSeal("✓", 30.dp, modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(b.title, fontFamily = b.era.display, fontSize = 20.sp, lineHeight = 24.sp, color = ink.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(listOf(b.author, b.year).filter { it.isNotBlank() }.joinToString(" · "), fontFamily = Fonts.fell, fontStyle = FontStyle.Italic,
                fontSize = 13.sp, color = ink.faded, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!p.finished) b.cards.getOrNull(p.pos)?.let { c ->
                Text("At: ${c.title}", fontFamily = Fonts.fell, fontSize = 14.sp, color = ink.ink, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(Modifier.height(6.dp))
            Progress(p)
            Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                Text(if (p.finished) "All ${p.total} folios read" else "${p.done} of ${p.total} folios · about ${p.total - p.done} min left",
                    fontFamily = Fonts.fell, fontSize = 12.sp, color = ink.faded, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(ago(p.opened), fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 12.sp, color = ink.faded)
            }
            Pill(if (p.finished) "Read again" else "Continue", filled = !p.finished, modifier = Modifier.padding(top = 8.dp)) { open(b, at) }
        }
    }
}
