package com.purval.folio

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.launch

class App(ctx: Context) {
    val store = Store(ctx)
    val books = mutableStateListOf<Book>().apply { addAll(Shelf.loadAll(ctx)) }
    private val appCtx = ctx.applicationContext
    var catalog by mutableStateOf<Library.Catalog?>(null)
    var checking by mutableStateOf(false)
    fun book(id: String) = books.firstOrNull { it.id == id }
    fun removeBook(b: Book) { Shelf.delete(appCtx, b.id); books.remove(b); store.forgetBook(b.id) }

    init { CloudConfig.load(appCtx) }
    var session by mutableStateOf(Cloud.load(appCtx))
    var syncNote by mutableStateOf<String?>(null)
    var syncing by mutableStateOf(false)

    suspend fun signedIn(s: Session) { session = s; Cloud.save(appCtx, s); sync() }

    /** Pull, merge, push. The cloud copy is encrypted with a key only this account's password can make. */
    suspend fun sync(pushOnly: Boolean = false) {
        val s0 = session ?: return
        if (syncing || !CloudConfig.ready) return
        syncing = true
        try {
            val s = Cloud.fresh(s0).also { if (it != s0) { session = it; Cloud.save(appCtx, it) } }
            if (!pushOnly) {
                when (val remote = Cloud.pull(s)) {
                    // never overwrite a cloud copy we can't read: it may be the only one
                    null -> { syncNote = "Your cloud copy is locked with a different password, so it was left untouched. Sign in with the password you used before."; return }
                    "" -> Unit
                    else -> store.merge(remote)
                }
            } else if (!store.dirty) return
            Cloud.push(s, store.snapshot().toString())
            store.dirty = false
            syncNote = "Synced just now"
        } catch (e: AuthError) {
            if (e.message?.contains("session ended") == true) { session = null; Cloud.save(appCtx, null) }
            syncNote = e.message
        } catch (e: Exception) {
            syncNote = "Offline — will sync next time."
        } finally { syncing = false }
    }

    suspend fun signOut() {
        session?.let { sync(pushOnly = true); Cloud.signOut(it) }
        session = null; Cloud.save(appCtx, null); syncNote = null
    }

    /** Fetch the catalog; quietly bring every book on the shelf up to its latest edition. */
    suspend fun refresh() {
        if (checking) return
        checking = true
        try {
            val c = Library.fetch()
            if (c != null) CloudConfig.update(appCtx, c.cloudBase, c.cloudKey)
            sync()
            if (c == null) return
            catalog = c
            c.books.forEach { e ->
                val have = book(e.id) ?: return@forEach
                if (have.imported || e.version <= have.version) return@forEach
                runCatching { Library.install(appCtx, e) }.onSuccess { put(it) }
            }
        } finally { checking = false }
    }

    fun put(b: Book) {
        val i = books.indexOfFirst { it.id == b.id }
        if (i >= 0) books[i] = b else books.add(books.indexOfFirst { it.imported }.takeIf { it >= 0 } ?: books.size, b)
    }

    /** Catalog books not yet on this shelf. */
    fun available() = catalog?.books.orEmpty().filter { e -> books.none { it.id == e.id } }

    fun updateWaiting(): Library.Release? = catalog?.app?.takeIf { it.versionCode > Library.installedVersionCode(appCtx) }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    LIBRARY("Library", Icons.Outlined.AutoStories),
    LEXICON("Lexicon", Icons.Outlined.Spellcheck),
    QUOTES("Quotes", Icons.Outlined.FormatQuote),
    MARKS("Marks", Icons.Outlined.BookmarkBorder),
    HONOURS("Honours", Icons.Outlined.WorkspacePremium),
}

private sealed interface Route {
    data object Home : Route
    data class Read(val bookId: String, val idx: Int) : Route
    data object Bind : Route
    data object Account : Route
    data object Recall : Route
    data object Desk : Route
    data class Topic(val id: String) : Route
    data class Scene(val id: String) : Route
    data class Trip(val id: String) : Route
    data class TripDay(val id: String, val day: Int) : Route
}

class MainActivity : ComponentActivity() {
    private lateinit var app: App

    override fun onStop() {
        super.onStop()
        // push unsynced progress when the reader leaves the app
        if (::app.isInitialized) kotlinx.coroutines.MainScope().launch { app.sync(pushOnly = true) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        app = App(applicationContext)
        setContent {
            FolioTheme(app.store.night) {
                val ink = LocalInk.current
                val view = LocalView.current
                SideEffect {
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !ink.dark; isAppearanceLightNavigationBars = !ink.dark
                    }
                }
                Root(app)
            }
        }
    }
}

