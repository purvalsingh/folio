"""One-time migration: leave only Folio-made illustrations in published books."""

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
third_party = set(json.loads((ROOT / "research/commons/credits.json").read_text()))
original = set(json.loads((ROOT / "research/generated/manifest.json").read_text()))
book_dirs = [ROOT / "app/src/main/assets/books", ROOT / "library-only/books"]
changed = 0
for directory in book_dirs:
    for path in directory.glob("*.json"):
        book = json.loads(path.read_text())
        book_changed = False
        if book.get("cover") in third_party:
            book["cover"] = ""
            changed += 1
            book_changed = True
        for card in book["cards"]:
            if card.get("img") in third_party:
                card["img"] = ""
                card["cap"] = ""
                changed += 1
                book_changed = True
        remaining = {card["img"] for card in book["cards"] if card.get("img")}
        if book.get("cover"):
            remaining.add(book["cover"])
        assert remaining <= original, (path, remaining - original)
        if book_changed:
            book["version"] = book.get("version", 1) + 1
            path.write_text(json.dumps(book, ensure_ascii=False, indent=1) + "\n")

for directory in [ROOT / "app/src/main/assets/img", ROOT / "library-only/img", ROOT / "library/img"]:
    for image_id in third_party:
        (directory / f"{image_id}.webp").unlink(missing_ok=True)

print(f"Hidden {changed} third-party image placements in source books")
