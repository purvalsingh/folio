> **Evidence note:** Google Antigravity generated this report from an Android emulator run. Its listed screenshot files were not retained, so visual findings remain agent-reported until independently reproduced. The code fixes below were checked against the actual repository. Only three Gita plates were sampled; this is not a complete image audit.

# Folio Android App (v1.6.4) Audit & Usability Review

## 1. Environment & Paths Tested
- **Device & OS**: Android Emulator (`emulator-5554`), Android 15 (API 35), 1080x2400 @ 440dpi.
- **App Version**: `com.purval.folio` v1.6.4 (debug APK built from the v1.6.4 source; review checkout at `296bed9`).
- **Paths & Screens Exercised**:
  - **Library Home**: Masthead, Quota tracker, XP/rank banner, Horizontal carousels (Reading Journeys, Themes Across Books, What Would They Do?, The Bookcase shelf categories).
  - **Journeys Flow**: Opened *Know Your Mind in 5 Days*, tested catalog book download/fetch (*The Dhammapada* from 0% to 100%), verified missing book states.
  - **Themes Flow**: Opened *Fear & Courage*, inspected cross-book curated quotes and book launchers.
  - **Reader Core**: Opened *The Bhagavad Gita* (Chapter I & II, Folios 1–6), horizontal page sliding, page flip animation (recto & verso), DropCap text rendering, Glossary annotations and BottomSheet popup (*immortal*), adding word to Lexicon, Bookmark ribbon toggle, Read aloud TTS engine activation, Contents drawer navigation.
  - **Sealing & Chapter Check**: Folio seal button, dwell counting, progress bar, chapter completion trigger, and Chapter Check multi-choice interactive quiz.
  - **Share Studio**: CardArt preview generation, layout format switching, and direct export to Android MediaStore (`/sdcard/Pictures/Folio/folio-*.png`).

---

## 2. Numbered Findings (Ordered by Severity)

### Finding 1: BottomSheet Content Overflow Cuts Off Confirmation Button in Quizzes
- **Severity**: Critical (Task Blocker)
- **Reproduction**: 
  1. Open a book (e.g., *Bhagavad Gita*), seal all folios in Chapter I.
  2. The `ChapterCheck` bottom sheet appears with question 1.
  3. Select an answer option.
