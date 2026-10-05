package com.purval.folio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object Gemini {
    private const val MODEL = "gemini-flash-latest"

    /** Returns the model's JSON text, or null on any failure (callers fall back to offline). */
    fun ask(key: String, prompt: String): String? = runCatching {
        val c = URL("https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent").openConnection() as HttpURLConnection
        c.requestMethod = "POST"; c.doOutput = true; c.connectTimeout = 15000; c.readTimeout = 90000
        c.setRequestProperty("Content-Type", "application/json")
        c.setRequestProperty("x-goog-api-key", key)
        val body = JSONObject()
            .put("contents", JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))))
            .put("generationConfig", JSONObject().put("responseMimeType", "application/json").put("temperature", 0.4))
        c.outputStream.use { it.write(body.toString().toByteArray()) }
        if (c.responseCode != 200) return null
        val r = JSONObject(c.inputStream.bufferedReader().readText())
        r.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
    }.getOrNull()
}

object Dict {
    /** Simple meaning + everyday example. Gemini if the reader added a key, else the free dictionary API. */
    /** Plain-English meaning + everyday example of a quote from an imported book (needs the reader's Gemini key). */
    suspend fun explainQuote(quote: String, title: String, key: String): Pair<String, String>? = withContext(Dispatchers.IO) {
        Gemini.ask(key, "A reader of the old book \"$title\" found this line hard: \"$quote\". Return JSON {\"m\": what it means in plain modern English, at most 30 words, \"e\": one short example of the same idea in everyday modern life}.")
            ?.let { runCatching { JSONObject(it) }.getOrNull() }
            ?.let { it.getString("m") to it.optString("e") }
    }

    suspend fun lookup(word: String, key: String): Gloss? = withContext(Dispatchers.IO) {
        if (key.isNotBlank()) {
            Gemini.ask(key, "Explain the English word \"$word\" for someone who has never seen it. Return JSON {\"m\": simple meaning in at most 15 plain words, \"e\": one short everyday modern example sentence using it}.")
                ?.let { runCatching { JSONObject(it) }.getOrNull() }
                ?.let { return@withContext Gloss(word, it.getString("m"), it.optString("e")) }
        }
        val forms = listOf(word, word.removeSuffix("s"), word.removeSuffix("ed"), word.removeSuffix("ing"), word.removeSuffix("ly")).distinct()
        for (f in forms) {
            val g = runCatching {
                val c = URL("https://api.dictionaryapi.dev/api/v2/entries/en/" + URLEncoder.encode(f, "UTF-8")).openConnection() as HttpURLConnection
                c.connectTimeout = 10000; c.readTimeout = 10000
                if (c.responseCode != 200) return@runCatching null
                val meanings = JSONArray(c.inputStream.bufferedReader().readText()).getJSONObject(0).getJSONArray("meanings")
                var def = ""; var ex = ""
                for (i in 0 until meanings.length()) {
                    val ds = meanings.getJSONObject(i).getJSONArray("definitions")
                    for (j in 0 until ds.length()) {
                        val d = ds.getJSONObject(j)
                        if (def.isEmpty()) def = d.getString("definition")
                        if (ex.isEmpty() && d.has("example")) { ex = d.getString("example"); if (def.isEmpty()) def = d.getString("definition") }
                    }
                }
                if (def.isEmpty()) null else Gloss(word, def, ex)
            }.getOrNull()
            if (g != null) return@withContext g
        }
        null
    }
}

object Importer {
    private const val MAX_PAGES = 800
    private const val PASSAGE_WORDS = 320

