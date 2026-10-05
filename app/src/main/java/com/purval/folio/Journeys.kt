package com.purval.folio

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray

/** A short themed path through pages of several books, a few cards a day. */
data class Journey(val id: String, val title: String, val blurb: String, val days: List<List<Pair<String, Int>>>) {
    val books get() = days.flatten().map { it.first }.distinct()
}

object Journeys {
    fun parse(a: JSONArray?): List<Journey> = (0 until (a?.length() ?: 0)).map { a!!.getJSONObject(it) }.map { o ->
        val d = o.getJSONArray("days")
        Journey(o.getString("id"), o.getString("title"), o.optString("blurb"), (0 until d.length()).map { k ->
            val day = d.getJSONArray(k)
            (0 until day.length()).map { day.getString(it) }.map { it.substringBefore('#') to it.substringAfter('#').toInt() }
        })
    }

    fun bundled(ctx: Context): List<Journey> =
        runCatching { parse(JSONArray(ctx.assets.open("journeys.json").bufferedReader().readText())) }.getOrDefault(emptyList())
}

fun App.journeys(ctx: Context) = catalog?.journeys?.takeIf { it.isNotEmpty() } ?: Journeys.bundled(ctx)

fun App.dayDone(j: Journey, d: Int) = j.days[d].all { (b, i) -> store.isSealed(b, i) }

/* ---------- on the Library page ---------- */

@Composable
fun JourneysRow(app: App, open: (Journey) -> Unit) {
    val ink = LocalInk.current
    val ctx = LocalContext.current
    val list = app.journeys(ctx)
    if (list.isEmpty()) return
    Spacer(Modifier.height(18.dp))
    Label("Reading journeys")
    Text("A few pages a day from several books, on one theme.", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 14.sp, color = ink.faded)
    Spacer(Modifier.height(8.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(list, key = { it.id }) { j ->
            val tick = app.store.tick
            val done = remember(tick, j.id) { j.days.indices.count { app.dayDone(j, it) } }
            Column(Modifier.width(220.dp).clip(RoundedCornerShape(3.dp)).clickable { open(j) }.paper(ink.paper, ink).doubleRule(ink).padding(14.dp)) {
                Text(j.title, fontFamily = Fonts.fraktur, fontSize = 20.sp, lineHeight = 25.sp, color = ink.ink, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Text(j.blurb, fontFamily = Fonts.fell, fontSize = 13.sp, lineHeight = 17.sp, color = ink.faded, maxLines = 3, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { done / j.days.size.toFloat() }, modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = ink.rubric, trackColor = ink.rule, drawStopIndicator = {}, gapSize = 0.dp)
                Text(if (done == j.days.size) "Journey complete" else "Day ${done + 1} of ${j.days.size}", fontFamily = Fonts.fellSc,
                    fontSize = 12.sp, color = ink.rubric, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

/* ---------- a journey's map ---------- */

@Composable
fun JourneyScreen(app: App, j: Journey, back: () -> Unit, read: (Int) -> Unit) {
    val ink = LocalInk.current
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val missing = j.books.filter { app.book(it) == null }
    val fetching = remember { mutableStateMapOf<String, Float>() }
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp)) {
        item {
            IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ink.ink) }
            Masthead(j.title, j.blurb)
            if (missing.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Panel {
                    Column {
                        Label("Books to fetch first")
                        missing.forEach { id ->
                            val e = app.catalog?.books?.firstOrNull { it.id == id }
                            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(e?.title ?: id, fontFamily = Fonts.fell, fontSize = 17.sp, color = ink.ink, modifier = Modifier.weight(1f))
                                val p = fetching[id]
                                if (p != null) Text("${(p * 100).toInt()}%", fontFamily = Fonts.fell, color = ink.faded)
                                else if (e != null) Pill("Fetch", filled = false) {
                                    fetching[id] = 0f
                                    scope.launch {
                                        runCatching { Library.install(ctx, e) { fetching[id] = it } }.onSuccess { app.put(it) }
                                        fetching.remove(id)
                                    }
                                } else Text("connect to fetch", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = ink.faded)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        items(j.days.size) { d ->
            val ready = j.days[d].all { app.book(it.first) != null }
            val done = remember(app.store.tick, d) { app.dayDone(j, d) }
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WaxSeal(roman(d + 1), 40.dp, earned = done)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Label("Day ${d + 1}", if (done) ink.rubric else ink.faded)
                        j.days[d].forEach { (bid, i) ->
                            val b = app.book(bid)
                            Text("${b?.cards?.getOrNull(i)?.title ?: "Download to read"} · ${b?.title ?: app.catalog?.books?.firstOrNull { it.id == bid }?.title ?: bid}", fontFamily = Fonts.fell, fontSize = 15.sp, lineHeight = 20.sp,
                                color = if (b != null && app.store.isSealed(bid, i)) ink.faded else ink.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    if (ready) Pill(if (done) "Reread" else "Read", filled = !done) { read(d) }
                }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(0.6.dp).background(ink.rule))
            }
        }
    }
}

/* ---------- reading one day ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyReader(app: App, j: Journey, day: Int, back: () -> Unit) {
    val ink = LocalInk.current
    val pages = j.days[day].mapNotNull { (b, i) -> app.book(b)?.let { it to i } }
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    var word by remember { mutableStateOf<Pair<Gloss, String>?>(null) }
    Column(Modifier.fillMaxSize().paper(ink.page, ink).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ink.ink) }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(j.title.uppercase(), fontFamily = Fonts.fellSc, fontSize = 13.sp, letterSpacing = 2.sp, color = ink.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Day ${day + 1} · page ${pager.currentPage + 1} of ${pages.size}", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 12.sp, color = ink.faded)
            }
            Spacer(Modifier.width(48.dp))
        }
        HorizontalPager(state = pager, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp), pageSpacing = 10.dp) { p ->
            val (b, i) = pages[p]
            FolioCard(app, b, i, onWord = { word = it to b.id }, onSealed = {
                scope.launch { delay(650); if (p + 1 < pages.size) pager.animateScrollToPage(p + 1) }
            })
        }
    }
    word?.let { (g, bid) -> ModalBottomSheet(onDismissRequest = { word = null }, containerColor = ink.paper) { WordSheet(app, g, bid) } }
}
