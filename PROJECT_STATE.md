# Folio — project state

CURRENT OBJECTIVE: Grow the short illustrated folio catalog from 31 to 120–130 diverse books. Every live plate must be original Folio art and relevant to its card. Audit Android UX with Antigravity and fix verified defects.

STATUS: 34 live books in Android 1.6.6 and the online catalog. A 2026-10-05 migration removed references/files for all 629 third-party plates; 25 Folio originals are live in Android 1.6.7 (Prince cover + 7 cards, Gita cover + first 13 cards, Wealth and Gitanjali covers, Walden card 2). The app uses its ornament for missing plates. `tools/publish_library.py` now rejects non-original IDs. Wealth of Nations, Walden, and Gitanjali five-card introductions are in the live catalog and retain staged source files.

COMPLETED THIS ROUND:
- Android 15 emulator `FolioReview` created. Antigravity reviewed the app; report in `reports/ANTIGRAVITY_REVIEW.md` (its claimed screenshots were not retained).
- Fixed half-height book sheet CTA, chapter quiz scrolling, fast flip back from card verso, possessive/initial glossary taps, Journey preview labels and title wrapping, dormant account copy, and Power Journey blurb.
- Built image provenance ledger `reports/image-inventory.tsv` via `tools/image_inventory.py`; staged catalog is 25 original / 0 third-party.
- Generated original Gita card 1 and Walden draft sketches in `research/generated/`; original-art manifest holds 25 works (14 Gita images).

DECISIONS / CONSTRAINTS:
- Keep the current short, illustrated folio format. New titles stay staged until source text, quote accuracy, card content, and original artwork are verified.
- Existing 31 editions need 629 card-specific original replacements to restore full coverage; do not restore Commons images. `research/commons/credits.json` is retained for provenance, not publishing.
- `research/shelf5_content.py` validates the three introductory editions but flags all as too shallow for a deep edition. Expand them later.
- Cloud sync remains dormant (`cloud=null`); prior review found account-switch, deletion, and stale-position issues before enabling.

NEXT ACTION: Publish Android 1.6.8 with the share-card crop/footer fix, then create and review Gita folios 14–59 before returning to catalog expansion.

IMPORTANT FILES: `tools/publish_library.py`, `tools/hide_third_party_plates.py`, `tools/image_inventory.py`, `reports/IMAGE_AUDIT_2026-10-05.md`, `reports/ANTIGRAVITY_REVIEW.md`, `research/generated/manifest.json`, `research/shelf5_content.py`, `research/staging/books/`.

LAST VALIDATION: Android 1.6.7 released with Gita cover + folios 1–13; catalog has 34 books and 25 original / 0 third-party plates. Share Studio Post and Square exports checked on Android emulator. Paparazzi rendered the full 3×3×4 Share Studio matrix for Gita, Arthashastra, and Prince, plus long-quote stress cases from Gita, Prince, Laws, and Meditations; all inspected without crop or border collision. Android 1.6.8 release pending.
