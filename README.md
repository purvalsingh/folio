# Folio

Old books, one page at a time. A native Android app (Kotlin + Jetpack Compose) that turns classic texts into short illustrated flashcards with tappable glossary words, a personal Lexicon, a Commonplace book of quotes, bookmarks, daily quotas, levels and milestone seals.

**Install:** download `Folio.apk` from the [latest release](https://github.com/purvalsingh/folio/releases/latest). After that, Folio updates itself: new app versions and new books appear inside the app.

Shelf: *The Prince* (Machiavelli), *The 48 Laws of Power* (a digest: original summaries with quotes from public-domain sources), *The Art of War* (Sun Tzŭ), *The Bhagavad Gita* (Arnold). You can also bind your own PDF into flashcards.

## Publishing
- `./release.sh books`: publish new or corrected books (bump the book's `version` first).
- `./release.sh 1.2 "What's new"`: build, sign and release a new app version. Needs the signing key in `~/.folio-signing/` (never committed).

Content sources and quote verification live in `research/` (`*_content.py`, `folio_build.py`).
