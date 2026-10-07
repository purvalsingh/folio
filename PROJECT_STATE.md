# Folio — project state

CURRENT OBJECTIVE: Reach 125 diverse, source-grounded illustrated folio books without changing app code. Preserve original quotation wording, explain it in clear modern English, define hard words, and give faithful everyday examples. Every page image must be original and relevant.

STATUS: 35 live books, 634 cards in the Android-downloadable catalog; Android APK remains 1.6.8. The 91-title path from the previous 34 books is in `research/CATALOG_125_ROADMAP.md`: Civil Disobedience is published; 90 titles remain. All 41 published image placements are original, 0 third-party. Gita has original cover and page images 1–16; pages 17–59 still show the ornament. Other older books still have many ornament pages awaiting original art.

COMPLETED THIS ROUND:
- Built `site/` as a responsive Folio marketing site and deployed it to Vercel at https://folio-site-liart.vercel.app/. The site reads the latest GitHub release and live book count.
- Curated 13 shelves × 7 titles as a candidate path to 125, with an edition gate that requires exact-source quotes, clear explanations, glossary, everyday uses, and reviewed page-specific art.
- Published a 12-page Civil Disobedience edition from Project Gutenberg #71, with full source paragraphs, 9 glossary entries, 12 distinct original wood-engraving-style page images, and original cover. Source text is in `research/civil_disobedience.txt`; staged and live JSON in `research/staging/books/` and `library-only/books/`.
- Added visually reviewed original Gita plates 14–16 in Indian miniature style, matched to each page. Updated original-art manifest and online catalog.
- Updated README image-provenance statement to match the original-only catalog.

DECISIONS / CONSTRAINTS:
- The user chose the current short illustrated folio format; make editions substantial enough to retain their argument or plot. No app code changes in this objective.
- New titles remain candidates until source edition, rights, quotes, explanation, glossary, and every original image are checked. Do not publish placeholders or third-party images.
- For difficult old English, preserve source words in the quote and original passage; write modern English in explanation fields.

NEXT ACTION: Complete Gita pages 17–59 with individually reviewed original Indian images, then build the next source-grounded candidate from the 90-title roadmap. The six queued Project Gutenberg texts for Alice, Douglass, Frankenstein, Origin, Pride, and Time Machine are in `/home/purvals/folio-research-next/`.

IMPORTANT FILES: `research/CATALOG_125_ROADMAP.md`, `research/generated/manifest.json`, `research/staging/books/civil_disobedience.json`, `library-only/books/civil_disobedience.json`, `research/civil_disobedience.txt`, `tools/publish_library.py`, `tools/image_inventory.py`, `TODO.md`.

WEBSITE: `site/index.html`, `site/style.css`, `site/app.js`, `site/vercel.json`; Vercel production URL: https://folio-site-liart.vercel.app/. Film section (#film) plays `site/assets/folio-film.mp4` (25s promo; source in ~/folio-promo, Remotion). Deploy: project root dir is `site`, so deploy from a folder that contains only `site/` (`vercel link --project folio-site && vercel deploy --prod`), not the whole repo.

LAST VALIDATION: `python3 tools/publish_library.py` passed at 35 books. `python3 tools/image_inventory.py` reported 41 original / 0 third-party placements. All 12 Civil Disobedience quotes were matched to Project Gutenberg #71 and all 13 illustrations were inspected together. Android app code was unchanged.