@Composable
private fun Root(app: App) {
    val ink = LocalInk.current
    var route by remember { mutableStateOf<Route>(Route.Home) }
    var tab by rememberSaveable { mutableStateOf(Tab.LIBRARY) }
    val open: (Book, Int) -> Unit = { b, i -> route = Route.Read(b.id, i) }
    val ctx = LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        app.refresh()
        QuoteWidget.refreshAll(ctx)
        Reminder.schedule(ctx, app.store.remind, app.store.remindHour)
    }
    fun journey(id: String) = app.journeys(ctx).firstOrNull { it.id == id }

    Box(Modifier.fillMaxSize().paper(ink.page, ink)) {
        AnimatedContent(route, transitionSpec = {
            (fadeIn(tween(260)) + slideInVertically(tween(320)) { it / 18 }) togetherWith fadeOut(tween(180))
        }, label = "route") { r ->
            when (r) {
                is Route.Read -> {
                    val b = app.book(r.bookId)
                    if (b == null) route = Route.Home
                    else {
                        BackHandler { route = Route.Home }
                        Reader(app, b, r.idx) { route = Route.Home }
                    }
                }
                Route.Account -> {
                    BackHandler { route = Route.Home }
                    AccountScreen(app) { route = Route.Home }
                }
                is Route.Topic -> {
                    BackHandler { route = Route.Home }
                    val t = app.themes(LocalContext.current).firstOrNull { it.id == r.id }
                    if (t == null) route = Route.Home else ThemeScreen(app, t, open) { route = Route.Home }
                }
                is Route.Scene -> {
                    BackHandler { route = Route.Home }
                    val s = app.scenarios(LocalContext.current).firstOrNull { it.id == r.id }
                    if (s == null) route = Route.Home else ScenarioScreen(app, s, open) { route = Route.Home }
                }
                Route.Desk -> {
                    BackHandler { route = Route.Home }
                    ReadingNowScreen(app, open) { route = Route.Home }
                }
                Route.Recall -> {
                    BackHandler { route = Route.Home }
                    RecallScreen(app) { route = Route.Home }
                }
                is Route.Trip -> {
                    val j = journey(r.id)
                    BackHandler { route = Route.Home }
                    if (j == null) route = Route.Home else JourneyScreen(app, j, back = { route = Route.Home }, read = { d -> route = Route.TripDay(j.id, d) })
                }
                is Route.TripDay -> {
                    val j = journey(r.id)
                    BackHandler { route = Route.Trip(r.id) }
                    if (j == null) route = Route.Home else JourneyReader(app, j, r.day) { route = Route.Trip(r.id) }
                }
                Route.Bind -> {
                    BackHandler { route = Route.Home }
                    BindScreen(app, onBack = { route = Route.Home }, onBound = { b -> open(b, 0) })
                }
                Route.Home -> Column(Modifier.fillMaxSize().statusBarsPadding()) {
                    Box(Modifier.weight(1f)) {
                        AnimatedContent(tab, transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) }, label = "tab") { t ->
                            when (t) {
                                Tab.LIBRARY -> BookcaseScreen(app, open, bind = { route = Route.Bind }, recall = { route = Route.Recall },
                                    journey = { route = Route.Trip(it.id) }, desk = { route = Route.Desk },
                                    theme = { route = Route.Topic(it.id) }, scenario = { route = Route.Scene(it.id) })
                                Tab.LEXICON -> LexiconScreen(app)
                                Tab.QUOTES -> CommonplaceScreen(app, open)
                                Tab.MARKS -> MarksScreen(app, open)
                                Tab.HONOURS -> HonoursScreen(app) { route = Route.Account }
                            }
                        }
                    }
                    TabBar(tab) { tab = it }
                }
            }
        }
        app.store.party.firstOrNull()?.let { c -> CelebrationOverlay(c) { app.store.party.removeAt(0) } }
    }
}

