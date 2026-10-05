"""Install visually reviewed original Gita plates through a selected folio."""

import argparse
import json
import shutil
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument("through", type=int, help="last visually reviewed folio, 1–59")
args = parser.parse_args()

book_path = ROOT / "app/src/main/assets/books/gita.json"
book = json.loads(book_path.read_text())
assert 1 <= args.through <= len(book["cards"]) == 59
generated_dir = ROOT / "research/generated"
manifest_path = generated_dir / "manifest.json"
manifest = json.loads(manifest_path.read_text())
credits_path = ROOT / "research/commons/credits.json"
credits = json.loads(credits_path.read_text())
asset_dir = ROOT / "app/src/main/assets/img"
changed = False

plates = [("gita_cover", None)] if (generated_dir / "gita_cover.webp").is_file() else []
plates += [(f"gita_{i:02d}", i - 1) for i in range(1, args.through + 1)]
for image_id, index in plates:
    source = generated_dir / f"{image_id}.webp"
    assert source.is_file(), f"Missing original drawing: {source}"
    destination = asset_dir / source.name
    if not destination.is_file() or destination.read_bytes() != source.read_bytes():
        shutil.copy2(source, destination)
        changed = True
    title = "Bhagavad Gita cover" if index is None else book["cards"][index]["title"]
    if index is None:
        if book.get("cover") != image_id:
            book["cover"] = image_id
            changed = True
    else:
        card = book["cards"][index]
        caption = f"Original Folio illustration: {title}."
        if card.get("img") != image_id or card.get("cap") != caption:
            card["img"] = image_id
            card["cap"] = caption
            changed = True
    manifest[image_id] = {
        "book": "gita",
        "card": title,
        "file": f"Original Folio illustration: {title}",
        "source": "ImageGen original",
        "license": "Folio original",
        "files": [f"{image_id}.png", f"{image_id}.webp"],
    }
    credits.pop(image_id, None)

if changed:
    book["version"] += 1
    book_path.write_text(json.dumps(book, ensure_ascii=False, indent=1) + "\n")
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n")
    credits_path.write_text(json.dumps(credits, ensure_ascii=False, indent=1) + "\n")
print(f"Gita: {args.through}/59 folios illustrated, version {book['version']}")
