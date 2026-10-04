package com.purval.folio

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class Gloss(val word: String, val meaning: String, val example: String)

data class Card(
    val idx: Int, val ch: String, val chTitle: String, val title: String,
    val text: String, val quote: String, val img: String?, val cap: String = "",
    /** who said the quote, when it is not the book's own author (e.g. the 48 Laws digest) */
    val qBy: String = "",
) {
    fun attribution(b: Book) = qBy.ifBlank { "${b.author}, ${b.title}" }
}

data class Book(
    val id: String, val title: String, val author: String, val year: String, val translator: String,
    val era: Era, val blurb: String, val cover: String?, val glossary: Map<String, Gloss>,
    val cards: List<Card>, val imported: Boolean,
    /** how quotes are signed on cards, e.g. "Machiavelli" */
    val short: String = author,
) {
    fun cardId(i: Int) = "$id#$i"
    /** Index of the first card of every chapter, used for drop caps and the contents sheet. */
    val chapterStarts: List<Int> by lazy { cards.indices.filter { it == 0 || cards[it].ch != cards[it - 1].ch } }
}

object Shelf {
    fun importedDir(ctx: Context) = File(ctx.filesDir, "books").apply { mkdirs() }

    fun loadAll(ctx: Context): List<Book> {
        val order = listOf("prince", "laws", "artofwar", "gita")
        val builtIn = ctx.assets.list("books").orEmpty().filter { it.endsWith(".json") }
            .sortedBy { order.indexOf(it.removeSuffix(".json")).let { i -> if (i < 0) 99 else i } }.map {
            parse(JSONObject(ctx.assets.open("books/$it").bufferedReader().readText()), imported = false)
        }
        val imported = importedDir(ctx).listFiles { f -> f.name.endsWith(".json") }.orEmpty()
            .sortedBy { it.lastModified() }
            .mapNotNull { runCatching { parse(JSONObject(it.readText()), imported = true) }.getOrNull() }
        return builtIn + imported
    }

    fun parse(o: JSONObject, imported: Boolean): Book {
        val gl = o.optJSONObject("glossary") ?: JSONObject()
        val glossary = gl.keys().asSequence().associate { k ->
            val g = gl.getJSONObject(k)
            k.lowercase() to Gloss(g.optString("root", k), g.getString("m"), g.optString("e"))
        }
        val arr = o.getJSONArray("cards")
        val cards = (0 until arr.length()).map { i ->
            val c = arr.getJSONObject(i)
            Card(i, c.optString("ch"), c.optString("chTitle"), c.optString("title"), c.optString("text"),
                c.optString("quote"), c.optString("img").ifBlank { null }, c.optString("cap"), c.optString("qBy"))
        }
        return Book(
            o.getString("id"), o.getString("title"), o.optString("author"), o.optString("year"),
            o.optString("translator"), Era.of(o.optString("era")), o.optString("blurb"),
            o.optString("cover").ifBlank { null }, glossary, cards, imported,
            o.optString("short").ifBlank { o.optString("author") },
        )
    }

    fun save(ctx: Context, b: Book) {
        val gl = JSONObject()
        b.glossary.forEach { (k, g) -> gl.put(k, JSONObject().put("m", g.meaning).put("e", g.example).put("root", g.word)) }
        val cards = JSONArray()
        b.cards.forEach { c ->
            cards.put(JSONObject().put("ch", c.ch).put("chTitle", c.chTitle).put("title", c.title)
                .put("text", c.text).put("quote", c.quote).put("img", c.img ?: "").put("cap", c.cap).put("qBy", c.qBy))
        }
        val o = JSONObject().put("id", b.id).put("title", b.title).put("author", b.author).put("year", b.year)
            .put("translator", b.translator).put("era", b.era.name).put("blurb", b.blurb)
            .put("cover", b.cover ?: "").put("glossary", gl).put("cards", cards)
        File(importedDir(ctx), "${b.id}.json").writeText(o.toString())
    }

    fun delete(ctx: Context, id: String) {
        File(importedDir(ctx), "$id.json").delete()
        File(importedDir(ctx), id).deleteRecursively()
    }
}
