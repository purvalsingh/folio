// Folio for the web: the same books, pages and habits as the Android app, for iPhone and any browser.
import { renderCard, FORMATS, STYLES, LOOKS } from "./share.js";

const RAW = "https://raw.githubusercontent.com/purvalsingh/folio/main/library/";
const APK = "https://github.com/purvalsingh/folio/releases/latest/download/Folio.apk";
const app = document.getElementById("app");

/* ---------- eras: the letterforms and bindings of each book's time ---------- */
export const ERAS = {
  ancient:       { display: "Cinzel", body: "Garamond", fleuron: "❧", bind: ["#D8CDB8", "#221E19", "#5E564A"], pale: true },
  indic:         { display: "Amita", body: "Tiro", fleuron: "ॐ", bind: ["#84281B", "#F3E7CF", "#E2C98F"] },
  eastern:       { display: "Brush", body: "Zen", fleuron: "❖", bind: ["#2F2925", "#F3E7CF", "#A8382A"] },
  renaissance:   { display: "Fraktur", body: "Fell", fleuron: "❦", bind: ["#221E1A", "#E2C98F", "#A8382A"] },
  baroque:       { display: "Copperplate", body: "Primer", fleuron: "✥", bind: ["#EDE3CF", "#1C1915", "#A8382A"], pale: true },
  enlightenment: { display: "Pica", body: "Pica", fleuron: "❦", bind: ["#34414B", "#F3E7CF", "#E2C98F"] },
  germanic:      { display: "Fraktur", body: "OldStandard", fleuron: "✠", bind: ["#221E1A", "#E2C98F", "#A8382A"] },
  victorian:     { display: "OldStandard", body: "OldStandard", fleuron: "❦", bind: ["#2F3C32", "#E2C98F", "#E2C98F"] },
  modern:        { display: "Typewriter", body: "OldStandard", fleuron: "❦", bind: ["#4A423A", "#F3E7CF", "rgba(243,231,207,.5)"] },
};
export const era = (e) => ERAS[(e || "").toLowerCase()] || ERAS.renaissance;

/* ---------- saved state, on this device ---------- */
const S = Object.assign({ sealed: {}, pos: {}, opened: {}, lexicon: [], quotes: [], marks: [], days: {}, xp: 0, goal: 10,
  theme: null, scale: 1, lines: 1, honours: [], hideInstall: false }, safeLoad());
function safeLoad() { try { return JSON.parse(localStorage.getItem("folio") || "{}"); } catch { return {}; } }
function save() { try { localStorage.setItem("folio", JSON.stringify(S)); } catch {} }
const today = () => new Date().toISOString().slice(0, 10);
const sealedIn = (b) => S.sealed[b] || [];
const isSealed = (b, i) => sealedIn(b).includes(i);

function applyLook() {
  document.documentElement.dataset.theme = S.theme || "";
  if (!S.theme) delete document.documentElement.dataset.theme;
  document.documentElement.style.setProperty("--scale", S.scale);
  document.documentElement.style.setProperty("--lines", S.lines);
}
applyLook();

/* ---------- levels ---------- */
const RANKS = ["Page", "Squire", "Scribe", "Clerk", "Scholar", "Courtier", "Counsellor", "Magister", "Sage", "Prince of Letters"];
const xpFor = (l) => (l <= 1 ? 0 : 100 * (l - 1) * l / 2);
const levelOf = (xp) => { let l = 1; while (l < RANKS.length && xp >= xpFor(l + 1)) l++; return l; };
const roman = (n) => [[1000,"M"],[900,"CM"],[500,"D"],[400,"CD"],[100,"C"],[90,"XC"],[50,"L"],[40,"XL"],[10,"X"],[9,"IX"],[5,"V"],[4,"IV"],[1,"I"]]
  .reduce((s, [v, r]) => { while (n >= v) { s += r; n -= v; } return s; }, "");
function streak() { let n = 0; const d = new Date(); if (!S.days[today()]) d.setDate(d.getDate() - 1);
  while (S.days[d.toISOString().slice(0, 10)]) { n++; d.setDate(d.getDate() - 1); } return n; }

/* ---------- data ---------- */
let catalog = null;
const books = {};
async function getCatalog() {
  if (catalog) return catalog;
  catalog = await fetch(RAW + "catalog.json", { cache: "no-cache" }).then((r) => r.json());
  return catalog;
}
async function getBook(id) {
  if (books[id]) return books[id];
  const e = (await getCatalog()).books.find((b) => b.id === id);
  books[id] = await fetch(`${RAW}books/${id}.json?v=${e ? e.version : 0}`).then((r) => r.json());
  return books[id];
}
export const img = (name) => (name ? `${RAW}img/${name}.webp` : "");
const entry = (id) => catalog?.books.find((b) => b.id === id);
const short = (b) => b.short || b.author;

