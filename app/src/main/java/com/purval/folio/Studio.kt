package com.purval.folio

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Choose how a quote is shared: size (Story / Post / Square), style and look, with a live preview. */
@Composable
fun ShareStudio(book: Book, i: Int, onDone: () -> Unit) {
    val ink = LocalInk.current
    val ctx = LocalContext.current
    var format by remember { mutableStateOf(CardArt.Format.STORY) }
    var style by remember { mutableStateOf(CardArt.Style.PLATE) }
    var look by remember { mutableStateOf(CardArt.Look.BOOK) }
    var saved by remember { mutableStateOf<String?>(null) }
    val inspect = LocalInspectionMode.current
    val bmp by produceState(if (inspect) CardArt.render(ctx, book, i, format, style, look) else null, format, style, look) {
        value = withContext(Dispatchers.Default) { CardArt.render(ctx, book, i, format, style, look) }
    }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
        Label("Share this quote")
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().heightIn(max = 430.dp), contentAlignment = Alignment.Center) {
            val b = bmp
            if (b == null) CircularProgressIndicator(color = ink.rubric)
            else Image(b.asImageBitmap(), "Preview of the share card",
                Modifier.aspectRatio(format.w / format.h.toFloat()).clip(RoundedCornerShape(6.dp)).border(1.dp, ink.rule, RoundedCornerShape(6.dp)))
        }
        Spacer(Modifier.height(14.dp))
        Choice("Size", CardArt.Format.entries, format, { it.label }) { format = it; saved = null }
        Choice("Style", CardArt.Style.entries, style, { it.label }) { style = it; saved = null }
        Choice("Look", CardArt.Look.entries, look, { it.label }) { look = it; saved = null }
        Text(when (look) {
            CardArt.Look.BOOK -> "In this book's own binding colours."
            CardArt.Look.CHAPTER -> "Every chapter has its own colourway."
            CardArt.Look.PAPER -> "Ink on cream paper."
            CardArt.Look.NIGHT -> "Lamplight on a dark page."
        }, fontFamily = Fonts.fell, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = ink.faded, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Pill("Share", modifier = Modifier.weight(1f)) { bmp?.let { CardArt.share(ctx, book, i, it); onDone() } }
            if (Build.VERSION.SDK_INT >= 29) Pill("Save to photos", filled = false, modifier = Modifier.weight(1f)) {
                bmp?.let { saved = if (CardArt.save(ctx, it)) "Saved to Pictures › Folio. Open Instagram › Story to add it." else "Couldn't save the picture." }
            }
        }
        saved?.let { Text(it, fontFamily = Fonts.fell, fontSize = 14.sp, color = ink.rubric, modifier = Modifier.padding(top = 10.dp)) }
    }
}

@Composable
private fun <T> Choice(title: String, options: List<T>, selected: T, label: (T) -> String, pick: (T) -> Unit) {
    val ink = LocalInk.current
    Text(title.uppercase(), fontFamily = Fonts.fellSc, fontSize = 11.sp, letterSpacing = 1.5.sp, color = ink.faded, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 8.dp)) {
        items(options) { o ->
            val on = o == selected
            Box(Modifier.clip(RoundedCornerShape(50)).background(if (on) ink.ink else Color.Transparent)
                .border(1.dp, if (on) ink.ink else ink.faded.copy(alpha = .6f), RoundedCornerShape(50))
                .clickable { pick(o) }.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text(label(o), fontFamily = Fonts.fellSc, fontSize = 14.sp, color = if (on) ink.paper else ink.ink)
            }
        }
    }
}
