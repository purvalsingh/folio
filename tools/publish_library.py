"""Builds library/ (what the app downloads) from the content in app/src/main/assets.

  python3 tools/publish_library.py                         # refresh books only
  python3 tools/publish_library.py --app CODE NAME APK "notes"   # also announce an app release

A book's "version" (in its *_content.py ABOUT) must be bumped for phones to fetch a corrected edition.
Books that should NOT ship inside the APK go in library-only/books/<id>.json with plates in library-only/img/.
"""
import hashlib, json, pathlib, re, shutil, sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ASSETS = ROOT / "app/src/main/assets"
EXTRA = ROOT / "library-only"
LIB = ROOT / "library"
REPO = "purvalsingh/folio"


def misquotes(b):
    """Release gate: every quote must sit word for word inside the original passage it was cut from."""
    norm = lambda s: re.sub(r"[^a-z0-9]+", " ", s.lower()).strip()
    return [f"{b['id']}#{i} {c['title']}" for i, c in enumerate(b["cards"])
            if c.get("quote") and norm(c["quote"].replace("…", "")) not in norm(c.get("orig", ""))]


# Indian scriptures and fables get Indian (Hindu, Buddhist, Jain, Tamil) art only, never Persian or Mughal folios.
INDIC = {"gita", "arthashastra", "panchatantra", "hitopadesa", "kural", "dhammapada"}
FOREIGN = re.compile(r"persian|razm|arab|islam|qur|koran|mughal|akbar|babur|jahangir|shah|safavid|ottoman|turk|urdu|"
                     r"nastaliq|kalila|dimna|anvar|suhayli|tuti|hamza|khamsa|shahnama|sultan|nawab|emperor|abbasi|mir sayyid", re.I)


def foreign_plates(b, credits):
    if b["id"] not in INDIC:
        return []
    imgs = {c.get("img") for c in b["cards"]} | {b.get("cover")}
    return sorted(f"{i}: {credits[i]['file']}" for i in imgs if i in credits and FOREIGN.search(credits[i]["file"]))


def remap(ref, books):
    """Curated refs ("prince#49") point at a card of the book's first, short edition (research/base/<id>.json).
    Deep editions insert pages, so the ref is resolved to the same card's position in the current edition."""
    bid, n = ref.split("#")
    base = ROOT / "research/base" / f"{bid}.json"
    if not base.exists():
        return ref
    old = json.loads(base.read_text())["cards"][int(n)]
    cur = books[bid]["cards"]
    for key in ("quote", "title"):
        hit = [i for i, c in enumerate(cur) if c.get(key) and c.get(key) == old.get(key)]
        if hit:
            return f"{bid}#{hit[0]}"
    sys.exit(f"ref {ref} ({old.get('title')}) has no card in the current edition")


def curated(name, books):
    f = EXTRA / f"{name}.json"
    if not f.exists():
        return []
    d = json.loads(f.read_text())
    for x in d:
        if "days" in x: x["days"] = [[remap(r, books) for r in day] for day in x["days"]]
        if "cards" in x: x["cards"] = [remap(r, books) for r in x["cards"]]
        for a in x.get("advice", []): a["card"] = remap(a["card"], books)
    return d


def main():
    (LIB / "books").mkdir(parents=True, exist_ok=True)
    (LIB / "img").mkdir(exist_ok=True)
    cat_path = LIB / "catalog.json"
    old = json.loads(cat_path.read_text()) if cat_path.exists() else {}
    books, full = [], {}
    credits = json.loads((ROOT / "research/commons/credits.json").read_text())
    sources = sorted((ASSETS / "books").glob("*.json")) + sorted((EXTRA / "books").glob("*.json"))
    for src in sources:
        b = json.loads(src.read_text())
        full[b["id"]] = b
        if bad := foreign_plates(b, credits):
            sys.exit("plate from the wrong tradition:\n  " + "\n  ".join(bad))
        if bad := misquotes(b):
            sys.exit("quote not found in its original passage:\n  " + "\n  ".join(bad))
        imgdir = src.parent.parent / "img"
        names = sorted({c["img"] for c in b["cards"] if c.get("img")} | ({b["cover"]} if b.get("cover") else set()))
        size = src.stat().st_size
        for n in names:
            f = imgdir / f"{n}.webp"
            if not f.exists():
                sys.exit(f"missing plate {f}")
            shutil.copy2(f, LIB / "img" / f.name)
            size += f.stat().st_size
        shutil.copy2(src, LIB / "books" / src.name)
        books.append(dict(id=b["id"], version=b.get("version", 1), title=b["title"], author=b.get("author", ""),
                          year=b.get("year", ""), era=b.get("era", ""), cards=len(b["cards"]), sizeKb=size // 1024,
                          blurb=b.get("blurb", ""), images=names, shelf=b.get("shelf", "")))
    app = old.get("app")
    if len(sys.argv) >= 5 and sys.argv[1] == "--app":
        code, name, apk = int(sys.argv[2]), sys.argv[3], pathlib.Path(sys.argv[4])
        notes = sys.argv[5] if len(sys.argv) > 5 else ""
        app = dict(versionCode=code, versionName=name, notes=notes, sizeKb=apk.stat().st_size // 1024,
                   sha256=hashlib.sha256(apk.read_bytes()).hexdigest(),
                   apk=f"https://github.com/{REPO}/releases/download/v{name}/Folio.apk")
    cloud = json.loads((ROOT / "cloud/public.json").read_text()) if (ROOT / "cloud/public.json").exists() else old.get("cloud")
    sh = json.loads((EXTRA / "shelves.json").read_text()) if (EXTRA / "shelves.json").exists() else {}
    cat = {"app": app, "books": books, "cloud": cloud, "shelves": sh.get("shelves", []), "coming": sh.get("coming", []),
           "journeys": curated("journeys", full), "themes": curated("themes", full), "scenarios": curated("scenarios", full)}
    cat_path.write_text(json.dumps(cat, ensure_ascii=False, indent=1))
    for name in ("journeys", "themes", "scenarios"):  # the APK's offline copies, with the same resolved refs
        (ASSETS / f"{name}.json").write_text(json.dumps(cat[name], ensure_ascii=False, indent=1))
    print(f"catalog: {len(books)} books" + (f", app {app['versionName']} ({app['versionCode']})" if app else ", no app release"))


if __name__ == "__main__":
    fake = {"id": "t", "cards": [{"title": "a", "quote": "Be bold.", "orig": "He said: be  bold!"}, {"title": "b", "quote": "Be shy", "orig": "x"}]}
    assert misquotes(fake) == ["t#1 b"]
    assert foreign_plates({"id": "gita", "cards": [{"img": "x"}]}, {"x": {"file": "File:Folio from a Razmnama.jpg"}})
    main()
