// Share cards drawn on a canvas: the same sizes, styles and looks as the Android app.
import { era, img } from "./app.js";

export const FORMATS = { STORY: { label: "Story", w: 1080, h: 1920 }, POST: { label: "Post", w: 1080, h: 1350 }, SQUARE: { label: "Square", w: 1080, h: 1080 } };
export const STYLES = { PLATE: { label: "Picture + quote" }, QUOTE: { label: "Quote only" }, PAGE: { label: "Full page" } };
export const LOOKS = { BOOK: { label: "Book" }, CHAPTER: { label: "Chapter" }, PAPER: { label: "Paper" }, NIGHT: { label: "Night" } };

const CREAM = "#F3E7CF", GILT = "#E2C98F";
const PAPER = ["#F6F1E6", "#1A1814", "#6A6357", "#8E1B12"], NIGHT = ["#15130F", "#EDE5D3", "#A39B8B", "#D0634B"];
const BOOK = {
  renaissance: ["#1E1A17", GILT, "#B9A57A", "#C4513D"], germanic: ["#1E1A17", GILT, "#B9A57A", "#C4513D"],
  indic: ["#7A2317", CREAM, "#E0C9A6", GILT], eastern: ["#2B2622", CREAM, "#B8AC98", "#C4513D"],
  baroque: ["#EDE3CF", "#1C1915", "#6A6357", "#9A2E20"], ancient: ["#D8CDB8", "#221E19", "#5E564A", "#7A3B22"],
  enlightenment: ["#2F3B45", CREAM, "#B3B9BC", GILT], victorian: ["#2E3B31", GILT, "#B7B49A", CREAM], modern: ["#4A423A", CREAM, "#C9BCA8", "#E7A96B"],
};
const CHAPTER = [["#5B1A14", CREAM, "#D9BFA8", GILT], ["#1F2A44", CREAM, "#AEB6C9", GILT], ["#243326", GILT, "#B5B79B", CREAM],
  ["#E9D9B4", "#2A1F14", "#6E5B40", "#8E1B12"], ["#3A3F44", CREAM, "#B5B9BC", "#E0A458"], ["#3E2438", CREAM, "#CDB5C6", GILT],
  ["#173B3A", CREAM, "#A9C2BE", "#E0A458"], ["#F1E6D2", "#1A1814", "#6A6357", "#1F4E79"]];
const ITALIC = { renaissance: "Fell", ancient: "Fell", indic: "Tiro", eastern: "Zen", baroque: "Primer", enlightenment: "Pica" };
const hash = (s) => [...s].reduce((a, c) => (a * 31 + c.charCodeAt(0)) | 0, 0);

function palette(b, i, look) {
  if (look === "PAPER") return PAPER;
  if (look === "NIGHT") return NIGHT;
  if (look === "BOOK") return BOOK[b.era] || BOOK.renaissance;
  return CHAPTER[((hash(b.id + "|" + b.cards[i].ch) % CHAPTER.length) + CHAPTER.length) % CHAPTER.length];
}
const light = (hex) => { const n = parseInt(hex.slice(1), 16); return ((n >> 16) * 299 + ((n >> 8) & 255) * 587 + (n & 255) * 114) / 1000 > 140; };

function lines(ctx, text, width) {
  const out = []; let line = "";
  for (const w of text.split(/\s+/)) {
    const t = line ? line + " " + w : w;
    if (ctx.measureText(t).width > width && line) { out.push(line); line = w; } else line = t;
  }
  if (line) out.push(line);
  return out;
}
function block(ctx, text, font, size, width, lh = 1.18) {
  ctx.font = font.replace("{s}", size); const ls = lines(ctx, text, width);
  return { font: font.replace("{s}", size), ls, lh: size * lh, h: ls.length * size * lh };
}
function fit(ctx, text, font, width, room, max, min) {
  for (let s = max; ; s -= 2) { const b = block(ctx, text, font, s, width); if (b.h <= room || s <= min) return b; }
}
function draw(ctx, b, cx, y, color) {
  ctx.font = b.font; ctx.fillStyle = color; ctx.textAlign = "center"; ctx.textBaseline = "top";
  b.ls.forEach((l, k) => ctx.fillText(l, cx, y + k * b.lh));
}
const loadImg = (src) => new Promise((res) => { if (!src) return res(null); const im = new Image(); im.crossOrigin = "anonymous";
  im.onload = () => res(im); im.onerror = () => res(null); im.src = src; });

