package com.purval.folio

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import java.io.File
import java.time.LocalDate

/** Takes your words and quotes out of Folio: a printable PDF, a Markdown note, or an Anki deck. */
object Export {
    private data class Item(val head: String, val body: String, val note: String, val by: String)

    private fun words(app: App) = app.store.lexicon.map { w ->
        Item(w.word, w.meaning, w.example, app.book(w.bookId)?.title.orEmpty())
    }

    private fun quotes(app: App) = app.store.quotes.map { q ->
        val b = app.book(q.bookId); val c = b?.cards?.getOrNull(q.idx)
        Item("“${q.text}”", c?.qMean.orEmpty(), c?.qLife.orEmpty(), c?.let { it.attribution(b) } ?: "")
    }

    fun markdown(app: App): String = buildString {
        appendLine("# My Folio notes"); appendLine("_Exported ${LocalDate.now()}_\n")
        val q = quotes(app); val w = words(app)
        if (q.isNotEmpty()) {
            appendLine("## Commonplace book (${q.size} quotes)\n")
            q.forEach { appendLine("> ${it.head}"); if (it.by.isNotBlank()) appendLine(">\n> — ${it.by}"); appendLine()
                if (it.body.isNotBlank()) appendLine("**In plain English:** ${it.body}\n"); if (it.note.isNotBlank()) appendLine("*In daily life:* ${it.note}\n") }
        }
        if (w.isNotEmpty()) {
            appendLine("## Lexicon (${w.size} words)\n")
            w.forEach { appendLine("- **${it.head}**: ${it.body}" + (if (it.note.isNotBlank()) " _e.g. ${it.note}_" else "") + (if (it.by.isNotBlank()) " (${it.by})" else "")) }
        }
    }

    /** Anki imports tab-separated text: front, back, tags. Words test the meaning; quotes test the meaning of the line. */
    fun anki(app: App): String = buildString {
        appendLine("#separator:tab"); appendLine("#html:true"); appendLine("#tags column:3")
        fun clean(s: String) = s.replace("\t", " ").replace("\n", "<br>")
        words(app).forEach { appendLine("${clean(it.head)}\t${clean(it.body)}${if (it.note.isNotBlank()) "<br><i>${clean(it.note)}</i>" else ""}\tfolio::words") }
        quotes(app).filter { it.body.isNotBlank() }.forEach { appendLine("${clean(it.head)}<br><small>${clean(it.by)}</small>\t${clean(it.body)}\tfolio::quotes") }
    }

    fun pdf(ctx: Context, app: App, out: File) {
        val doc = PdfDocument()
        val w = 595; val h = 842; val m = 56 // A4 in points
        fun face(r: Int) = ResourcesCompat.getFont(ctx, r)
        val title = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 30f; typeface = face(R.font.unifraktur) }
        val head = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 13f; typeface = face(R.font.fell_italic) }
        val body = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f; typeface = face(R.font.fell_regular) }
        val small = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9f; typeface = face(R.font.fell_sc); color = 0xFF6A6357.toInt() }
        val sec = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f; typeface = face(R.font.fell_sc); color = 0xFF8E1B12.toInt(); letterSpacing = .12f }
        var page: PdfDocument.Page? = null; var y = 0f; var n = 0
        fun newPage() { page?.let { doc.finishPage(it) }; n++; page = doc.startPage(PdfDocument.PageInfo.Builder(w, h, n).create()); y = m.toFloat()
            page!!.canvas.drawText("Folio · page $n", w / 2f - 30, h - 28f, small) }
        fun put(text: String, p: TextPaint, after: Float = 4f) {
            val l = StaticLayout.Builder.obtain(text, 0, text.length, p, w - 2 * m).setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(0f, 1.15f).build()
            if (y + l.height > h - m) newPage()
            page!!.canvas.save(); page!!.canvas.translate(m.toFloat(), y); l.draw(page!!.canvas); page!!.canvas.restore(); y += l.height + after
        }
        newPage()
        put("My Folio notes", title, 2f); put("Exported ${LocalDate.now()}", small, 18f)
        val q = quotes(app); val wd = words(app)
        if (q.isNotEmpty()) { put("COMMONPLACE BOOK · ${q.size} QUOTES", sec, 10f)
            q.forEach { put(it.head, head, 2f); if (it.by.isNotBlank()) put("— ${it.by}", small, 3f); if (it.body.isNotBlank()) put("In plain English: ${it.body}", body, 2f)
                if (it.note.isNotBlank()) put("In daily life: ${it.note}", body, 2f); y += 12f } }
        if (wd.isNotEmpty()) { y += 8f; put("LEXICON · ${wd.size} WORDS", sec, 10f)
            wd.forEach { put("${it.head} — ${it.body}", body, 1f); if (it.note.isNotBlank()) put("e.g. ${it.note}", small, 8f) else y += 6f } }
        page?.let { doc.finishPage(it) }
        out.outputStream().use { doc.writeTo(it) }
        doc.close()
    }

    fun send(ctx: Context, app: App, kind: String) {
        val dir = File(ctx.cacheDir, "share").apply { mkdirs() }
        val (f, mime) = when (kind) {
            "pdf" -> File(dir, "Folio-notes.pdf").also { pdf(ctx, app, it) } to "application/pdf"
            "anki" -> File(dir, "Folio-anki.txt").also { it.writeText(anki(app)) } to "text/plain"
            else -> File(dir, "Folio-notes.md").also { it.writeText(markdown(app)) } to "text/markdown"
        }
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.updates", f)
        ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType(mime).putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), "Export notes").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

@Composable
fun ExportSheet(app: App, onDone: () -> Unit) {
    val ink = LocalInk.current
    val ctx = LocalContext.current
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 28.dp)) {
        Label("Export your notes")
        Text("${app.store.quotes.size} quotes and ${app.store.lexicon.size} words.", fontFamily = Fonts.fell, fontSize = 15.sp, color = ink.faded)
        listOf(
            Triple("pdf", "PDF", "A printable booklet of your quotes and words."),
            Triple("md", "Markdown", "For Notion, Obsidian, Google Keep or any notes app."),
            Triple("anki", "Anki deck", "Flashcards for Anki and AnkiDroid: File › Import."),
        ).forEach { (k, name, line) ->
            Spacer(Modifier.height(12.dp))
            Pill(name, filled = k == "pdf", modifier = Modifier.fillMaxWidth()) { Export.send(ctx, app, k); onDone() }
            Text(line, fontFamily = Fonts.fell, fontSize = 13.sp, color = ink.faded, modifier = Modifier.padding(start = 8.dp, top = 3.dp))
        }
    }
}
