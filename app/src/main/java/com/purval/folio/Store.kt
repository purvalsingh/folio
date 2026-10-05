package com.purval.folio

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class SavedWord(val word: String, val meaning: String, val example: String, val bookId: String, val at: Long)
data class SavedQuote(val text: String, val bookId: String, val idx: Int, val at: Long)
data class Celebration(val title: String, val message: String, val seal: String)

data class Stats(val folios: Int, val books: Int, val words: Int, val quotes: Int, val streak: Int, val goalDays: Int)

class Milestone(val id: String, val title: String, val line: String, val seal: String, val reached: (Stats) -> Boolean)

val MILESTONES = listOf(
    Milestone("f1", "First Folio", "You read your first page.", "I") { it.folios >= 1 },
    Milestone("f10", "Ten Folios", "Congratulations — you have read 10 pages!", "X") { it.folios >= 10 },
    Milestone("f25", "Quarter Hundred", "25 pages read. The habit is forming.", "XXV") { it.folios >= 25 },
    Milestone("f50", "Half a Hundred", "50 pages read. Most people never get here.", "L") { it.folios >= 50 },
    Milestone("f100", "The Centurion", "Congratulations — you have read 100 pages!", "C") { it.folios >= 100 },
    Milestone("f250", "Bookworm", "250 pages turned.", "CCL") { it.folios >= 250 },
    Milestone("f500", "Librarian", "500 pages. A small library lives in your head.", "D") { it.folios >= 500 },
    Milestone("f1000", "The Thousand", "1,000 pages read.", "M") { it.folios >= 1000 },
    Milestone("b1", "Finis", "Congratulations — you have completed one book!", "✠") { it.books >= 1 },
    Milestone("b3", "Trilogy", "Three whole books finished.", "III") { it.books >= 3 },
    Milestone("w5", "Word Hoarder", "5 words saved to your Lexicon.", "V") { it.words >= 5 },
    Milestone("w25", "Lexicographer", "25 words in your Lexicon.", "XXV") { it.words >= 25 },
    Milestone("q5", "Commonplacer", "5 quotes kept in your Commonplace Book.", "❦") { it.quotes >= 5 },
    Milestone("q25", "Keeper of Sayings", "25 quotes kept.", "❦") { it.quotes >= 25 },
    Milestone("s3", "Three Days Running", "A 3-day reading streak.", "III") { it.streak >= 3 },
    Milestone("s7", "A Week of Letters", "A 7-day reading streak.", "VII") { it.streak >= 7 },
    Milestone("s30", "Month of Devotion", "A 30-day reading streak.", "XXX") { it.streak >= 30 },
    Milestone("g1", "Quota Kept", "You met your daily quota for the first time.", "✓") { it.goalDays >= 1 },
    Milestone("g10", "Ten Good Days", "Daily quota met on 10 days.", "X") { it.goalDays >= 10 },
)

val RANKS = listOf("Page", "Squire", "Scribe", "Clerk", "Scholar", "Courtier", "Counsellor", "Magister", "Sage", "Prince of Letters")

/** XP needed to reach level n (1-based): 0, 100, 300, 600, 1000 … */
fun xpFor(level: Int) = 50 * (level - 1) * level

fun levelOf(xp: Int): Int { var l = 1; while (xpFor(l + 1) <= xp) l++; return l }

fun rankOf(level: Int) = RANKS[(level - 1).coerceAtMost(RANKS.lastIndex)]

fun roman(n: Int): String {
    val v = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
    val s = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
    var x = n; val sb = StringBuilder()
    for (i in v.indices) while (x >= v[i]) { sb.append(s[i]); x -= v[i] }
    return sb.toString()
}

const val XP_FOLIO = 10
const val XP_SAVE = 5
const val XP_GOAL = 50
const val XP_BOOK = 200

class Store(ctx: Context) {
    private val prefs = ctx.getSharedPreferences("folio", Context.MODE_PRIVATE)

    val sealed = mutableMapOf<String, MutableSet<Int>>()
    val position = mutableMapOf<String, Int>()
    val bookmarks = mutableStateListOf<String>()
    val lexicon = mutableStateListOf<SavedWord>()
    val quotes = mutableStateListOf<SavedQuote>()
    private val days = mutableMapOf<String, Int>()
    private val honours = mutableSetOf<String>()
    var xp by mutableStateOf(0); private set
    var goal by mutableStateOf(10)
    var night by mutableStateOf<Boolean?>(null)
    var geminiKey by mutableStateOf("")
    var lastBook by mutableStateOf<String?>(null)
    /** bumps whenever progress changes so screens recompose */
    var tick by mutableStateOf(0); private set
    val party = mutableStateListOf<Celebration>()

