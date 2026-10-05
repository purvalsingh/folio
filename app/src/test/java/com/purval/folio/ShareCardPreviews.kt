package com.purval.folio

import android.graphics.Bitmap
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Renders share cards for a spread of books, styles and looks into build/share-previews (for eyeballing, not asserting). */
class ShareCardPreviews {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6, showSystemUi = false)

    @Test fun render() {
        val ctx = paparazzi.context
        fun load(f: File) = Shelf.parse(JSONObject(f.readText()), false)
        val books = (File("src/main/assets/books").listFiles()!! + File("../library-only/books").listFiles()!!).associate { load(it).let { b -> b.id to b } }
        val out = File("build/share-previews").apply { deleteRecursively(); mkdirs() }
        val picks = listOf(
            Triple("prince", 31, CardArt.Look.BOOK), Triple("gita", 4, CardArt.Look.BOOK), Triple("meditations", 5, CardArt.Look.CHAPTER),
            Triple("kural", 3, CardArt.Look.BOOK), Triple("artofwar", 11, CardArt.Look.NIGHT), Triple("emerson", 0, CardArt.Look.PAPER),
            Triple("prince", 33, CardArt.Look.CHAPTER), Triple("dhammapada", 1, CardArt.Look.CHAPTER),
        )
        for ((id, i, look) in picks) for (style in CardArt.Style.entries) {
            val bmp = CardArt.render(ctx, books.getValue(id), i, CardArt.Format.STORY, style, look)
            File(out, "story-$id-$i-${style.name.lowercase()}-${look.name.lowercase()}.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        for (f in CardArt.Format.entries) {
            val bmp = CardArt.render(ctx, books.getValue("prince"), 31, f, CardArt.Style.PLATE, CardArt.Look.BOOK)
            File(out, "format-${f.name.lowercase()}.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}
