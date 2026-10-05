package com.purval.folio

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.json.JSONArray

/** One idea traced through many books, e.g. what Machiavelli, the Gita and the Stoics say about anger. */
data class Theme(val id: String, val title: String, val blurb: String, val cards: List<Pair<String, Int>>)

/** An everyday problem, with three old books' advice. */
data class Scenario(val id: String, val title: String, val situation: String, val advice: List<Triple<String, Int, String>>)

object Explore {
    private fun ref(s: String) = s.substringBefore('#') to s.substringAfter('#').toInt()

    fun themes(a: JSONArray?) = (0 until (a?.length() ?: 0)).map { a!!.getJSONObject(it) }.map { o ->
        val c = o.getJSONArray("cards")
        Theme(o.getString("id"), o.getString("title"), o.optString("blurb"), (0 until c.length()).map { ref(c.getString(it)) })
    }

    fun scenarios(a: JSONArray?) = (0 until (a?.length() ?: 0)).map { a!!.getJSONObject(it) }.map { o ->
        val adv = o.getJSONArray("advice")
        Scenario(o.getString("id"), o.getString("title"), o.optString("situation"), (0 until adv.length()).map { adv.getJSONObject(it) }.map { x ->
            val (b, i) = ref(x.getString("card")); Triple(b, i, x.getString("say"))
        })
    }

    private fun asset(ctx: Context, name: String) = runCatching { JSONArray(ctx.assets.open(name).bufferedReader().readText()) }.getOrNull()
    fun bundledThemes(ctx: Context) = runCatching { themes(asset(ctx, "themes.json")) }.getOrDefault(emptyList())
    fun bundledScenarios(ctx: Context) = runCatching { scenarios(asset(ctx, "scenarios.json")) }.getOrDefault(emptyList())
}

fun App.themes(ctx: Context) = catalog?.themes?.takeIf { it.isNotEmpty() } ?: Explore.bundledThemes(ctx)
fun App.scenarios(ctx: Context) = catalog?.scenarios?.takeIf { it.isNotEmpty() } ?: Explore.bundledScenarios(ctx)

/* ---------- on the Library page ---------- */

