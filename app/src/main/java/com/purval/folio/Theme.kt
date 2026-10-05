package com.purval.folio

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle

@Immutable
data class Ink(
    val page: Color,      // app background
    val paper: Color,     // card surface
    val ink: Color,       // main text
    val faded: Color,     // secondary text
    val rule: Color,      // hairlines
    val rubric: Color,    // the one accent: red initials, seals
    val dark: Boolean,
)

val Vellum = Ink(
    page = Color(0xFFEFE9DD), paper = Color(0xFFF8F4EA), ink = Color(0xFF1A1814),
    faded = Color(0xFF6A6357), rule = Color(0x331A1814), rubric = Color(0xFF8E1B12), dark = false,
)
val Lamplight = Ink(
    page = Color(0xFF12100D), paper = Color(0xFF1C1915), ink = Color(0xFFE8E1D2),
    faded = Color(0xFFA39B8B), rule = Color(0x33E8E1D2), rubric = Color(0xFFC9503F), dark = true,
)

val LocalInk = staticCompositionLocalOf { Vellum }

object Fonts {
    val fraktur = FontFamily(Font(R.font.unifraktur))
    val fell = FontFamily(Font(R.font.fell_regular), Font(R.font.fell_italic, style = FontStyle.Italic))
    val fellSc = FontFamily(Font(R.font.fell_sc))
    val pica = FontFamily(Font(R.font.pica), Font(R.font.pica_italic, style = FontStyle.Italic))
    val cinzel = FontFamily(Font(R.font.cinzel))
    val oldStandard = FontFamily(Font(R.font.oldstandard), Font(R.font.oldstandard_italic, style = FontStyle.Italic))
    val typewriter = FontFamily(Font(R.font.specialelite))
    val garamond = FontFamily(Font(R.font.garamond))
    val brush = FontFamily(Font(R.font.yujimai))
    val zen = FontFamily(Font(R.font.zenantique))
    val amita = FontFamily(Font(R.font.amita))
    val tiro = FontFamily(Font(R.font.tiro), Font(R.font.tiro_italic, style = FontStyle.Italic))
    val copperplate = FontFamily(Font(R.font.pinyon))
    val primer = FontFamily(Font(R.font.greatprimer), Font(R.font.greatprimer_italic, style = FontStyle.Italic))
}

/**
 * Each era gets the letterforms its books were actually written or printed in: [display] for titles
 * and the big initial, [body] for reading, [fleuron] as the ornament between summary and quote.
 */
enum class Era(val label: String, val span: String, val display: FontFamily, val body: FontFamily, val fleuron: String = "❦", val capScale: Float = 0.78f) {
    ANCIENT("Antiquity", "Greece & Rome", Fonts.cinzel, Fonts.garamond, "❧"),
    INDIC("Ancient India", "Sanskrit classics", Fonts.amita, Fonts.tiro, "ॐ", 0.7f),
    EASTERN("Ancient China", "brush & woodblock", Fonts.brush, Fonts.zen, "❖", 0.72f),
    RENAISSANCE("Renaissance", "1450–1600", Fonts.fraktur, Fonts.fell),
    BAROQUE("Baroque", "1600–1750", Fonts.copperplate, Fonts.primer, "✥", 0.9f),
    ENLIGHTENMENT("Enlightenment", "1650–1800", Fonts.pica, Fonts.pica),
    VICTORIAN("Victorian", "1800–1900", Fonts.oldStandard, Fonts.oldStandard),
    MODERN("Modern", "1900–", Fonts.typewriter, Fonts.oldStandard);

    companion object {
        fun of(s: String?) = entries.firstOrNull { it.name.equals(s, true) } ?: RENAISSANCE
    }
}

@Composable
fun FolioTheme(night: Boolean?, content: @Composable () -> Unit) {
    val ink = if (night ?: isSystemInDarkTheme()) Lamplight else Vellum
    val scheme = if (ink.dark) darkColorScheme(
        primary = ink.rubric, background = ink.page, surface = ink.paper, onSurface = ink.ink, onBackground = ink.ink,
    ) else lightColorScheme(
        primary = ink.rubric, background = ink.page, surface = ink.paper, onSurface = ink.ink, onBackground = ink.ink,
    )
    CompositionLocalProvider(LocalInk provides ink) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
