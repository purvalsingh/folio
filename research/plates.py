"""Pick plates by eye instead of trusting the first search hit.

  python3 plates.py search "<query>" [n]        # candidate sheet -> scratchpad cand.jpg, numbered
  python3 plates.py set <book> <key> <n|File:...> [y]  # y = crop height 0 top..1 bottom; use candidate n (from the last search) or an exact Commons file
  python3 plates.py bump <book>                  # new edition so phones fetch the corrected plates
"""
import io, json, pathlib, sys, urllib.parse
from PIL import Image, ImageOps, ImageDraw

UA = {"User-Agent": "FolioApp/1.0 (purvalsingh841@gmail.com) plate-fetcher"}


def get(url):
    import time, urllib.request
    for k in range(5):
        try:
            return urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=40).read()
        except Exception:
            if k == 4: raise
            time.sleep(4 * (k + 1))
from folio_build import caption

HERE = pathlib.Path(__file__).parent
ROOT = HERE.parent
SCRATCH = pathlib.Path("/tmp/claude-1000/-home-purvals/185ad76f-cc04-48ec-acc8-f2a1991b5793/scratchpad")
CANDS = SCRATCH / "cands.json"
CREDITS = HERE / "commons/credits.json"


def api(**kw):
    return json.loads(get("https://commons.wikimedia.org/w/api.php?" + urllib.parse.urlencode(dict(format="json", **kw))))


def info(pages):
    out = []
    for p in sorted(pages.values(), key=lambda p: p.get("index", 99)):
        ii = (p.get("imageinfo") or [{}])[0]
        lic = ii.get("extmetadata", {}).get("LicenseShortName", {}).get("value", "")
        if ii and ("public domain" in lic.lower() or lic.upper().startswith("PD") or "CC0" in lic):
            out.append(dict(file=p["title"], thumb=ii["thumburl"], lic=lic))
    return out


def search(q, n=12):
    r = api(action="query", generator="search", gsrsearch=q + " filetype:bitmap", gsrnamespace=6, gsrlimit=30,
            prop="imageinfo", iiprop="url|extmetadata|size", iiurlwidth=1000)
    c = info(r.get("query", {}).get("pages", {}))[:n]
    CANDS.write_text(json.dumps(c, ensure_ascii=False))
    sheet = Image.new("RGB", (4 * 300, ((len(c) + 3) // 4) * 240 or 240), "white")
    for k, x in enumerate(c):
        try:
            t = ImageOps.contain(Image.open(io.BytesIO(get(x["thumb"]))).convert("RGB"), (296, 220))
        except Exception:
            continue
        sheet.paste(t, ((k % 4) * 300, (k // 4) * 240))
        d = ImageDraw.Draw(sheet); d.rectangle(((k % 4) * 300, (k // 4) * 240, (k % 4) * 300 + 26, (k // 4) * 240 + 14), fill="black")
        d.text(((k % 4) * 300 + 3, (k // 4) * 240), str(k), fill="white")
        print(k, x["file"][5:110])
    sheet.save(SCRATCH / "cand.jpg", quality=82)


def trim(im):
    """Cut the blank paper or wall around a print: keep rows and columns that actually vary."""
    import numpy as np
    a = np.asarray(im, dtype=float)
    rows, cols = np.where(a.std(1) > 22)[0], np.where(a.std(0) > 22)[0]
    if len(rows) < 50 or len(cols) < 50:
        return im
    return im.crop((cols[0], rows[0], cols[-1] + 1, rows[-1] + 1))


def book_files(book):
    return [p for p in (ROOT / "app/src/main/assets/books" / f"{book}.json", ROOT / "library-only/books" / f"{book}.json") if p.exists()]


def set_plate(book, key, choice, y="0"):
    if choice.startswith("File:"):
        r = api(action="query", titles=choice, prop="imageinfo", iiprop="url|extmetadata", iiurlwidth=1000)
        c = info(r["query"]["pages"])
        if not c:
            sys.exit(f"{choice}: not found or not public domain")
        c = c[0]
    else:
        c = json.loads(CANDS.read_text())[int(choice)]
    name = f"{book}_{key}"
    src = book_files(book)[0]
    out = src.parent.parent / "img" / f"{name}.webp"
    im = ImageOps.exif_transpose(Image.open(io.BytesIO(get(c["thumb"])))).convert("L")
    im = trim(im)
    w, h = im.size
    if h > w * .75: top = int((h - w * .75) * float(y)); im = im.crop((0, top, w, top + int(w * .75)))  # y: 0 top .. 1 bottom
    elif w > h / .75: nw = int(h / .75); im = im.crop(((w - nw) // 2, 0, (w - nw) // 2 + nw, h))
    ImageOps.autocontrast(im.resize((900, 675), Image.LANCZOS), cutoff=1).save(out, "WEBP", quality=72)
    cr = json.loads(CREDITS.read_text()); cr[name] = {"file": c["file"], "license": c["lic"]}
    CREDITS.write_text(json.dumps(cr, indent=1, ensure_ascii=False))
    for f in book_files(book) + [HERE / "base" / f"{book}.json"]:
        if not f.exists():
            continue
        b = json.loads(f.read_text())
        for card in b["cards"]:
            if card.get("img") == name:
                card["cap"] = caption(c["file"])
        f.write_text(json.dumps(b, ensure_ascii=False, indent=1))
    print(name, "<-", c["file"][5:100])


def bump(book):
    for f in book_files(book):
        b = json.loads(f.read_text()); b["version"] = b.get("version", 1) + 1
        f.write_text(json.dumps(b, ensure_ascii=False, indent=1)); print(book, "version", b["version"])


if __name__ == "__main__":
    a = sys.argv[1:]
    {"search": lambda: search(a[1], int(a[2]) if len(a) > 2 else 12), "set": lambda: set_plate(*a[1:5]), "bump": lambda: bump(a[1])}[a[0]]()
