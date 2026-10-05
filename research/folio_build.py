"""Shared book builder: verifies quotes against public-domain source texts, ships glossary + cards as JSON."""
import difflib, json, re, sys, pathlib

HERE = pathlib.Path(__file__).parent
SOURCES = {  # key -> (file, attribution)
    "prince": ("prince.txt", "Niccolò Machiavelli, The Prince (1532)"),
    "sunzi": ("artofwar.txt", "Sun Tzŭ, The Art of War (tr. Giles, 1910)"),
    "gracian": ("gracian.txt", "Baltasar Gracián, The Art of Worldly Wisdom (1647, tr. Jacobs)"),
    "roche": ("rochefoucauld.txt", "La Rochefoucauld, Maxims (1665)"),
    "gita": ("gita.txt", "The Bhagavad Gita (tr. Edwin Arnold, 1885)"),
}
_cache = {}
OCR = {"gracian"}  # scanned sources: allow small OCR differences
CAPS = {  # hand-written where Commons titles are catalogue-speak
    "artofwar_10": "Chinese repeating crossbow", "artofwar_15": "Dai Jin, Travellers Through Mountain Passes",
    "artofwar_20": "Portrait of the Qing general Mingliang, 1776", "artofwar_21": "The siege of Songtao, Qing painting",
    "artofwar_22": "Water scene with a distant fortress, Chinese school", "artofwar_26": "Two illustrations from a Chinese Three Kingdoms edition",
    "artofwar_09": "The three heroes fight Lü Bu, Three Kingdoms woodblock print",
    "artofwar_14": "Breaking through the siege at Hesui, Qing battle painting",
    "artofwar_24": "Zhuge Liang and Zhang Fei, Three Kingdoms illustration",
    "gita_05": "Krishna and Balarama lead the cattle, Bhagavata Purana manuscript",
    "gita_23": "The churning of the ocean, Indian painting, c. 1820",
    "gita_26": "Illustrated Sanskrit Bhagavad Gita manuscript, 19th century",
    "gita_48": "The sacred lotus, botanical illustration",
    "gita_51": "Rigveda manuscript", "gita_12": "Dadupanthi ascetics, 18th-century painting",
    "gita_14": "Krishna, Kangra painting", "gita_17": "Baby Vishnu, Indian school, late 18th century",
    "gita_24": "Krishna playing the flute, temple fresco",
    "laws_02": "The Madrid Skylitzes, Byzantine chronicle", "laws_05": "Pericles, Roman copy of a Greek bust",
    "laws_08": "The Congress of Vienna, 1815", "laws_12": "Gravelot, The Trojan Horse", "laws_13": "Thucydides, bust",
    "laws_15": "Empress Wu Zetian", "laws_17": "Caxton, The Game and Playe of the Chesse, 1474",
    "laws_19": "Vasnetsov, Tsar Ivan IV", "laws_21": "Emperor Claudius, marble bust",
    "laws_23": "The House of Rothschild", "laws_28": "Titian, Portrait of Pietro Aretino",
    "laws_18": "Qin Shi Huang, 18th-century portrait",
    "laws_54": "P. T. Barnum's mermaid advertisement",
    "laws_70": "Birds flocking at a willow embankment, Xia Shuwen",
}


def norm(s):
    s = s.replace("’", "'").replace("‘", "'").replace("“", '"').replace("”", '"').replace("ŭ", "u").replace("œ", "oe").replace("Œ", "Oe").replace("--", "—")
    s = re.sub(r"-\s*\n\s*", "", s)
    s = re.sub(r"\[(FN#)?\d+\]", "", s)
    s = re.sub(r"_", "", s)
    return re.sub(r"\s+", " ", s).strip().lower()


def text(key):
    if key not in _cache:
        _cache[key] = norm((HERE / SOURCES[key][0]).read_text())
    return _cache[key]


