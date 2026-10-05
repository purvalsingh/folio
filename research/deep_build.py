"""Deep editions: rebuild a book from an ordered list of pages, keeping the old pages (K) and adding new ones (N),
with a bridge at the start of every chapter. Every quote is verified against its public-domain source.

A deep file (research/deep/<id>.py) defines:
    SRC = "tao"                     # the book's own source key in folio_build.SOURCES
    TARGET = 40                     # pages this book should reach
    BRIDGE = {"Chapter II": "..."}  # opener for each chapter after the first
    PAGES = [K(0), N(ch, chTitle, title, text, quote, mean, life, picture_search, src=None), ...]
    GLOSS = {"word": ("meaning", "example", ["forms"])}   # optional additions
    CAPS = {"<id>_45": "hand-written caption"}            # optional
Run: python3 deep_build.py <id> [<id> ...]
"""
import importlib, json, pathlib, re, sys
import folio_build
from folio_build import SOURCES, found, original, caption, depth

HERE = pathlib.Path(__file__).parent
# register every source the shelf scripts know about
import shelf2_content, shelf3_content, shelf4_content  # noqa: F401  (their SOURCES.update calls)

SOURCES.update({
    "prince": ("prince.txt", "Niccolò Machiavelli, The Prince (1532)"),
})
BUNDLED = {"prince", "laws", "artofwar", "gita"}


def K(n, **change):
    """An existing page, by its index in the current edition."""
    return ("K", n, change)


def N(ch, cht, title, text, quote, mean, life, pic, src=None):
    """A new page."""
    return ("N", dict(ch=ch, chTitle=cht, title=title, text=text, quote=quote, qMean=mean, qLife=life), pic, src)


def path(book_id):
    return (HERE.parent / ("app/src/main/assets/books" if book_id in BUNDLED else "library-only/books") / f"{book_id}.json")


def build(book_id):
    mod = importlib.import_module(f"deep.{book_id}")
    current = json.loads(path(book_id).read_text())
    base = HERE / "base" / f"{book_id}.json"  # the edition before deepening: K(n) always refers to it
    if not base.exists():
        assert not current.get("deep"), f"{book_id}: no base edition saved"
        base.parent.mkdir(exist_ok=True); base.write_text(json.dumps(current, ensure_ascii=False, indent=1))
    old = json.loads(base.read_text())
    src = mod.SRC
    used = {c.get("img") for c in old["cards"]}
    nums = [int(m.group(1)) for x in used if x and (m := re.match(rf"{book_id}_(\d+)$", x))]
    nxt = max(nums + [0]) + 1
    cards, queries, bad = [], {}, []
    credits = json.loads((HERE / "commons/credits.json").read_text())
    caps = {**folio_build.CAPS, **getattr(mod, "CAPS", {})}
    for p in mod.PAGES:
        if p[0] == "K":
            c = dict(old["cards"][p[1]]); c.update(p[2]); c.pop("link", None)
        else:
            _, c, pic, qsrc = p
            c = dict(c)
            if not c.get("chTitle"):
                c["chTitle"] = next((o.get("chTitle", "") for o in old["cards"] if o["ch"] == c["ch"]), "")
            key = f"{nxt:02d}"; nxt += 1
            c["img"] = f"{book_id}_{key}"; queries[key] = pic
            s = qsrc or src
            if not found(c["quote"], s):
                bad.append((c["title"], s, c["quote"]))
            c["orig"] = original(c["quote"], s)
            c["origFrom"] = SOURCES[s][1]
            if s != src:
                c["qBy"] = SOURCES[s][1]
            c["_new"] = True
        cards.append(c)
    # chapter bridges
    seen = []
    for c in cards:
        if c["ch"] not in seen:
            seen.append(c["ch"])
            if c["ch"] in mod.BRIDGE:
                c["link"] = mod.BRIDGE[c["ch"]]
    for c in cards:
        if c.pop("_new", False) or not c.get("cap"):
            cr = credits.get(c["img"])
            c["cap"] = (caps[c["img"]] + " · public domain") if c["img"] in caps else (caption(cr["file"]) if cr else "")
    # glossary: old + additions, shipped only if used
    gl = dict(old.get("glossary", {}))
    for w, (m, e, forms) in getattr(mod, "GLOSS", {}).items():
        gl[w] = {"m": m, "e": e}
        for f in forms:
            gl[f] = {"m": m, "e": e, "root": w}
    alltext = " ".join(c["text"] + " " + c["quote"] for c in cards).lower()
    gl = {w: v for w, v in gl.items() if re.search(r"\b" + re.escape(w.lower()) + r"\b", alltext)}
    out = {k: v for k, v in old.items() if k not in ("cards", "glossary")}
    out.update(version=current.get("version", 1) + 1, deep=True, glossary=gl, cards=cards)
    if bad:
        print(f"{book_id}: QUOTES NOT FOUND")
        for b in bad:
            print("   ", b)
        sys.exit(1)
    path(book_id).write_text(json.dumps(out, ensure_ascii=False, indent=1))
    # picture searches for the new pages
    qp = HERE / "queries.json"
    q = json.loads(qp.read_text()); q.setdefault(book_id, {}).update(queries); qp.write_text(json.dumps(q, indent=1, ensure_ascii=False))
    n_new = sum(1 for p in mod.PAGES if p[0] == "N")
    print(f"{book_id}: {len(cards)} pages ({n_new} new), {len(mod.BRIDGE)} bridges, version {out['version']}")
    ok = len(cards) >= mod.TARGET and all(len([c for c in cards if c["ch"] == ch]) >= 2 for ch in seen) and \
        all(c.get("link") for k, c in enumerate(cards) if k > 0 and cards[k - 1]["ch"] != c["ch"])
    print(f"  depth: {len(cards)}/{mod.TARGET} pages · {'OK' if ok else 'NOT YET'}")
    return list(queries)


if __name__ == "__main__":
    for b in sys.argv[1:]:
        build(b)
