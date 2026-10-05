package com.purval.folio

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.util.Calendar

/** Quote of the day: the same for everyone on a given day, cycling through every quote on the shelf. */
fun quoteOfTheDay(ctx: Context): Pair<Book, Int>? {
    val all = Shelf.loadAll(ctx).filter { !it.imported }.flatMap { b -> b.cards.indices.filter { b.cards[it].quote.isNotBlank() }.map { b to it } }
    if (all.isEmpty()) return null
    return all[((LocalDate.now().toEpochDay() * 7919) % all.size).toInt()]
}

/** Home-screen widget showing the quote of the day as a small printed plate. */
class QuoteWidget : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) = ids.forEach { update(ctx, mgr, it) }

    override fun onAppWidgetOptionsChanged(ctx: Context, mgr: AppWidgetManager, id: Int, opts: Bundle) = update(ctx, mgr, id)

    companion object {
        fun update(ctx: Context, mgr: AppWidgetManager, id: Int) {
            val (book, i) = quoteOfTheDay(ctx) ?: return
            val o = mgr.getAppWidgetOptions(id)
            val d = ctx.resources.displayMetrics.density.coerceAtMost(2.2f)
            val wDp = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 300).takeIf { it > 0 } ?: 300
            val hDp = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 140).takeIf { it > 0 } ?: 140
            val dark = (ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val views = RemoteViews(ctx.packageName, R.layout.widget_quote)
            views.setImageViewBitmap(R.id.plate, CardArt.widget(ctx, book, i, (wDp * d).toInt(), (hDp * d).toInt(), dark))
            views.setContentDescription(R.id.plate, "Quote of the day from ${book.title}: ${book.cards[i].quote}")
            views.setOnClickPendingIntent(R.id.plate, PendingIntent.getActivity(ctx, 0,
                Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            mgr.updateAppWidget(id, views)
        }

        fun refreshAll(ctx: Context) {
            val mgr = AppWidgetManager.getInstance(ctx)
            mgr.getAppWidgetIds(ComponentName(ctx, QuoteWidget::class.java)).forEach { update(ctx, mgr, it) }
        }
    }
}

/** A gentle daily nudge at the reader's chosen hour — skipped once the day's quota is already met. */
object Reminder {
    private const val CHANNEL = "folio_daily"

    private fun intent(ctx: Context) = PendingIntent.getBroadcast(ctx, 7, Intent(ctx, ReminderReceiver::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    fun schedule(ctx: Context, on: Boolean, hour: Int) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        am.cancel(intent(ctx))
        if (!on) return
        val t = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, t, AlarmManager.INTERVAL_DAY, intent(ctx))
    }

    fun canNotify(ctx: Context) = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun post(ctx: Context) {
        val store = Store(ctx)
        if (!store.remind || !canNotify(ctx)) return
        val read = store.readToday()
        if (read >= store.goal) return
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "Daily reading", NotificationManager.IMPORTANCE_DEFAULT))
        val left = store.goal - read
        val streak = store.streak()
        val title = if (streak > 0) "Keep your $streak-day streak" else "Your daily folios await"
        val line = quoteOfTheDay(ctx)?.let { (b, i) -> "“${b.cards[i].quote}” — ${b.short.ifBlank { b.author }}" }
        val text = "$left folio${if (left == 1) "" else "s"} to keep today's quota. One takes a minute."
        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle(title).setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(if (line != null) "$text\n\n$line" else text))
            .setContentIntent(PendingIntent.getActivity(ctx, 0, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            .setAutoCancel(true).build()
        @Suppress("MissingPermission")
        NotificationManagerCompat.from(ctx).notify(1, n)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val s = Store(ctx)
            Reminder.schedule(ctx, s.remind, s.remindHour)
            return
        }
        Reminder.post(ctx)
    }
}
