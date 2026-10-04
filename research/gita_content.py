"""The Bhagavad Gita flashcards (Edwin Arnold's 1885 verse translation, Gutenberg #2388). Run: python3 gita_content.py"""
from folio_build import build

G = {
 "kurukshetra": ("The plain in north India where the great Mahabharata war was fought.", "Pilgrims still visit Kurukshetra in Haryana.", []),
 "pandavas": ("The five brothers, including Arjuna, on one side of the war.", "The Pandavas lost their kingdom in a rigged game of dice.", []),
 "kauravas": ("Their hundred cousins, led by Duryodhana, on the other side.", "The Kauravas refused to return the Pandavas' land.", []),
 "yoga": ("A path of discipline that joins the self with the divine; in the Gita, any steady way of living — through work, knowledge or devotion.", "For her, gardening every morning is a kind of yoga — calm, focused work.", ["yog", "yogi", "yogin", "yogins"]),
 "karma": ("Action — and the idea that every action has results.", "Karma yoga means doing your work well without obsessing over rewards.", []),
 "dharma": ("Your rightful duty; the right way to live for your role in life.", "A doctor's dharma is to treat the sick, even at 3 a.m.", []),
 "renouncer": ("Someone who gives up attachment to the rewards of action.", "A true renouncer can work hard without worrying about praise.", ["renouncing", "renunciation", "renounce"]),
 "equanimity": ("Calmness that stays steady in good times and bad.", "She heard the exam results with equanimity — no gloating, no despair.", []),
 "immortal": ("Never dying.", "Myths tell of gods who are immortal.", []),
 "indestructible": ("Impossible to destroy.", "The old steel trunk seemed indestructible.", []),
 "steadfast": ("Firm and unwavering.", "He stayed steadfast in his promise to visit every Sunday.", []),
 "maya": ("Illusion — the changing world that hides the deeper reality.", "Chasing every new gadget can feel like chasing maya.", []),
 "brahma": ("In the Gita, the infinite, ultimate reality (Brahman).", "Teachers describe Brahma as the one reality behind everything.", []),
 "sattwa": ("The quality of goodness, clarity and calm.", "A walk at dawn can bring a sattwa mood.", ["sattwan", "soothfastness"]),
 "rajas": ("The quality of passion, restlessness and desire.", "Doom-scrolling at midnight is pure rajas.", []),
 "tamas": ("The quality of darkness, laziness and ignorance.", "Sleeping till noon every day is tamas.", []),
 "piety": ("Deep religious devotion and good conduct.", "Her grandmother's piety showed in her daily prayers.", ["pious"]),
 "devotion": ("Loving dedication to someone or something.", "His devotion to his old dog was touching.", ["devotee"]),
 "sanjaya": ("The blind king's charioteer, given the gift of seeing the battle from afar and narrating it.", "Sanjaya is like a live sports commentator for the blind king.", []),
 "arjuna": ("The great archer-prince of the Pandavas, who doubts before the battle.", "Arjuna's doubt before the battle is the doubt we all feel before big choices.", ["arjun"]),
 "krishna": ("Arjuna's charioteer and friend, revealed as God, who teaches the Gita.", "Krishna answers Arjuna's questions like a patient mentor.", []),
 "calumny": ("False statements meant to damage someone's name.", "He ignored the calumny spread about him online.", []),
 "aswattha": ("The sacred fig (peepal) tree; in the Gita, an upside-down tree picturing the world.", "Many temples have an old aswattha tree in the courtyard.", []),
 "dejected": ("Sad and discouraged.", "He felt dejected after missing the train by a minute.", []),
 "succouring": ("Helping someone in trouble.", "Volunteers spent the night succouring flood victims.", []),
 "refuge": ("A place of safety and shelter.", "The library was her refuge from the noisy house.", []),
}

C = []
def card(ch, cht, title, text, quote):
    C.append(dict(ch=ch, chTitle=cht, title=title, text=text, quote=quote, src="gita"))

card("Chapter I", "The Distress of Arjuna", "Two armies, one doubt",
 "On the plain of Kurukshetra, two armies of one family face each other: the Pandavas and their cousins the Kauravas. The blind king Dhritarashtra asks his charioteer Sanjaya to describe the battle. Arjuna, the finest archer alive, asks Krishna, his charioteer, to drive between the armies so he can see who he must fight.",
 "Ranged thus for battle on the sacred plain— On Kurukshetra—say, Sanjaya! say What wrought my people, and the Pandavas?")
card("Chapter I", "The Distress of Arjuna", "The bow slips",
 "Arjuna sees teachers, uncles, cousins and friends on the other side. His limbs go weak, his mouth goes dry, his great bow slips from his hand. What victory is worth killing your own family? He sits down in the chariot and refuses to fight. This is where the Gita begins — with a man frozen by doubt.",
 "In pity lost, by doubtings tossed, My thoughts-distracted-turn To Thee, the Guide I reverence most, That I may counsel learn")