    suspend fun import(
        ctx: Context, uri: Uri, title: String, author: String, era: Era, key: String,
        progress: (String, Float) -> Unit,
    ): Book = withContext(Dispatchers.IO) {
        PDFBoxResourceLoader.init(ctx)
        val id = "imp" + System.currentTimeMillis()
        val dir = File(Shelf.importedDir(ctx), id).apply { mkdirs() }
        val pdf = File(ctx.cacheDir, "$id.pdf")
        ctx.contentResolver.openInputStream(uri)!!.use { i -> pdf.outputStream().use { i.copyTo(it) } }
        try {
            progress("Reading the pages…", 0f)
            val pages = PDDocument.load(pdf).use { doc ->
                val n = doc.numberOfPages.coerceAtMost(MAX_PAGES)
                val strip = PDFTextStripper()
                (1..n).map { p ->
                    if (p % 10 == 0) progress("Reading page $p of $n…", 0.3f * p / n)
                    strip.startPage = p; strip.endPage = p
                    strip.getText(doc)
                }
            }
            val passages = Distiller.passages(pages, PASSAGE_WORDS)
            if (passages.isEmpty()) error("No readable text in this PDF. Scanned books need a PDF with a text layer (most archive.org and Gutenberg PDFs have one).")

            val common = ctx.assets.open("common_words.txt").bufferedReader().readLines().toHashSet()
            val glossary = HashMap<String, Gloss>()
            val leaves = ArrayList<Distiller.Leaf>()
            val batch = 3
            for (s in passages.indices step batch) {
                val group = passages.subList(s, minOf(s + batch, passages.size))
                progress(if (key.isBlank()) "Distilling passage ${s + 1} of ${passages.size}…" else "Writing plain-English cards ${s + 1}–${s + group.size} of ${passages.size}…",
                    0.3f + 0.5f * s / passages.size)
                val ai = if (key.isNotBlank()) aiLeaves(key, title, group, glossary) else null
                group.forEachIndexed { k, p ->
                    val l = ai?.getOrNull(k)
                    leaves += if (l == null) Distiller.distil(p.text, common)
                    else l.apply { if (quote.isBlank()) quote = Distiller.distil(p.text, common).quote }
                }
            }
            // offline hard words get filled in lazily (looked up when tapped)
            leaves.forEach { l -> l.words.forEach { w -> glossary.putIfAbsent(w.lowercase(), Gloss(w, "", "")) } }

            progress("Engraving the plates…", 0.8f)
            ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
                PdfRenderer(fd).use { r ->
                    passages.forEachIndexed { i, p ->
                        if (i % 5 == 0) progress("Engraving plate ${i + 1} of ${passages.size}…", 0.8f + 0.2f * i / passages.size)
                        if (p.firstPage < r.pageCount) r.openPage(p.firstPage).use { page -> savePlate(page, File(dir, "c$i.webp")) }
                    }
                }
            }
            val cards = passages.mapIndexed { i, p ->
                val l = leaves[i]
                Card(i, p.chapter, "", l.title ?: "A Passage", l.summary ?: "", l.quote ?: "", "c$i")
            }
            val book = Book(id, title, author, "", "Imported from your PDF", era,
                "Your own book, bound as ${cards.size} flashcards.", "c0", glossary, cards, imported = true, dir = dir)
            Shelf.save(ctx, book)
            book
        } catch (t: Throwable) {
            dir.deleteRecursively(); throw t
        } finally {
            pdf.delete()
        }
    }

    /** Plate = the real page, cropped to its top two-thirds, greyscale — a facsimile of the folio. */
    private fun savePlate(page: PdfRenderer.Page, out: File) {
        val w = 720
        val h = (w * page.height / page.width.toFloat()).toInt().coerceAtLeast(1)
        val full = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        full.eraseColor(android.graphics.Color.WHITE)
        page.render(full, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        val cropH = minOf(h, w * 3 / 4)
        val plate = Bitmap.createBitmap(w, cropH, Bitmap.Config.ARGB_8888)
        Canvas(plate).drawBitmap(full, 0f, 0f, Paint().apply { colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) }) })
        out.outputStream().use { plate.compress(Bitmap.CompressFormat.WEBP_LOSSY, 72, it) }
        full.recycle(); plate.recycle()
    }

    private fun aiLeaves(key: String, title: String, group: List<Distiller.Passage>, glossary: MutableMap<String, Gloss>): List<Distiller.Leaf>? {
        val prompt = buildString {
            append("You turn passages of the old book \"").append(title).append("\" into flashcards for readers with short attention spans.\n")
            append("For EACH passage return an object with:\n")
            append("- title: 3 to 6 catchy words\n")
            append("- summary: 55 to 85 words in plain modern English. Keep the book's key terms and tone; do not invent facts.\n")
            append("- quote: ONE important sentence copied EXACTLY, word for word, from the passage\n")
            append("- words: 2 to 5 words from the passage an average modern reader may not know; each {w: word as written, m: simple meaning in at most 15 words, e: one everyday modern example sentence}\n")
            append("Return a JSON array with one object per passage, in order.\n\n")
            group.forEachIndexed { i, p -> append("### Passage ").append(i + 1).append("\n").append(Distiller.clean(p.text).take(6000)).append("\n\n") }
        }
        val raw = Gemini.ask(key, prompt) ?: return null
        val arr = runCatching { JSONArray(raw) }.getOrNull() ?: return null
        if (arr.length() != group.size) return null
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val src = Distiller.clean(group[i].text)
            Distiller.Leaf().apply {
                this.title = o.optString("title").ifBlank { "A Passage" }
                summary = o.optString("summary")
                // only keep the quote if it really is the author's words
                quote = o.optString("quote").takeIf { it.isNotBlank() && src.contains(it.trim().trimEnd('.')) } ?: ""
                val ws = o.optJSONArray("words") ?: JSONArray()
                for (j in 0 until ws.length()) {
                    val w = ws.getJSONObject(j)
                    val word = w.optString("w").trim()
                    if (word.isBlank()) continue
                    glossary[word.lowercase()] = Gloss(word, w.optString("m"), w.optString("e"))
                    words.add(word)
                }
            }
        }
    }
}
