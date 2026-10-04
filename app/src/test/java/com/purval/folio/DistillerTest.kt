package com.purval.folio

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Runs the offline importer core on the real Gutenberg text of The Prince. */
class DistillerTest {
    @Test fun distilsThePrince() {
        val lines = File("../research/prince.txt").readLines().drop(540).take(3700)
        val pages = lines.chunked(45).map { it.joinToString("\n") }
        val common = File("src/main/assets/common_words.txt").readLines().toHashSet()
        val ps = Distiller.passages(pages, 320)
        assertTrue("too few passages: ${ps.size}", ps.size > 20)
        assertTrue("chapters not detected", ps.any { it.chapter.startsWith("CHAPTER") })
        ps.take(40).forEach { p ->
            val l = Distiller.distil(p.text, common)
            assertTrue(l.summary.isNotBlank())
            assertTrue("quote must be the author's words", l.quote.isEmpty() || Distiller.clean(p.text).contains(l.quote))
            l.words.forEach { w -> assertTrue("$w is common", w !in common) }
        }
        val s = Distiller.distil(ps[25].text, common)
        println("${ps[25].chapter} | ${s.title}\nSUMMARY: ${s.summary}\nQUOTE: ${s.quote}\nWORDS: ${s.words}")
    }
}
