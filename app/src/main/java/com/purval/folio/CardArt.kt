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

    private fun layout(text: CharSequence, paint: TextPaint, width: Int, mult: Float = 1.18f) =
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

    /** 1080×1350 portrait card: plate, quote, attribution — ready for WhatsApp or Instagram. */
    fun quoteCard(ctx: Context, book: Book, i: Int): Bitmap {
        val card = book.cards[i]
        val w = 1080; val h = 1350
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(PAPER)
        frame(c, w, h, 36f)
        var y = 100f
        plate(ctx, book, card.img ?: book.cover)?.let { img ->
            val pw = w - 220; val ph = (pw * 0.62f).toInt()
            val src = run {
                val r = pw / ph.toFloat(); val iw = img.width; val ih = img.height
                if (iw / ih.toFloat() > r) { val cw = (ih * r).toInt(); Rect((iw - cw) / 2, 0, (iw + cw) / 2, ih) }
                else { val chh = (iw / r).toInt(); Rect(0, (ih - chh) / 2, iw, (ih + chh) / 2) }
            }
            val dst = RectF(110f, y, 110f + pw, y + ph)
            c.drawBitmap(img, src, dst, Paint(Paint.FILTER_BITMAP_FLAG).apply { colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) }) })
            c.drawRect(dst, Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 2f; color = INK })
            y += ph + 60
        } ?: run { y += 120 }
        val orn = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = RUBRIC; textSize = 46f; textAlign = Paint.Align.CENTER }
        c.drawText(book.era.fleuron, w / 2f, y, orn)
        y += 40
        val qp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = INK; typeface = font(ctx, italicRes(book.era)) }
        val bottomReserve = 230
        val ql = fit("“${card.quote}”", qp, w - 220, (h - bottomReserve - y).toInt(), 62f, 30f)
        c.save(); c.translate(110f, y + ((h - bottomReserve - y) - ql.height) / 2f); ql.draw(c); c.restore()
        val ap = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = FADED; textSize = 30f; typeface = font(ctx, R.font.fell_sc); letterSpacing = .06f }
        val al = layout("— ${card.attribution(book)}", ap, w - 220)
        c.save(); c.translate(110f, h - bottomReserve + 20f); al.draw(c); c.restore()
        val fp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = RUBRIC; textSize = 40f; typeface = font(ctx, R.font.unifraktur); textAlign = Paint.Align.CENTER }
        c.drawText("Folio", w / 2f, h - 96f, fp)
        val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = FADED; textSize = 22f; typeface = font(ctx, R.font.fell_italic); textAlign = Paint.Align.CENTER }
        c.drawText("old books, one page at a time", w / 2f, h - 64f, tp)
        return bmp
    }

    /** Shares the quote as a picture, with the text too for apps that only take text. */
    fun share(ctx: Context, book: Book, i: Int) {
        val dir = File(ctx.cacheDir, "share").apply { mkdirs() }
        val f = File(dir, "folio-quote.png")
        f.outputStream().use { quoteCard(ctx, book, i).compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.updates", f)
        val c = book.cards[i]
        ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("image/png")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .putExtra(Intent.EXTRA_TEXT, "“${c.quote}”\n— ${c.attribution(book)}\n\nRead it in Folio: github.com/purvalsingh/folio")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), "Share quote").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

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