card("Chapter II", "The Book of Doctrines", "Grief without cause",
 "Krishna's first answer is almost a smile: you grieve for the wrong thing. The wise do not mourn the living or the dead, because the true self was never born and never dies. Bodies change like seasons; heat and cold, joy and pain come and go. Bear them steadily.",
 "Thou grievest where no grief should be!")
card("Chapter II", "The Book of Doctrines", "The spirit never dies",
 "This is one of the Gita's most famous ideas. The spirit is immortal and indestructible — weapons cannot cut it, fire cannot burn it. Death is like changing clothes: the spirit puts away a worn-out body and takes a new one.",
 "Never the spirit was born; the spirit shall cease to be never; Never was time it was not; End and Beginning are dreams!")
card("Chapter II", "The Book of Doctrines", "Work, not rewards",
 "The heart of karma yoga. You have a right to your work, but not to its results. Don't act only for the reward, and don't fall into doing nothing either. Do your duty well, let go of the outcome, and you find peace whether you succeed or fail.",
 "Let right deeds be Thy motive, not the fruit which comes from them.")
card("Chapter II", "The Book of Doctrines", "The steady mind",
 "Arjuna asks: what does a truly wise person look like? Krishna describes someone not crushed by sorrow nor carried away by joy, free of fear and anger. Like a tortoise pulling its limbs into its shell, such a person can draw the senses back from the world.",
 "As the wise tortoise draws its four feet safe Under its shield, his five frail senses back")

card("Chapter III", "Virtue in Work", "Better to act",
 "If knowledge is higher, Arjuna asks, why must I fight? Krishna says nobody can escape action — even keeping the body alive is work. The world itself runs on action. So act, but act without selfishness, as an offering. Great people should work, because others copy them.",
 "Work is more excellent than idleness")
card("Chapter III", "Virtue in Work", "Your own path",
 "One of the Gita's most practical lessons: your own duty, done imperfectly, is better than someone else's duty done well. Comparing your path with another's breeds confusion. Know your role and fulfil it.",
 "Finally, this is better, that one do His own task as he may, even though he fail, Than take tasks not his own, though they seem good.")

card("Chapter IV", "The Religion of Knowledge", "Whenever goodness fails",
 "Krishna reveals that he taught this wisdom long ago and has been born many times. Whenever righteousness declines and evil grows strong, he comes into the world to protect the good and restore dharma. Knowing this frees the devotee from fear.",
 "When Righteousness Declines, O Bharata! when Wickedness Is strong, I rise, from age to age, and take Visible shape, and move a man with men, Succouring the good, thrusting the evil back")
card("Chapter IV", "The Religion of Knowledge", "The fire of knowledge",
 "Every act can become an offering. Knowledge is the greatest offering of all: like a blazing fire that turns wood to ash, true knowledge burns away the binding force of past actions. Find a teacher, ask humbly, and serve.",
 "As the kindled flame Feeds on the fuel till it sinks to ash, So unto ash, Arjuna! unto nought The flame of Knowledge wastes works' dross away!")

card("Chapter V", "Renouncing the Fruit of Works", "Doing and letting go",
 "Is it better to give up action or to act without attachment? Krishna says both lead to the goal, but acting without attachment is easier and better. The real renouncer is not the one who quits the world, but the one who works, wanting nothing and rejecting nothing.",
 "That is the true Renouncer, firm and fixed, Who—seeking nought, rejecting nought—dwells proof Against the \"opposites.\"")

card("Chapter VI", "Self-Restraint", "Be your own friend",
 "We must lift ourselves up by our own effort, never drag ourselves down. The mind can be our best friend or our worst enemy. When we master ourselves, the self is a friend; when we don't, it acts like an enemy.",
 "Let each man raise The Self by Soul, not trample down his Self, Since Soul that is Self's friend may grow Self's foe.")
card("Chapter VI", "Self-Restraint", "A lamp out of the wind",
 "Krishna describes meditation: a quiet place, a steady posture, moderate eating and sleeping. Arjuna admits the mind is wild — as hard to hold as the wind. Krishna agrees, but says practice and detachment can tame it. Each time the mind wanders, gently bring it back.",
 "Steadfast a lamp burns sheltered from the wind; Such is the likeness of the Yogi's mind")

card("Chapter VII", "Discernment", "Few truly know",
 "Among thousands, hardly one seeks the truth, and among those who seek, hardly one truly knows. Krishna explains his two natures: a lower one — earth, water, fire, air, ether, mind, intellect and ego — and a higher one, the life that holds the universe together like pearls on a thread.",
 "Earth, water, flame, air, ether, life, and mind, And individuality—those eight Make up the showing of Me, Manifest.")