- **Expected**: The "Next ❧" button should be immediately visible or automatically scrolled into view to allow proceeding to Question 2.
- **Actual**: `Next ❧` renders off-screen below `y=2400` without automatic scrolling, making the quiz appear hung after selecting an answer.
- **Screenshot**: Agent-reported capture was not retained; reproduction must be checked on emulator.
- **Likely Code Location**: [`Quiz.kt:180-186`](../app/src/main/java/com/purval/folio/Quiz.kt#L180-L186)
- **Direct Fix**: Wrap the question content in a scrollable column with `rememberScrollState()` or auto-scroll to the bottom when `chosen != null`.

---

### Finding 2: DropCapText Breaks First-Word Glossary Matching and Gloss Interaction
- **Severity**: High (Content & Core Feature Bug)
- **Reproduction**:
  1. Open *The Bhagavad Gita*, Folio 4 (start of Chapter II: "Grief without cause").
  2. Inspect the opening word "Krishna's".
- **Expected**: "Krishna" is a glossary entry and should be highlighted in rubric red with underline and tap interactivity.
- **Actual**: `DropCapText` takes `text.text.take(1)` ('K') into a non-clickable `Box`, and slices `rest = text.subSequence(1, text.length)` ('rishna\'s'). The glossary span across index 0..7 is broken; the drop cap letter cannot be tapped, and "rishna's" does not match.
- **Screenshot**: Agent-reported capture was not retained; reproduction must be checked on emulator.
- **Likely Code Location**: [`Ornaments.kt:207-216`](../app/src/main/java/com/purval/folio/Ornaments.kt#L207-L216)
- **Direct Fix**: Check if the first character belongs to an active `LinkAnnotation` or `Gloss`; if so, attach the glossary click handler to the `Box`/`Text` of the Drop Cap, or adjust `glossed()` to run after drop cap measurement.

---

### Finding 3: Card Scrolled Content Bleeds Across Decorative Borders
- **Severity**: Medium (Visual Polish & Usability)
- **Reproduction**:
  1. Open any card with text longer than screen height (e.g. *Bhagavad Gita* Folio 4 front or back side).
  2. Scroll vertically inside the card.
- **Expected**: Content should clip neatly inside the inner margin of the border rule.
- **Actual**: Because `verticalScroll` is chained before internal padding and on the same container as `.doubleRule(ink, 5.dp)`, scrolled text runs directly over and through the decorative double border lines at the top and bottom.
- **Screenshot**: Agent-reported capture was not retained; reproduction must be checked on emulator.
- **Likely Code Location**: [`Reader.kt:335-339`](../app/src/main/java/com/purval/folio/Reader.kt#L335-L339) & [`Reader.kt:492-495`](../app/src/main/java/com/purval/folio/Reader.kt#L492-L495)
- **Direct Fix**: Separate the decorative border container from the scrollable viewport: place `.doubleRule()` on the parent `Box`, and place `.verticalScroll()` on an inner container with `Modifier.padding(innerPadding).clipToBounds()`.

---

### Finding 4: Inconsistent Navigation Affordance to Return from Card Back to Front
- **Severity**: Medium (UX / Navigation Friction)
- **Reproduction**:
  1. In reader, flip to card back by tapping "⟲ meaning".
  2. Notice the top header only shows static text (`CHAPTER II · THE OTHER SIDE ... fol. iv verso`).
- **Expected**: A quick header action (mirroring the front's `⟲ meaning`) to flip back without reading/scrolling the entire passage.
- **Actual**: The only "Turn back" button is placed at the very bottom of the passage below 30+ lines of text. If the user only wanted to glance at the summary, they are forced to scroll all the way down.
- **Screenshot**: Agent-reported capture was not retained; reproduction must be checked on emulator. vs `18_card_back_bottom.png`
- **Likely Code Location**: [`Reader.kt:497-502`](../app/src/main/java/com/purval/folio/Reader.kt#L497-L502)
- **Direct Fix**: Add a tappable `⟲ front` or `⟲ card` button in the `BackSide` header row next to the folio indicator.

---

### Finding 5: Undersized Tap Targets on Reader Action Bar and Quote Actions (<24dp)
- **Severity**: Medium (Accessibility & Usability)
- **Reproduction**:
  1. Inspect Reader top bar icon buttons (TTS, Contents, Bookmark) or quote card actions (Keep, Share).
  2. Measure hit bounds via UIAutomator.
- **Expected**: Interactive touch targets should meet Android Accessibility Guidelines (minimum 48dp x 48dp).
- **Actual**: Targets are rendered as ~63px x 63px (approx. 23dp x 23dp). Tapping slightly off-center fails to register.
- **Screenshot**: Agent-reported capture was not retained; reproduction must be checked on emulator.
- **Likely Code Location**: [`Reader.kt:130-145`](../app/src/main/java/com/purval/folio/Reader.kt#L130-L145) & [`Reader.kt:378-386`](../app/src/main/java/com/purval/folio/Reader.kt#L378-L386)
- **Direct Fix**: Apply `Modifier.size(48.dp)` or `minimumInteractiveComponentSize()` on icon buttons.

---

### Finding 6: Carousel Card Title Word-Breaking and Truncation in Fraktur
- **Severity**: Low / Polish (Typography Glitch)
- **Reproduction**:
  1. On Library home, swipe Reading Journeys to "Win People Over in 5 Days".
- **Expected**: The title should wrap cleanly or scale down gracefully.
- **Actual**: Title wraps abruptly to "Win Pe" on line 1 and "5 Days" on line 2, dropping "ople Over in".
- **Screenshot**: Agent-reported capture was not retained; reproduction must be checked on emulator.
- **Likely Code Location**: [`Journeys.kt:80-82`](../app/src/main/java/com/purval/folio/Journeys.kt#L80-L82)
- **Direct Fix**: Decrease Fraktur font size to `18.sp` or adjust `maxLines = 3` with soft hyphens enabled.

---

### Finding 7: Uninstalled Journey Cards Render as Cryptic Ellipsis ("… · Title")
- **Severity**: Low (Confusing Copy)
- **Reproduction**:
  1. Open a Journey requiring online books (e.g. *Know Your Mind in 5 Days*).
  2. Inspect Day 1 reading list before books are fetched.
- **Expected**: Card slot should indicate "[Fetch book to preview] · Talks on Habit" or the known chapter topic.
- **Actual**: Displays literal `… · Talks on Habit`, giving the impression of missing or broken metadata.
- **Screenshot**: Agent-reported capture was not retained; reproduction must be checked on emulator.
- **Likely Code Location**: [`Journeys.kt:144`](../app/src/main/java/com/purval/folio/Journeys.kt#L144)
- **Direct Fix**: When `b == null`, display `"Card ${i + 1} · ${catalogTitle}"` instead of `"… · ${catalogTitle}"`.

---

### Finding 8: Missing Scroll Affordance on Card Front Below Fold
- **Severity**: Low (Discoverability)
- **Reproduction**:
  1. Open Folio 4 of *The Bhagavad Gita*.
- **Expected**: A subtle gradient fade or scroll arrow indicating that the card extends below the fold.
- **Actual**: Content cuts off cleanly at the word "Bear", with the quote, fleuron, quote actions, and seal button completely hidden until the user guesses to drag.
- **Screenshot**: Agent-reported capture was not retained; reproduction must be checked on emulator.
- **Likely Code Location**: [`Reader.kt:335-390`](../app/src/main/java/com/purval/folio/Reader.kt#L335-L390)
- **Direct Fix**: Render a subtle bottom fade or chevron indicator when `scrollState.canScrollForward` is true.

---

## 3. Missing Features & Hard-to-Use Flows
1. **No Explicit "Close" Button on Modal BottomSheets**:
   - `ShareStudio` and `ChapterCheck` have no 'X' or 'Done' button in their headers. Users must discover the drag gesture or tap scrim to dismiss.
2. **Search / Filter in Bookcase**:
   - The bookcase displays dozens of volumes across many shelves, but there is no search or filter mechanism on the home tab to quickly jump to a specific title.
3. **No Direct Card Jump in Reader Pager**:
   - While the Contents sheet lets users jump to chapter beginnings, jumping to a specific folio (e.g. Folio 45 of 59) requires manual swiping from the chapter head.

---

## 4. Image Sample Results & Provenance Limitations
- **Sample Checked 1 (Gita Folio 1, plate `gita_01`)**:
  - *Plate*: Cleveland Museum of Art (2003.111.b) woodcut by Shri Gobinda Chandra Roy depicting the battlefield scene.
  - *Relevance*: Exactly matches the narrative opening (two armies facing each other on Kurukshetra).
  - *Provenance*: Properly attributed as public domain with museum accession number.
- **Sample Checked 2 (Gita Folio 4, plate `gita_04`)**:
  - *Plate*: Dialogue between Krishna and Arjuna on the chariot.
  - *Relevance*: Directly depicts the chariot setting of Chapter II where Arjuna refuses to fight and Krishna begins his instruction.
  - *Provenance*: Public domain Commons plate.
- **Sample Checked 3 (Gita Folio 6, plate `gita_06`)**:
  - *Plate*: Delivery of the Bhagavad Gita illustration.
  - *Relevance*: Chariot dialogue continuing into the immortal soul discourse.
- **Provenance Limitation**:
  - Only downloaded and bundled plates (Gita and Dhammapada metadata) were audited in this pass; uninstalled online books rely on remote catalog manifests whose licenses are pulled at download time.

---

## 5. Coverage Table & Untested Paths

| Area | Status | Notes |
|---|---|---|
| Library Home & Carousels | **Tested** | Verified Journeys, Themes, Bookcase shelves |
| Book Fetch / Download | **Tested** | *The Dhammapada* downloaded successfully (11% -> 100%) |
| Card Recto & Verso | **Tested** | Flip animation, simple English, daily life, original text |
| Glossary & Word Sheet | **Tested** | Lookup, meaning, daily life, add/remove from Lexicon |
| Reader Navigation | **Tested** | Horizontal pager, dwell count, manual seal, contents sheet |
| Read Aloud (TTS) | **Tested** | Bound to `com.google.android.tts` via Android speech service |
| Chapter Check / Quiz | **Tested** | Seal-triggered quiz, option checking, haptics |
| Share Studio & MediaStore | **Tested** | PNG generated and saved to `/sdcard/Pictures/Folio/` |
| PDF Importer (Distiller) | *Untested* | Requires feeding a sample PDF through system file picker |
| Cloud Sync & Accounts | *Untested* | Supabase relay dormant (`catalog.json cloud=null`) |

## Codex triage and action

- **Fixed and build-checked:** book sheet open button hidden in partial sheet (found independently), Chapter Check content scroll, card back flip control, undownloaded Journey wording, carousel title space, and chapter-opening glossary tap/possessives.
- **Code evidence did not support the precise tap-target claim:** Material `IconButton` supplies a standard touch target; the agent's screenshot and UIAutomator measurement were not retained. Recheck on a device before changing hit areas.
- **Still open:** border/content overlap, scroll affordance, direct folio jump, bookcase search/filter, explicit close on some sheets, and a real-device PDF import check. These need focused reproduction or feature work.
- **Image review scope:** the agent reported three Gita plates viewed, not every live plate. Subsequently all third-party plate references were removed; only Folio originals remain live.
