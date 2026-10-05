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
    "artofwar_20": "Portrait of the Qing general Mingliang, 1776", "artofwar_21": "The siege of Jinzhou, Qing painting",
    "artofwar_22": "Water scene with a distant fortress, Chinese school", "artofwar_26": "Poems on painting plum blossoms and bamboo",
    "gita_05": "Krishna and Arjuna, fresco from Mansar", "gita_12": "Dadupanthi ascetics, 18th-century painting",
    "gita_14": "Krishna, Kangra painting", "gita_17": "Baby Vishnu, Indian school, late 18th century",
    "gita_24": "Krishna playing the flute, temple fresco",
    "laws_02": "The Madrid Skylitzes, Byzantine chronicle", "laws_05": "Pericles, Roman copy of a Greek bust",
    "laws_08": "The Congress of Vienna, 1815", "laws_12": "Gravelot, The Trojan Horse", "laws_13": "Thucydides, bust",
    "laws_15": "Empress Wu Zetian", "laws_17": "Caxton, The Game and Playe of the Chesse, 1474",
    "laws_19": "Vasnetsov, Tsar Ivan IV", "laws_21": "Emperor Claudius, marble bust",
    "laws_23": "The House of Rothschild", "laws_28": "Titian, Portrait of Pietro Aretino",
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


def found(quote, key):
    """Exact match after normalisation; OCR'd sources (Gracián) may differ by a few characters."""
    q, t = norm(quote), text(key)
    if q in t:
        return True
    if key not in OCR:
        return False
    head = q[:18]
    for m in re.finditer(re.escape(head[:10]), t):
        window = t[m.start(): m.start() + len(q) + 10]
        if difflib.SequenceMatcher(None, q, window[:len(q)]).ratio() >= 0.9:
            return True
    # head may itself contain an OCR error: slide over candidate windows sharing a rare word
    words = sorted(set(re.findall(r"[a-z]{7,}", q)), key=len, reverse=True)[:2]
    for w in words:
        for m in re.finditer(w, t):
            off = q.find(w)
            start = max(0, m.start() - off)
            if difflib.SequenceMatcher(None, q, t[start: start + len(q)]).ratio() >= 0.9:
                return True
    return False


def build(about, glossary, cards, credits_file=None, caps=None, out_dir=None):
    caps = caps or {}
    credits = json.loads((HERE / credits_file).read_text()) if credits_file and (HERE / credits_file).exists() else {}
    bad = []
    for i, c in enumerate(cards):
        src = c.pop("src")
        if not found(c["quote"], src):
            bad.append((i + 1, src, c["quote"]))
        if src != about.get("self_src"):
            c["qBy"] = SOURCES[src][1]
        c["img"] = f"{about['id']}_{i + 1:02d}"
        cr = credits.get(c["img"])
        caps = {**CAPS, **caps}
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
    dst = (out_dir or HERE.parent / "app/src/main/assets/books") / f"{about['id']}.json"
    dst.write_text(json.dumps(out, ensure_ascii=False, indent=1))
    print(f"{about['id']}: {len(cards)} cards, {len(roots)}/{len(glossary)} glossary words used -> {dst.name}")
    if bad:
        print("QUOTES NOT FOUND:")
        for b in bad:
            print("  ", b)
        sys.exit(1)


def caption(f):
    f = re.sub(r"^File:|\.(jpe?g|png|tiff?)$", "", f, flags=re.I)
    f = re.sub(r"\s*[-,]?\s*(WGA\d+|MET DP\d+|RP-P-[\w.-]+|NGA \d+|Google Art Project|Walters \d+|LACMA [\d.]+|RMG \w+|\(BM [^)]*\)|[\d.]+ - Cleveland Museum of Art|\(cropped\)|\(titel op object\)|\(serietitel\))", "", f)
    f = re.sub(r"[\u3000-\u9fff\uff00-\uffef]+[-\s]*", "", f)  # CJK title prefix
    f = re.sub(r"^Anonymous - |\s*-\s*[\d.]+\s*-\s*(Metropolitan Museum of Art|Cleveland Museum of Art)|MET [\d ]+$|\((CBL|IA|BM)[^)]*\)|OeNB \d+|inv\d+|\(\d{6,}\)|- FA\d+.*$|- RCIN.*$|-bust-cutout ROM|- B19[\d.]+.*$", "", f)
    return re.sub(r"\s+", " ", f).strip(" -,") + " · public domain"
