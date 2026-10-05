"""Builds library/ (what the app downloads) from the content in app/src/main/assets.

  python3 tools/publish_library.py                         # refresh books only
  python3 tools/publish_library.py --app CODE NAME APK "notes"   # also announce an app release

A book's "version" (in its *_content.py ABOUT) must be bumped for phones to fetch a corrected edition.
Books that should NOT ship inside the APK go in library-only/books/<id>.json with plates in library-only/img/.
"""
import hashlib, json, pathlib, shutil, sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ASSETS = ROOT / "app/src/main/assets"
EXTRA = ROOT / "library-only"
LIB = ROOT / "library"
REPO = "purvalsingh/folio"


def main():
    (LIB / "books").mkdir(parents=True, exist_ok=True)
    (LIB / "img").mkdir(exist_ok=True)
    cat_path = LIB / "catalog.json"
    old = json.loads(cat_path.read_text()) if cat_path.exists() else {}
    books = []
    sources = sorted((ASSETS / "books").glob("*.json")) + sorted((EXTRA / "books").glob("*.json"))
    for src in sources:
        b = json.loads(src.read_text())
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
                          blurb=b.get("blurb", ""), images=names))
    app = old.get("app")
    if len(sys.argv) >= 5 and sys.argv[1] == "--app":
        code, name, apk = int(sys.argv[2]), sys.argv[3], pathlib.Path(sys.argv[4])
        notes = sys.argv[5] if len(sys.argv) > 5 else ""
        app = dict(versionCode=code, versionName=name, notes=notes, sizeKb=apk.stat().st_size // 1024,
                   sha256=hashlib.sha256(apk.read_bytes()).hexdigest(),
                   apk=f"https://github.com/{REPO}/releases/download/v{name}/Folio.apk")
    cloud = json.loads((ROOT / "cloud/public.json").read_text()) if (ROOT / "cloud/public.json").exists() else old.get("cloud")
    cat = {"app": app, "books": books, "cloud": cloud}
    cat_path.write_text(json.dumps(cat, ensure_ascii=False, indent=1))
    print(f"catalog: {len(books)} books" + (f", app {app['versionName']} ({app['versionCode']})" if app else ", no app release"))


if __name__ == "__main__":
    main()
