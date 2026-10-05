#!/usr/bin/env bash
# One-command release. Phones running Folio see the update the next time they open the app.
#   ./release.sh books                 # publish new/corrected books only (no APK)
#   ./release.sh 1.2 "What's new"      # new app version + books
set -euo pipefail
cd "$(dirname "$0")"
export JAVA_HOME="$HOME/.local/jdk-21"

if [ "${1:-}" = "books" ]; then
  python3 tools/publish_library.py
  git add library && git commit -m "Library: update books" && git push
  exit 0
fi

NAME=${1:?usage: ./release.sh <versionName> "notes"  |  ./release.sh books}
NOTES=${2:-}
code=$(grep -oP 'versionCode = \K[0-9]+' app/build.gradle.kts); code=$((code + 1))
sed -i "s/versionCode = [0-9]*/versionCode = $code/; s/versionName = \"[^\"]*\"/versionName = \"$NAME\"/" app/build.gradle.kts
./gradlew -q assembleRelease
mkdir -p release && cp app/build/outputs/apk/release/app-release.apk release/Folio.apk
git add -A && git commit -m "Folio $NAME" && git push
gh release create "v$NAME" release/Folio.apk --target main --title "Folio $NAME" --notes "${NOTES:-Folio $NAME}"
# announce only after the APK is downloadable
python3 tools/publish_library.py --app "$code" "$NAME" release/Folio.apk "$NOTES"
git add library && git commit -m "Library: announce Folio $NAME" && git push
echo "Released Folio $NAME (versionCode $code)"
