package com.purval.folio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

/** Design snapshots: ./gradlew :app:recordPaparazziDebug → app/src/test/snapshots/images */
class Shots {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6, showSystemUi = false, maxPercentDifference = 1.0)

    private fun shot(night: Boolean = false, body: @Composable (App) -> Unit) {
        val app = App(paparazzi.context)
        if (app.books.isEmpty()) listOf("prince", "laws", "artofwar", "gita").forEach {
            app.books += Shelf.parse(org.json.JSONObject(java.io.File("src/main/assets/books/$it.json").readText()), false)
        }
        paparazzi.snapshot {
            androidx.compose.runtime.CompositionLocalProvider(androidx.activity.compose.LocalActivityResultRegistryOwner provides NoResults,
                androidx.compose.ui.platform.LocalInspectionMode provides true) {
                FolioTheme(night) { Box(Modifier.fillMaxSize().paper(LocalInk.current.page, LocalInk.current)) { body(app) } }
            }
        }
    }

    private fun seeded(app: App) = app.apply {
        val b = books[0]
        (0 until 12).forEach { store.seal(b, it, books) }
        listOf("patrimony", "clemency", "prudence").forEach { store.toggleWord(b.glossary.getValue(it), b.id, books) }
        listOf(30, 33, 41).forEach { store.toggleQuote(b, it, books) }
        store.toggleBookmark(b.cardId(33)); store.setPosition(b.id, 12)
    }

    @Test fun librarySeeded() = shot { LibraryScreen(seeded(it), { _, _ -> }, {}) }
    @Test fun lexicon() = shot { LexiconScreen(seeded(it)) }
    @Test fun commonplace() = shot { CommonplaceScreen(seeded(it)) { _, _ -> } }
    @Test fun marks() = shot { MarksScreen(seeded(it)) { _, _ -> } }
    @Test fun honoursSeeded() = shot { HonoursScreen(seeded(it)) }
    @Test fun celebration() = shot { CelebrationCard(Celebration("The Centurion", "Congratulations — you have read 100 pages!", "C"), 1f, -10f, .35f) {} }
    @Test fun library() = shot { LibraryScreen(it, { _, _ -> }, {}) }
    @Test fun readerFirst() = shot { Reader(it, it.books[0], 0) {} }
    @Test fun readerChapter() = shot { Reader(it, it.books[0], 4) {} }
    @Test fun readerNight() = shot(night = true) { Reader(it, it.books[0], 5) {} }
    @Test fun honours() = shot { HonoursScreen(it) }
    @Test fun word() = shot { WordSheet(it, it.books[0].glossary.getValue("patrimony"), "prince") }
    @Test fun laws() = shot { Reader(it, it.book("laws")!!, 0) {} }
    @Test fun artofwar() = shot { Reader(it, it.book("artofwar")!!, 10) {} }
    private fun desk(app: App) = app.apply {
        seeded(this)
        listOf("gita" to 6, "artofwar" to 3).forEach { (id, n) -> val b = book(id)!!; (0 until n).forEach { store.seal(b, it, books) }; store.setPosition(id, n) }
        val laws = book("laws")!!; laws.cards.indices.forEach { store.seal(laws, it, books) }
        store.setPosition("prince", 12)
    }
    @Test fun readingNow() = shot { ReadingNowScreen(desk(it), { _, _ -> }) {} }
    @Test fun readingNowNight() = shot(night = true) { ReadingNowScreen(desk(it), { _, _ -> }) {} }
    @Test fun libraryDesk() = shot { LibraryScreen(desk(it), { _, _ -> }, {}) }
    @Test fun recall() = shot { RecallScreen(seeded(it)) {} }
    @Test fun chapterCheck() = shot { ChapterCheck(it, it.books[0], 0) {} }
    @Test fun journey() = shot { app ->
        val j = Journeys.parse(org.json.JSONArray(java.io.File("src/main/assets/journeys.json").readText())).first()
        app.catalog = Library.Catalog(null, emptyList(), journeys = listOf(j))
        JourneyScreen(app, j, {}, {})
    }
    @Test fun librarySeededJourneys() = shot { app ->
        app.catalog = Library.Catalog(null, emptyList(), journeys = Journeys.parse(org.json.JSONArray(java.io.File("src/main/assets/journeys.json").readText())))
        LibraryScreen(seeded(app), { _, _ -> }, {})
    }
    @Test fun quoteCard() = shot { app ->
        val ctx = androidx.compose.ui.platform.LocalContext.current
        androidx.compose.foundation.Image(CardArt.quoteCard(ctx, app.books[0], 30).asImageBitmap(), null, Modifier.fillMaxSize())
    }
    private fun lib(app: App) = app.apply {
        java.io.File("../library-only/books").listFiles()!!.sorted().forEach { f -> if (book(f.nameWithoutExtension) == null) books += Shelf.parse(org.json.JSONObject(f.readText()), false) }
    }
    @Test fun backSide() = shot { FolioCard(it, it.books[0], 31, {}, {}, startFlipped = true) }
    @Test fun backSideNight() = shot(night = true) { FolioCard(it, it.book("artofwar")!!, 1, {}, {}, startFlipped = true) }
    @Test fun theme() = shot { app -> lib(app); ThemeScreen(app, Explore.bundledThemes(paparazzi.context).ifEmpty { Explore.themes(org.json.JSONArray(java.io.File("src/main/assets/themes.json").readText())) }.first { it.id == "anger" }, { _, _ -> }) {} }
    @Test fun scenario() = shot { app -> lib(app); ScenarioScreen(app, Explore.scenarios(org.json.JSONArray(java.io.File("src/main/assets/scenarios.json").readText())).first(), { _, _ -> }) {} }
    @Test fun studio() = shot { ShareStudio(it.books[0], 31) {} }
    @Test fun export() = shot { ExportSheet(seeded(it)) {} }
    @Test fun gita() = shot { Reader(it, it.book("gita")!!, 3) {} }
}

/** Permission launchers need a registry; snapshots never launch anything. */
private object NoResults : androidx.activity.result.ActivityResultRegistryOwner {
    override val activityResultRegistry = object : androidx.activity.result.ActivityResultRegistry() {
        override fun <I, O> onLaunch(requestCode: Int, contract: androidx.activity.result.contract.ActivityResultContract<I, O>, input: I,
            options: androidx.core.app.ActivityOptionsCompat?) {}
    }
}
