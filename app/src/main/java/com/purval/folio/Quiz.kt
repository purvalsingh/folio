package com.purval.folio

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

/** One multiple-choice question. [key] is the spaced-repetition item it trains (null for chapter checks). */
data class Question(
    val key: String?, val ask: String, val prompt: String, val promptFont: FontFamily, val italic: Boolean,
    val options: List<String>, val answer: Int, val note: String,
)

object Quiz {
    private fun <T> pickOthers(pool: List<T>, not: T, n: Int, r: Random) = pool.filter { it != not }.distinct().shuffled(r).take(n)

    private fun build(key: String?, ask: String, prompt: String, font: FontFamily, italic: Boolean, right: String, pool: List<String>, note: String, r: Random): Question? {
        val wrong = pickOthers(pool, right, 3, r)
        if (wrong.size < 2) return null
        val opts = (wrong + right).shuffled(r)
        return Question(key, ask, prompt, font, italic, opts, opts.indexOf(right), note)
    }

    private fun meanings(app: App) = app.books.flatMap { b -> b.glossary.values.map { it.meaning } }.filter { it.isNotBlank() }.distinct()
    private fun quoteMeanings(app: App) = app.books.flatMap { b -> b.cards.map { it.qMean } }.filter { it.isNotBlank() }

    private fun wordQ(app: App, g: Gloss, r: Random) =
        build("w:${g.word.lowercase()}", "What does this word mean?", g.word, Fonts.fraktur, false, g.meaning, meanings(app),
            if (g.example.isNotBlank()) "“${g.example}”" else "", r)

    private fun quoteQ(app: App, b: Book, i: Int, r: Random): Question? {
        val c = b.cards.getOrNull(i) ?: return null
        return if (c.qMean.isNotBlank())
            build("q:${b.id}#$i", "What does this line mean?", "“${c.quote}”", b.era.body, true, c.qMean, quoteMeanings(app),
                if (c.qLife.isNotBlank()) "In daily life: ${c.qLife}" else "", r)
        else build("q:${b.id}#$i", "Which book is this line from?", "“${c.quote}”", b.era.body, true, b.title, app.books.map { it.title }, "", r)
    }

    /**
     * Today's recall deck: saved words and quotes that are due, topped up with words from pages already
     * sealed, so even a new reader has something to practise.
     */
    fun deck(app: App, size: Int = 10, seed: Long = System.currentTimeMillis()): List<Question> {
        val s = app.store
        val r = Random(seed)
        val qs = mutableListOf<Question>()
        s.lexicon.filter { s.isDue("w:${it.word.lowercase()}") && it.meaning.isNotBlank() }
            .forEach { w -> wordQ(app, Gloss(w.word, w.meaning, w.example), r)?.let { qs += it } }
        s.quotes.filter { s.isDue("q:${it.bookId}#${it.idx}") }
            .forEach { q -> app.book(q.bookId)?.let { b -> quoteQ(app, b, q.idx, r)?.let { qs += it } } }
        if (qs.size < size) {
            val seen = app.books.flatMap { b ->
                b.cards.indices.filter { s.isSealed(b.id, it) }.flatMap { i ->
                    Regex("[\\p{L}]+").findAll(b.cards[i].text + " " + b.cards[i].quote).mapNotNull { b.glossary[it.value.lowercase()] }.toList()
                }
            }.filter { it.meaning.isNotBlank() }.distinctBy { it.word.lowercase() }
                .filter { s.isDue("w:${it.word.lowercase()}") && qs.none { q -> q.key == "w:${it.word.lowercase()}" } }
            seen.shuffled(r).take(size - qs.size).forEach { g -> wordQ(app, g, r)?.let { qs += it } }
        }
        return qs.shuffled(r).take(size)
    }

    fun dueCount(app: App) = deck(app, 50).size

    /** Three questions on a finished chapter: a quote's meaning, a hard word, and which idea belongs here. */
    fun chapter(app: App, b: Book, start: Int): List<Question> {
        val r = Random(start * 31 + b.id.hashCode())
        val end = b.chapterStarts.firstOrNull { it > start } ?: b.cards.size
        val range = start until end
        val qs = mutableListOf<Question>()
        range.shuffled(r).firstOrNull()?.let { i -> quoteQ(app, b, i, r)?.let { qs += it.copy(key = null) } }
        range.flatMap { i -> Regex("[\\p{L}]+").findAll(b.cards[i].text).mapNotNull { b.glossary[it.value.lowercase()] }.toList() }
            .filter { it.meaning.isNotBlank() }.shuffled(r).firstOrNull()?.let { g -> wordQ(app, g, r)?.let { qs += it.copy(key = null) } }
        val inside = b.cards[range.random(r)].title
        val outside = b.cards.indices.filter { it !in range }.map { b.cards[it].title } + app.books.filter { it.id != b.id }.flatMap { o -> o.cards.map { it.title } }
        build(null, "Which of these ideas is from this chapter?", b.cards[start].chTitle.ifBlank { b.cards[start].ch }, b.era.display, false,
            inside, outside, "", r)?.let { qs += it }
        return qs
    }
}

/* ---------- shared runner ---------- */