def locate(q, t, ocr):
    """Index of normalised quote q in normalised text t, or -1. OCR'd sources may differ by a few characters."""
    i = t.find(q)
    if i >= 0 or not ocr:
        return i
    head = q[:18]
    for m in re.finditer(re.escape(head[:10]), t):
        window = t[m.start(): m.start() + len(q) + 10]
        if difflib.SequenceMatcher(None, q, window[:len(q)]).ratio() >= 0.9:
            return m.start()
    # head may itself contain an OCR error: slide over candidate windows sharing a rare word
    words = sorted(set(re.findall(r"[a-z]{7,}", q)), key=len, reverse=True)[:2]
    for w in words:
        for m in re.finditer(w, t):
            off = q.find(w)
            start = max(0, m.start() - off)
            if difflib.SequenceMatcher(None, q, t[start: start + len(q)]).ratio() >= 0.9:
                return start
    return -1


def found(quote, key):
    return locate(norm(quote), text(key), key in OCR) >= 0


_paras = {}


def clean(p):
    p = re.sub(r"\[(FN#)?\d+\]|_", "", p)
    p = re.sub(r"-\s*\n\s*(?=[a-z])", "", p)
    return re.sub(r"\s+", " ", p).strip()


def original(quote, key, lo=320, hi=1300):
    """The passage a quote comes from, as printed: its paragraph, widened to neighbours when the
    paragraph is a single verse line, trimmed to whole sentences around the quote when very long."""
    if key not in _paras:
        raw = [x for x in re.split(r"\n\s*\n", (HERE / SOURCES[key][0]).read_text()) if x.strip()]
        nrm = [norm(x) for x in raw]
        starts, pos = [], 0
        for n in nrm:
            starts.append(pos); pos += len(n) + 1
        _paras[key] = (raw, nrm, starts, " ".join(nrm))
    raw, nrm, starts, joined = _paras[key]
    q = norm(quote)
    i = locate(q, joined, key in OCR)
    if i < 0:
        return ""
    first = max(k for k, st in enumerate(starts) if st <= i)
    last = max(k for k, st in enumerate(starts) if st <= i + len(q) - 1)
    # editors' bracketed notes (Giles, Arnold) are not the author's words: widen over the text only
    note = lambda k: raw[k].lstrip().startswith("[") and not (first <= k <= last)
    pick = list(range(first, last + 1))
    size = lambda: sum(len(nrm[k]) for k in pick)
    a, b = first, last
    while size() < lo and (a > 0 or b < len(raw) - 1):
        if b < len(raw) - 1:
            b += 1
            if not note(b): pick.append(b)
        if size() < lo and a > 0:
            a -= 1
            if not note(a): pick.insert(0, a)
    out = "\n\n".join(clean(raw[k]) for k in pick)
    if len(out) > hi:  # one huge paragraph: keep whole sentences around the quote
        sents = re.split(r"(?<=[.!?;:])\s+", out)
        nq = q[:40]
        at = next((k for k, x in enumerate(sents) if nq[:25] in norm(x)), None)
        if at is None:
            at = next((k for k, x in enumerate(sents) if any(w in norm(x) for w in q.split()[:4] if len(w) > 5)), 0)
        a2 = b2 = at
        while len(" ".join(sents[a2:b2 + 1])) < hi * .8 and (a2 > 0 or b2 < len(sents) - 1):
            if a2 > 0: a2 -= 1
            if b2 < len(sents) - 1: b2 += 1
        out = ("… " if a2 > 0 else "") + " ".join(sents[a2:b2 + 1]) + (" …" if b2 < len(sents) - 1 else "")
    if out[:1].islower():  # scanned page breaks can start mid-sentence
        out = "… " + out
    return out


