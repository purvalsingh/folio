#!/usr/bin/env python3
"""Folio popularity counter: APK downloads, repo visits, clones, stars.

GitHub only keeps visit and clone numbers for 14 days, so every run folds the daily
numbers into stats/traffic.json (kept on this machine, not committed). Run it any time:
    python3 tools/stats.py          # update + print the summary
Cron runs it every 6 hours (see PROJECT_STATE.md).
"""
import json, subprocess, sys, datetime, pathlib

REPO = "purvalsingh/folio"
HERE = pathlib.Path(__file__).resolve().parent.parent / "stats"
STORE = HERE / "traffic.json"


def api(path):
    out = subprocess.run(["gh", "api", f"repos/{REPO}{path}"], capture_output=True, text=True)
    if out.returncode:
        sys.exit(f"gh api {path} failed: {out.stderr.strip()}")
    return json.loads(out.stdout)


def fold(old, days):
    """Merge GitHub's last-14-days list into the saved per-day history (newer numbers win)."""
    for d in days:
        old[d["timestamp"][:10]] = {"count": d["count"], "uniques": d["uniques"]}
    return old


def main():
    HERE.mkdir(exist_ok=True)
    s = json.loads(STORE.read_text()) if STORE.exists() else {"views": {}, "clones": {}, "snapshots": []}
    s["views"] = fold(s["views"], api("/traffic/views")["views"])
    s["clones"] = fold(s["clones"], api("/traffic/clones")["clones"])
    releases = api("/releases")
    dl = {r["tag_name"]: sum(a["download_count"] for a in r["assets"]) for r in releases}
    repo = api("")
    refs = api("/traffic/popular/referrers")
    now = datetime.datetime.now().isoformat(timespec="minutes")
    snap = {"at": now, "downloads": sum(dl.values()), "stars": repo["stargazers_count"],
            "forks": repo["forks_count"], "watchers": repo["subscribers_count"]}
    s["snapshots"].append(snap)
    s["downloads_by_release"] = dl
    s["referrers"] = refs
    STORE.write_text(json.dumps(s, indent=1))

    views = sum(v["count"] for v in s["views"].values())
    clones = sum(v["count"] for v in s["clones"].values())
    today = datetime.date.today().isoformat()
    week = [(datetime.date.today() - datetime.timedelta(days=k)).isoformat() for k in range(7)]
    v7 = sum(s["views"].get(d, {}).get("count", 0) for d in week)
    u7 = sum(s["views"].get(d, {}).get("uniques", 0) for d in week)
    first = s["snapshots"][0]
    print(f"Folio stats · {now}")
    print(f"  APK downloads   {snap['downloads']}  (since tracking began {first['at'][:10]}: +{snap['downloads'] - first['downloads']})")
    print("    by release    " + ", ".join(f"{k} {v}" for k, v in dl.items()))
    print(f"  Repo visits     {views} total since tracking · last 7 days {v7} ({u7} visitor-days) · today {s['views'].get(today, {}).get('count', 0)}")
    print(f"  Clones          {clones}")
    print(f"  Stars {snap['stars']} · forks {snap['forks']} · watchers {snap['watchers']}")
    if refs:
        print("  Top referrers   " + ", ".join(f"{r['referrer']} {r['count']} ({r['uniques']} people)" for r in refs[:6]))
    print("  Note: downloads include in-app updates (each phone that updates counts once per version).")


if __name__ == "__main__":
    main()
