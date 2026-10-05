package com.purval.folio

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/** A page counts as read when sealed, or when you linger this long and then turn forward. */
private const val DWELL_MS = 6000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Reader(app: App, book: Book, start: Int, onBack: () -> Unit) {
    val ink = LocalInk.current
    val store = app.store
    val pager = rememberPagerState(initialPage = start.coerceIn(0, (book.cards.size - 1).coerceAtLeast(0))) { book.cards.size }
    val scope = rememberCoroutineScope()
    var word by remember { mutableStateOf<Gloss?>(null) }
    var contents by remember { mutableStateOf(false) }
    var explain by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(pager) {
        var page = pager.currentPage
        var since = System.currentTimeMillis()
        snapshotFlow { pager.currentPage }.collect { p ->
            if (p == page + 1 && System.currentTimeMillis() - since >= DWELL_MS) store.seal(book, page, app.books)
            page = p; since = System.currentTimeMillis()
            store.setPosition(book.id, p)
        }
    }

    Column(Modifier.fillMaxSize().paper(ink.page, ink).statusBarsPadding().navigationBarsPadding()) {
        // top bar
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ink.ink) }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(book.title.uppercase(), fontFamily = Fonts.fellSc, fontSize = 13.sp, color = ink.ink, letterSpacing = 2.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Folio ${pager.currentPage + 1} of ${book.cards.size}", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic,
                    fontSize = 12.sp, color = ink.faded)
            }
            IconButton(onClick = { contents = true }) { Icon(Icons.AutoMirrored.Outlined.MenuBook, "Contents", tint = ink.ink) }
            val id = book.cardId(pager.currentPage)
            val marked = id in store.bookmarks
            IconButton(onClick = { store.toggleBookmark(id) }) {
                Icon(if (marked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, "Bookmark",
                    tint = if (marked) ink.rubric else ink.ink)
            }
        }
        val done = remember(store.tick) { store.sealedCount(book.id) }
        LinearProgressIndicator(
            progress = { if (book.cards.isEmpty()) 0f else done / book.cards.size.toFloat() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(2.dp),
            color = ink.rubric, trackColor = ink.rule, drawStopIndicator = {}, gapSize = 0.dp,
        )
        if (remember(store.tick) { store.stats(app.books).folios } == 0) {
            Text("Tap a red word for its meaning · seal each folio to count it · swipe to turn",
                fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = ink.faded, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp))
        }
        HorizontalPager(
            state = pager,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            pageSpacing = 10.dp,
            beyondViewportPageCount = 1,
        ) { i ->
            val off = (pager.currentPage - i) + pager.currentPageOffsetFraction
            Box(Modifier.graphicsLayer {
                // a leaf turning on its spine rather than a slide
                cameraDistance = 14f * density
                rotationY = (off * 14f).coerceIn(-30f, 30f)
                val s = 1f - 0.05f * off.absoluteValue.coerceAtMost(1f)
                scaleX = s; scaleY = s
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(if (off > 0) 1f else 0f, 0.5f)
            }) {
                FolioCard(app, book, i, onWord = { word = it }, onQuote = { explain = i }, onSealed = {
                    scope.launch { delay(650); if (i + 1 < book.cards.size) pager.animateScrollToPage(i + 1, animationSpec = tween(520)) }
                })
            }
        }
    }

    explain?.let { i ->
        ModalBottomSheet(onDismissRequest = { explain = null }, containerColor = ink.paper) { QuoteSheet(app, book, i) }
    }
    word?.let { g ->
        ModalBottomSheet(onDismissRequest = { word = null }, containerColor = ink.paper) {
            WordSheet(app, g, book.id)
        }
    }
    if (contents) {
        ModalBottomSheet(onDismissRequest = { contents = false }, containerColor = ink.paper) {
            Text("Contents", fontFamily = book.era.display, fontSize = 30.sp, color = ink.ink,
                modifier = Modifier.padding(horizontal = 24.dp))
            Text(book.blurb, fontFamily = Fonts.fell, fontSize = 15.sp, lineHeight = 21.sp, color = ink.ink,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))
            Text(book.translator, fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 13.sp, lineHeight = 18.sp,
                color = ink.faded, modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 8.dp))
            LazyColumn(Modifier.padding(bottom = 24.dp)) {
                items(book.chapterStarts) { s ->
                    val c = book.cards[s]
                    val end = book.chapterStarts.firstOrNull { it > s } ?: book.cards.size
                    val read = (s until end).count { store.isSealed(book.id, it) }
                    Row(Modifier.fillMaxWidth().clickable {
                        contents = false; scope.launch { pager.scrollToPage(s) }
                    }.padding(horizontal = 24.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(c.ch.uppercase(), fontFamily = Fonts.fellSc, fontSize = 12.sp, color = ink.rubric, letterSpacing = 1.5.sp)
                            Text(c.chTitle, fontFamily = book.era.body, fontSize = 17.sp, color = ink.ink)
                        }
                        Text("$read/${end - s}", fontFamily = Fonts.fell, fontSize = 13.sp,
                            color = if (read == end - s) ink.rubric else ink.faded)
                    }
                }
            }
        }
    }
}

