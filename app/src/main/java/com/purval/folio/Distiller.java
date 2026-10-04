package com.purval.folio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Offline flashcard maker for imported books: no network, no model.
 * Picks the sentences that carry the most of a passage's vocabulary, the most quotable line,
 * and the words a modern reader is least likely to know. It is extractive — it keeps the
 * author's own words rather than inventing a paraphrase. (With a Gemini key the importer
 * writes true plain-English summaries instead.)
 */
public final class Distiller {
    private Distiller() {}

    public static final class Passage {
        public final int firstPage;
        public final String chapter;
        public final String text;
        Passage(int firstPage, String chapter, String text) { this.firstPage = firstPage; this.chapter = chapter; this.text = text; }
    }

    public static final class Leaf {
        public String title, summary, quote;
        public List<String> words = new ArrayList<>();
    }

    private static final Set<String> STOP = new HashSet<>(Arrays.asList((
        "a an the and or but if of to in on at by for with from as is are was were be been being it its this that these those " +
        "he she they them his her their him we us our you your i me my not no nor so than then there here which who whom whose " +
        "what when where why how all any each every some such one two may might must shall should will would can could do does did " +
        "have has had also very more most much many own same into upon unto out up down over under again further once only other " +
        "therefore thus hence yet because while though although therein thereof thereby whereby being made make thing things man men " +
        "always never need great well little others himself itself themselves without against according whilst like just even still " +
        "said says shall first second last good know known come came take taken give given long able").split(" ")));

    private static final Pattern CHAPTER = Pattern.compile(
        "(?m)^\\s*((?:CHAPTER|Chapter|BOOK|Book|PART|Part|CANTO|Canto)\\s+(?:[IVXLCDM]+|\\d+)\\b\\.?)");
    private static final Pattern SENT = Pattern.compile("[^.!?]+[.!?]+[\"'’”)]*");
    private static final Pattern WORD = Pattern.compile("[A-Za-z][a-z]+");
    private static final String[] APHORISM = {"always", "never", "must", "ought", "whoever", "he who", "better", "nothing", "everyone", "every man"};

    /** Group pages into passages of roughly targetWords, starting a new passage at each chapter heading. */
    public static List<Passage> passages(List<String> pages, int targetWords) {
        List<Passage> out = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        int first = 0, words = 0;
        String chapter = "Opening", bufChapter = chapter;
        for (int p = 0; p < pages.size(); p++) {
            String page = pages.get(p);
            Matcher m = CHAPTER.matcher(page);
            if (m.find()) {
                if (words > 40) out.add(new Passage(first, bufChapter, buf.toString()));
                if (words > 40 || buf.length() == 0) { buf.setLength(0); words = 0; first = p; }
                chapter = m.group(1).replaceAll("\\s+", " ").replaceAll("\\.$", "");
                bufChapter = chapter;
            }
            if (buf.length() == 0) { first = p; bufChapter = chapter; }
            buf.append(page).append('\n');
            words += countWords(page);
            if (words >= targetWords) {
                out.add(new Passage(first, bufChapter, buf.toString()));
                buf.setLength(0); words = 0;
            }
        }
        if (words > 40) out.add(new Passage(first, bufChapter, buf.toString()));
        return out;
    }

