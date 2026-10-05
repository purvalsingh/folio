# Folio image inventory — 2026-10-05

Scope: the 34 books in the live `library/catalog.json`. The page-level ledger is
[`image-inventory.tsv`](image-inventory.tsv); rerun `python3 tools/image_inventory.py`
after any catalog change.

| Measure | Count |
| --- | ---: |
| Live books | 34 |
| Image placements and unique image IDs | 12 |
| Credited third-party images in published books | 0 |
| Recorded original Folio images | 12 |
| Missing local WEBP files | 0 |
| Unknown provenance | 0 |

The originals are the Prince cover and seven plates, the opening Gita plate,
the *Walden* cabin plate, and covers for *The Wealth of Nations* and
*Gitanjali*. The catalog previously referenced
638 plates: 629 third-party and 9 original. The 629 third-party references and
files have been removed from published books and assets. Their sources remain
documented in `research/commons/credits.json`, but no published book refers
to them. Cards without a new drawing show Folio's built-in ornament. All 31
affected editions have new versions so phones can fetch the change.

## Review state

The prior release audited visible subject mismatches and replaced a number of
plates, particularly in the Gita and Dhammapada. The 12 remaining live images
are Folio-made originals. All 12 were viewed together against their mapped
book/page titles. The Gita chariot scene, Prince cover and seven political
scenes, Walden cabin, Wealth pin workshop, and Gitanjali river/flute/vessel
cover fit their respective pages or books. None shows unrelated language or a
third-party watermark. The old third-party set was inventoried but not
visually rechecked page by page before removal, so this report does not claim
a complete visual review of that removed set.

## Release rule for new books

Keep new book JSON and illustrations in `research/staging/` until the card
text, quote/source, caption, page relevance, and image originality have been
checked. No third-party plate should enter the live library for these books.
The 629 hidden placements still need card-specific original drawings if the
library is to regain full illustration coverage.