@Composable
private fun FolioCard(app: App, book: Book, i: Int, onWord: (Gloss) -> Unit, onQuote: () -> Unit, onSealed: () -> Unit) {
    val ink = LocalInk.current
    val store = app.store
    val c = book.cards[i]
    val ctx = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val era = book.era
    val chapterStart = i in book.chapterStarts
    val body = TextStyle(fontFamily = era.body, fontSize = 18.sp, lineHeight = 27.sp, color = ink.ink)
    val sealedNow = remember(store.tick) { store.isSealed(book.id, i) }

    Column(
        Modifier.fillMaxSize().clip(RoundedCornerShape(3.dp)).paper(ink.paper, ink).doubleRule(ink, 5.dp)
            .verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(c.ch.uppercase(), fontFamily = Fonts.fellSc, fontSize = 12.sp, letterSpacing = 2.sp, color = ink.rubric,
                modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("fol. ${roman(i + 1).lowercase()}", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = ink.faded)
        }
        if (c.chTitle.isNotBlank()) Text(c.chTitle, fontFamily = era.body, fontStyle = FontStyle.Italic, fontSize = 14.sp, color = ink.faded)
        Spacer(Modifier.height(12.dp))
        Plate(book, c.img)
        if (c.cap.isNotBlank()) Text(c.cap, fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 11.sp, color = ink.faded,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(16.dp))
        Text(c.title, fontFamily = era.display, fontSize = when (era) { Era.RENAISSANCE -> 31.sp; Era.BAROQUE -> 36.sp; else -> 28.sp },
            lineHeight = if (era == Era.BAROQUE) 42.sp else 36.sp, color = ink.ink)
        Spacer(Modifier.height(12.dp))
        val text = remember(c.text, ink) { glossed(c.text, book.glossary, ink, onWord) }
        if (chapterStart) DropCapText(text, body, era.display, lines = if (i == 0) 4 else 3, illuminated = i == 0, capScale = era.capScale)
        else Text(text, style = body)

        if (c.quote.isNotBlank()) {
            Spacer(Modifier.height(18.dp))
            Fleuron(glyph = era.fleuron)
            Spacer(Modifier.height(10.dp))
            val q = remember(c.quote, ink) { glossed("“${c.quote}”", book.glossary, ink, onWord) }
            Text(q, fontFamily = era.body, fontStyle = FontStyle.Italic, fontSize = 20.sp, lineHeight = 29.sp,
                color = ink.ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().clickable(onClick = onQuote))
            Text(if (c.qBy.isNotBlank()) "— ${c.qBy}" else "— ${book.short}, ${c.ch}", fontFamily = Fonts.fellSc, fontSize = 12.sp,
                color = ink.faded, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                val kept = remember(store.quotes.size) { store.hasQuote(book.id, i) }
                IconButton(onClick = { store.toggleQuote(book, i, app.books) }) {
                    Icon(if (kept) Icons.Filled.FormatQuote else Icons.Outlined.FormatQuote,
                        if (kept) "Remove from Commonplace Book" else "Keep this quote", tint = if (kept) ink.rubric else ink.faded)
                }
                IconButton(onClick = {
                    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, "“${c.quote}”\n— ${c.attribution(book)}"), "Share quote"))
                }) { Icon(Icons.Outlined.Share, "Share quote", tint = ink.faded) }
            }
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).clickable(onClick = onQuote).padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Lightbulb, null, tint = ink.rubric, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Tap the quote for its meaning", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = ink.rubric)
            }
        }
        Spacer(Modifier.height(14.dp))
        SealButton(sealedNow, last = i == book.cards.size - 1) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            store.seal(book, i, app.books)
            onSealed()
        }
    }
}

@Composable
private fun SealButton(sealed: Boolean, last: Boolean, onSeal: () -> Unit) {
    val ink = LocalInk.current
    val press = remember { Animatable(1f) }
    val turn = remember { Animatable(0f) }
    var stamping by remember { mutableStateOf(false) }
    LaunchedEffect(stamping) {
        if (stamping) {
            press.snapTo(1.6f); turn.snapTo(-25f)
            launch { turn.animateTo(-8f, spring(Spring.DampingRatioMediumBouncy)) }
            press.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow))
        }
    }
    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
        if (sealed || stamping) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WaxSeal("✓", 54.dp, modifier = Modifier.scale(press.value).rotate(turn.value))
                Spacer(Modifier.width(12.dp))
                Text(if (last) "Sealed. Finis." else "Sealed — swipe on", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic,
                    fontSize = 16.sp, color = ink.faded)
            }
        } else {
            Row(
                Modifier.clip(RoundedCornerShape(50)).background(ink.ink).clickable { stamping = true; onSeal() }
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Seal this folio", fontFamily = Fonts.fellSc, fontSize = 16.sp, letterSpacing = 1.sp, color = ink.paper)
                Spacer(Modifier.width(10.dp))
                Text("❧", fontSize = 18.sp, color = ink.paper)
            }
        }
    }
}

