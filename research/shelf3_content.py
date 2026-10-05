"""The Human Mind shelf: psychology classics + a digest of Jung's Red Book. Run: python3 shelf3_content.py
The Red Book (Liber Novus, published 2009) is in copyright: its cards are original summaries, and every
quote comes from Jung's public-domain 1916 English editions."""
import folio_build
from folio_build import build, SOURCES
from shelf2_content import C

SOURCES.update({
    "james": ("james.txt", "William James, Talks to Teachers on Psychology (1899)"),
    "freud": ("freud.txt", "Sigmund Freud, Dream Psychology (tr. Eder, 1920)"),
    "darwin": ("darwin.txt", "Charles Darwin, The Expression of the Emotions (1872)"),
    "jung": ("jung.txt", "C. G. Jung, Psychology of the Unconscious (tr. Hinkle, 1916)"),
    "jungpapers": ("jungpapers.txt", "C. G. Jung, Collected Papers on Analytical Psychology (1916)"),
})
SHELF = "The Human Mind"
folio_build.CAPS.update({
 "habit_03": "Spinning wheel, engraving", "habit_04": "Pocket watch, 1905", "dreams_02": "Van Gogh, Child with Orange",
 "dreams_06": "Freud's couch, Freud Museum, London", "emotions_01": "Duchenne de Boulogne's experiments on facial expression",
 "emotions_06": "Mother and child, 19th-century painting", "jung_01": "Poussin, Joseph Interprets Pharaoh's Dream",
 "jung_02": "Geological map showing rock strata", "redbook_02": "Mutus Liber (the Silent Book), 1677",
 "redbook_06": "Four Mandalas, Ngor Monastery, Tibet", "redbook_cover": "Rosarium Philosophorum, alchemical woodcut",
})
BOOKS = []

BOOKS.append((dict(id="habit", title="Talks on Habit", short="William James", author="William James", year="1899",
    translator="From Talks to Teachers on Psychology (public domain, Project Gutenberg #16287)", era="victorian", shelf=SHELF,
    blurb="The father of American psychology on why we are 'walking bundles of habits' — and four rules for building better ones."),
 {"plastic": ("Easy to shape or change.", "A child's mind is plastic — it learns languages fast.", []),
  "habit": ("Something you do regularly, often without thinking.", "Brushing your teeth before bed is a habit.", ["habits"]),
  "lapse": ("A small slip or failure.", "One lapse in your diet doesn't ruin the week.", []),
  "resolution": ("A firm decision to do something.", "Her New Year resolution was to read daily.", []),
  "gratuitous": ("Done without need or reason.", "A gratuitous cold shower builds willpower.", []),
  "dereliction": ("Failure to do your duty.", "Skipping the night shift was a dereliction.", [])},
 C("james", [
  ("Chapter VIII", "The Laws of Habit", "Walking bundles of habits",
   "William James's warning to young people: you are setting into habits right now. Every small choice repeated becomes who you are, so pay attention while you are still 'plastic' enough to change.",
   "how soon they will become mere walking bundles of habits",
   "We quickly become little more than our habits.", "Your morning phone-check routine runs on autopilot before you're even awake."),
  ("Chapter VIII", "The Laws of Habit", "Second nature",
   "Habit is a second nature — the Duke of Wellington called it ten times nature. By adulthood, our trained habits have replaced most of our natural impulses.",
   "Habit is thus a second nature",
   "Habits become as natural to us as instinct.", "After years of driving, you change gear without thinking."),
  ("Chapter VIII", "The Laws of Habit", "No exceptions",
   "James's second rule for a new habit: never allow an exception until it's firmly rooted. One slip is like dropping a ball of string you're winding — it undoes many turns.",
   "Never suffer an exception to occur till the new habit is securely rooted in your life",
   "Don't skip even once until a new habit is solid.", "Not missing a single morning walk in the first month."),
  ("Chapter VIII", "The Laws of Habit", "Act at once",
   "Resolutions don't build habits; actions do. Act on a good intention the very first chance you get, or the feeling fades and leaves you weaker.",
   "Seize the very first possible opportunity to act on every resolution you make",
   "Act on a good decision immediately, before the feeling fades.", "Signing up for the course the moment you decide, not 'next week'."),
  ("Chapter VIII", "The Laws of Habit", "It's all being counted",
   "A character in a play keeps drinking, saying 'I won't count this time!' James replies: you may not count it, but your nerves and brain are counting every time.",
   "We are spinning our own fates, good or evil, and never to be undone.",
   "Every small choice adds up to the life we end up with.", "'Just one more episode' every night adds up to months of lost sleep."),
  ("Chapter VIII", "The Laws of Habit", "Practise being heroic",
   "Do something difficult every day for no reason except that it's difficult. Then, when a real test comes, you'll be ready — like buying insurance.",
   "Keep the faculty of effort alive in you by a little gratuitous exercise every day.",
   "Do one hard thing daily just to keep your willpower strong.", "Taking the stairs instead of the lift, even when you're tired."),
 ])))

