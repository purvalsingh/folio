"""The Prince, deeper edition: the original 52 pages plus new pages in every thin chapter, and a bridge
that opens each chapter by linking it to the one before, so the book reads as one argument.
Run: python3 prince_deep.py   (writes app/src/main/assets/books/prince.json, verifies every quote)"""
import json, pathlib
import prince_content as P
import meanings, folio_build

HERE = pathlib.Path(__file__).parent

# what each chapter picks up from the last one: shown at the top of its first page
BRIDGE = {
    "Chapter I": "The gift is offered. The book begins by sorting every kind of state, so we know which ones it will study.",
    "Chapter II": "Of all those kinds, the easiest to keep comes first: the throne you inherit.",
    "Chapter III": "Inherited thrones are easy. Now the hard case: adding new lands to the ones you already rule.",
    "Chapter IV": "Holding new land depends on how it was governed before. Here Machiavelli compares two very different kingdoms.",
    "Chapter V": "Some conquered lands were used to their own laws. Free cities are the hardest of all to hold.",
    "Chapter VI": "So far, states taken over. Now states founded new, by men who rose through their own ability.",
    "Chapter VII": "The opposite of the great founders: a man who rose through luck and other people's power, Cesare Borgia.",
    "Chapter VIII": "Neither talent nor luck: some climb to power by crime. What does that win, and what does it cost?",
    "Chapter IX": "From power seized by force to power given by fellow citizens: the civil principality.",
    "Chapter X": "Whatever the kind of state, can it defend itself? The test is whether it can stand alone in a siege.",
    "Chapter XI": "One kind of state breaks all the rules above: the states of the Church.",
    "Chapter XII": "The kinds of state are done. Machiavelli turns to what holds every one of them up: its soldiers.",
    "Chapter XIII": "Hired soldiers are bad. Borrowed armies, lent by a stronger ruler, turn out to be worse.",
    "Chapter XIV": "If you must have your own army, the prince's own first duty is clear: war.",
    "Chapter XV": "From armies to character. The famous turn: how a ruler should behave, as things really are.",
    "Chapter XVI": "The first quality on trial: generosity, the virtue everyone praises.",
    "Chapter XVII": "Generosity can ruin you. Next on trial: mercy and cruelty, and the old question of love or fear.",
    "Chapter XVIII": "If fear can be useful, what about lies? Should a prince keep his word?",
    "Chapter XIX": "All these qualities serve one goal: not to be hated or despised. This chapter gathers the threads.",
    "Chapter XX": "How to stay safe in practice: weapons, fortresses, and dividing your enemies.",
    "Chapter XXI": "Safety is not enough. How does a prince win real esteem?",
    "Chapter XXII": "A prince is judged by the people around him. First, the ministers he chooses.",
    "Chapter XXIII": "Good ministers bring a danger with them: the flatterers who crowd every court.",
    "Chapter XXIV": "The advice is complete. Machiavelli asks why Italy's princes lost their states anyway.",
    "Chapter XXV": "They blame fortune. How much of life does fortune really rule?",
    "Chapter XXVI": "The book ends where it was always going: a call to free Italy.",
}

CAPS = {
    "prince_53": "Pieter Bruegel the Elder, The Tower of Babel", "prince_54": "Quentin Massys, The Moneylender and his Wife, 1514",
    "prince_56": "Louis XII sets out for Genoa, from Marot's Voyage de Gênes", "prince_57": "Gentile Bellini, Sultan Mehmed II",
    "prince_58": "Giovanni Stradano, The Siege of Florence", "prince_59": "Moses with the tablets of the law",
    "prince_60": "Jan van Eyck, Saint Barbara, a tower being built, 1437", "prince_61": "A state banquet, woodcut from a Virgil printed at Lyons, 1517",
    "prince_62": "A festival in the Piazza della Signoria, Florence", "prince_63": "Merian, a fortified town in Franconia",
    "prince_64": "Pope Alexander VI", "prince_65": "Landsknecht and lady on horseback, 16th-century print",
    "prince_66": "Cesare Borgia, Duke of Romagna", "prince_67": "Albrecht Dürer, Knight, Death and the Devil, 1513",
    "prince_70": "John Cousen after Turner, Hannibal Crossing the Alps", "prince_71": "Hans Holbein workshop, two Swiss soldiers",
    "prince_72": "Rubens after Leonardo, The Battle of Anghiari", "prince_73": "Saint Eligius in his goldsmith's workshop",
    "prince_74": "Raphael, The School of Athens", "prince_75": "A king's council of war, from Der Weisskunig",
    "prince_76": "Castel Sant'Angelo, Rome",
}

