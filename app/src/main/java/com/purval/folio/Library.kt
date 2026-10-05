package com.purval.folio

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * The online library: one catalog.json in the GitHub repo lists the latest app release and every
 * book edition. New books and corrected editions download without touching the APK; app releases
 * download from GitHub Releases and go through Android's own installer (which refuses any APK not
 * signed with Folio's key).
 */
object Library {
    const val BASE = "https://raw.githubusercontent.com/purvalsingh/folio/main/library/"

    data class Release(val versionCode: Int, val versionName: String, val apk: String, val sha256: String, val notes: String, val sizeKb: Int)
    data class Entry(
        val id: String, val version: Int, val title: String, val author: String, val year: String,
        val era: Era, val cards: Int, val sizeKb: Int, val blurb: String, val images: List<String>,
    )
    data class Catalog(val app: Release?, val books: List<Entry>)

    private fun get(url: String): HttpURLConnection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 15000; readTimeout = 30000
        setRequestProperty("Cache-Control", "no-cache")
    }

    suspend fun fetch(): Catalog? = withContext(Dispatchers.IO) {
        runCatching {
            val c = get(BASE + "catalog.json?t=" + System.currentTimeMillis() / 60000)
            if (c.responseCode != 200) return@runCatching null
            val o = JSONObject(c.inputStream.bufferedReader().readText())
            val app = o.optJSONObject("app")?.let {
                Release(it.getInt("versionCode"), it.getString("versionName"), it.getString("apk"),
                    it.getString("sha256"), it.optString("notes"), it.optInt("sizeKb"))
            }
            val arr = o.getJSONArray("books")
            val books = (0 until arr.length()).map { i ->
                val b = arr.getJSONObject(i)
                val imgs = b.getJSONArray("images")
                Entry(b.getString("id"), b.getInt("version"), b.getString("title"), b.optString("author"),
                    b.optString("year"), Era.of(b.optString("era")), b.optInt("cards"), b.optInt("sizeKb"),
                    b.optString("blurb"), (0 until imgs.length()).map { imgs.getString(it) })
            }
            Catalog(app, books)
        }.getOrNull()
    }

    /** Downloads a book edition into a temp folder, then swaps it in, so a failed download never leaves half a book. */
    suspend fun install(ctx: Context, e: Entry, progress: (Float) -> Unit = {}): Book = withContext(Dispatchers.IO) {
        val root = Shelf.remoteDir(ctx)
        val tmp = File(root, "${e.id}.tmp").apply { deleteRecursively(); mkdirs() }
        try {
            val json = get(BASE + "books/${e.id}.json?v=${e.version}").also { check(it.responseCode == 200) { "Book not found" } }
                .inputStream.bufferedReader().readText()
            val old = File(root, e.id)
            e.images.forEachIndexed { i, name ->
                val out = File(tmp, "$name.webp")
                get(BASE + "img/$name.webp?v=${e.version}").let { c ->
                    check(c.responseCode == 200) { "Missing plate $name" }
                    c.inputStream.use { inp -> out.outputStream().use { inp.copyTo(it) } }
                }
                progress((i + 1f) / (e.images.size + 1))
            }
            File(tmp, "book.json").writeText(json)
            old.deleteRecursively()
            check(tmp.renameTo(old)) { "Could not save the book" }
            File(old, "book.json").renameTo(File(root, "${e.id}.json"))
            progress(1f)
            Shelf.parse(JSONObject(json), false, old)
        } catch (t: Throwable) {
            tmp.deleteRecursively(); throw t
        }
    }

    fun installedVersionCode(ctx: Context): Int {
        val p = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        @Suppress("DEPRECATION")
        return if (Build.VERSION.SDK_INT >= 28) p.longVersionCode.toInt() else p.versionCode
    }

    fun installedVersionName(ctx: Context): String = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "?"

    /** Fetches the new APK and verifies its SHA-256 against the catalog before handing it to the installer. */
    suspend fun downloadApk(ctx: Context, r: Release, progress: (Float) -> Unit): File = withContext(Dispatchers.IO) {
        val dir = File(ctx.cacheDir, "updates").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val out = File(dir, "Folio-${r.versionName}.apk")
        val c = get(r.apk).apply { instanceFollowRedirects = true }
        check(c.responseCode == 200) { "Download failed (${c.responseCode})" }
        val total = c.contentLengthLong.takeIf { it > 0 } ?: (r.sizeKb * 1024L)
        val md = MessageDigest.getInstance("SHA-256")
        c.inputStream.use { inp ->
            out.outputStream().use { os ->
                val buf = ByteArray(64 * 1024); var n: Int; var done = 0L
                while (inp.read(buf).also { n = it } >= 0) {
                    os.write(buf, 0, n); md.update(buf, 0, n); done += n
                    if (total > 0) progress((done / total.toFloat()).coerceAtMost(1f))
                }
            }
        }
        val sha = md.digest().joinToString("") { "%02x".format(it) }
        if (!sha.equals(r.sha256, true)) { out.delete(); error("The download was corrupted. Please try again.") }
        out
    }

    /** Android asks the reader once to allow Folio to install updates; afterwards it is one tap. */
    fun canInstall(ctx: Context) = ctx.packageManager.canRequestPackageInstalls()

    fun askInstallPermission(ctx: Context) = ctx.startActivity(
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )

    fun launchInstaller(ctx: Context, apk: File) {
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.updates", apk)
        ctx.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