BOOKS.append((dict(id="dreams", title="Dream Psychology", short="Sigmund Freud", author="Sigmund Freud", year="1920",
    translator="M. D. Eder, 1920 (public domain, Project Gutenberg #15489)", era="germanic", shelf=SHELF,
    blurb="Freud's own short guide to his most famous idea: dreams are not nonsense — they are wishes in disguise."),
 {"manifest": ("Clearly shown; visible on the surface.", "The manifest problem was a flat tyre; the real one was no spare.", []),
  "latent": ("Hidden; present but not yet visible.", "Her latent talent for drawing appeared at forty.", []),
  "repression": ("Pushing painful thoughts out of awareness.", "Repression made him 'forget' the embarrassing day.", []),
  "censorship": ("Blocking what is not allowed to be seen or said.", "The film was cut by censorship.", []),
  "condensation": ("Squeezing many things into one.", "In the dream, condensation made her teacher look like her aunt.", [])},
 C("freud", [
  ("Chapter I", "Dreams Have a Meaning", "What you see and what it means",
   "Freud splits every dream in two: what you remember (the manifest content) and the hidden thoughts behind it (the latent content). Interpretation means going from the first to the second.",
   "the former I call the dream's manifest content; the latter, without at first further subdivision, its latent content.",
   "A dream has a surface story and a hidden meaning.", "Dreaming of missing a train might really be about fearing a missed chance."),
  ("Chapter III", "Why the Dream Disguises", "Children's dreams are simple",
   "Small children's dreams are plain: they wanted something during the day and got it in the dream. Adult dreams are harder because the wish is disguised.",
   "They are simply and undisguisedly realizations of wishes.",
   "Children's dreams openly give them what they wanted.", "A kid denied ice cream dreams of an ice-cream mountain."),
  ("Chapter II", "The Dream-Work", "Many in one",
   "Dreams compress: one dream person can combine several real people, one place several places. Freud calls this condensation.",
   "the more deeply you go into the analysis, the more deeply you are impressed by it.",
   "The more you study a dream, the more you see how much is packed into it.", "A dream house that is both your school and your grandmother's home."),
  ("Chapter IV", "The Censor", "The inner censor",
   "Between the unconscious and awareness, Freud imagines a censor. It lets through only what is acceptable and pushes the rest back — and that is why dreams come in code.",
   "a censorship is established which only passes what pleases it, keeping back everything else.",
   "Your mind blocks thoughts it finds unacceptable.", "Laughing off a remark that actually hurt you."),
  ("Chapter IV", "Repression", "Pushed out of mind",
   "What the censor rejects stays repressed — not gone, just kept out of awareness. Dreams are one of the few ways it slips back.",
   "I call this particular condition \"Repression.\"",
   "Freud named this pushing-away of thoughts 'repression'.", "Not being able to remember a painful exam result at all."),
  ("Chapter V", "Forgetting Dreams", "The part you forget matters",
   "When part of a dream vanishes as you wake, Freud says that missing piece is usually the most important clue — forgotten exactly because it was too revealing.",
   "This fragment so forgotten invariably contains the best and readiest approach to an understanding of the dream.",
   "The part of a dream you forget is often the key to it.", "Remembering everything about a dream except who was in the car."),
 ])))

