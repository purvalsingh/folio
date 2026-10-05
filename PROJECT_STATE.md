# Folio — project state

CURRENT OBJECTIVE: Grow the short illustrated folio catalog from 31 to 120–130 diverse books. Every live plate must be original Folio art and relevant to its card. Audit Android UX with Antigravity and fix verified defects.

STATUS: 31 live books. A 2026-10-05 migration removed references/files for all 629 third-party plates; 9 Folio originals remain (Prince cover + 7 cards, Gita opening card). The app uses its ornament for missing plates. `tools/publish_library.py` now rejects non-original IDs. New Wealth of Nations, Walden, and Gitanjali five-card drafts are in `research/staging/books`; they are not yet adequate editions or live. Original Walden sketch is staged.

COMPLETED THIS ROUND:
- Android 15 emulator `FolioReview` created. Antigravity reviewed the app; report in `reports/ANTIGRAVITY_REVIEW.md` (its claimed screenshots were not retained).
- Fixed half-height book sheet CTA, chapter quiz scrolling, fast flip back from card verso, possessive/initial glossary taps, Journey preview labels and title wrapping, dormant account copy, and Power Journey blurb.
- Built image provenance ledger `reports/image-inventory.tsv` via `tools/image_inventory.py`; current live set is 9 original / 0 third-party.
- Generated original Gita card 1 and Walden draft sketches in `research/generated/`; original-art manifest holds 10 works (9 live, 1 staged).

DECISIONS / CONSTRAINTS:
- Keep the current short, illustrated folio format. New titles stay staged until source text, quote accuracy, card content, and original artwork are verified.
- Existing 31 editions need 629 card-specific original replacements to restore full coverage; do not restore Commons images. `research/commons/credits.json` is retained for provenance, not publishing.
- `research/shelf5_content.py` validates the three drafts but flags all as too shallow for a deep edition. Continue source-grounded content before release.
- Cloud sync remains dormant (`cloud=null`); prior review found account-switch, deletion, and stale-position issues before enabling.

NEXT ACTION: Finish release validation, publish current original-only/UX changes, then develop original art and source-grounded editions in batches toward 125 books. Prioritize bookcase search/filter before a large catalog; track each batch in TODO.md.

IMPORTANT FILES: `tools/publish_library.py`, `tools/hide_third_party_plates.py`, `tools/image_inventory.py`, `reports/IMAGE_AUDIT_2026-10-05.md`, `reports/ANTIGRAVITY_REVIEW.md`, `research/generated/manifest.json`, `research/shelf5_content.py`, `research/staging/books/`.

LAST VALIDATION: `python3 tools/publish_library.py` passed at 31 books; `python3 tools/image_inventory.py` passed at 9 original / 0 third-party; Android debug build passed after UX fixes. Release build in progress.
