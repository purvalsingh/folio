"""Pictures for a deep edition: fetch any missing plates from Wikimedia Commons, convert them, and draw a contact sheet.
Usage: python3 deep_images.py <id> [--refetch key key ...]"""
import json, pathlib, subprocess, sys
from PIL import Image, ImageOps, ImageDraw

HERE = pathlib.Path(__file__).parent
BUNDLED = {"prince", "laws", "artofwar", "gita"}
SHEET = pathlib.Path("/tmp/claude-1000/-home-purvals/185ad76f-cc04-48ec-acc8-f2a1991b5793/scratchpad")


def run(book_id, refetch=()):
    q = json.loads((HERE / "queries.json").read_text()).get(book_id, {})
    book = json.loads((HERE.parent / ("app/src/main/assets/books" if book_id in BUNDLED else "library-only/books") / f"{book_id}.json").read_text())
    names = {c["img"] for c in book["cards"] if c.get("img")}
    keys = [k for k in q if f"{book_id}_{k}" in names and k != "cover"]
    out = HERE.parent / ("app/src/main/assets/img" if book_id in BUNDLED else "library-only/img")
    # a plate is done once its webp exists; originals are deleted after converting (the disk is small)
    missing = [k for k in keys if k in refetch or not (out / f"{book_id}_{k}.webp").exists()]
    for i in range(0, len(missing), 12):
        subprocess.run([sys.executable, "commons.py", book_id, *missing[i:i + 12]], cwd=HERE)
    thumbs, absent = [], []
    for k in sorted(keys, key=lambda x: int(x) if x.isdigit() else 0):
        f = HERE / f"commons/{book_id}_{k}.jpg"
        if not f.exists():
            done = out / f"{book_id}_{k}.webp"
            if done.exists():
                t = Image.open(done).convert("L").resize((160, 120)); ImageDraw.Draw(t).rectangle((0, 0, 26, 12), fill=0); ImageDraw.Draw(t).text((2, 0), k, fill=255); thumbs.append(t)
            else:
                absent.append(k)
            continue
        im = ImageOps.exif_transpose(Image.open(f)).convert("L"); w, h = im.size
        if h > w * .75: im = im.crop((0, 0, w, int(w * .75)))
        elif w > h / .75: nw = int(h / .75); im = im.crop(((w - nw) // 2, 0, (w - nw) // 2 + nw, h))
        im = ImageOps.autocontrast(im.resize((900, 675), Image.LANCZOS), cutoff=1)
        im.save(out / f"{book_id}_{k}.webp", "WEBP", quality=72)
        f.unlink()
        t = im.resize((160, 120)); ImageDraw.Draw(t).rectangle((0, 0, 26, 12), fill=0); ImageDraw.Draw(t).text((2, 0), k, fill=255); thumbs.append(t)
    cols = 10
    sheet = Image.new("L", (cols * 160, ((len(thumbs) + cols - 1) // cols) * 120 or 120), 255)
    for n, t in enumerate(thumbs):
        sheet.paste(t, ((n % cols) * 160, (n // cols) * 120))
    sheet.save(SHEET / f"sheet_{book_id}.jpg", quality=80)
    print(f"{book_id}: {len(thumbs)} plates ready, missing {absent} · sheet_{book_id}.jpg")


if __name__ == "__main__":
    a = sys.argv[1:]
    refetch = a[a.index("--refetch") + 1:] if "--refetch" in a else []
    run(a[0], refetch)