def build(about, glossary, cards, credits_file=None, caps=None, out_dir=None):
    caps = caps or {}
    credits = json.loads((HERE / credits_file).read_text()) if credits_file and (HERE / credits_file).exists() else {}
    generated = HERE / "generated/manifest.json"
    if generated.exists():
        credits.update(json.loads(generated.read_text()))
    bad = []
    for i, c in enumerate(cards):
        src = c.pop("src")
        if not found(c["quote"], src):
            bad.append((i + 1, src, c["quote"]))
        c["orig"] = original(c["quote"], src)
        c["origFrom"] = SOURCES[src][1]
        if src != about.get("self_src"):
            c["qBy"] = SOURCES[src][1]
        c["img"] = f"{about['id']}_{i + 1:02d}"
        cr = credits.get(c["img"])
        caps = {**CAPS, **caps}
        if cr and cr.get("license") == "Folio original":
            c["cap"] = caps.get(c["img"], cr["file"]) + " · original Folio illustration"
        else:
            c["cap"] = (caps[c["img"]] + " · public domain") if c["img"] in caps else (caption(cr["file"]) if cr else "")
    gl = {}
    for w, (m, e, forms) in glossary.items():
        gl[w] = {"m": m, "e": e}
        for f in forms:
            gl[f] = {"m": m, "e": e, "root": w}
    alltext = " ".join(c["text"] + " " + c["quote"] for c in cards).lower()
    gl = {w: v for w, v in gl.items() if re.search(r"\b" + re.escape(w.lower()) + r"\b", alltext)}
    roots = {v.get("root", w) for w, v in gl.items()}
    import meanings
    meanings.apply(about["id"], cards)
    out = {k: v for k, v in about.items() if k != "self_src"}
    out.update(glossary=gl, cards=cards)
    if bad:
        print("QUOTES NOT FOUND:")
        for b in bad:
            print("  ", b)
        sys.exit(1)
    dst = (out_dir or HERE.parent / "app/src/main/assets/books") / f"{about['id']}.json"
    dst.write_text(json.dumps(out, ensure_ascii=False, indent=1))
    print(f"{about['id']}: {len(cards)} cards, {len(roots)}/{len(glossary)} glossary words used -> {dst.name}")
    depth(about["id"], cards, about.get("self_src") or cards[0].get("src", ""), strict=about.get("deep", False))


def depth(book_id, cards, src_key, strict=False):
    """Folio's depth rule, for every book now and later: about one page per 400 words of the original
    (at least 25, at most 120), every chapter at least two pages, and every chapter opened by a bridge
    to the one before. Prints the gap; with strict=True a thin book fails the build."""
    if src_key not in SOURCES:  # digests of copyrighted books have no source text of their own
        return True
    raw = (HERE / SOURCES[src_key][0]).read_text()
    a, b = raw.find("*** START OF"), raw.find("*** END OF")
    words = len(raw[a if a >= 0 else 0: b if b >= 0 else None].split())
    want = max(25, min(120, words // 400))
    chapters = {}
    for c in cards:
        chapters.setdefault(c["ch"], []).append(c)
    thin = [ch for ch, cs in chapters.items() if len(cs) < 2]
    nolink = [ch for k, (ch, cs) in enumerate(chapters.items()) if k > 0 and not cs[0].get("link")]
    ok = len(cards) >= want and not thin and not nolink
    print(f"  depth {book_id}: {len(cards)}/{want} pages for {words} words · {len(thin)} thin chapters · {len(nolink)} chapters without a bridge"
          + ("" if ok else "  <- needs a deeper edition"))
    if strict and not ok:
        sys.exit(f"{book_id} is too thin for Folio")
    return ok


def _selfcheck():
    """python3 folio_build.py: the passage must contain its quote, start with a capital, stay readable in size."""
    for quote, key in [("it is much safer to be feared than loved", "prince"),
                       ("All warfare is based on deception", "sunzi")]:
        o = original(quote, key)
        assert norm(quote) in norm(o), (key, o[:200])
        assert 200 < len(o) < 1700, len(o)
    print("original(): ok")


def caption(f):
    f = re.sub(r"^File:|\.(jpe?g|png|tiff?)$", "", f, flags=re.I)
    f = re.sub(r"\s*[-,]?\s*(WGA\d+|MET DP\d+|RP-P-[\w.-]+|NGA \d+|Google Art Project|Walters \d+|LACMA [\d.]+|RMG \w+|\(BM [^)]*\)|[\d.]+ - Cleveland Museum of Art|\(cropped\)|\(titel op object\)|\(serietitel\))", "", f)
    f = re.sub(r"[\u3000-\u9fff\uff00-\uffef]+[-\s]*", "", f)  # CJK title prefix
    f = re.sub(r"^Anonymous - |\s*-\s*[\d.]+\s*-\s*(Metropolitan Museum of Art|Cleveland Museum of Art)|MET [\d ]+$|\((CBL|IA|BM)[^)]*\)|OeNB \d+|inv\d+|\(\d{6,}\)|- FA\d+.*$|- RCIN.*$|-bust-cutout ROM|- B19[\d.]+.*$", "", f)
    return re.sub(r"\s+", " ", f).strip(" -,") + " · public domain"


if __name__ == "__main__":
    _selfcheck()
