"""Read a source text quickly: each section's opening and its most quotable sentences.
Usage: python3 survey.py <source key> "<heading regex>" [per-section] [start marker] [end marker]"""
import re, sys
import deep_build  # noqa: F401  (registers every source)
from folio_build import SOURCES, HERE

key, head = sys.argv[1], sys.argv[2]
per = int(sys.argv[3]) if len(sys.argv) > 3 else 10
raw = (HERE / SOURCES[key][0]).read_text()
a = raw.find(sys.argv[4]) if len(sys.argv) > 4 and sys.argv[4] else raw.find("*** START OF")
b = raw.find(sys.argv[5], a + 1) if len(sys.argv) > 5 and sys.argv[5] else raw.find("*** END OF")
raw = raw[max(a, 0): b if b > 0 else None]
parts = re.split(f"(?m)^({head}.*)$", raw)
MARK = re.compile(r"\b(never|always|must|whoever|he who|those who|the wise|the fool|nothing|every|no man|is not|are not|better|best|worst|greatest|truth|ought)\b", re.I)
for k in range(1, len(parts) - 1, 2):
    title, body = parts[k].strip(), re.sub(r"\s+", " ", re.sub(r"\[[^\]]*\]", "", parts[k + 1]))
    sents = [s.strip() for s in re.split(r"(?<=[.!?])\s+(?=[A-Z\"“‘'])", body) if 8 <= len(s.split()) <= 38]
    scored = sorted(sents, key=lambda s: -(len(MARK.findall(s)) * 3 - abs(len(s.split()) - 18) / 6))
    pick = sorted(scored[:per], key=lambda s: body.find(s))
    print(f"\n## {title}  ({len(body.split())} words)")
    print("   first:", (sents[0][:220] if sents else body[:220]))
    for s in pick:
        print("   -", s[:260])