BOOKS.append((dict(id="emotions", title="The Expression of the Emotions", short="Charles Darwin", author="Charles Darwin", year="1872",
    translator="Original English text (public domain, Project Gutenberg #1227)", era="victorian", shelf=SHELF,
    blurb="Darwin studies smiles, sneers, tears and blushes in people and animals — the first science of body language."),
 {"blush": ("To go red in the face from shyness or shame.", "She blushed when everyone sang to her.", ["blushing"]),
  "gestures": ("Movements of the hands or body that show meaning.", "He talks with big gestures.", []),
  "repression": ("Holding back or hiding a feeling.", "Repression of anger only makes it simmer.", []),
  "intensifies": ("Makes stronger.", "Shouting intensifies an argument.", []),
  "passion": ("Strong emotion, especially anger or love.", "In a fit of passion he slammed the door.", [])},
 C("darwin", [
  ("Chapter XIII", "Blushing", "The most human expression",
   "Of all expressions, blushing is the most uniquely human. It comes from thinking about what others think of us.",
   "Blushing is the most peculiar and the most human of all expressions.",
   "Blushing is the most unusual and human way we show feelings.", "Going red when the class turns to look at you."),
  ("Chapter XIII", "Blushing", "No animal blushes",
   "Monkeys go red with anger, but Darwin doubts any animal blushes from shame. It needs self-awareness.",
   "Monkeys redden from passion, but it would require an overwhelming amount of evidence to make us believe that any animal could blush.",
   "Animals can redden with anger, but probably never blush.", "Your dog looks guilty, but it isn't blushing."),
  ("Chapter XIV", "Concluding Remarks", "Showing a feeling grows it",
   "Letting an emotion show strengthens it; holding back its outward signs calms it.",
   "The free expression by outward signs of an emotion intensifies it.",
   "Acting out a feeling makes it stronger.", "Pacing and shouting when angry makes you angrier."),
  ("Chapter XIV", "Concluding Remarks", "Rage feeds on gestures",
   "Whoever gives in to violent gestures increases their rage. Control the signs and you control the feeling.",
   "He who gives way to violent gestures will increase his rage",
   "Wild angry movements make anger worse.", "Taking a slow breath instead of banging the table."),
  ("Chapter XIV", "Concluding Remarks", "Faces matter",
   "Our facial expressions aren't trivial — they help us survive and connect, whatever their origin.",
   "The movements of expression in the face and body, whatever their origin may have been, are in themselves of much importance for our welfare.",
   "Facial expressions are important for our wellbeing.", "A smile from a stranger can change a bad day."),
  ("Chapter XIV", "Concluding Remarks", "The first language",
   "Long before words, a mother and her baby talk with faces: she smiles approval, frowns warning.",
   "They serve as the first means of communication between the mother and her infant",
   "Facial expressions are how mothers and babies first talk.", "A baby smiling back at its mother before it can speak."),
 ])))

BOOKS.append((dict(id="jung", title="Psychology of the Unconscious", short="C. G. Jung", author="Carl Gustav Jung", year="1912",
    translator="Beatrice M. Hinkle, 1916 (public domain, Project Gutenberg #65903)", era="germanic", shelf=SHELF,
    blurb="The book that split Jung from Freud: myths, dreams and symbols as the deep, shared language of the human mind."),
 {"unconscious": ("The part of the mind we are not aware of.", "Her unconscious fear of dogs showed in her dreams.", []),
  "persona": ("The public 'mask' or role we show the world.", "His office persona is cheerful; at home he is quiet.", []),
  "strata": ("Layers, one on top of another.", "The cliff shows strata of rock from different ages.", ["stratum"]),
  "antiquity": ("The ancient past.", "The coin dates from antiquity.", []),
  "collective": ("Shared by a whole group.", "The festival is a collective memory of the town.", [])},
 C("jung", [
  ("Part I", "Two Kinds of Thinking", "Dreams are ancient",
   "Jung starts with history: humans have always believed dreams mean something, from Egypt to the Bible's Joseph. He wants to explain why.",
   "The dream interpretations of the Egyptians and Chaldeans, and the story of Joseph who interpreted Pharaoh's dreams, are known to every one",
   "People have interpreted dreams since ancient times.", "Grandparents who still ask 'what did you dream?' at breakfast."),
  ("Part I", "Two Kinds of Thinking", "Layers of the soul",
   "Jung proposes that the mind has layers, like rock. The deepest layer is the unconscious, and it is old — shared with our ancestors.",
   "the soul possesses in some degree historical strata, the oldest stratum of which would correspond to the unconscious.",
   "The mind has layers; the deepest, oldest one is the unconscious.", "A sudden fear of the dark that feels older than you."),
  ("Part I", "The Hero", "Myths speak for everyone",
   "A myth is not a record of real events; it carries a thought common to all humanity, told again in new form.",
   "It does not set forth any account of the old events, but rather acts in such a way that it always reveals a thought common to humanity, and once more rejuvenated.",
   "Myths express shared human ideas, renewed in every age.", "Superhero films retell the old hero myths for today."),
  ("Part II", "Symbols", "Sun father, moon mother",
   "The ancients saw the sun as a great father and the moon as a fruitful mother. Jung finds these images still alive in modern dreams.",
   "The naïve man of antiquity saw in the sun the great Father of the heaven and the earth, and in the moon the fruitful good Mother.",
   "Ancient people saw the sun as a father and the moon as a mother.", "Calling the moon 'Chanda Mama' in children's songs."),
  ("Essay", "The Persona", "The actor's mask",
   "Jung's word for the face we show society is persona — Latin for an actor's mask. We often mistake the mask for ourselves.",
   "The term persona is really an excellent one, for persona was originally the mask which an actor wore",
   "'Persona' comes from the actor's mask — the role we play in public.", "The cheerful 'work voice' you use on the phone."),
  ("Essay", "Dreams", "Everything is you",
   "In Jung's view, every person and object in a dream can be a part of the dreamer.",
   "The whole dream is the dreamer",
   "Every part of your dream represents part of you.", "Dreaming of a lost child may be about a neglected part of yourself."),
 ])))

