const previews = [
  {title:'One page at a time', description:'A short, clear explanation opens each idea. Original artwork gives you a place and a moment to remember it by.', image:'/assets/reader.jpg', alt:'An illustrated reading page from The Prince'},
  {title:'A word makes sense', description:'Tap an unfamiliar word for a plain meaning and an everyday example. Keep the words you want to remember.', image:'/assets/word.jpg', alt:'Folio explaining a difficult word'},
  {title:'Take the idea with you', description:'Save a line that matters, revisit it later, or turn it into a shareable card.', image:'/assets/share.jpg', alt:'A quote composed in Folio Share Studio'}
];
let page = 0;
const image = document.querySelector('#stage-image');
const next = document.querySelector('#next-page');
const previous = document.querySelector('#prev-page');
let turning = false;
function showPage(index) {
  if (turning || index < 0 || index >= previews.length) return;
  turning = true;
  const stack = document.querySelector('.page-stack');
  stack.classList.add('turning');
  window.setTimeout(() => {
    page = index;
    const current = previews[page];
    image.src = current.image;
    image.alt = current.alt;
    document.querySelector('#stage-title').textContent = current.title;
    document.querySelector('#stage-description').textContent = current.description;
    document.querySelector('#stage-step').textContent = `0${page + 1} / 0${previews.length}`;
    document.querySelector('#stage-progress').style.width = `${(page + 1) / previews.length * 100}%`;
    previous.disabled = page === 0;
    next.disabled = page === previews.length - 1;
    stack.classList.remove('turning');
    turning = false;
  }, matchMedia('(prefers-reduced-motion: reduce)').matches ? 0 : 220);
}
next.addEventListener('click', () => showPage(page + 1));
previous.addEventListener('click', () => showPage(page - 1));
document.querySelector('#year').textContent = new Date().getFullYear();
let ticking = false;
addEventListener('scroll', () => {
  if (ticking) return;
  ticking = true;
  requestAnimationFrame(() => {
    const max = document.documentElement.scrollHeight - innerHeight;
    document.documentElement.style.setProperty('--read', `${max > 0 ? scrollY / max * 100 : 0}%`);
    ticking = false;
  });
}, {passive:true});
// GitHub's latest release API keeps the download current without a site redeploy.
// The static latest/download URL above remains a usable fallback if the API is unavailable.
fetch('https://api.github.com/repos/purvalsingh/folio/releases/latest', {headers:{Accept:'application/vnd.github+json'}})
  .then(response => { if (!response.ok) throw new Error(`GitHub ${response.status}`); return response.json(); })
  .then(release => {
    const apk = release.assets?.find(asset => asset.name === 'Folio.apk' || asset.name?.toLowerCase().endsWith('.apk'));
    if (apk?.browser_download_url) document.querySelectorAll('[data-apk]').forEach(link => { link.href = apk.browser_download_url; });
    if (release.tag_name) {
      document.querySelector('#release-version').textContent = `Latest: ${release.tag_name}`;
      document.querySelector('#download-version').textContent = `Latest Android release: ${release.tag_name}`;
    }
  }).catch(() => {});
fetch('https://raw.githubusercontent.com/purvalsingh/folio/main/library/catalog.json')
  .then(response => { if (!response.ok) throw new Error(`Catalog ${response.status}`); return response.json(); })
  .then(catalog => { if (Array.isArray(catalog.books)) document.querySelector('#book-count').textContent = `${catalog.books.length} books and growing`; })
  .catch(() => {});