card("Chapter VIII", "Devotion to the One Supreme", "The last thought",
 "What we think of at the moment of death shapes where we go next, because the soul becomes like what it has dwelt on. So Krishna's advice is simple and brave: keep me in your heart always — and fight.",
 "Have Me, then, in thy heart always! and fight!")

card("Chapter IX", "The Kingly Knowledge", "A leaf, a flower",
 "God does not need grand rituals. Even the smallest offering — a leaf, a flower, a fruit, a little water — given with love is accepted. Whatever you eat, give or do, offer it to the divine, and work itself becomes worship.",
 "Whoso shall offer Me in faith and love A leaf, a flower, a fruit, water poured forth, That offering I accept, lovingly made With pious will.")

card("Chapter X", "Divine Glories", "The lamp of wisdom",
 "Krishna lists his glories: among lights he is the sun, among waters the ocean, among trees the aswattha, among weapons the thunderbolt. Wherever there is anything glorious or powerful, it is a spark of his splendour. And for those who love him, he lights the lamp of wisdom inside.",
 "And, with bright rays of wisdom's lamp, their ignorance dispel.")

card("Chapter XI", "The Vision of the Universal Form", "I am Time",
 "Arjuna asks to see Krishna's true form. He is given divine sight and sees a terrifying vision: a body with countless faces, brighter than a thousand suns, swallowing the warriors of both armies. Krishna declares he is Time itself — the fate of the battle is already decided.",
 "Thou seest Me as Time who kills, Time who brings all to doom")

card("Chapter XII", "Devotion", "Who is dear to God",
 "The path of loving devotion is the easiest. Krishna describes the devotee he loves: one who hates no living thing, is kind and compassionate, free from ego, steady in pleasure and pain, and the same to friend and foe.",
 "Who hateth nought Of all which lives, living himself benign, Compassionate, from arrogance exempt")

card("Chapter XIII", "Matter and Spirit", "The field and its knower",
 "The body is a 'field', and the soul is the 'knower of the field'. Krishna lists what true knowledge looks like in a person: humility, honesty, non-violence, patience, purity, self-control and freedom from ego.",
 "Humbleness, truthfulness, and harmlessness, Patience and honour, reverence for the wise.")

card("Chapter XIV", "The Three Qualities", "Three threads",
 "All nature is woven from three qualities: sattwa (clarity and goodness), rajas (passion and restlessness) and tamas (darkness and dullness). They bind the soul in different ways. The wise person watches them rise and fall and is not ruled by them.",
 "Sattwan, Rajas, and Tamas, so are named The qualities of Nature")

card("Chapter XV", "The Supreme Person", "The upside-down tree",
 "Krishna describes the world as an eternal aswattha tree with roots above and branches below; its leaves are the Vedas. Its branches spread through our desires and actions. With the axe of detachment, cut through it and seek the source.",
 "Which hath its boughs beneath, its roots above")

card("Chapter XVI", "Divine and Demonic Natures", "Two kinds of people",
 "The divine nature: fearlessness, purity, generosity, self-control, truthfulness, gentleness, patience and freedom from anger. The demonic nature: pride, arrogance, harshness and greed. The first leads to freedom; the second to bondage.",
 "Truthfulness, slowness unto wrath, a mind That lightly letteth go what others prize")

card("Chapter XVII", "Three Kinds of Faith", "You are what you worship",
 "Each person's faith matches their nature. The food we like, the way we give, the effort we make — all can be in sattwa, rajas or tamas. Even austerity can be pure, showy, or foolish.",
 "The faith of each believer, Indian Prince! Conforms itself to what he truly is.")

card("Chapter XVIII", "Deliverance", "Take refuge",
 "Krishna's final and most loving teaching: let go of every worry about rules and rituals, give your heart to him, and take refuge in him alone. Arjuna's doubts are gone. He picks up his bow, ready to do his duty.",
 "Make Me thy single refuge! I will free Thy soul from all its sins! Be of good cheer!")

ABOUT = {
 "id": "gita", "title": "The Bhagavad Gita", "short": "Bhagavad Gita", "author": "Vyasa (trad.)", "year": "c. 400 BC",
 "translator": "Sir Edwin Arnold, The Song Celestial, 1885 (public domain, Project Gutenberg #2388)",
 "era": "ancient", "cover": "gita_cover", "self_src": "gita",
 "blurb": "On a battlefield, a great archer loses his nerve, and his charioteer — God himself — talks him through duty, action, death and devotion. Eighteen chapters of the world's most loved spiritual dialogue.",
}

if __name__ == "__main__":
    build(ABOUT, G, C, credits_file="commons/credits.json")
