"""Fill card plates with public-domain period art from Wikimedia Commons. Usage: python3 commons.py [ids...]"""
import json, sys, urllib.parse, urllib.request, pathlib, re
UA = {"User-Agent": "FolioApp/1.0 (purvalsingh841@gmail.com) plate-fetcher"}
Q = {
 "08": "Suleiman the Magnificent woodcut", "09": "Florence Nuremberg Chronicle", "10": "Romulus and Remus engraving",
 "11": "Savonarola preaching woodcut", "12": "execution of Savonarola", "13": "tree storm woodcut",
 "14": "Cesare Borgia portrait", "15": "papal conclave engraving", "16": "Agathocles tyrant Syracuse",
 "17": "Justice with scales and sword engraving", "18": "Lorenzo de Medici portrait", "19": "peasants woodcut 16th century",
 "20": "Nuremberg Chronicle city view", "21": "Saint Peter's Basilica construction Heemskerck", "22": "Landsknechte woodcut",
 "23": "battle engraving 1520", "24": "David Saul armour engraving", "25": "David and Goliath woodcut",
 "26": "hunting woodcut 16th century", "27": "Saint Jerome in his Study Dürer", "28": "Janus two faces engraving",
 "29": "The Moneylender and his Wife Massys", "30": "Triumphs of Caesar Mantegna engraving", "31": "execution woodcut 16th century",
 "32": "emperor throne woodcut", "33": "Prodigal Son Dürer engraving", "34": "fox Aesop woodcut",
 "35": "Pazzi conspiracy", "36": "Machiavelli portrait Santi di Tito", "37": "Triumphal Procession Maximilian Burgkmair",
 "38": "Ship of Fools Narrenschiff woodcut", "39": "conspirators engraving", "40": "Louis XII of France portrait",
 "41": "Merian castle engraving", "42": "Ferdinand II of Aragon portrait", "43": "Battle of Ravenna 1512",
 "44": "council woodcut 16th century", "45": "court jester woodcut", "46": "Erasmus Holbein writing",
 "47": "Bruegel ship storm", "48": "deluge woodcut", "49": "Wheel of Fortune woodcut",
 "50": "Pope Julius II Raphael", "51": "Roman ruins Heemskerck", "52": "pikemen woodcut",
}
def get(url):
    import time
    for k in range(5):
        try: return urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=40).read()
        except Exception:
            if k == 4: raise
            time.sleep(4 * (k + 1))
def pick(q, skip=0):
    api = "https://commons.wikimedia.org/w/api.php?" + urllib.parse.urlencode(dict(
        action="query", format="json", generator="search", gsrsearch=q + " filetype:bitmap", gsrnamespace=6, gsrlimit=12,
        prop="imageinfo", iiprop="url|extmetadata|size", iiurlwidth=1000))
    pages = sorted(json.loads(get(api)).get("query", {}).get("pages", {}).values(), key=lambda p: p.get("index", 99))
    ok = []
    for p in pages:
        ii = p["imageinfo"][0]; md = ii.get("extmetadata", {})
        lic = md.get("LicenseShortName", {}).get("value", "")
        if not re.search(r"public domain|^PD|CC0", lic, re.I): continue
        if ii["width"] < 500: continue
        ok.append((p["title"], ii["thumburl"], lic))
    return ok[skip] if len(ok) > skip else None
ids = sys.argv[1:] or list(Q)
credits = json.loads(pathlib.Path("commons/credits.json").read_text()) if pathlib.Path("commons/credits.json").exists() else {}
for i in ids:
    q = Q[i]; skip = 0
    if ":" in i: i, skip = i.split(":")[0], int(i.split(":")[1]); q = Q[i]
    r = pick(q, skip)
    if not r: print(i, "NONE", q); continue
    title, url, lic = r
    (pathlib.Path("commons") / f"prince_{i}.jpg").write_bytes(get(url))
    credits[f"prince_{i}"] = {"file": title, "license": lic}
    print(i, title, "|", lic, flush=True)
    pathlib.Path("commons/credits.json").write_text(json.dumps(credits, indent=1, ensure_ascii=False))
