package com.purval.folio

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import java.io.File

/**
 * Draws quotes as printed plates — for sharing as images and for the home-screen widget.
 * Plain android.graphics so it works outside Compose (widgets have no Compose).
 */
object CardArt {
    private const val PAPER = 0xFFF6F1E6.toInt()
    private const val INK = 0xFF1A1814.toInt()
    private const val FADED = 0xFF6A6357.toInt()
    private const val RUBRIC = 0xFF8E1B12.toInt()

    private fun font(ctx: Context, res: Int): Typeface = ResourcesCompat.getFont(ctx, res) ?: Typeface.SERIF

    private fun italicRes(era: Era) = when (era) {
        Era.RENAISSANCE -> R.font.fell_italic
        Era.INDIC -> R.font.tiro_italic
        Era.EASTERN -> R.font.zenantique
        Era.BAROQUE -> R.font.greatprimer_italic
        Era.ENLIGHTENMENT -> R.font.pica_italic
        Era.ANCIENT -> R.font.fell_italic
        else -> R.font.oldstandard_italic
    }

    private fun displayRes(era: Era) = when (era) {
        Era.RENAISSANCE, Era.GERMANIC -> R.font.unifraktur
        Era.INDIC -> R.font.amita
        Era.EASTERN -> R.font.yujimai
        Era.BAROQUE -> R.font.pinyon
        Era.ANCIENT -> R.font.cinzel
        Era.ENLIGHTENMENT -> R.font.pica
        Era.MODERN -> R.font.specialelite
        else -> R.font.oldstandard
    }