@Composable
private fun TabBar(current: Tab, pick: (Tab) -> Unit) {
    val ink = LocalInk.current
    Column(Modifier.fillMaxWidth().paper(ink.paper, ink).navigationBarsPadding()) {
        Box(Modifier.fillMaxWidth().height(0.8.dp).background(ink.ink.copy(alpha = .5f)))
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Tab.entries.forEach { t ->
                val on = t == current
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).clickable { pick(t) }.padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(Modifier.height(2.dp).fillMaxWidth(0.3f).background(if (on) ink.rubric else ink.paper))
                    Spacer(Modifier.height(4.dp))
                    Icon(t.icon, t.label, tint = if (on) ink.rubric else ink.faded)
                    Text(t.label.uppercase(), fontFamily = Fonts.fellSc, fontSize = 10.sp, letterSpacing = 1.sp,
                        color = if (on) ink.ink else ink.faded)
                }
            }
        }
    }
}

/* ---------- binding (PDF import) ---------- */

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BindScreen(app: App, onBack: () -> Unit, onBound: (Book) -> Unit) {
    val ink = LocalInk.current
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var uri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var era by rememberSaveable { mutableStateOf(Era.RENAISSANCE) }
    var busy by remember { mutableStateOf<Pair<String, Float>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
        if (u != null) {
            uri = u
            if (title.isBlank()) title = displayName(ctx, u).removeSuffix(".pdf").replace('_', ' ').replace('-', ' ')
        }
    }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, enabled = busy == null) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ink.ink) }
        }
        Masthead("Bind a Book", "Give Folio a PDF — an old book from Project Gutenberg or archive.org works best. It becomes short flashcards with plates, quotes and a glossary.")
        Spacer(Modifier.height(18.dp))
        val b = busy
        if (b != null) {
            Panel {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    WaxSeal("✒", 70.dp)
                    Spacer(Modifier.height(12.dp))
                    Text(b.first, fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 17.sp, color = ink.ink, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(progress = { b.second }, modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = ink.rubric, trackColor = ink.rule, drawStopIndicator = {}, gapSize = 0.dp)
                }
            }
            return@Column
        }
        Label("1 · The book")
        Spacer(Modifier.height(8.dp))
        Pill(if (uri == null) "Choose a PDF" else "✓  ${displayName(ctx, uri!!)}", filled = uri == null, modifier = Modifier.fillMaxWidth()) {
            pick.launch(arrayOf("application/pdf"))
        }
        Spacer(Modifier.height(16.dp))
        val fieldStyle = TextStyle(fontFamily = Fonts.fell, fontSize = 18.sp, color = ink.ink)
        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Title", fontFamily = Fonts.fell) },
            singleLine = true, colors = fieldColors(), textStyle = fieldStyle)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(author, { author = it }, Modifier.fillMaxWidth(), label = { Text("Author", fontFamily = Fonts.fell) },
            singleLine = true, colors = fieldColors(), textStyle = fieldStyle)
        Spacer(Modifier.height(20.dp))
        Label("2 · Its age")
        Text("Cards are set in the type of the book's era.", fontFamily = Fonts.fell, fontSize = 14.sp, color = ink.faded)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Era.entries.forEach { e ->
                val on = e == era
                Column(
                    Modifier.clip(RoundedCornerShape(4.dp)).background(if (on) ink.ink else ink.paper)
                        .border(1.dp, if (on) ink.ink else ink.rule, RoundedCornerShape(4.dp))
                        .clickable { era = e }.padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Text(e.label, fontFamily = e.display, fontSize = 22.sp, color = if (on) ink.paper else ink.ink)
                    Text(e.span, fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 12.sp, color = if (on) ink.paper else ink.faded)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            if (app.store.geminiKey.isBlank()) "Offline binding: cards keep the author's own key sentences. For plain-English rewrites, add a Gemini key in Honours."
            else "Your Gemini key will write plain-English summaries. Quotes are checked against the real text.",
            fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 14.sp, color = ink.faded,
        )
        error?.let { Text(it, fontFamily = Fonts.fell, fontSize = 15.sp, color = ink.rubric, modifier = Modifier.padding(top = 10.dp)) }
        Spacer(Modifier.height(16.dp))
        Pill("Bind it  ❧", filled = uri != null && title.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            val u = uri ?: return@Pill
            if (title.isBlank()) return@Pill
            error = null
            busy = "Opening the book…" to 0f
            scope.launch {
                runCatching {
                    Importer.import(ctx, u, title.trim(), author.trim(), era, app.store.geminiKey) { m, p -> busy = m to p }
                }.onSuccess { book -> app.books.add(book); busy = null; onBound(book) }
                    .onFailure { busy = null; error = it.message ?: "That PDF could not be read." }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

private fun displayName(ctx: Context, u: Uri): String =
    ctx.contentResolver.query(u, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
        if (c.moveToFirst()) c.getString(0) else null
    } ?: "book.pdf"
