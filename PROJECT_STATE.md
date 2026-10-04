# Folio — PROJECT_STATE

CURRENT OBJECTIVE: native Android (Kotlin + Compose, one Java class) flashcard reader for old books; first book = The Prince.

STATUS: v1.0 built, signed release APK at ~/Desktop/Folio.apk (also app/build/outputs/apk/release/).

COMPLETED
- 52 cards of The Prince (Marriott 1908, Gutenberg #1232): plain summaries + verbatim quotes (auto-verified) + 55 glossary forms.
- Plates: cover + 7 AI engravings (HF Z-Image, quota ran out), 44 public-domain prints/paintings from Wikimedia Commons (research/commons/credits.json), caption under each card.
- Reader: page-turn pager, illuminated drop cap (book) / red initials (chapters), tappable red words -> meaning + daily-life example + add to Lexicon, quote keep/share, bookmark ribbon, contents sheet, seal button (+ 6 s dwell auto-count).
- Lexicon (search + "Test me" mode), Commonplace (quotes), Marks, Honours (levels/ranks, xp, 19 milestone seals, daily quota 5–30, night mode, Gemini key).
- Celebrations: wax-seal stamp overlay for milestones (10/100 pages, book done, quota, streaks, level up).
- Bind (import) a PDF: PdfBox text -> Distiller.java (offline extractive) or Gemini (plain-English, quote verified against text); plates = real page facsimiles; per-era typefaces.

DECISIONS
- No Room/Navigation libs: state in one JSON in SharedPreferences, routes as a sealed interface.
- Era -> typeface map in Theme.kt (Renaissance = UnifrakturMaguntia + IM Fell).

NEXT ACTION
- Replace Commons plates with AI engravings when HF ZeroGPU quota resets (research/prince_content.py has the prompts; raw/ holds generated ones).
- Real-device pass of PDF import with a Gutenberg PDF.

IMPORTANT FILES
- research/prince_content.py — content source of truth; run it to regenerate assets/books/prince.json and verify quotes.
- research/commons.py — Commons plate fetcher.
- app/src/test/.../Shots.kt — Paparazzi design snapshots: ./gradlew :app:recordPaparazziDebug
- app/src/test/.../DistillerTest.kt — offline importer self-check on the real text.
- Signing: ~/.folio-signing/ (keystore + passwords). Build: JAVA_HOME=~/.local/jdk-21 ./gradlew assembleRelease

LAST VALIDATION: 2026-10-05 assembleRelease OK; Paparazzi 6/6; DistillerTest pass.