    init { load() }

    fun today() = LocalDate.now().toString()
    fun readToday() = days[today()] ?: 0
    fun isSealed(book: String, i: Int) = sealed[book]?.contains(i) == true
    fun sealedCount(book: String) = sealed[book]?.size ?: 0
    fun earned(id: String) = id in honours

    fun stats(books: List<Book>): Stats {
        val done = books.count { b -> b.cards.isNotEmpty() && sealedCount(b.id) >= b.cards.size }
        return Stats(sealed.values.sumOf { it.size }, done, lexicon.size, quotes.size, streak(), days.values.count { it >= goal })
    }

    fun streak(): Int {
        var d = LocalDate.now()
        if ((days[d.toString()] ?: 0) == 0) d = d.minusDays(1) // today not started yet doesn't break it
        var n = 0
        while ((days[d.toString()] ?: 0) > 0) { n++; d = d.minusDays(1) }
        return n
    }

    fun week(): List<Pair<LocalDate, Int>> = (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
        .map { it to (days[it.toString()] ?: 0) }

    fun seal(book: Book, i: Int, all: List<Book>) {
        val set = sealed.getOrPut(book.id) { mutableSetOf() }
        if (!set.add(i)) return
        val levelBefore = levelOf(xp)
        val t = today()
        days[t] = (days[t] ?: 0) + 1
        xp += XP_FOLIO
        if (days[t] == goal) {
            xp += XP_GOAL
            party += Celebration("Quota Kept", "You read $goal pages today. Your daily quota is met — huzzah!", "✓")
        }
        if (set.size == book.cards.size) {
            xp += XP_BOOK
            party += Celebration("Finis", "You have completed “${book.title}”. Every page, every lesson.", "✠")
        }
        afterGain(levelBefore, all)
    }

    private fun afterGain(levelBefore: Int, all: List<Book>) {
        val s = stats(all)
        MILESTONES.filter { it.id !in honours && it.reached(s) }.forEach {
            honours += it.id
            if (it.id != "b1" && it.id != "g1") party += Celebration(it.title, it.line, it.seal)
        }
        val lv = levelOf(xp)
        if (lv > levelBefore) party += Celebration("Level ${roman(lv)}", "You have risen to the rank of ${rankOf(lv)}.", roman(lv))
        tick++
        persist()
    }

    fun setPosition(book: String, i: Int) { position[book] = i; lastBook = book; persist() }

    fun toggleBookmark(id: String) { if (!bookmarks.remove(id)) bookmarks.add(0, id); persist() }

    fun hasWord(w: String) = lexicon.any { it.word.equals(w, true) }
    fun toggleWord(g: Gloss, bookId: String, all: List<Book>) {
        if (lexicon.removeAll { it.word.equals(g.word, true) }) { persist(); return }
        val lv = levelOf(xp)
        lexicon.add(0, SavedWord(g.word, g.meaning, g.example, bookId, System.currentTimeMillis()))
        xp += XP_SAVE
        afterGain(lv, all)
    }

    fun hasQuote(book: String, i: Int) = quotes.any { it.bookId == book && it.idx == i }
    fun toggleQuote(book: Book, i: Int, all: List<Book>) {
        if (quotes.removeAll { it.bookId == book.id && it.idx == i }) { persist(); return }
        val lv = levelOf(xp)
        quotes.add(0, SavedQuote(book.cards[i].quote, book.id, i, System.currentTimeMillis()))
        xp += XP_SAVE
        afterGain(lv, all)
    }

    fun forgetBook(id: String) {
        sealed.remove(id); position.remove(id)
        bookmarks.removeAll { it.startsWith("$id#") }
        quotes.removeAll { it.bookId == id }
        if (lastBook == id) lastBook = null
        tick++; persist()
    }

    fun persist() {
        val o = snapshot()
        o.put("gemini", geminiKey)
        night?.let { o.put("night", it) }
        prefs.edit().putString("state", o.toString())?.apply() // ?. : layoutlib stub returns null
        dirty = true
    }

    /** set whenever local progress changes; cleared after a successful cloud push */
    var dirty = false

    /** Everything worth syncing (no API keys, no display settings). */
    fun snapshot(): JSONObject {
        val o = JSONObject()
        o.put("sealed", JSONObject().apply { sealed.forEach { (k, v) -> put(k, JSONArray(v.toList())) } })
        o.put("position", JSONObject(position.toMap()))
        o.put("bookmarks", JSONArray(bookmarks.toList()))
        o.put("lexicon", JSONArray().apply {
            lexicon.forEach { put(JSONObject().put("w", it.word).put("m", it.meaning).put("e", it.example).put("b", it.bookId).put("t", it.at)) }
        })
        o.put("quotes", JSONArray().apply {
            quotes.forEach { put(JSONObject().put("q", it.text).put("b", it.bookId).put("i", it.idx).put("t", it.at)) }
        })
        o.put("days", JSONObject(days.toMap()))
        o.put("honours", JSONArray(honours.toList()))
        o.put("xp", xp).put("goal", goal).put("last", lastBook ?: "")
        return o
    }

    /**
     * Folds another device's library into this one. Nothing is ever lost: pages, words, quotes,
     * bookmarks and seals are unioned, daily counts and xp take the larger value.
     */
    fun merge(json: String) {
        val o = runCatching { JSONObject(json) }.getOrNull() ?: return
        o.optJSONObject("sealed")?.let { s ->
            s.keys().forEach { k -> val a = s.getJSONArray(k); sealed.getOrPut(k) { mutableSetOf() }.addAll((0 until a.length()).map { a.getInt(it) }) }
        }
        o.optJSONObject("position")?.let { p -> p.keys().forEach { if (it !in position) position[it] = p.getInt(it) } }
        o.optJSONArray("bookmarks")?.let { a -> (0 until a.length()).map { a.getString(it) }.filter { it !in bookmarks }.forEach { bookmarks += it } }
        o.optJSONArray("lexicon")?.let { a ->
            (0 until a.length()).map { a.getJSONObject(it) }.filter { w -> lexicon.none { it.word.equals(w.getString("w"), true) } }.forEach {
                lexicon += SavedWord(it.getString("w"), it.getString("m"), it.optString("e"), it.optString("b"), it.optLong("t"))
            }
        }
        o.optJSONArray("quotes")?.let { a ->
            (0 until a.length()).map { a.getJSONObject(it) }.filter { q -> quotes.none { it.bookId == q.getString("b") && it.idx == q.getInt("i") } }.forEach {
                quotes += SavedQuote(it.getString("q"), it.getString("b"), it.getInt("i"), it.optLong("t"))
            }
        }
        o.optJSONObject("days")?.let { d -> d.keys().forEach { days[it] = maxOf(days[it] ?: 0, d.getInt(it)) } }
        o.optJSONArray("honours")?.let { a -> (0 until a.length()).forEach { honours += a.getString(it) } }
        xp = maxOf(xp, o.optInt("xp"))
        if (lastBook == null) lastBook = o.optString("last").ifBlank { null }
        tick++
        persist()
    }

    private fun load() {
        val o = runCatching { JSONObject(prefs.getString("state", "{}")!!) }.getOrElse { JSONObject() }
        o.optJSONObject("sealed")?.let { s ->
            s.keys().forEach { k -> val a = s.getJSONArray(k); sealed[k] = (0 until a.length()).map { a.getInt(it) }.toMutableSet() }
        }
        o.optJSONObject("position")?.let { p -> p.keys().forEach { position[it] = p.getInt(it) } }
        o.optJSONArray("bookmarks")?.let { a -> (0 until a.length()).forEach { bookmarks += a.getString(it) } }
        o.optJSONArray("lexicon")?.let { a ->
            (0 until a.length()).map { a.getJSONObject(it) }.forEach {
                lexicon += SavedWord(it.getString("w"), it.getString("m"), it.optString("e"), it.optString("b"), it.optLong("t"))
            }
        }
        o.optJSONArray("quotes")?.let { a ->
            (0 until a.length()).map { a.getJSONObject(it) }.forEach {
                quotes += SavedQuote(it.getString("q"), it.getString("b"), it.getInt("i"), it.optLong("t"))
            }
        }
        o.optJSONObject("days")?.let { d -> d.keys().forEach { days[it] = d.getInt(it) } }
        o.optJSONArray("honours")?.let { a -> (0 until a.length()).forEach { honours += a.getString(it) } }
        xp = o.optInt("xp"); goal = o.optInt("goal", 10); geminiKey = o.optString("gemini")
        lastBook = o.optString("last").ifBlank { null }
        night = if (o.has("night")) o.getBoolean("night") else null
    }
}