@Composable
fun ExploreRows(app: App, theme: (Theme) -> Unit, scenario: (Scenario) -> Unit) {
    val ink = LocalInk.current
    val ctx = LocalContext.current
    val themes = remember(app.catalog) { app.themes(ctx) }
    val scenes = remember(app.catalog) { app.scenarios(ctx) }
    if (themes.isNotEmpty()) {
        Spacer(Modifier.height(18.dp))
        Label("Themes across books")
        Text("One idea, many authors.", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 14.sp, color = ink.faded)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(themes, key = { it.id }) { t ->
                Column(Modifier.width(150.dp).clip(RoundedCornerShape(3.dp)).clickable { theme(t) }.paper(ink.paper, ink).doubleRule(ink).padding(12.dp)) {
                    Text(t.title, fontFamily = Fonts.fraktur, fontSize = 20.sp, lineHeight = 23.sp, color = ink.ink, maxLines = 2,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.height(48.dp))
                    Text("${t.cards.map { it.first }.distinct().size} books · ${t.cards.size} pages", fontFamily = Fonts.fellSc, fontSize = 11.sp, color = ink.rubric)
                }
            }
        }
    }
    if (scenes.isNotEmpty()) {
        Spacer(Modifier.height(18.dp))
        Label("What would they do?")
        Text("Everyday problems, answered by the old books.", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 14.sp, color = ink.faded)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(scenes, key = { it.id }) { s ->
                Column(Modifier.width(220.dp).clip(RoundedCornerShape(3.dp)).clickable { scenario(s) }.paper(ink.paper, ink).doubleRule(ink).padding(14.dp)) {
                    Text(s.title, fontFamily = Fonts.fell, fontSize = 18.sp, lineHeight = 23.sp, color = ink.ink, maxLines = 3,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.height(70.dp))
                    Text(s.advice.map { app.book(it.first)?.short ?: app.catalog?.books?.firstOrNull { e -> e.id == it.first }?.title ?: it.first }
                        .joinToString(" · "), fontFamily = Fonts.fellSc, fontSize = 11.sp, color = ink.rubric, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/* ---------- fetching books a page needs ---------- */

@Composable
private fun FetchMissing(app: App, ids: List<String>) {
    val ink = LocalInk.current
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val fetching = remember { mutableStateMapOf<String, Float>() }
    val missing = ids.distinct().filter { app.book(it) == null }
    if (missing.isEmpty()) return
    Spacer(Modifier.height(12.dp))
    Panel {
        Column {
            Label("From the online library")
            Text("Fetch these to read their pages here.", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = ink.faded)
            missing.forEach { id ->
                val e = app.catalog?.books?.firstOrNull { it.id == id }
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(e?.title ?: id, fontFamily = Fonts.fell, fontSize = 17.sp, color = ink.ink, modifier = Modifier.weight(1f))
                    val p = fetching[id]
                    if (p != null) Text("${(p * 100).toInt()}%", fontFamily = Fonts.fell, color = ink.faded)
                    else if (e != null) Pill("Fetch", filled = false) {
                        fetching[id] = 0f
                        scope.launch { runCatching { Library.install(ctx, e) { fetching[id] = it } }.onSuccess { app.put(it) }; fetching.remove(id) }
                    } else Text("connect to fetch", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = ink.faded)
                }
            }
        }
    }
}

/** A quote from a book on the phone, with its author and plain meaning; tap to open that page. */
@Composable
private fun QuoteRow(app: App, bookId: String, i: Int, open: (Book, Int) -> Unit, lead: String? = null) {
    val ink = LocalInk.current
    val b = app.book(bookId)
    val c = b?.cards?.getOrNull(i)
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(3.dp))
        .then(if (b != null) Modifier.clickable { open(b, i) } else Modifier).paper(ink.paper, ink).doubleRule(ink).padding(14.dp)) {
        Text((b?.short?.ifBlank { b.author } ?: app.catalog?.books?.firstOrNull { it.id == bookId }?.title ?: bookId).uppercase(),
            fontFamily = Fonts.fellSc, fontSize = 12.sp, letterSpacing = 1.5.sp, color = ink.rubric)
        lead?.let { Text(it, fontFamily = Fonts.fell, fontSize = 18.sp, lineHeight = 25.sp, color = ink.ink, modifier = Modifier.padding(top = 4.dp)) }
        if (c != null) {
            Text("“${c.quote}”", fontFamily = b.era.body, fontStyle = FontStyle.Italic, fontSize = 16.sp, lineHeight = 23.sp,
                color = if (lead != null) ink.faded else ink.ink, modifier = Modifier.padding(top = 6.dp))
            if (lead == null && c.qMean.isNotBlank()) Text(c.qMean, fontFamily = Fonts.fell, fontSize = 15.sp, lineHeight = 21.sp, color = ink.faded,
                modifier = Modifier.padding(top = 6.dp))
            Text("${b.title} · ${c.ch} — read the page ›", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 12.sp, color = ink.rubric,
                modifier = Modifier.padding(top = 6.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        } else Text("Fetch this book to read the passage.", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = ink.faded)
    }
}

@Composable
private fun Page(back: () -> Unit, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    val ink = LocalInk.current
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
        item { IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ink.ink) } }
        content()
    }
}

@Composable
fun ThemeScreen(app: App, t: Theme, open: (Book, Int) -> Unit, back: () -> Unit) = Page(back) {
    item {
        Masthead(t.title, t.blurb)
        FetchMissing(app, t.cards.map { it.first })
        Spacer(Modifier.height(8.dp))
    }
    items(t.cards, key = { "${it.first}#${it.second}" }) { (b, i) -> QuoteRow(app, b, i, open) }
}

@Composable
fun ScenarioScreen(app: App, s: Scenario, open: (Book, Int) -> Unit, back: () -> Unit) = Page(back) {
    item {
        Masthead(s.title, s.situation, font = Fonts.fell, size = 32.sp)
        FetchMissing(app, s.advice.map { it.first })
        Spacer(Modifier.height(4.dp))
        Label("Three old books advise")
    }
    items(s.advice, key = { "${it.first}#${it.second}" }) { (b, i, say) -> QuoteRow(app, b, i, open, lead = say) }
    item {
        Text("Old advice for thinking with, not orders. You know your situation best.", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic,
            fontSize = 13.sp, color = LocalInk.current.faded, modifier = Modifier.padding(top = 12.dp))
    }
}