for b in BOOKS[-1:]:
    for c in b[2][4:]:
        c["src"] = "jungpapers"

BOOKS.append((dict(id="redbook", title="The Red Book", short="C. G. Jung", author="Carl Gustav Jung", year="1914–1930",
    translator="A Folio digest. Jung's Liber Novus (published 2009) is in copyright: these are original summaries, with quotes from his public-domain writings of 1916.",
    era="germanic", shelf=SHELF,
    blurb="For sixteen years Jung recorded his own visions in a huge red-leather book, in calligraphy and paintings. Kept secret for a century, it is the map of his journey into the unconscious."),
 {"liber novus": ("Latin for 'The New Book' — Jung's title for the Red Book.", "", []),
  "visions": ("Images seen in the mind, as in a dream while awake.", "The artist painted her visions.", ["vision"]),
  "unconscious": ("The part of the mind we're not aware of.", "Habits run on the unconscious.", []),
  "individuation": ("Jung's word for becoming your whole, true self.", "Therapy helped her individuation — she stopped living others' plans.", []),
  "mandala": ("A circular design symbolising wholeness.", "She draws a mandala every morning to calm her mind.", ["mandalas"]),
  "persona": ("The public mask we show the world.", "Her online persona is very different from her real self.", []),
  "collective": ("Shared by everyone.", "Fairy tales live in our collective memory.", [])},
 C("jungpapers", [
  ("The Book", "Liber Novus", "A secret red book",
   "From about 1914 to 1930, Jung copied his inner visions into a large red-leather volume, in gothic calligraphy with his own paintings. He called it Liber Novus, 'The New Book', and kept it private; it was published only in 2009.",
   "we naturally dwell unconsciously in a world of werwolves, demons, magicians",
   "Deep down, our minds still live among old myths and monsters.", "Why a horror film can scare grown adults who 'know' it's fake."),
  ("1913", "Confrontation", "Letting the depths speak",
   "After his painful break with Freud in 1913, Jung chose to let his fantasies rise freely and wrote down what he saw — floods, blood, voices. He later called this his 'confrontation with the unconscious'.",
   "the soul possesses in some degree historical strata, the oldest stratum of which would correspond to the unconscious.",
   "The mind has layers; its deepest layer is the unconscious.", "Journalling late at night and surprising yourself with what comes out."),
  ("Liber Primus", "Spirit of the Depths", "The mask falls",
   "The book opens with Jung realising he had lived for success and the 'spirit of the time'. Another voice — the spirit of the depths — tells him to find his lost soul. The respectable public self, he decides, was only a mask.",
   "a mask which simulates individuality, making others and oneself believe that one is individual, whilst one is only acting a part through which the collective psyche speaks.",
   "The public mask makes us think we're being ourselves when we're just playing a role.", "Realising your 'dream job' was really your parents' dream."),
  ("Liber Primus", "The Murder of the Hero", "Killing the hero",
   "In one vision, Jung kills Siegfried, the shining hero. He reads it as the end of his own heroic ideal — the will to conquer everything — so that something deeper can live.",
   "It does not set forth any account of the old events, but rather acts in such a way that it always reveals a thought common to humanity, and once more rejuvenated.",
   "Myths express shared human ideas again and again.", "Letting go of 'I must win at everything' after burnout."),
  ("Liber Secundus", "Inner Teachers", "Gods and devils within",
   "Figures appear and teach him: the prophet Elijah, Salome, and above all Philemon, a wise old man with kingfisher wings. Jung treats them as real voices of the psyche — parts of himself he must listen to.",
   "We have just as much a part in gods and devils, saviours and criminals.",
   "We all carry both saint and villain inside us.", "Recognising the bully and the kind friend in yourself."),
  ("Later Years", "Mandalas", "Becoming whole",
   "In the later pages Jung paints circular mandalas. They became, for him, pictures of wholeness — the self uniting its opposites. This process he named individuation, the heart of his later psychology.",
   "leads to individuation beyond the type, and thereby to a new relation to the world and mind.",
   "Growing past your usual type leads to becoming your whole self.", "A logical person learning to trust feelings, and growing because of it."),
 ])))

for c in BOOKS[-1][2][1:2] + BOOKS[-1][2][3:4]:
    c["src"] = "jung"

if __name__ == "__main__":
    import pathlib
    out = pathlib.Path(__file__).parent.parent / "library-only/books"
    for about, gl, cards in BOOKS:
        own = {"habit": "james", "dreams": "freud", "emotions": "darwin", "jung": "jung", "redbook": "none"}[about["id"]]
        about = dict(about, version=2, self_src=own, cover=f"{about['id']}_cover")
        build(about, gl, cards, credits_file="commons/credits.json", out_dir=out)