    public static Leaf distil(String passage, Set<String> common) {
        String text = clean(passage);
        List<String> sents = sentences(text);
        Map<String, Integer> tf = new HashMap<>();
        for (String s : sents) for (String w : content(s)) tf.merge(w, 1, Integer::sum);

        double[] score = new double[sents.size()];
        for (int i = 0; i < sents.size(); i++) {
            List<String> ws = content(sents.get(i));
            double sum = 0;
            for (String w : ws) sum += tf.get(w);
            int n = countWords(sents.get(i));
            score[i] = n < 6 ? 0 : sum / Math.sqrt(n);
        }

        Leaf leaf = new Leaf();
        // quote: medium length, aphoristic, high score
        int qi = -1; double best = -1;
        for (int i = 0; i < sents.size(); i++) {
            int n = countWords(sents.get(i));
            if (n < 8 || n > 40) continue;
            String low = sents.get(i).toLowerCase(Locale.ROOT);
            double s = score[i];
            for (String a : APHORISM) if (low.contains(a)) s *= 1.35;
            if (s > best) { best = s; qi = i; }
        }
        leaf.quote = qi >= 0 ? sents.get(qi).trim() : "";

        // summary: top sentences by score (excluding the quote), in reading order, ~80 words
        Integer[] order = new Integer[sents.size()];
        for (int i = 0; i < order.length; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> Double.compare(score[b], score[a]));
        List<Integer> pick = new ArrayList<>();
        int total = 0;
        for (int i : order) {
            if (i == qi || score[i] == 0) continue;
            int n = countWords(sents.get(i));
            if (n > 55) continue;
            if (total + n > 90 && !pick.isEmpty()) continue;
            pick.add(i); total += n;
            if (total >= 60) break;
        }
        Collections.sort(pick);
        StringBuilder sb = new StringBuilder();
        for (int i : pick) sb.append(sents.get(i).trim()).append(' ');
        leaf.summary = sb.toString().trim();
        if (leaf.summary.isEmpty()) leaf.summary = text.length() > 420 ? text.substring(0, 420) + "…" : text;

        // title: "On X and Y" from the two heaviest content words
        List<Map.Entry<String, Integer>> top = new ArrayList<>(tf.entrySet());
        top.removeIf(e -> e.getKey().length() < 5 || e.getKey().endsWith("ly"));
        top.sort((a, b) -> b.getValue() - a.getValue());
        if (top.size() >= 2) leaf.title = "On " + cap(top.get(0).getKey()) + " and " + cap(top.get(1).getKey());
        else if (top.size() == 1) leaf.title = "On " + cap(top.get(0).getKey());
        else leaf.title = "A Passage";

        leaf.words = hardWords(text, common, 4);
        return leaf;
    }

    /** Words outside the 10,000 most common, skipping names (capitalised mid-sentence). */
    public static List<String> hardWords(String text, Set<String> common, int max) {
        Map<String, Integer> seen = new HashMap<>();
        Matcher m = WORD.matcher(text);
        while (m.find()) {
            String raw = m.group();
            if (Character.isUpperCase(raw.charAt(0))) continue;
            String w = raw.toLowerCase(Locale.ROOT);
            if (w.length() < 7 || STOP.contains(w) || known(w, common)) continue;
            seen.merge(w, 1, Integer::sum);
        }
        List<String> ws = new ArrayList<>(seen.keySet());
        ws.sort((a, b) -> b.length() - a.length());
        return new ArrayList<>(new LinkedHashSet<>(ws.subList(0, Math.min(max, ws.size()))));
    }

    static boolean known(String w, Set<String> common) {
        if (common.contains(w)) return true;
        String[] suffixes = {"s", "es", "ed", "d", "ing", "ly", "er", "est", "ness", "ment", "ion"};
        for (String s : suffixes) {
            if (w.endsWith(s) && w.length() - s.length() >= 3) {
                String stem = w.substring(0, w.length() - s.length());
                if (common.contains(stem) || common.contains(stem + "e")) return true;
            }
        }
        return false;
    }

    public static String clean(String s) {
        return s.replaceAll("-\\s*\\n\\s*", "")      // re-join hyphenated line breaks
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static List<String> sentences(String text) {
        List<String> out = new ArrayList<>();
        Matcher m = SENT.matcher(text);
        while (m.find()) {
            String s = m.group().trim();
            // a passage can start mid-sentence (page break): skip that dangling tail
            if (out.isEmpty() && m.start() == 0 && !s.isEmpty() && Character.isLowerCase(s.charAt(0))) continue;
            if (!s.isEmpty()) out.add(s);
        }
        return out;
    }

    static List<String> content(String s) {
        List<String> out = new ArrayList<>();
        Matcher m = WORD.matcher(s);
        while (m.find()) {
            String w = m.group().toLowerCase(Locale.ROOT);
            if (w.length() > 2 && !STOP.contains(w)) out.add(w);
        }
        return out;
    }

    static int countWords(String s) {
        int n = 0; boolean in = false;
        for (int i = 0; i < s.length(); i++) {
            boolean ws = Character.isWhitespace(s.charAt(i));
            if (!ws && !in) n++;
            in = !ws;
        }
        return n;
    }

    static String cap(String w) { return Character.toUpperCase(w.charAt(0)) + w.substring(1); }
}
