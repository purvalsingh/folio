#!/usr/bin/env bash
# Switch on Folio accounts for a Supabase project.
#   cloud/setup.sh <project-ref> <publishable-or-anon-key>
# 1) deploys a tiny Vercel rewrite (supabase.co is blocked on some Indian ISPs),
# 2) publishes the address + public key through the catalog, so phones enable sign-in without an update.
set -euo pipefail
cd "$(dirname "$0")"
REF=${1:?project ref, e.g. abcdxyzabcdxyz}; KEY=${2:?publishable/anon key}
cat > proxy/vercel.json <<JSON
{ "rewrites": [ { "source": "/sb/:path*", "destination": "https://$REF.supabase.co/:path*" } ] }
JSON
echo '<!doctype html><title>Folio sync</title><p>Folio sync relay.</p>' > proxy/index.html
cd proxy
npx -y vercel link --yes --project folio-reader-sync >/dev/null
rm -f .env*; printf '.env*\n' > .vercelignore
URL=$(npx -y vercel deploy --prod --yes 2>/dev/null | tail -1)
ALIAS=https://folio-reader-sync.vercel.app
BASE=${ALIAS:-$URL}/sb
cd ..
code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/auth/v1/health" -H "apikey: $KEY")
echo "relay $BASE -> health $code"
[ "$code" = 200 ] || { echo "relay check failed"; exit 1; }
printf '{"base": "%s", "anonKey": "%s"}\n' "$BASE" "$KEY" > public.json
cd .. && python3 tools/publish_library.py && git add cloud library && git commit -m "Accounts: switch on sync relay" && git push
echo "Accounts are live: phones pick this up on next launch."