@Composable
fun QuizRunner(questions: List<Question>, onAnswer: (Question, Boolean) -> Unit, onFinish: (Int) -> Unit) {
    val ink = LocalInk.current
    val haptic = LocalHapticFeedback.current
    var index by remember { mutableIntStateOf(0) }
    var chosen by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            questions.indices.forEach { k ->
                Box(Modifier.padding(3.dp).size(9.dp).clip(CircleShape)
                    .background(if (k < index || (k == index && chosen != null)) ink.rubric else Color.Transparent)
                    .border(1.dp, if (k <= index) ink.rubric else ink.faded.copy(alpha = .5f), CircleShape))
            }
        }
        Spacer(Modifier.height(14.dp))
        AnimatedContent(index, transitionSpec = { (fadeIn(tween(220)) + slideInHorizontally { it / 6 }) togetherWith fadeOut(tween(120)) }, label = "q") { qi ->
            val q = questions[qi]
            Column {
                Label(q.ask)
                Spacer(Modifier.height(6.dp))
                Text(q.prompt, fontFamily = q.promptFont, fontStyle = if (q.italic) FontStyle.Italic else FontStyle.Normal,
                    fontSize = if (q.italic) 20.sp else 34.sp, lineHeight = if (q.italic) 28.sp else 40.sp, color = ink.ink)
                Spacer(Modifier.height(14.dp))
                q.options.forEachIndexed { k, opt ->
                    val c = chosen
                    val isRight = k == q.answer
                    val border = when {
                        c == null -> ink.ink.copy(alpha = .35f)
                        isRight -> ink.rubric
                        k == c -> ink.faded
                        else -> ink.ink.copy(alpha = .15f)
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(6.dp))
                            .background(if (c != null && isRight) ink.rubric.copy(alpha = .1f) else ink.paper)
                            .border(if (c != null && isRight) 1.6.dp else 1.dp, border, RoundedCornerShape(6.dp))
                            .clickable(enabled = c == null) {
                                chosen = k
                                val right = k == q.answer
                                if (right) score++
                                haptic.performHapticFeedback(if (right) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove)
                                onAnswer(q, right)
                            }.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(opt, fontFamily = Fonts.fell, fontSize = 16.sp, lineHeight = 21.sp, color = ink.ink, modifier = Modifier.weight(1f),
                            textDecoration = if (c == k && !isRight) TextDecoration.LineThrough else TextDecoration.None)
                        if (c != null && isRight) Text("✓", color = ink.rubric, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                if (chosen != null) {
                    if (q.note.isNotBlank()) Text(q.note, fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 14.sp, lineHeight = 19.sp,
                        color = ink.faded, modifier = Modifier.padding(top = 8.dp))
                    Spacer(Modifier.height(14.dp))
                    Pill(if (qi + 1 < questions.size) "Next  ❧" else "Finish", modifier = Modifier.fillMaxWidth()) {
                        if (qi + 1 < questions.size) { index = qi + 1; chosen = null } else onFinish(score)
                    }
                }
            }
        }
    }
}

/* ---------- Daily Recall ---------- */

@Composable
fun RecallScreen(app: App, seed: Long = System.currentTimeMillis(), done: () -> Unit) {
    val ink = LocalInk.current
    val questions = remember { Quiz.deck(app, seed = seed) }
    var result by remember { mutableStateOf<Int?>(null) }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        IconButton(onClick = done) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ink.ink) }
        Masthead("Daily Recall", "Words and lines you met before, brought back just before you'd forget them.")
        Spacer(Modifier.height(16.dp))
        val r = result
        when {
            questions.isEmpty() -> Empty("✦", "Nothing due today.\nSeal pages, save words and keep quotes —\nthey return here at the right moment.")
            r != null -> Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                WaxSeal("$r/${questions.size}", 110.dp)
                Spacer(Modifier.height(10.dp))
                Text(if (r == questions.size) "Flawless recall." else if (r * 2 >= questions.size) "Well remembered." else "These will come back again soon.",
                    fontFamily = Fonts.fraktur, fontSize = 30.sp, color = ink.ink, textAlign = TextAlign.Center)
                Text("Right answers move further into the future; missed ones return tomorrow.", fontFamily = Fonts.fell,
                    fontStyle = FontStyle.Italic, fontSize = 14.sp, color = ink.faded, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Pill("Back to the Library", modifier = Modifier.fillMaxWidth(), onClick = done)
            }
            else -> QuizRunner(questions, onAnswer = { q, right -> q.key?.let { app.store.answer(it, right, app.books) } }, onFinish = { result = it })
        }
        Spacer(Modifier.height(30.dp))
    }
}

/** Shown when a chapter's last page is sealed. Two of three right passes the chapter. */
@Composable
fun ChapterCheck(app: App, book: Book, start: Int, onClose: () -> Unit) {
    val ink = LocalInk.current
    val questions = remember(book.id, start) { Quiz.chapter(app, book, start) }
    var result by remember { mutableStateOf<Int?>(null) }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 28.dp)) {
        Label("Chapter check · ${book.cards[start].ch}")
        Text("Three quick questions", fontFamily = book.era.display, fontSize = 28.sp, color = ink.ink)
        Spacer(Modifier.height(12.dp))
        val r = result
        if (questions.isEmpty()) {
            // too few cards or words for a fair check: don't ask again
            app.store.checked.add("${book.id}#$start")
            Text("This chapter is too short for a check.", fontFamily = Fonts.fell, fontSize = 17.sp, color = ink.ink)
            Spacer(Modifier.height(14.dp))
            Pill("Onward", modifier = Modifier.fillMaxWidth(), onClick = onClose)
        } else if (r == null) QuizRunner(questions, onAnswer = { _, _ -> }, onFinish = { s ->
            result = s
            if (s * 3 >= questions.size * 2) app.store.passChapter(book, start, app.books)
        }) else Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            val passed = r * 3 >= questions.size * 2
            WaxSeal(if (passed) "✦" else "$r/${questions.size}", 90.dp, earned = passed)
            Text(if (passed) "Chapter mastered." else "Not yet — reread the chapter and try again from Contents.",
                fontFamily = Fonts.fell, fontSize = 17.sp, color = ink.ink, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(14.dp))
            Pill("Onward", modifier = Modifier.fillMaxWidth(), onClick = onClose)
        }
    }
}