@Composable
fun WordSheet(app: App, g: Gloss, bookId: String) {
    val ink = LocalInk.current
    val store = app.store
    val looked by produceState(if (g.meaning.isNotBlank()) g else null, g) {
        if (value == null) value = Dict.lookup(g.word, store.geminiKey) ?: Gloss(g.word, "No definition found offline. Connect to the internet and try again.", "")
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 26.dp).padding(bottom = 32.dp)) {
        Text(g.word, fontFamily = Fonts.fraktur, fontSize = 40.sp, color = ink.ink)
        Spacer(Modifier.height(4.dp))
        val e = looked
        if (e == null) {
            Text("Consulting the dictionary…", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, color = ink.faded, fontSize = 16.sp)
        } else {
            Text("MEANING", fontFamily = Fonts.fellSc, fontSize = 12.sp, letterSpacing = 2.sp, color = ink.rubric)
            Text(e.meaning, fontFamily = Fonts.fell, fontSize = 19.sp, lineHeight = 27.sp, color = ink.ink)
            if (e.example.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                Text("IN DAILY LIFE", fontFamily = Fonts.fellSc, fontSize = 12.sp, letterSpacing = 2.sp, color = ink.rubric)
                Text("“${e.example}”", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 18.sp, lineHeight = 26.sp, color = ink.ink)
            }
            Spacer(Modifier.height(20.dp))
            val has = remember(store.lexicon.size) { store.hasWord(e.word) }
            AnimatedVisibility(true, enter = fadeIn() + scaleIn()) {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(if (has) ink.paper else ink.ink)
                        .then(if (has) Modifier.doubleRule(ink, 3.dp) else Modifier)
                        .clickable { store.toggleWord(e, bookId, app.books) }.padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (has) "✓ In your Lexicon (tap to remove)" else "Add to my Lexicon", fontFamily = Fonts.fellSc,
                        fontSize = 16.sp, letterSpacing = 1.sp, color = if (has) ink.rubric else ink.paper)
                }
            }
        }
    }
}

/** The quote, then what it means in plain English and how it shows up in everyday life. */
@Composable
fun QuoteSheet(app: App, book: Book, i: Int) {
    val ink = LocalInk.current
    val c = book.cards[i]
    val store = app.store
    val explained by produceState(if (c.qMean.isNotBlank()) c.qMean to c.qLife else null, c) {
        if (value == null && store.geminiKey.isNotBlank()) value = Dict.explainQuote(c.quote, book.title, store.geminiKey)
    }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 26.dp).padding(bottom = 32.dp)) {
        Text("“${c.quote}”", fontFamily = book.era.body, fontStyle = FontStyle.Italic, fontSize = 21.sp, lineHeight = 30.sp, color = ink.ink)
        Text("— ${c.attribution(book)}", fontFamily = Fonts.fellSc, fontSize = 12.sp, color = ink.faded, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(18.dp))
        val e = explained
        when {
            e != null -> {
                Text("IN PLAIN ENGLISH", fontFamily = Fonts.fellSc, fontSize = 12.sp, letterSpacing = 2.sp, color = ink.rubric)
                Text(e.first, fontFamily = Fonts.fell, fontSize = 19.sp, lineHeight = 27.sp, color = ink.ink)
                if (e.second.isNotBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Text("IN DAILY LIFE", fontFamily = Fonts.fellSc, fontSize = 12.sp, letterSpacing = 2.sp, color = ink.rubric)
                    Text(e.second, fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 18.sp, lineHeight = 26.sp, color = ink.ink)
                }
            }
            store.geminiKey.isNotBlank() -> Text("Translating into plain English…", fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, color = ink.faded, fontSize = 16.sp)
            else -> Text("Plain-English explanations for your own imported books need the optional Gemini key (Honours → Plain-English helper).",
                fontFamily = Fonts.fell, fontSize = 15.sp, lineHeight = 21.sp, color = ink.faded)
        }
        Spacer(Modifier.height(20.dp))
        val kept = remember(store.quotes.size) { store.hasQuote(book.id, i) }
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(if (kept) ink.paper else ink.ink)
                .then(if (kept) Modifier.doubleRule(ink, 3.dp) else Modifier)
                .clickable { store.toggleQuote(book, i, app.books) }.padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (kept) "✓ In your Commonplace Book" else "Keep this quote", fontFamily = Fonts.fellSc,
                fontSize = 16.sp, letterSpacing = 1.sp, color = if (kept) ink.rubric else ink.paper)
        }
    }
}
