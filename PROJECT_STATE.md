# Folio — PROJECT_STATE

CURRENT OBJECTIVE: native Android (Kotlin + Compose, one Java class) flashcard reader for old books. Shelf: The Prince (52), 48 Laws of Power digest (48), The Art of War (26), Bhagavad Gita (25).

STATUS: v1.6.2 released (versionCode 11). 1.6: page flip (back = simple English + daily life + original passage `orig`/`origFrom` on all cards via folio_build.original()), Themes (library-only/themes.json, 14), What would they do? (scenarios.json, 14), Share studio (CardArt.render: Story/Post/Square x Plate/Quote/Page x Book/Chapter/Paper/Night; Studio.kt), Export (Export.kt: PDF/Markdown/Anki TSV). 1.6.1: The Prince deep edition (research/prince_deep.py, 77 cards, `link` chapter bridges). 1.6.2: iPhone/web app LIVE at https://purvalsingh.github.io/folio/ (docs/: app.js, share.js, app.css, sw.js; GitHub Pages from main /docs; reads library/ via raw.githubusercontent). Popularity: tools/stats.py cron 6h; LinkedIn post live.

COMPLETED
- 52 cards of The Prince (Marriott 1908, Gutenberg #1232): plain summaries + verbatim quotes (auto-verified) + 55 glossary forms.
- Plates: cover + 7 AI engravings (HF Z-Image, quota ran out), 44 public-domain prints/paintings from Wikimedia Commons (research/commons/credits.json), caption under each card.
- Reader: page-turn pager, illuminated drop cap (book) / red initials (chapters), tappable red words -> meaning + daily-life example + add to Lexicon, quote keep/share, bookmark ribbon, contents sheet, seal button (+ 6 s dwell auto-count).
- Lexicon (search + "Test me" mode), Commonplace (quotes), Marks, Honours (levels/ranks, xp, 19 milestone seals, daily quota 5–30, night mode, Gemini key).
- Celebrations: wax-seal stamp overlay for milestones (10/100 pages, book done, quota, streaks, level up).
- Bind (import) a PDF: PdfBox text -> Distiller.java (offline extractive) or Gemini (plain-English, quote verified against text); plates = real page facsimiles; per-era typefaces.

DECISIONS
- Online-only books: research/shelf2_content.py (10) + shelf3_content.py (Human Mind 5, incl. Red Book digest: copyrighted -> original summaries + Jung 1916 PD quotes). Output library-only/books + library-only/img; shelves + 'coming' shelves in library-only/shelves.json.
- Accounts: Supabase Auth via Vercel relay folio-reader-sync.vercel.app/sb (ISP block). Cloud config (base+anon key) is delivered in catalog.json 'cloud' -> no APK needed to switch on. Library blob is E2E encrypted (PBKDF2 210k -> AES-256-GCM); session + key sealed by Android Keystore. Password reset = 6-digit email code; old cloud copy unreadable after reset, phone's local copy re-uploaded.
- Quote meanings: research/meanings.py (qMean/qLife per card, order-checked).
- Updates: app fetches raw.githubusercontent.com/purvalsingh/folio/main/library/catalog.json on launch. App releases = GitHub Releases APK (sha256-checked, system installer). Books = library/books + library/img, auto-updated when catalog version > local. Release with ./release.sh <ver> "notes" or ./release.sh books. Bump a book's "version" in its *_content.py to push a corrected edition.
- Signing key ~/.folio-signing is the ONLY key that can update installed copies — back it up.
- 48 Laws is copyrighted (Greene 1998): original summaries + PD quotes (Machiavelli/Sun Tzu/Gracián/La Rochefoucauld), each card carries qBy attribution. Never ship Greene's text.
- Content scripts: research/{prince,laws,artofwar,gita}_content.py via folio_build.py (quote verifier; fuzzy only for OCR'd Gracián). Plates: research/commons.py <book> with queries.json.
- No Room/Navigation libs: state in one JSON in SharedPreferences, routes as a sealed interface.
- research/shelf4_content.py: 12 books (bindery + Indian Wisdom), CAPS4 caption overrides; Kural (Aiyar 1916) + Panchatantra (Ryder 1925) are OCR -> fuzzy match. Aristotle's Rhetoric dropped (OCR too garbled), replaced by Franklin + Chesterfield. Chanakya Niti skipped (no clean PD English source found).
- APK size: ML Kit translate ships ~17 MB native lib per ABI -> abiFilters arm64-v8a + armeabi-v7a, compressed jniLibs, bouncycastle pqc tables excluded (90 MB -> 36 MB).
- Era -> typeface map in Theme.kt (Renaissance = UnifrakturMaguntia + IM Fell).

NEXT ACTION
- DEEP EDITIONS for every book (user demand 2026-10-05): folio_build.depth() gate = max(25,min(120,words/400)) pages, >=2 pages/chapter, `link` bridge per chapter; set about['deep']=True once a book passes. Words counts include Gutenberg intros/whole collections, so give each book a scoped word count when deepening. Order: gita, artofwar, laws(digest), meditations, enchiridion, taoteching, dhammapada, thinketh, then the rest. Pattern = prince_deep.py (insert-after list, explicit img names, CAPS, new Commons plates).
- Replace Commons plates with AI engravings when HF ZeroGPU quota resets (research/prince_content.py has the prompts; raw/ holds generated ones).
- Real-device pass of PDF import with a Gutenberg PDF.

IMPORTANT FILES
- research/prince_content.py — content source of truth; run it to regenerate assets/books/prince.json and verify quotes.
- research/commons.py — Commons plate fetcher.
- app/src/test/.../Shots.kt — Paparazzi design snapshots: ./gradlew :app:recordPaparazziDebug
- app/src/test/.../DistillerTest.kt — offline importer self-check on the real text.
- Signing: ~/.folio-signing/ (keystore + passwords). Build: JAVA_HOME=~/.local/jdk-21 ./gradlew assembleRelease

LAST VALIDATION: 2026-10-05 v1.4.1: all 12 new books' quotes verified; Paparazzi 24/24 (recall, chapterCheck, journey, quoteCard added); CryptTest + DistillerTest pass; catalog sha256 == local APK; README images 12/12 load on GitHub.