# new pages: (insert after old page n, chapter, title, text, quote, plain meaning, everyday example, picture search)
NEW = [
 (3, "Chapter II", "Old habits protect the throne",
  "Why is an inherited throne so safe? Time does the work. After generations of the same family, people forget there was ever another way, and the reasons anyone might want change fade away. Machiavelli adds a warning: every change leaves a notch for the next one, like the toothed edge of a half-built wall waiting for new stones.",
  "one change always leaves the toothing for another",
  "Every change makes the next change easier.", "Once one friend quits the group, others start thinking about leaving too.",
  "Renaissance stone wall construction masons woodcut"),
 (5, "Chapter III", "The urge to grab more",
  "Machiavelli does not scold ambition. Wanting more land and power is normal, he says, and those who succeed are praised for it. What earns blame is trying when you cannot pull it off. The fault is not the wish; it is a wish without the means.",
  "The wish to acquire is in truth very natural and common, and men always do so when they can",
  "Wanting more is normal, and people take more whenever they can.", "A shop that can afford a second branch usually opens one.",
  "merchant counting money Renaissance engraving"),
 (5, "Chapter III", "Trouble only gets bigger",
  "The Romans watched for trouble far ahead and dealt with it while it was small. They never put off a war just to avoid it, because a delay only helps the other side. A wise ruler, Machiavelli says, attends to future problems as well as today's.",
  "war is not to be avoided, but is only to be put off to the advantage of others",
  "A fight you can't avoid only gets worse if you delay it.", "Ignoring a small leak in the roof just means a bigger repair bill in the monsoon.",
  "Roman legion marching engraving"),
 (5, "Chapter III", "The king's five mistakes",
  "Louis XII of France marched into Italy and lost it again. Machiavelli lists exactly why: he crushed the small powers who would have been his friends, made a strong power stronger, invited in a foreign rival, never went to live there, and never sent settlers. Five errors, every one of them avoidable.",
  "Therefore Louis made these five errors",
  "The king lost Italy through five avoidable mistakes.", "A startup that hires the wrong people, ignores customers and copies a rival makes several mistakes at once.",
  "Louis XII of France entering Genoa"),
 (7, "Chapter IV", "One master or many lords",
  "Machiavelli compares the Turk's empire, ruled by one master with everyone else his servants, with France, full of proud barons. The Turk is hard to conquer, since no one inside will help you, but easy to keep once his family is gone. France is easy to enter with a baron's help and hard to hold, because the barons stay powerful.",
  "The entire monarchy of the Turk is governed by one lord, the others are his servants",
  "In the Turk's empire one man rules and everyone else serves him.", "A company with one strong founder falls apart if the founder leaves; one run by many partners is harder to take over completely.",
  "Ottoman sultan with court engraving"),
 (8, "Chapter V", "Three ways to hold a free city",
  "A city used to its own laws leaves a conqueror three choices: destroy it, go and live there, or let it keep its laws under a few friendly rulers who pay tribute. The third is weakest, Machiavelli warns, because a free people never forgets its old freedom.",
  "the first is to ruin them, the next is to reside there in person, the third is to permit them to live under their own laws",
  "You can wreck a free city, move there yourself, or leave it its own laws.", "A new manager can scrap a team's way of working, sit with the team every day, or let them carry on with someone reporting back.",
  "Renaissance Italian city walls view woodcut"),
 (9, "Chapter VI", "Opportunity meets ability",
  "Moses, Cyrus, Romulus and Theseus got only one thing from luck: an opening. A people enslaved, a nation scattered, a city waiting to be founded. The opening was worthless without their talent, and their talent would have died unused without the opening.",
  "without that opportunity their powers of mind would have been extinguished",
  "Without the right chance, their great abilities would have come to nothing.", "A brilliant singer in a small town still needs the audition, and the audition needs the singer.",
  "Moses with the tablets engraving"),
 (13, "Chapter VII", "Build the foundations first",
  "Cesare Borgia got his state through his father the Pope, with other people's soldiers and luck. Machiavelli admires how hard he then worked to put down his own roots. Someone who skips the foundations can still lay them later, but only with great skill and at great risk to the whole building.",
  "he who has not first laid his foundations may be able with great ability to lay them afterwards",
  "If you skipped the groundwork, you can still do it later, but it is far harder.", "Learning grammar after years of speaking a language wrongly is possible, but painful.",
  "building foundations construction 16th century woodcut"),
 (15, "Chapter VIII", "The banquet at Fermo",
  "Oliverotto, raised by his uncle Giovanni, came home with a hundred horsemen as if to honour him. At a grand dinner he steered the talk to secret matters, led the guests into a private room, and had them killed. Within a year he ruled Fermo. Machiavelli tells it coldly, as power won by crime.",
  "No sooner were they seated than soldiers issued from secret places and slaughtered Giovanni and the rest.",
  "As soon as the guests sat down, hidden soldiers killed them.", "A betrayal planned under the cover of friendship is the hardest one to see coming.",
  "Renaissance banquet hall engraving"),
 (17, "Chapter IX", "Not built on mud",
  "An old proverb says that whoever builds on the people builds on mud. Machiavelli disagrees. It is true for a private citizen who expects the crowd to save him. But a prince who can command, keeps his nerve in bad times and keeps everyone's spirits up will not be let down by his people.",
  "he who builds on the people, builds on the mud",
  "The proverb says ordinary people make a weak foundation; Machiavelli says that's only true for the weak.", "A boss who earns the team's trust finds it holds when the company has a bad year.",
  "crowd of citizens in Italian piazza engraving"),
 (19, "Chapter X", "Hard targets are left alone",
  "The free cities of Germany obey the emperor only when they want to and fear no neighbour. Their walls are strong, their stores hold a year of food and fuel, and their people are kept busy with work. Attackers see how hard a siege would be and give up before they start.",
  "men are always adverse to enterprises where difficulties can be seen",
  "People avoid plans that obviously look hard.", "A house with a dog and bright lights is rarely the one burglars pick.",
  "Nuremberg city walls Merian engraving"),
 (20, "Chapter XI", "How the Church grew strong",
  "Not long ago, Machiavelli says, even small barons ignored the Pope's worldly power. Then Alexander VI showed what a pope could do with money and soldiers, using his son Cesare. Julius II built on it. The Church's state became one of the strongest in Italy.",
  "Alexander the Sixth arose afterwards, who of all the pontiffs that have ever been showed how a pope with both money and arms was able to prevail",
  "Pope Alexander VI proved that a pope with money and an army could win.", "An old institution can become powerful again under a leader who brings in money and muscle.",
  "Pope Alexander VI portrait"),
 (21, "Chapter XII", "Why hired soldiers fail",
  "Mercenaries are useless and dangerous. They are disunited, ambitious and unfaithful, brave among friends and cowardly before enemies. They have no love for you, only a small wage, and that wage is never enough to make them want to die for you.",
  "disunited, ambitious, and without discipline",
  "Hired soldiers don't stick together, chase their own gain and won't follow orders.", "A team of freelancers paid by the hour rarely fights for your company the way owners do.",
  "Landsknecht mercenaries woodcut"),
 (24, "Chapter XIII", "Master of your own forces",
  "Cesare Borgia tried French troops, then hired captains, and found both unreliable and dangerous. Only when he relied on his own soldiers did his reputation grow, and it kept growing. Nothing, Machiavelli says, raised him higher than everyone seeing that his army was truly his.",
  "he was never esteemed more highly than when every one saw that he was complete master of his own forces",
  "People respected him most once his army was fully his own.", "A founder who owns the company is taken more seriously than one at the investors' mercy.",
  "Cesare Borgia portrait engraving"),
 (25, "Chapter XIV", "Unarmed means despised",
  "A prince without his own arms is in danger from everyone, and worse, he is looked down on. No armed man willingly obeys an unarmed one, and an unarmed master is never safe among armed servants. Contempt, Machiavelli will argue later, is one of the two things a ruler must avoid most.",
  "among other evils which being unarmed brings you, it causes you to be despised",
  "Being defenceless makes people look down on you.", "A shop with no lock invites more than just thieves; it invites disrespect.",
  "knight in armour engraving Dürer"),
 (25, "Chapter XIV", "Learn from history",
  "War is not only practice in the field. The prince should exercise his mind too, reading histories and studying how great men won and lost, so he can copy the wins and avoid the defeats. Alexander copied Achilles, Caesar copied Alexander, Scipio copied Cyrus.",
  "the prince should read histories, and study there the actions of illustrious men",
  "A leader should study history and the lives of great people.", "Reading how others built businesses helps you avoid their mistakes in your own.",
  "Renaissance scholar reading in study engraving"),
 (27, "Chapter XV", "Avoid the fatal vices",
  "Nobody has every good quality, so a prince must be wise enough to avoid the vices that would cost him his state, and avoid the others if he can. Some things that look like virtues will ruin him, and some that look like vices will keep him safe.",
  "avoid the reproach of those vices which would lose him his state",
  "Above all, avoid the faults that would cost you your position.", "A manager can be moody, but cannot be caught stealing.",
  "allegory of vices and virtues engraving"),
 (31, "Chapter XVII", "Hannibal's iron army",
  "Hannibal led a huge army of many nations far from home, and it never mutinied, in good luck or bad. Machiavelli credits his cruelty as much as his courage: his soldiers feared and revered him. Writers who praise the army but blame the cruelty, he says, forget that one caused the other.",
  "inhuman cruelty, which, with his boundless valour, made him revered and terrible in the sight of his soldiers",
  "His harshness, together with his courage, made his soldiers respect and fear him.", "A strict coach whose team never falls apart may be strict for exactly that reason.",
  "Hannibal crossing the Alps engraving"),
 (40, "Chapter XX", "Arm your people",
  "A new prince never disarms his subjects. He arms them, because then their weapons become his and doubters turn into supporters. Disarm them and you insult them, showing you distrust them, and that breeds hatred.",
  "There never was a new prince who has disarmed his subjects",
  "No wise new ruler takes weapons away from his people.", "A new team lead who shares real responsibility wins loyalty faster than one who takes it away.",
  "citizen militia drilling 16th century woodcut"),
 (40, "Chapter XX", "Divided cities fall",
  "Some rulers kept their cities split into rival factions, thinking it made them easier to control. Machiavelli says it never works. When an enemy arrives, the weaker faction joins him and the city falls.",
  "when the enemy comes upon you in divided cities you are quickly lost",
  "A divided city falls quickly when an enemy attacks.", "A family that fights among itself is easy for outsiders to cheat.",
  "Guelphs and Ghibellines street fight"),
 (42, "Chapter XXI", "Reward talent",
  "A prince wins esteem by honouring skill in every art and letting people work and trade in peace, without fear their gains will be taxed away. He should offer prizes to anyone who improves the city, and entertain his people with festivals at the right seasons.",
  "A prince ought also to show himself a patron of ability",
  "A leader should support and reward talented people.", "A city that gives awards to its best teachers gets more good teachers.",
  "Renaissance artists workshop engraving"),
 (43, "Chapter XXII", "Three kinds of mind",
  "There are three kinds of mind: one that understands things by itself, one that understands when someone else explains, and one that understands neither way. The first is excellent, the second good, the third useless. A prince needs at least the second, to tell good ministers from bad.",
  "there are three classes of intellects",
  "People's minds come in three kinds.", "Some people invent, some can learn from a good teacher, and some won't learn either way.",
  "three scholars debating engraving"),
 (44, "Chapter XXIII", "Ask, then decide alone",
  "A wise prince chooses a few wise advisers and gives only them the freedom to tell him the truth, and only about what he asks. He questions them on everything, listens, then decides by himself and holds to it. A ruler who changes his mind with every opinion loses respect.",
  "liberty of speaking the truth to him",
  "Let a few trusted people speak honestly to you.", "A good captain asks the senior players for honest advice, then makes the call alone.",
  "king with counsellors at council table engraving"),
 (46, "Chapter XXIV", "Only your own defence counts",
  "The Italian princes who lost their states hoped the people would recall them after the invaders grew hated. Machiavelli scorns that hope. A rescue that does not depend on you is worth nothing. The only defences that are certain and lasting are your own.",
  "those only are reliable, certain, and durable that depend on yourself and your valour",
  "The only protection you can count on is the one you build yourself.", "Savings in your own account are safer than a friend's promise to lend you money someday.",
  "fortress on hill Italian Renaissance engraving"),
 (50, "Chapter XXVI", "A war that is just",
  "Machiavelli calls on the Medici to lead Italy against the foreign armies. He argues the cause is right: a war is just when it is necessary, and weapons are holy when they are the only hope left.",
  "that war is just which is necessary",
  "A war is right when there is truly no other choice.", "Defending your home from a break-in is different from starting a fight.",
  "Italian Renaissance army banners battle engraving"),
]


