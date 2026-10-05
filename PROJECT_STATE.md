# Folio — PROJECT_STATE

CURRENT OBJECTIVE: native Android (Kotlin + Compose, one Java class) flashcard reader for old books. Shelf: The Prince (52), 48 Laws of Power digest (48), The Art of War (26), Bhagavad Gita (25).

STATUS: v1.2 released (quote meanings, per-era typography, account code dormant). BLOCKER: accounts need user's new Supabase 'folio' project ref + publishable key -> run cloud/setup.sh REF KEY, run cloud/schema.sql in SQL editor, Auth: confirm email OFF, recovery email template must contain {{ .Token }}. v1.1 released at github.com/purvalsingh/folio (public). In-app updater + online library live. v1.0 built, signed release APK at ~/Desktop/Folio.apk (also app/build/outputs/apk/release/).

COMPLETED
- 52 cards of The Prince (Marriott 1908, Gutenberg #1232): plain summaries + verbatim quotes (auto-verified) + 55 glossary forms.
- Plates: cover + 7 AI engravings (HF Z-Image, quota ran out), 44 public-domain prints/paintings from Wikimedia Commons (research/commons/credits.json), caption under each card.
- Reader: page-turn pager, illuminated drop cap (book) / red initials (chapters), tappable red words -> meaning + daily-life example + add to Lexicon, quote keep/share, bookmark ribbon, contents sheet, seal button (+ 6 s dwell auto-count).
- Lexicon (search + "Test me" mode), Commonplace (quotes), Marks, Honours (levels/ranks, xp, 19 milestone seals, daily quota 5–30, night mode, Gemini key).
- Celebrations: wax-seal stamp overlay for milestones (10/100 pages, book done, quota, streaks, level up).
- Bind (import) a PDF: PdfBox text -> Distiller.java (offline extractive) or Gemini (plain-English, quote verified against text); plates = real page facsimiles; per-era typefaces.

DECISIONS
- Accounts: Supabase Auth via Vercel relay folio-reader-sync.vercel.app/sb (ISP block). Cloud config (base+anon key) is delivered in catalog.json 'cloud' -> no APK needed to switch on. Library blob is E2E encrypted (PBKDF2 210k -> AES-256-GCM); session + key sealed by Android Keystore. Password reset = 6-digit email code; old cloud copy unreadable after reset, phone's local copy re-uploaded.
- Quote meanings: research/meanings.py (qMean/qLife per card, order-checked).
- Updates: app fetches raw.githubusercontent.com/purvalsingh/folio/main/library/catalog.json on launch. App releases = GitHub Releases APK (sha256-checked, system installer). Books = library/books + library/img, auto-updated when catalog version > local. Release with ./release.sh <ver> "notes" or ./release.sh books. Bump a book's "version" in its *_content.py to push a corrected edition.
- Signing key ~/.folio-signing is the ONLY key that can update installed copies — back it up.
- 48 Laws is copyrighted (Greene 1998): original summaries + PD quotes (Machiavelli/Sun Tzu/Gracián/La Rochefoucauld), each card carries qBy attribution. Never ship Greene's text.
- Content scripts: research/{prince,laws,artofwar,gita}_content.py via folio_build.py (quote verifier; fuzzy only for OCR'd Gracián). Plates: research/commons.py <book> with queries.json.
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

LAST VALIDATION: 2026-10-05 4 books, all quotes verified; assembleRelease OK (26 MB); Paparazzi 15/15; DistillerTest pass.