    private fun layout(text: CharSequence, paint: TextPaint, width: Int, mult: Float = 1.18f): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width).setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, mult).setIncludePad(false).build()

    /** Largest text size (≤ max) at which [text] fits in [w]×[h]. */
    private fun fit(text: String, paint: TextPaint, w: Int, h: Int, max: Float, min: Float): StaticLayout {
        var size = max
        while (true) {
            paint.textSize = size
            val l = layout(text, paint, w)
            if (l.height <= h || size <= min) return l
            size -= 2f
        }
    }

    private fun frame(c: Canvas, w: Int, h: Int, inset: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = INK }
        p.strokeWidth = 3f; c.drawRect(inset, inset, w - inset, h - inset, p)
        p.strokeWidth = 1.4f; p.alpha = 150; c.drawRect(inset + 14, inset + 14, w - inset - 14, h - inset - 14, p)
    }

    private fun plate(ctx: Context, book: Book, name: String?): Bitmap? = runCatching {
        if (name == null) return null
        val f = book.dir?.let { File(it, "$name.webp") }
        if (f != null && f.exists()) BitmapFactory.decodeFile(f.path)
        else if (book.imported) null else ctx.assets.open("img/$name.webp").use { BitmapFactory.decodeStream(it) }
    }.getOrNull()

    /* ---------- share cards ---------- */

    enum class Format(val label: String, val w: Int, val h: Int) {
        STORY("Story", 1080, 1920), POST("Post", 1080, 1350), SQUARE("Square", 1080, 1080)
    }

    enum class Style(val label: String) { PLATE("Picture + quote"), QUOTE("Quote only"), PAGE("Full page") }

    enum class Look(val label: String) { BOOK("Book"), CHAPTER("Chapter"), PAPER("Paper"), NIGHT("Night") }

    data class Palette(val bg: Int, val ink: Int, val faded: Int, val accent: Int)

    private const val CREAM = 0xFFF3E7CF.toInt()
    private const val GILT = 0xFFE2C98F.toInt()
    private val PAPER_P = Palette(PAPER, INK, FADED, RUBRIC)
    private val NIGHT_P = Palette(0xFF15130F.toInt(), 0xFFEDE5D3.toInt(), 0xFFA39B8B.toInt(), 0xFFD0634B.toInt())

    /** Each book wears its binding: the same cloth and lettering as its spine on the shelf. */
    private fun bookPalette(era: Era) = when (era) {
        Era.RENAISSANCE, Era.GERMANIC -> Palette(0xFF1E1A17.toInt(), GILT, 0xFFB9A57A.toInt(), 0xFFC4513D.toInt())
        Era.INDIC -> Palette(0xFF7A2317.toInt(), CREAM, 0xFFE0C9A6.toInt(), GILT)
        Era.EASTERN -> Palette(0xFF2B2622.toInt(), CREAM, 0xFFB8AC98.toInt(), 0xFFC4513D.toInt())
        Era.BAROQUE -> Palette(0xFFEDE3CF.toInt(), 0xFF1C1915.toInt(), 0xFF6A6357.toInt(), 0xFF9A2E20.toInt())
        Era.ANCIENT -> Palette(0xFFD8CDB8.toInt(), 0xFF221E19.toInt(), 0xFF5E564A.toInt(), 0xFF7A3B22.toInt())
        Era.ENLIGHTENMENT -> Palette(0xFF2F3B45.toInt(), CREAM, 0xFFB3B9BC.toInt(), GILT)
        Era.VICTORIAN -> Palette(0xFF2E3B31.toInt(), GILT, 0xFFB7B49A.toInt(), CREAM)
        Era.MODERN -> Palette(0xFF4A423A.toInt(), CREAM, 0xFFC9BCA8.toInt(), 0xFFE7A96B.toInt())
    }

    /** Chapter looks: every chapter of every book gets its own period colourway, the same each time. */
    private val CHAPTER_P = listOf(
        Palette(0xFF5B1A14.toInt(), CREAM, 0xFFD9BFA8.toInt(), GILT),               // oxblood
        Palette(0xFF1F2A44.toInt(), CREAM, 0xFFAEB6C9.toInt(), GILT),               // indigo
        Palette(0xFF243326.toInt(), GILT, 0xFFB5B79B.toInt(), CREAM),               // forest
        Palette(0xFFE9D9B4.toInt(), 0xFF2A1F14.toInt(), 0xFF6E5B40.toInt(), 0xFF8E1B12.toInt()), // ochre parchment
        Palette(0xFF3A3F44.toInt(), CREAM, 0xFFB5B9BC.toInt(), 0xFFE0A458.toInt()), // slate
        Palette(0xFF3E2438.toInt(), CREAM, 0xFFCDB5C6.toInt(), GILT),               // plum
        Palette(0xFF173B3A.toInt(), CREAM, 0xFFA9C2BE.toInt(), 0xFFE0A458.toInt()), // verdigris
        Palette(0xFFF1E6D2.toInt(), 0xFF1A1814.toInt(), 0xFF6A6357.toInt(), 0xFF1F4E79.toInt()), // ivory and blue ink
    )

    fun palette(book: Book, i: Int, look: Look): Palette = when (look) {
        Look.PAPER -> PAPER_P
        Look.NIGHT -> NIGHT_P
        Look.BOOK -> bookPalette(book.era)
        Look.CHAPTER -> CHAPTER_P[Math.floorMod((book.id + "|" + book.cards[i].ch).hashCode(), CHAPTER_P.size)]
    }

    private fun light(c: Int) = (android.graphics.Color.red(c) * 299 + android.graphics.Color.green(c) * 587 + android.graphics.Color.blue(c) * 114) / 1000 > 140

    private fun tp(color: Int, size: Float, face: Typeface, spacing: Float = 0f) =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; textSize = size; typeface = face; letterSpacing = spacing }

    /** A share card for any quote, in any size, style and look. Plain Canvas, so it works for every book, now and later. */
    fun render(ctx: Context, book: Book, i: Int, format: Format, style: Style, look: Look): Bitmap {
        val card = book.cards[i]
        val pal = palette(book, i, look)
        val w = format.w; val h = format.h
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(pal.bg)
        // double rule, like a printed plate
        val rule = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.style = Paint.Style.STROKE; color = pal.ink }
        rule.strokeWidth = 3f; rule.alpha = 200; c.drawRect(36f, 36f, w - 36f, h - 36f, rule)
        rule.strokeWidth = 1.4f; rule.alpha = 120; c.drawRect(50f, 50f, w - 50f, h - 50f, rule)

        val story = format == Format.STORY
        val margin = 110
        val cw = w - 2 * margin
        val top = if (story) 230f else 100f          // Instagram covers the top and bottom of a story
        val footer = if (story) 300f else 150f
        val gap = if (story) 44f else 32f

        val label = layout("${book.title} · ${card.ch}".uppercase(), tp(pal.accent, if (story) 32f else 28f, font(ctx, R.font.fell_sc), .14f), cw)
        val title = if (style == Style.PAGE) layout(card.title, tp(pal.ink, if (story) 70f else 58f, font(ctx, displayRes(book.era))), cw) else null
        val img = if (style == Style.QUOTE) null else plate(ctx, book, card.img ?: book.cover)
        val imgH = img?.let { (cw * when { style == Style.PAGE -> .56f; story -> .78f; format == Format.SQUARE -> .48f; else -> .62f }).toInt() } ?: 0
        val orn = if (style == Style.QUOTE) layout("“", tp(pal.accent, if (story) 260f else 200f, font(ctx, displayRes(book.era))), cw, 0.7f)
            else layout(book.era.fleuron, tp(pal.accent, 46f, Typeface.SERIF), cw)
        val mean = if (style == Style.PAGE && card.qMean.isNotBlank())
            layout("In plain English: ${card.qMean}", tp(pal.faded, if (story) 40f else 30f, font(ctx, R.font.fell_regular)), cw, 1.25f) else null
        val attr = layout("— ${card.attribution(book)}", tp(pal.faded, if (story) 36f else 30f, font(ctx, R.font.fell_sc), .05f), cw)

        val parts = listOfNotNull(label.height, title?.height, if (img != null) imgH else null, orn.height, mean?.height, attr.height)
        val fixed = parts.sum() + gap * (parts.size)
        val room = h - top - footer - fixed
        val maxQ = when (style) { Style.QUOTE -> if (story) 96f else 76f; Style.PLATE -> if (story) 78f else 58f; Style.PAGE -> if (story) 62f else 46f }
        val q = fit("“${card.quote}”", tp(pal.ink, 0f, font(ctx, italicRes(book.era))), cw, room.toInt().coerceAtLeast(200), maxQ, 26f)
        val total = fixed + q.height
        var y = top + ((h - top - footer) - total).coerceAtLeast(0f) / 2f

        fun put(l: StaticLayout) { c.save(); c.translate(margin.toFloat(), y); l.draw(c); c.restore(); y += l.height + gap }
        put(label)
        title?.let { put(it) }
        if (img != null) {
            val dst = RectF(margin.toFloat(), y, (margin + cw).toFloat(), y + imgH)
            val r = dst.width() / dst.height(); val iw = img.width; val ih = img.height
            val src = if (iw / ih.toFloat() > r) { val k = (ih * r).toInt(); Rect((iw - k) / 2, 0, (iw + k) / 2, ih) }
                else { val k = (iw / r).toInt(); Rect(0, 0, iw, k) } // keep the top of tall pictures: heads, not feet
            c.drawBitmap(img, src, dst, Paint(Paint.FILTER_BITMAP_FLAG).apply {
                colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
                if (!light(pal.bg)) alpha = 225
            })
            c.drawRect(dst, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.style = Paint.Style.STROKE; strokeWidth = 2f; color = pal.ink; alpha = 190 })
            y += imgH + gap
        }
        if (style == Style.QUOTE) { c.save(); c.translate(margin.toFloat(), y - gap * .5f); orn.draw(c); c.restore(); y += orn.height * .55f }
        else put(orn)
        put(q)
        mean?.let { put(it) }
        put(attr)

        val fy = h - footer + (if (story) 90f else 40f)
        c.drawText("Folio", w / 2f, fy + 40f, tp(pal.accent, 46f, font(ctx, R.font.unifraktur)).apply { textAlign = Paint.Align.CENTER })
        c.drawText("old books, one page at a time · free on Android", w / 2f, fy + 78f,
            tp(pal.faded, 22f, font(ctx, R.font.fell_italic)).apply { textAlign = Paint.Align.CENTER })
        return bmp
    }

    /** Old entry point: the classic portrait plate. */
    fun quoteCard(ctx: Context, book: Book, i: Int) = render(ctx, book, i, Format.POST, Style.PLATE, Look.PAPER)

    private fun file(ctx: Context, bmp: Bitmap): File {
        val dir = File(ctx.cacheDir, "share").apply { mkdirs() }
        return File(dir, "folio-quote.png").also { f -> f.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) } }
    }

    /** Shares the picture, with the words too for apps that only take text. */
    fun share(ctx: Context, book: Book, i: Int, bmp: Bitmap = quoteCard(ctx, book, i)) {
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.updates", file(ctx, bmp))
        val c = book.cards[i]
        ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("image/png")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .putExtra(Intent.EXTRA_TEXT, "“${c.quote}”\n— ${c.attribution(book)}\n\nRead it in Folio: github.com/purvalsingh/folio")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), "Share quote").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** Saves the picture into Pictures/Folio, where Instagram's story picker finds it. Android 10+. */
    fun save(ctx: Context, bmp: Bitmap): Boolean = runCatching {
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "folio-${System.currentTimeMillis()}.png")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Folio")
        }
        val uri = ctx.contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        ctx.contentResolver.openOutputStream(uri)!!.use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }.isSuccess

    /** The widget's plate: quote of the day, sized to the widget. */
    fun widget(ctx: Context, book: Book, i: Int, wPx: Int, hPx: Int, dark: Boolean): Bitmap {
        val w = wPx.coerceIn(300, 1400); val h = hPx.coerceIn(150, 1400)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val paper = if (dark) 0xFF1C1915.toInt() else PAPER
        val ink = if (dark) 0xFFE8E1D2.toInt() else INK
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = paper }
        c.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), 28f, 28f, p)
        val s = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = ink; strokeWidth = 2f; alpha = 170 }
        c.drawRoundRect(RectF(12f, 12f, w - 12f, h - 12f), 18f, 18f, s)
        val pad = (w * 0.07f).toInt()
        val label = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = RUBRIC; textSize = h * 0.075f; typeface = font(ctx, R.font.fell_sc); letterSpacing = .12f }
        c.drawText("QUOTE OF THE DAY", pad.toFloat(), pad + label.textSize, label)
        val card = book.cards[i]
        val qp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = ink; typeface = font(ctx, italicRes(book.era)) }
        val top = pad + label.textSize * 1.8f
        val by = h * 0.17f
        val ql = fit("“${card.quote}”", qp, w - 2 * pad, (h - top - by - pad * .5f).toInt(), h * 0.13f, 18f)
        c.save(); c.translate(pad.toFloat(), top + ((h - top - by - pad * .5f) - ql.height) / 2f); ql.draw(c); c.restore()
        val ap = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = if (dark) 0xFFA39B8B.toInt() else FADED; textSize = h * 0.065f; typeface = font(ctx, R.font.fell_italic) }
        val attr = "— ${book.short.ifBlank { book.author }}, ${book.title}"
        c.drawText(android.text.TextUtils.ellipsize(attr, ap, (w - 2 * pad).toFloat(), android.text.TextUtils.TruncateAt.END).toString(),
            pad.toFloat(), h - pad * .9f, ap)
        return bmp
    }
}