/* ---------- small helpers ---------- */
const esc = (s) => String(s ?? "").replace(/[&<>"]/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]));
const $ = (sel, root = document) => root.querySelector(sel);
function toast(t) { const d = document.createElement("div"); d.className = "toast"; d.textContent = t; document.body.append(d); setTimeout(() => d.remove(), 2600); }
function sheet(html, onOpen) {
  closeSheet();
  const bg = document.createElement("div"); bg.className = "sheet-bg"; bg.onclick = closeSheet;
  const s = document.createElement("div"); s.className = "sheet"; s.innerHTML = html;
  document.body.append(bg, s); onOpen && onOpen(s); return s;
}
function closeSheet() { document.querySelectorAll(".sheet,.sheet-bg").forEach((e) => e.remove()); }
const isIOS = /iphone|ipad|ipod/i.test(navigator.userAgent) || (navigator.platform === "MacIntel" && navigator.maxTouchPoints > 1);
const standalone = matchMedia("(display-mode: standalone)").matches || navigator.standalone;

/* ---------- celebrations ---------- */
function honour(key, title, line, mark) {
  if (S.honours.includes(key)) return;
  S.honours.push(key); save();
  const d = document.createElement("div"); d.className = "party";
  d.innerHTML = `<div class="card"><span class="seal" style="width:96px;height:96px;font-size:34px">${esc(mark)}</span><h3>${esc(title)}</h3><p>${esc(line)}</p>
    <button class="pill on" style="margin-top:8px">Onward</button></div>`;
  d.onclick = () => d.remove(); document.body.append(d);
}
function totalSealed() { return Object.values(S.sealed).reduce((n, a) => n + a.length, 0); }
function afterSeal(b) {
  const n = totalSealed(), t = S.days[today()] || 0;
  if (n >= 1) honour("p1", "The First Folio", "You sealed your first page. Every library starts with one.", "I");
  if (n >= 10) honour("p10", "Ten Folios", "Ten pages read. The habit is forming.", "X");
  if (n >= 100) honour("p100", "The Centurion", "Congratulations — you have read 100 pages!", "C");
  if (t >= S.goal) honour("q" + today(), "Quota Kept", `Today's ${S.goal} folios are done. Anything more is glory.`, "✓");
  if (b && sealedIn(b.id).length >= b.cards.length) honour("b" + b.id, "Book Finished", `You have read all of ${b.title}.`, "✦");
  const s = streak(); if (s >= 7) honour("s7", "A Week of Reading", "Seven days in a row.", "VII");
}
function seal(b, i) {
  if (isSealed(b.id, i)) return;
  (S.sealed[b.id] ||= []).push(i);
  S.days[today()] = (S.days[today()] || 0) + 1;
  S.xp += 10; save(); afterSeal(b);
}

/* ---------- router ---------- */
const route = () => location.hash.replace(/^#\/?/, "").split("/").filter(Boolean);
window.addEventListener("hashchange", render);
let leaving = null;
async function render() {
  leaving && leaving(); leaving = null; closeSheet();
  const [r, a, b] = route();
  if (r === "read") return reader(a, +(b || 0));
  document.querySelector(".reader")?.remove();
  try { await getCatalog(); } catch { app.innerHTML = `<div class="screen"><div class="empty"><b>❦</b>Folio needs the internet the first time it opens.<br>Connect and reload.</div></div>`; return; }
  const tab = { lexicon: lexicon, quotes: quotes, honours: honours, theme: () => themePage(a), scene: () => scenePage(a), desk: desk, print: printView }[r] || library;
  app.innerHTML = await tab();
  app.querySelectorAll("[data-go]").forEach((e) => (e.onclick = () => (location.hash = e.dataset.go)));
  bindCommon();
  nav(r || "");
  window.scrollTo(0, 0);
}
function nav(r) {
  let n = $("nav.tabs");
  if (!n) { n = document.createElement("nav"); n.className = "tabs"; document.body.append(n); }
  const t = (h, g, label) => `<a href="#/${h}" class="${(r || "") === h ? "on" : ""}"><i>${g}</i>${label}</a>`;
  n.innerHTML = `<div>${t("", "📖", "Library")}${t("lexicon", "Ꭿ", "Lexicon")}${t("quotes", "❝", "Quotes")}${t("honours", "✪", "Honours")}</div>`;
  n.style.display = r === "print" ? "none" : "";
}
function bindCommon() {
  app.querySelectorAll("[data-spine]").forEach((e) => (e.onclick = () => bookSheet(e.dataset.spine)));
  app.querySelectorAll("[data-x]").forEach((e) => (e.onclick = (ev) => { ev.stopPropagation(); S.hideInstall = true; save(); e.closest(".install").remove(); }));
}

/* ---------- Library ---------- */
function installHint() {
  if (standalone || S.hideInstall) return "";
  if (isIOS) return `<div class="install"><div><b>Install Folio on your iPhone</b><br>Tap <span class="share-glyph"></span> Share below, then <b>Add to Home Screen</b>. It opens like an app and works offline.</div><button class="x" data-x aria-label="Close">×</button></div>`;
  if (/android/i.test(navigator.userAgent)) return `<div class="install"><div><b>On Android?</b> Get the full app: <a style="color:inherit;text-decoration:underline" href="${APK}">download Folio.apk</a></div><button class="x" data-x aria-label="Close">×</button></div>`;
  return "";
}
function quota() {
  const t = S.days[today()] || 0, p = Math.min(100, Math.round((t / S.goal) * 100)), lv = levelOf(S.xp);
  return `<div class="panel quota"><div class="ring" style="--p:${p}"><div><span><b>${t}</b><br><small>of ${S.goal}</small></span></div></div>
    <div style="flex:1"><div class="label">Today's quota</div><div>${t >= S.goal ? "Quota kept. Anything more is glory." : `${S.goal - t} folio${S.goal - t === 1 ? "" : "s"} to go today.`}</div>
    <div class="faded it" style="font-size:13px">${streak()}-day streak · ${RANKS[lv - 1]}</div></div></div>`;
}
function deskList() {
  return Object.keys(S.opened).map((id) => ({ id, e: entry(id), at: S.opened[id], done: sealedIn(id).length, pos: S.pos[id] || 0 }))
    .filter((d) => d.e).sort((a, b) => b.at - a.at);
}
function readingNow() {
  const d = deskList().filter((x) => x.done < x.e.cards);
  if (!d.length) return "";
  return `<div class="section"><div class="row"><div class="label" style="flex:1">Reading now · ${d.length}</div><button class="label" data-go="#/desk">See all ›</button></div>
    <div class="scroller" style="margin-top:8px">${d.map((x) => `<button class="mini" data-go="#/read/${x.id}/${x.pos}">
      <h3 style="font-family:${era(x.e.era).display}">${esc(x.e.title)}</h3><div class="bar"><i style="width:${(x.done / x.e.cards) * 100}%"></i></div>
      <div class="faded it" style="font-size:12px;margin-top:4px">folio ${x.pos + 1} of ${x.e.cards} · continue ›</div></button>`).join("")}</div></div>`;
}
function exploreRows() {
  const th = catalog.themes || [], sc = catalog.scenarios || [];
  return (th.length ? `<div class="section"><div class="label">Themes across books</div><div class="faded it" style="font-size:14px">One idea, many authors.</div>
    <div class="scroller" style="margin-top:8px">${th.map((t) => `<button class="mini" data-go="#/theme/${t.id}"><h3>${esc(t.title)}</h3>
      <div class="label" style="font-size:10px">${new Set(t.cards.map((c) => c.split("#")[0])).size} books · ${t.cards.length} pages</div></button>`).join("")}</div></div>` : "") +
    (sc.length ? `<div class="section"><div class="label">What would they do?</div><div class="faded it" style="font-size:14px">Everyday problems, answered by the old books.</div>
    <div class="scroller" style="margin-top:8px">${sc.map((s) => `<button class="mini wide" data-go="#/scene/${s.id}"><h3>${esc(s.title)}</h3>
      <div class="label" style="font-size:10px">${s.advice.map((a) => esc(entry(a.card.split("#")[0])?.title || "")).join(" · ")}</div></button>`).join("")}</div></div>` : "");
}
function spine(b) {
  const e = era(b.era), h = Math.abs([...b.id].reduce((a, c) => (a * 31 + c.charCodeAt(0)) | 0, 7));
  const w = 50 + (h % 16), hh = 160 + ((h / 7) | 0) % 34, n = b.title.length, long = n > 15;
  const size = n <= 11 ? 16 : n > 24 ? 13 : 14, [bg, fg, band] = e.bind;
  const done = sealedIn(b.id).length, reading = done > 0 && done < b.cards, fin = done >= b.cards && b.cards > 0;
  const shade = (h % 3) * 0.045;
  return `<button class="spine" data-spine="${b.id}" aria-label="${esc(b.title)}"
    style="width:${w}px;height:${hh}px;background:linear-gradient(rgba(0,0,0,${shade}),rgba(0,0,0,${shade})),${bg};color:${fg};${e.pale ? "border:1px solid rgba(0,0,0,.45);" : ""}">
    <span class="band" style="top:8%;background:${band}"></span><span class="band" style="top:12%;background:${band}"></span>
    <span class="band" style="top:86%;background:${band}"></span><span class="band" style="top:90%;background:${band}"></span><span class="hl"></span>
    <span class="t" style="font-family:${long ? e.body : e.display};font-size:${size}px">${esc(b.title)}</span>
    <span class="foot" style="color:${band}">${fin ? "✓" : e.fleuron}</span>${reading ? '<span class="ribbon"></span>' : ""}</button>`;
}
function bookcase() {
  const shelves = catalog.shelves || [], coming = catalog.coming || [];
  const named = shelves.map((s) => [s, catalog.books.filter((b) => b.shelf === s.name)]);
  const homeless = catalog.books.filter((b) => !shelves.some((s) => s.name === b.shelf));
  if (named.length) named[0][1].push(...homeless);
  const plate = (name, note) => `<div class="plate"><span class="orn">❧</span><div class="name"><b>${esc(name.toUpperCase())}</b><span>${esc(note)}</span></div><span class="orn">❧</span></div>`;
  return `<div class="section"><h2>The Bookcase</h2><div class="faded it" style="font-size:14px">Tap a spine to pull a book from the shelf.</div></div>
    <div class="shelfchips scroller">${named.map(([s, v], k) => `<button class="chip" onclick="document.getElementById('shelf${k}').scrollIntoView({behavior:'smooth'})">${esc(s.name)} · ${v.length}</button>`).join("")}</div>
    ${named.map(([s, v], k) => `<div id="shelf${k}" style="scroll-margin-top:52px">${plate(s.name, `${s.note} · ${v.length} volume${v.length === 1 ? "" : "s"}`)}
      <div class="shelfwrap"><div class="books">${v.map(spine).join("")}${v.length < 4 ? '<div class="ghost"></div>'.repeat(4 - v.length) : ""}</div></div></div>`).join("")}
    ${coming.map((s) => `${plate(s.name, "In the bindery — coming soon")}<div class="shelfwrap"><div class="books">${(s.titles || []).map((t) => `<div class="ghost"><span>${esc(t)}</span></div>`).join("")}</div></div>`).join("")}`;
}
async function library() {
  const hour = new Date().getHours(), greet = hour < 12 ? "Good morrow, reader." : hour < 18 ? "Good afternoon, reader." : "Good evening, reader.";
  return `<div class="screen"><div class="masthead"><h1>Folio</h1><p>${greet}</p></div>${installHint()}
    <div class="section" style="margin-top:14px">${quota()}</div>${readingNow()}${exploreRows()}${bookcase()}</div>`;
}
function bookSheet(id) {
  const b = entry(id); if (!b) return; const e = era(b.era), done = sealedIn(id).length;
  sheet(`<div class="label">${esc(b.shelf || "")}</div><h2 style="font:30px/1.15 ${e.display},serif;margin:6px 0">${esc(b.title)}</h2>
    <div class="faded it">${esc(b.author)} · ${esc(b.year)}</div><p>${esc(b.blurb)}</p>
    <div class="faded" style="font-size:14px">${b.cards} pages${done ? ` · ${done} read` : ""}</div>
    <button class="pill on" style="width:100%;margin-top:14px" data-go="#/read/${id}/${S.pos[id] || 0}">${done ? "Continue reading" : "Read"}</button>`,
    (s) => s.querySelectorAll("[data-go]").forEach((x) => (x.onclick = () => (location.hash = x.dataset.go))));
}

/* ---------- Reader ---------- */
function glossed(text, gl) {
  const keys = Object.keys(gl || {}).sort((a, b) => b.length - a.length);
  let out = esc(text);
  if (!keys.length) return out;
  const re = new RegExp(`\\b(${keys.map((k) => k.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")).join("|")})\\b`, "gi");
  return out.replace(re, (m) => `<button class="w" data-w="${esc(m.toLowerCase())}">${m}</button>`);
}
const chapterStarts = (b) => b.cards.map((c, i) => (i === 0 || b.cards[i - 1].ch !== c.ch ? i : -1)).filter((i) => i >= 0);
function face(b, i) {
  const c = b.cards[i], e = era(b.era), first = chapterStarts(b).includes(i), done = isSealed(b.id, i);
  const kept = S.quotes.some((q) => q.b === b.id && q.i === i);
  const tsize = { renaissance: 31, baroque: 36 }[b.era] || 28;
  return `<div class="leaf" data-i="${i}"><div class="flip">
  <div class="face front" style="--display:${e.display}">
    <div class="fhead"><span class="label">${esc(c.ch)}</span><button class="turn" data-flip>⟲ meaning</button><span class="faded it" style="font-size:13px">fol. ${roman(i + 1).toLowerCase()}</span></div>
    ${c.chTitle ? `<div class="faded it" style="font-family:${e.body};font-size:14px">${esc(c.chTitle)}</div>` : ""}
    ${c.link ? `<div class="link">${esc(c.link)}</div>` : ""}
    ${c.img ? `<div class="pic"><img loading="lazy" alt="" src="${img(c.img)}" onerror="this.parentNode.style.display='none'"></div><div class="cap">${esc(c.cap || "")}</div>` : ""}
    <h2 class="ctitle" style="font-family:${e.display};font-size:${tsize}px">${esc(c.title)}</h2>
    <div class="ctext ${first ? "cap1" : ""} ${i === 0 ? "ill" : ""}" style="font-family:${e.body}">${glossed(c.text, b.glossary)}</div>
    ${c.quote ? `<div class="fleuron">${e.fleuron}</div><div class="quote" data-flip style="font-family:${e.body}">“${glossed(c.quote, b.glossary)}”</div>
      <div class="attr">— ${esc(c.qBy || `${short(b)}, ${c.ch}`)}</div>
      <div class="acts"><button data-keep class="${kept ? "on" : ""}" aria-label="Keep this quote">❝</button><button data-share aria-label="Share as a picture">⇪</button></div>
      <button class="hint" data-flip>💡 Turn the page over for its meaning</button>` : ""}
    <button class="sealbtn ${done ? "done" : ""}" data-seal>${done ? "✓ Sealed" : i === b.cards.length - 1 ? "Seal the last folio" : "Seal this folio"}</button>
  </div>
  <div class="face back">
    <div class="fhead"><span class="label">${esc(c.ch)} · the other side</span><span class="faded it" style="font-size:13px">fol. ${roman(i + 1).toLowerCase()} verso</span></div>
    <h2 class="ctitle" style="font-family:${e.display};font-size:26px">${esc(c.title)}</h2>
    ${c.quote ? `<div class="quote" style="text-align:left;font-family:${e.body};color:var(--faded);font-size:calc(18px * var(--scale))">“${esc(c.quote)}”</div>` : ""}
    ${c.qMean ? `<div class="h">IN SIMPLE ENGLISH</div><div class="ctext">${esc(c.qMean)}</div>` : ""}
    ${c.qLife ? `<div class="h">IN DAILY LIFE</div><div class="ctext it">${esc(c.qLife)}</div>` : ""}
    ${c.orig ? `<div class="fleuron">${e.fleuron}</div><div class="h" style="margin-top:0">AS THE AUTHOR WROTE IT</div><div class="faded it" style="font-size:13px">${esc(c.origFrom || "")}</div>
      <div class="passage" style="font-family:${e.body};margin-top:8px">${highlight(c.orig, c.quote)}</div>` : ""}
    <button class="pill on" style="width:100%;margin-top:18px" data-flip>⟲ Turn back</button>
  </div></div></div>`;
}
function highlight(p, q) {
  const n = (s) => s.toLowerCase().replace(/[’‘]/g, "'").replace(/[“”]/g, '"');
  const qq = q.trim().replace(/\.$/, ""), at = n(p).indexOf(n(qq));
  return at < 0 ? esc(p) : esc(p.slice(0, at)) + "<mark>" + esc(p.slice(at, at + qq.length)) + "</mark>" + esc(p.slice(at + qq.length));
}
async function reader(id, start) {
  document.querySelector(".reader")?.remove();
  const r = document.createElement("div"); r.className = "reader";
  r.innerHTML = `<div class="empty" style="margin-top:40vh"><b>❦</b>Opening the book…</div>`;
  document.body.append(r); $("nav.tabs") && ($("nav.tabs").style.display = "none");
  let b;
  try { b = await getBook(id); } catch { r.innerHTML = `<div class="empty" style="margin-top:35vh"><b>❦</b>Couldn't fetch this book. Check your connection.<br><br><button class="pill" onclick="history.back()">Back</button></div>`; return; }
  const e = era(b.era); start = Math.max(0, Math.min(start, b.cards.length - 1));
  r.innerHTML = `<div class="rtop"><button class="icon" data-back aria-label="Back">←</button>
    <div class="mid"><b>${esc(b.title.toUpperCase())}</b><small data-count>Folio ${start + 1} of ${b.cards.length}</small></div>
    <button class="icon" data-contents aria-label="Contents">☰</button><button class="icon" data-mark aria-label="Bookmark">🔖</button></div>
    <div class="pages">${b.cards.map((_, i) => face(b, i)).join("")}</div>`;
  const pages = $(".pages", r);
  requestAnimationFrame(() => { pages.scrollLeft = start * pages.clientWidth; });
  let cur = start;
  const mark = () => { $("[data-mark]", r).style.opacity = S.marks.includes(`${id}#${cur}`) ? 1 : 0.35; };
  const at = (i) => { cur = i; S.pos[id] = i; S.opened[id] = Date.now(); save(); $("[data-count]", r).textContent = `Folio ${i + 1} of ${b.cards.length}`; mark(); };
  at(start);
  let t; pages.addEventListener("scroll", () => { clearTimeout(t); t = setTimeout(() => { const i = Math.round(pages.scrollLeft / pages.clientWidth); if (i !== cur) at(i); }, 80); });
  const go = (i) => pages.scrollTo({ left: i * pages.clientWidth, behavior: "smooth" });
  const key = (ev) => { if (ev.key === "ArrowRight") go(cur + 1); if (ev.key === "ArrowLeft") go(cur - 1); };
  document.addEventListener("keydown", key); leaving = () => document.removeEventListener("keydown", key);
  $("[data-back]", r).onclick = () => (history.length > 1 ? history.back() : (location.hash = "#/"));
  $("[data-mark]", r).onclick = () => { const k = `${id}#${cur}`; S.marks = S.marks.includes(k) ? S.marks.filter((x) => x !== k) : [...S.marks, k]; save(); mark(); toast(S.marks.includes(k) ? "Bookmarked" : "Bookmark removed"); };
  $("[data-contents]", r).onclick = () => sheet(`<div class="label">Contents</div>${chapterStarts(b).map((i) => {
      const c = b.cards[i], n = b.cards.filter((x) => x.ch === c.ch).length, d = b.cards.map((x, k) => (x.ch === c.ch && isSealed(id, k) ? 1 : 0)).reduce((a, x) => a + x, 0);
      return `<button class="list-item" style="display:block;width:100%;text-align:left" data-jump="${i}"><b style="font-family:FellSC">${esc(c.ch)}</b> <span class="faded it">${esc(c.chTitle || c.title)}</span>
        <div class="faded" style="font-size:12px">${d}/${n} sealed${d === n ? " ✦" : ""}</div></button>`; }).join("")}`,
    (s) => s.querySelectorAll("[data-jump]").forEach((x) => (x.onclick = () => { closeSheet(); go(+x.dataset.jump); })));
  pages.addEventListener("click", (ev) => {
    const leaf = ev.target.closest(".leaf"); if (!leaf) return; const i = +leaf.dataset.i, c = b.cards[i];
    const w = ev.target.closest("[data-w]");
    if (!w && ev.target.closest("[data-flip]")) { leaf.querySelector(".flip").classList.toggle("over"); return; }
    if (w) { const g = b.glossary[w.dataset.w] || {}; const root = g.root || w.dataset.w; const have = S.lexicon.some((x) => x.w === root);
      sheet(`<div class="label">A word from ${esc(b.title)}</div><h2 style="font:32px ${e.display},serif;margin:6px 0">${esc(root)}</h2><p style="font-size:19px">${esc(g.m || "")}</p>
        ${g.e ? `<div class="h">IN DAILY LIFE</div><p class="it">${esc(g.e)}</p>` : ""}<button class="pill ${have ? "" : "on"}" style="width:100%" data-add>${have ? "✓ In your Lexicon" : "Add to my Lexicon"}</button>`,
        (s) => ($("[data-add]", s).onclick = () => { if (!have) { S.lexicon.unshift({ w: root, m: g.m, e: g.e, b: id, t: Date.now() }); S.xp += 5; save(); toast("Added to your Lexicon"); } closeSheet(); }));
      return; }
    if (ev.target.closest("[data-keep]")) { const k = S.quotes.findIndex((q) => q.b === id && q.i === i);
      if (k >= 0) S.quotes.splice(k, 1); else { S.quotes.unshift({ q: c.quote, b: id, i, t: Date.now() }); S.xp += 5; }
      save(); ev.target.closest("[data-keep]").classList.toggle("on", k < 0); toast(k < 0 ? "Kept in your Commonplace book" : "Removed"); return; }
    if (ev.target.closest("[data-share]")) { studio(b, i); return; }
    if (ev.target.closest("[data-seal]")) { const btn = ev.target.closest("[data-seal]"); if (isSealed(id, i)) return;
      seal(b, i); btn.classList.add("done"); btn.textContent = "✓ Sealed"; if (i + 1 < b.cards.length) setTimeout(() => go(i + 1), 650); }
  });
}

/* ---------- share studio ---------- */
async function studio(b, i) {
  let f = "STORY", s = "PLATE", l = "BOOK";
  const s1 = sheet(`<div class="label">Share this quote</div><canvas class="studio-prev" style="margin-top:10px"></canvas>
    <div data-opts></div><div class="row" style="margin-top:14px"><button class="pill on" style="flex:1" data-send>Share</button><button class="pill" style="flex:1" data-dl>Save image</button></div>`);
  const cv = $("canvas", s1);
  const opts = () => {
    const group = (title, list, cur, key) => `<div class="label faded" style="margin-top:10px;color:var(--faded)">${title}</div><div class="scroller" style="margin-top:4px">${
      Object.entries(list).map(([k, v]) => `<button class="chip ${k === cur ? "red" : ""}" data-k="${key}" data-v="${k}">${v.label}</button>`).join("")}</div>`;
    $("[data-opts]", s1).innerHTML = group("Size", FORMATS, f, "f") + group("Style", STYLES, s, "s") + group("Look", LOOKS, l, "l");
    $("[data-opts]", s1).querySelectorAll("[data-k]").forEach((x) => (x.onclick = () => { if (x.dataset.k === "f") f = x.dataset.v; if (x.dataset.k === "s") s = x.dataset.v; if (x.dataset.k === "l") l = x.dataset.v; draw(); }));
  };
  const draw = async () => { opts(); await renderCard(cv, b, i, f, s, l); };
  await draw();
  const blob = () => new Promise((res) => cv.toBlob(res, "image/png"));
  $("[data-send]", s1).onclick = async () => {
    const file = new File([await blob()], "folio-quote.png", { type: "image/png" }), c = b.cards[i];
    const text = `“${c.quote}” — ${c.qBy || `${b.author}, ${b.title}`}\n\nRead it free in Folio: https://purvalsingh.github.io/folio/`;
    if (navigator.canShare && navigator.canShare({ files: [file] })) { try { await navigator.share({ files: [file], text }); } catch {} }
    else { download(file); toast("Image saved. Share it from your photos."); }
  };
  $("[data-dl]", s1).onclick = async () => { download(new File([await blob()], "folio-quote.png", { type: "image/png" })); toast(isIOS ? "Long-press the image to save it to Photos" : "Image saved"); };
}
function download(file) { const a = document.createElement("a"); a.href = URL.createObjectURL(file); a.download = file.name; document.body.append(a); a.click(); a.remove(); }

/* ---------- Lexicon, Quotes, Desk ---------- */
async function lexicon() {
  return `<div class="screen"><div class="masthead"><h1>Lexicon</h1><p>Your dictionary of rare words — ${S.lexicon.length} collected.</p></div>
    ${S.lexicon.length ? `<button class="label" style="margin-top:10px" onclick="location.hash='#/print'">Export as PDF, Markdown or Anki ›</button>` : ""}
    ${S.lexicon.length ? S.lexicon.map((w, k) => `<div class="list-item"><div class="row"><b style="font:22px Fraktur,serif;font-weight:normal;flex:1">${esc(w.w)}</b>
      <button class="faded" onclick="window.folioDel('lexicon',${k})" aria-label="Remove">✕</button></div><div>${esc(w.m)}</div>${w.e ? `<div class="faded it" style="font-size:15px">${esc(w.e)}</div>` : ""}
      <div class="label faded" style="font-size:10px;color:var(--faded)">${esc(entry(w.b)?.title || "")}</div></div>`).join("")
    : `<div class="empty"><b>Ꭿ</b>Tap any red word while reading<br>to collect it here.</div>`}</div>`;
}
async function quotes() {
  return `<div class="screen"><div class="masthead"><h1>Commonplace</h1><p>Renaissance readers copied the lines worth keeping into a commonplace book. This is yours.</p></div>
    ${S.quotes.length ? `<button class="label" style="margin-top:10px" onclick="location.hash='#/print'">Export as PDF, Markdown or Anki ›</button>` : ""}
    ${S.quotes.length ? S.quotes.map((q, k) => `<div class="list-item"><div class="it" style="font-size:19px">“${esc(q.q)}”</div>
      <div class="row" style="margin-top:6px"><span class="label faded" style="flex:1;color:var(--faded)">${esc(entry(q.b)?.title || "")}</span>
      <button class="chip" data-go="#/read/${q.b}/${q.i}">Open</button><button class="chip" onclick="window.folioShare('${q.b}',${q.i})">Share</button>
      <button class="faded" onclick="window.folioDel('quotes',${k})" aria-label="Remove">✕</button></div></div>`).join("")
    : `<div class="empty"><b>❝</b>Tap ❝ under any quote<br>to keep it here.</div>`}</div>`;
}
window.folioDel = (k, i) => { S[k].splice(i, 1); save(); render(); };
window.folioShare = async (id, i) => studio(await getBook(id), i);
async function desk() {
  const d = deskList();
  return `<div class="screen"><button class="icon" onclick="history.back()">←</button><div class="masthead"><h1>Reading Now</h1><p>The books open on your desk, and the ones you have finished.</p></div>
    ${d.length ? d.map((x) => `<button class="panel" style="display:block;width:100%;text-align:left;margin-top:10px" data-go="#/read/${x.id}/${x.done >= x.e.cards ? 0 : x.pos}">
      <div style="font:21px ${era(x.e.era).display},serif">${esc(x.e.title)}</div><div class="faded it" style="font-size:13px">${esc(x.e.author)}</div>
      <div class="bar"><i style="width:${(x.done / x.e.cards) * 100}%"></i></div><div class="faded" style="font-size:12px;margin-top:4px">${x.done >= x.e.cards ? `All ${x.e.cards} folios read ✓` : `${x.done} of ${x.e.cards} folios · about ${x.e.cards - x.done} min left`}</div></button>`).join("")
    : `<div class="empty"><b>❦</b>No book is open yet.<br>Pull one from the bookcase to begin.</div>`}</div>`;
}

/* ---------- Themes & scenarios ---------- */
async function quoteBox(ref, lead) {
  const [id, i] = ref.split("#"), b = await getBook(id).catch(() => null), c = b?.cards[+i];
  if (!c) return "";
  return `<button class="panel" style="display:block;width:100%;text-align:left;margin-top:10px" data-go="#/read/${id}/${i}">
    <div class="label">${esc(short(b))}</div>${lead ? `<div style="font-size:18px;margin-top:4px">${esc(lead)}</div>` : ""}
    <div class="it ${lead ? "faded" : ""}" style="font-family:${era(b.era).body};margin-top:6px">“${esc(c.quote)}”</div>
    ${!lead && c.qMean ? `<div class="faded" style="font-size:15px;margin-top:6px">${esc(c.qMean)}</div>` : ""}
    <div class="it" style="color:var(--rubric);font-size:12px;margin-top:6px">${esc(b.title)} · ${esc(c.ch)} — read the page ›</div></button>`;
}
async function themePage(id) {
  const t = (catalog.themes || []).find((x) => x.id === id); if (!t) return library();
  const rows = await Promise.all(t.cards.map((r) => quoteBox(r)));
  return `<div class="screen"><button class="icon" onclick="history.back()">←</button><div class="masthead"><h1>${esc(t.title)}</h1><p>${esc(t.blurb)}</p></div>${rows.join("")}</div>`;
}
async function scenePage(id) {
  const s = (catalog.scenarios || []).find((x) => x.id === id); if (!s) return library();
  const rows = await Promise.all(s.advice.map((a) => quoteBox(a.card, a.say)));
  return `<div class="screen"><button class="icon" onclick="history.back()">←</button><div class="masthead"><h1 style="font:32px/1.15 Fell,serif">${esc(s.title)}</h1><p>${esc(s.situation)}</p></div>
    <div class="label" style="margin-top:14px">Three old books advise</div>${rows.join("")}
    <p class="faded it" style="font-size:13px">Old advice for thinking with, not orders. You know your situation best.</p></div>`;
}

/* ---------- Honours & settings ---------- */
async function honours() {
  const lv = levelOf(S.xp), from = xpFor(lv), to = xpFor(lv + 1), fin = catalog.books.filter((b) => sealedIn(b.id).length >= b.cards && b.cards).length;
  const pill = (v, label) => `<button class="pill ${S.theme === v ? "on" : ""}" onclick="window.folioSet('theme',${v === null ? "null" : `'${v}'`})">${label}</button>`;
  return `<div class="screen"><div class="masthead"><h1>Honours</h1><p>Levels, seals and the rules of your reading.</p></div>
    <div class="center" style="margin-top:18px"><span class="seal" style="width:110px;height:110px;font-size:38px">${roman(lv)}</span>
      <div style="font:30px Fraktur,serif;margin-top:8px">${RANKS[lv - 1]}</div><div class="faded it">Level ${roman(lv)} · ${S.xp} xp</div>
      <div class="bar" style="max-width:280px;margin:8px auto"><i style="width:${lv >= RANKS.length ? 100 : ((S.xp - from) / (to - from)) * 100}%"></i></div></div>
    <div class="label" style="margin-top:22px">The ledger</div>
    <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:12px;text-align:center;margin-top:8px">${[[totalSealed(), "folios"], [fin, "books"], [streak(), "streak"], [S.lexicon.length, "words"], [S.quotes.length, "quotes"], [Object.keys(S.days).length, "reading days"]]
      .map(([n, l]) => `<div><div style="font-size:30px">${n}</div><div class="label faded" style="font-size:10px;color:var(--faded)">${l}</div></div>`).join("")}</div>
    <div class="label" style="margin-top:24px">Reading light</div><div class="row" style="margin-top:8px;flex-wrap:wrap">${pill(null, "System")}${pill("light", "Paper")}${pill("dark", "Lamplight")}</div>
    <div class="label" style="margin-top:20px">Text size</div><input type="range" min="0.85" max="1.4" step="0.05" value="${S.scale}" oninput="window.folioSet('scale',+this.value,true)">
    <div class="label" style="margin-top:12px">Line spacing</div><input type="range" min="1" max="1.6" step="0.05" value="${S.lines}" oninput="window.folioSet('lines',+this.value,true)">
    <div class="label" style="margin-top:12px">Daily quota: ${S.goal} folios</div><input type="range" min="5" max="30" step="1" value="${S.goal}" onchange="window.folioSet('goal',+this.value)">
    <div class="label" style="margin-top:22px">Folio everywhere</div>
    <p style="margin:6px 0">${isIOS ? (standalone ? "You're using Folio from your Home Screen. ✓" : "Install: tap <span class=\"share-glyph\"></span> Share, then <b>Add to Home Screen</b>.") : `On Android, the full app has widgets, reminders and Hindi meanings: <a href="${APK}">download Folio.apk</a>.`}</p>
    <p class="faded" style="font-size:14px">Your progress is saved on this device. Folio is free, with no ads and no tracking. <a href="https://github.com/purvalsingh/folio">github.com/purvalsingh/folio</a></p></div>`;
}
window.folioSet = (k, v, live) => { S[k] = v; save(); applyLook(); if (!live) render(); };

/* ---------- export (print to PDF, Markdown, Anki) ---------- */
async function printView() {
  const qs = S.quotes.map((q) => ({ ...q, title: entry(q.b)?.title || "" }));
  const md = `# My Folio notes\n_Exported ${today()}_\n\n## Commonplace book\n\n` + qs.map((q) => `> “${q.q}”\n>\n> — ${q.title}\n`).join("\n") +
    `\n## Lexicon\n\n` + S.lexicon.map((w) => `- **${w.w}**: ${w.m}${w.e ? ` _e.g. ${w.e}_` : ""}`).join("\n") + "\n";
  const anki = "#separator:tab\n#html:true\n#tags column:3\n" + S.lexicon.map((w) => `${w.w}\t${(w.m || "").replace(/\t/g, " ")}${w.e ? `<br><i>${w.e.replace(/\t/g, " ")}</i>` : ""}\tfolio::words`).join("\n");
  window.folioFile = (name, text, type) => download(new File([text], name, { type }));
  window._md = md; window._anki = anki;
  return `<div class="screen"><style>@media print{.noprint{display:none!important}nav.tabs{display:none}body{background:#fff}}</style>
    <div class="noprint"><button class="icon" onclick="history.back()">←</button><div class="label">Export your notes</div>
      <div class="row" style="flex-wrap:wrap;margin:10px 0 18px"><button class="pill on" onclick="print()">PDF (print → Save as PDF)</button>
      <button class="pill" onclick="folioFile('Folio-notes.md',_md,'text/markdown')">Markdown</button><button class="pill" onclick="folioFile('Folio-anki.txt',_anki,'text/plain')">Anki deck</button></div></div>
    <h1 style="font:34px Fraktur,serif;font-weight:normal;margin:0">My Folio notes</h1><div class="faded it">Exported ${today()}</div>
    <div class="label" style="margin-top:18px">Commonplace book · ${qs.length} quotes</div>${qs.map((q) => `<p><span class="it">“${esc(q.q)}”</span><br><small class="faded">— ${esc(q.title)}</small></p>`).join("")}
    <div class="label" style="margin-top:18px">Lexicon · ${S.lexicon.length} words</div>${S.lexicon.map((w) => `<p><b>${esc(w.w)}</b> — ${esc(w.m)}${w.e ? `<br><small class="faded it">e.g. ${esc(w.e)}</small>` : ""}</p>`).join("")}</div>`;
}

/* ---------- start ---------- */
if ("serviceWorker" in navigator) navigator.serviceWorker.register("sw.js").catch(() => {});
render();
