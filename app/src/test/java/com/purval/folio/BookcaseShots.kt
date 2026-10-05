package com.purval.folio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import java.io.File

/** The whole bookcase on one tall canvas, with the online library's books shown as faded spines. */
class BookcaseShots {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6.copy(screenHeight = 8600), showSystemUi = false, maxPercentDifference = 1.0, useDeviceResolution = true)

    @Test fun bookcase() {
        val app = App(paparazzi.context)
        if (app.books.isEmpty()) listOf("prince", "laws", "artofwar", "gita").forEach {
            app.books += Shelf.parse(JSONObject(File("src/main/assets/books/$it.json").readText()), false)
        }
        app.store.seal(app.books[0], 0, app.books)
        val remote = File("../library-only/books").listFiles()!!.sorted().map { f ->
            val b = Shelf.parse(JSONObject(f.readText()), false)
            Library.Entry(b.id, b.version, b.title, b.author, b.year, b.era, b.cards.size, 900, b.blurb, emptyList(), b.shelf)
        }
        app.catalog = Library.Catalog(null, remote)
        paparazzi.snapshot {
            FolioTheme(false) { Box(Modifier.fillMaxSize().paper(LocalInk.current.page, LocalInk.current)) { BookcaseScreen(app, { _, _ -> }, {}) } }
        }
    }
}