export async function renderCard(canvas, book, i, format, style, look) {
  const F = FORMATS[format], card = book.cards[i], e = era(book.era), [bg, ink, faded, accent] = palette(book, i, look);
  const ital = ITALIC[book.era] || "OldStandard";
  await Promise.all([`32px FellSC`, `40px ${e.display}`, `italic 40px ${ital}`, `40px Fell`, `40px Fraktur`].map((f) => document.fonts.load(f).catch(() => {})));
  canvas.width = F.w; canvas.height = F.h;
  const c = canvas.getContext("2d"), w = F.w, h = F.h, story = format === "STORY";
  c.fillStyle = bg; c.fillRect(0, 0, w, h);
  c.strokeStyle = ink; c.globalAlpha = .8; c.lineWidth = 3; c.strokeRect(36, 36, w - 72, h - 72);
  c.globalAlpha = .45; c.lineWidth = 1.4; c.strokeRect(50, 50, w - 100, h - 100); c.globalAlpha = 1;

  const m = 110, cw = w - 2 * m, top = story ? 230 : 100, footer = story ? 300 : 150, gap = story ? 44 : 32;
  const label = block(c, `${book.title} · ${card.ch}`.toUpperCase(), `{s}px FellSC`, story ? 32 : 28, cw);
  const title = style === "PAGE" ? block(c, card.title, `{s}px ${e.display}`, story ? 70 : 58, cw) : null;
  const pic = style === "QUOTE" ? null : await loadImg(img(card.img || book.cover));
  let picH = pic ? Math.round(cw * (style === "PAGE" ? .56 : story ? .78 : format === "SQUARE" ? .48 : .62)) : 0, meanS = story ? 40 : 30;
  const orn = style === "QUOTE" ? block(c, "“", `{s}px ${e.display}`, story ? 260 : 200, cw, .7) : block(c, e.fleuron, `{s}px serif`, 46, cw);
  const attr = block(c, `— ${card.qBy || `${book.author}, ${book.title}`}`, `{s}px FellSC`, story ? 36 : 30, cw);
  const maxQ = { QUOTE: story ? 96 : 76, PLATE: story ? 78 : 58, PAGE: story ? 62 : 46 }[style], room = h - top - footer;
  let mean, fixed, q;
  // shrink the picture, then the plain-English line, until the whole page fits inside the frame
  for (;;) {
    mean = style === "PAGE" && card.qMean ? block(c, `In plain English: ${card.qMean}`, `{s}px Fell`, meanS, cw, 1.25) : null;
    const parts = [label.h, title?.h, pic ? picH : null, orn.h, mean?.h, attr.h].filter((x) => x != null);
    fixed = parts.reduce((a, x) => a + x, 0) + gap * parts.length;
    q = fit(c, `“${card.quote}”`, `italic {s}px ${ital}`, cw, room - fixed, maxQ, 26);
    if (fixed + q.h <= room) break;
    if (pic && picH > cw * .3) picH = Math.round(picH * .88);
    else if (mean && meanS > 22) meanS -= 2;
    else break;
  }
  let y = top + Math.max(0, (h - top - footer) - (fixed + q.h)) / 2;
  const put = (b, color) => { draw(c, b, w / 2, y, color); y += b.h + gap; };
  put(label, accent);
  if (title) put(title, ink);
  if (pic) {
    const r = cw / picH, iw = pic.naturalWidth, ih = pic.naturalHeight;
    const [sx, sy, sw, sh] = iw / ih > r ? [(iw - ih * r) / 2, 0, ih * r, ih] : [0, 0, iw, iw / r];
    c.save(); c.filter = "grayscale(1)"; if (!light(bg)) c.globalAlpha = .88;
    c.drawImage(pic, sx, sy, sw, sh, m, y, cw, picH); c.restore();
    c.strokeStyle = ink; c.globalAlpha = .75; c.lineWidth = 2; c.strokeRect(m, y, cw, picH); c.globalAlpha = 1;
    y += picH + gap;
  }
  if (style === "QUOTE") { draw(c, orn, w / 2, y - gap * .5, accent); y += orn.h * .55; } else put(orn, accent);
  put(q, ink);
  if (mean) put(mean, faded);
  put(attr, faded);
  const fy = h - footer + (story ? 90 : 0);
  c.textAlign = "center"; c.textBaseline = "alphabetic";
  c.font = "46px Fraktur"; c.fillStyle = accent; c.fillText("Folio", w / 2, fy + 40);
  c.font = "italic 22px Fell"; c.fillStyle = faded; c.fillText("old books, one page at a time · free on Android & iPhone", w / 2, fy + 78);
}