def build():
    old = [dict(c) for c in P.C]
    rows = meanings.M["prince"]
    assert len(rows) == len(old)
    for k, (c, (m, l)) in enumerate(zip(old, rows)):
        c.update(qMean=m, qLife=l, img=f"prince_{k + 1:02d}")
        c.pop("prompt", None)
    extra = {}
    for n, (after, ch, title, text, quote, m, l, q) in enumerate(NEW):
        cht = next(c["chTitle"] for c in old if c["ch"] == ch)
        extra.setdefault(after, []).append(dict(ch=ch, chTitle=cht, title=title, text=text, quote=quote, qMean=m, qLife=l,
                                                img=f"prince_{53 + n:02d}"))
    cards = []
    for k, c in enumerate(old):
        cards.append(c)
        cards += extra.get(k, [])
    seen = set()
    for c in cards:
        if c["ch"] not in seen:
            seen.add(c["ch"])
            if c["ch"] in BRIDGE:
                c["link"] = BRIDGE[c["ch"]]
    credits = json.loads((HERE / "commons/credits.json").read_text())
    bad = []
    for c in cards:
        if not folio_build.found(c["quote"], "prince"):
            bad.append(c["quote"])
        c["orig"] = folio_build.original(c["quote"], "prince")
        c["origFrom"] = folio_build.SOURCES["prince"][1]
        cr = credits.get(c["img"])
        c["cap"] = (CAPS[c["img"]] + " · public domain") if c["img"] in CAPS else (P.CAP[c["img"]] + " · public domain") if c["img"] in P.CAP else P.caption(cr["file"]) if cr else "Engraving made for Folio"
    gl = {}
    for w, (m, e, forms) in P.G.items():
        gl[w] = {"m": m, "e": e}
        for f in forms:
            gl[f] = {"m": m, "e": e, "root": w}
    import re
    alltext = " ".join(c["text"] + " " + c["quote"] for c in cards).lower()
    gl = {w: v for w, v in gl.items() if re.search(r"\b" + re.escape(w.lower()) + r"\b", alltext)}
    about = dict(P.ABOUT, version=5)
    out = dict(about, glossary=gl, cards=cards)
    dst = HERE.parent / "app/src/main/assets/books/prince.json"
    dst.write_text(json.dumps(out, ensure_ascii=False, indent=1))
    print(f"prince: {len(cards)} cards ({len(NEW)} new), {len(BRIDGE)} chapter bridges")
    assert not bad, bad
    folio_build.depth("prince", cards, "prince")
    return cards


if __name__ == "__main__":
    build()
