// Offline: the app shell is cached on install; books and pictures are kept once read, so Folio opens without a connection.
const SHELL = "folio-shell-v1", DATA = "folio-data-v1";
const FILES = ["./", "index.html", "app.css", "app.js", "share.js", "manifest.webmanifest", "icons/icon-192.png",
  "fonts/unifraktur.woff2", "fonts/fell_regular.woff2", "fonts/fell_italic.woff2", "fonts/fell_sc.woff2"];
self.addEventListener("install", (e) => { e.waitUntil(caches.open(SHELL).then((c) => c.addAll(FILES))); self.skipWaiting(); });
self.addEventListener("activate", (e) => { e.waitUntil(caches.keys().then((ks) => Promise.all(ks.filter((k) => ![SHELL, DATA].includes(k)).map((k) => caches.delete(k))))); self.clients.claim(); });
self.addEventListener("fetch", (e) => {
  const url = new URL(e.request.url);
  if (e.request.method !== "GET") return;
  const fresh = url.pathname.endsWith("catalog.json") || url.origin === location.origin;
  if (fresh) {  // network first, so updates arrive; cache as fallback offline
    e.respondWith(fetch(e.request).then((r) => { const copy = r.clone(); caches.open(url.origin === location.origin ? SHELL : DATA).then((c) => c.put(e.request, copy)); return r; })
      .catch(() => caches.match(e.request, { ignoreSearch: url.origin === location.origin })));
  } else if (url.hostname === "raw.githubusercontent.com") {  // books and plates: cache first
    e.respondWith(caches.match(e.request).then((hit) => hit || fetch(e.request).then((r) => { if (r.ok) { const copy = r.clone(); caches.open(DATA).then((c) => c.put(e.request, copy)); } return r; })));
  }
});
