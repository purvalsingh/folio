"""Write a card-level inventory of live catalog illustrations."""

import csv
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
catalog = json.loads((ROOT / "library/catalog.json").read_text())
commons = json.loads((ROOT / "research/commons/credits.json").read_text())
original = json.loads((ROOT / "research/generated/manifest.json").read_text())
output = ROOT / "reports/image-inventory.tsv"
output.parent.mkdir(exist_ok=True)

rows = []
for item in catalog["books"]:
    book = json.loads((ROOT / "library/books" / f"{item['id']}.json").read_text())
    placements = [("cover", book["title"], book.get("cover"))]
    placements += [
        (str(index), card.get("title", ""), card["img"])
        for index, card in enumerate(book["cards"], 1)
        if card.get("img")
    ]
    for page, title, image_id in placements:
        if not image_id:
            continue
        credit = original.get(image_id) or commons.get(image_id) or {}
        provenance = (
            "original" if image_id in original else
            "third-party" if image_id in commons else "unknown"
        )
        rows.append({
            "book": item["id"],
            "page": page,
            "page_title": title,
            "image_id": image_id,
            "provenance": provenance,
            "source_file": credit.get("file", ""),
            "license": credit.get("license", ""),
            "local_file_exists": (ROOT / "library/img" / f"{image_id}.webp").is_file(),
            "page_relevance_review": "pending" if provenance == "third-party" else "original",
        })

with output.open("w", newline="") as handle:
    writer = csv.DictWriter(handle, rows[0], delimiter="\t")
    writer.writeheader()
    writer.writerows(rows)

assert len(rows) == len({row["image_id"] for row in rows}), "Image ID reused across pages"
assert all(row["local_file_exists"] for row in rows), "Missing live image"
print(f"{len(catalog['books'])} books, {len(rows)} placements, "
      f"{sum(row['provenance'] == 'third-party' for row in rows)} third-party, "
      f"{sum(row['provenance'] == 'original' for row in rows)} original -> {output}")
