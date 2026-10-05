-- Folio sync table. Each user owns exactly one row holding their library,
-- encrypted on the phone (AES-256-GCM) before it ever reaches this database.
create table if not exists public.folio_sync (
  user_id    uuid primary key references auth.users (id) on delete cascade,
  blob       text not null check (length(blob) < 4000000),
  updated_at timestamptz not null default now()
);

alter table public.folio_sync enable row level security;

drop policy if exists "read own"   on public.folio_sync;
drop policy if exists "insert own" on public.folio_sync;
drop policy if exists "update own" on public.folio_sync;
drop policy if exists "delete own" on public.folio_sync;
create policy "read own"   on public.folio_sync for select using (auth.uid() = user_id);
create policy "insert own" on public.folio_sync for insert with check (auth.uid() = user_id);
create policy "update own" on public.folio_sync for update using (auth.uid() = user_id) with check (auth.uid() = user_id);
create policy "delete own" on public.folio_sync for delete using (auth.uid() = user_id);

revoke all on public.folio_sync from anon;
grant select, insert, update, delete on public.folio_sync to authenticated;
