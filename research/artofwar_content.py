"""The Art of War flashcards. Run: python3 artofwar_content.py"""
from folio_build import build

G = {
 "stratagem": ("A clever trick or plan to fool an opponent.", "Her stratagem: tell the kids bedtime was early so they'd settle by the real time.", ["stratagems"]),
 "deception": ("Making someone believe something that is not true.", "The 'closing down sale' that never ends is a deception.", []),
 "sovereign": ("The highest ruler of a state.", "The sovereign approved the new law with a signature.", ["sovereigns"]),
 "commander": ("The person in charge of an army or group.", "The team captain acted as commander during the trek.", []),
 "provisions": ("Supplies of food and other needs, kept for later.", "We packed provisions for the long train journey.", []),
 "protracted": ("Lasting much longer than expected or wanted.", "The protracted meeting ran two hours over time.", []),
 "foreknowledge": ("Knowing something before it happens.", "With foreknowledge of the traffic jam, she took the metro.", []),
 "choleric": ("Easily made angry; bad-tempered.", "The choleric customer shouted over a cold coffee.", []),
 "manoeuvres": ("Skilful moves made to gain an advantage.", "The parking manoeuvres took five tries.", ["manoeuvre", "manœuvres", "manœuvre"]),
 "terrain": ("The land and its features — hills, rivers, roads.", "Mountain bikes are made for rough terrain.", []),
 "ambush": ("A surprise attack from a hidden place.", "Her little brother waited behind the door to ambush her with a water gun.", ["ambushes"]),
 "impregnable": ("Impossible to capture or break into.", "His password was long enough to be impregnable.", []),
 "arrogant": ("Thinking you are better than others.", "The arrogant player ignored the coach and lost the ball.", ["arrogance"]),
 "feign": ("To pretend.", "He feigned sleep when asked to do the dishes.", ["feigned"]),
 "spies": ("People who secretly gather information about others.", "Companies sometimes use 'mystery shoppers' — friendly spies — to check their stores.", ["spy", "spying"]),
 "vanguard": ("The front part of an army; the leaders of a movement.", "The vanguard of the parade carried the big banner.", []),
 "discipline": ("Training people to follow rules and order.", "It takes discipline to study every evening.", []),
 "morale": ("The confidence and spirit of a group.", "A surprise pizza lunch lifted the team's morale.", []),
 "plunder": ("To steal goods by force, especially in war.", "Pirates would plunder ships for gold.", []),
 "rapidity": ("Great speed.", "The rapidity of the delivery surprised her — it came in an hour.", []),
 "torrent": ("A fast, strong rush of water.", "After the storm, the street became a torrent.", []),
 "impenetrable": ("Impossible to get through or to understand.", "The jungle was impenetrable without a guide.", []),
 "desperate": ("Willing to do anything because the situation is so bad.", "The desperate student wrote the essay in one night.", []),
 "adversary": ("An opponent or enemy.", "In chess, always think about your adversary's next move.", ["adversaries"]),
 "converted": ("Changed to the other side.", "The double agent was converted to work for the other country.", []),
 "kindle": ("To start a fire, or start a feeling.", "Kindle the fire with dry twigs first.", []),
 "spleen": ("Bad temper or anger (old use).", "He vented his spleen on the referee.", []),
 "pique": ("Irritation from hurt pride.", "She left the party in a fit of pique.", []),
 "enlightened": ("Wise and well-informed.", "An enlightened boss listens before deciding.", []),
 "formless": ("Without a fixed shape.", "Water is formless — it takes the shape of its cup.", []),
}

C = []
def card(ch, cht, title, text, quote, src="sunzi"):
    C.append(dict(ch=ch, chTitle=cht, title=title, text=text, quote=quote, src=src))

card("Chapter I", "Laying Plans", "Life or death",
 "Sun Tzŭ opens plainly: war is the most serious thing a state does. It decides who lives and who falls. So before any fight, a wise sovereign compares five things on both sides — the Moral Law (do people believe in their leader?), Heaven (weather, seasons), Earth (terrain, distance), the commander, and discipline. Whoever wins the comparison usually wins the war.",
 "The art of war is of vital importance to the State.")
card("Chapter I", "Laying Plans", "All war is deception",
 "The most famous line of the book. Hide your strength and show weakness. When near, seem far; when far, seem near. Hold out bait to tempt the enemy, feign disorder, then strike. If your opponent is choleric, irritate him. If he is arrogant, make him prouder. Attack where he is not ready.",
 "All warfare is based on deception.")

card("Chapter II", "Waging War", "Make it quick",
 "War burns money. Armies need provisions, chariots, armour and pay, a thousand ounces of silver a day. A protracted campaign drains the treasury and tires the soldiers, and then neighbours take advantage. Sun Tzŭ prefers a fast, rough victory over a long, clever one.",
 "In war, then, let your great object be victory, not lengthy campaigns.")
card("Chapter II", "Waging War", "Live off the enemy",
 "Carrying food from home across long distances ruins your own people. A wise general feeds his army from the enemy's stores — one cartload taken from them is worth twenty of your own. Treat captured soldiers kindly and use them; this way you grow stronger with every win.",
 "There is no instance of a country having benefited from prolonged warfare.")

card("Chapter III", "Attack by Stratagem", "Win without fighting",
 "The best victory keeps the enemy's country whole. Smashing it is second best. Fighting and winning a hundred battles is not the height of skill. The highest skill is to break the enemy's will and plans so completely that no battle is needed at all.",
 "supreme excellence consists in breaking the enemy's resistance without fighting.")
card("Chapter III", "Attack by Stratagem", "Know the enemy, know yourself",
 "Sun Tzŭ ranks the ways to win: first, spoil the enemy's plans; then break his alliances; then fight his army; last and worst, besiege his walled cities. And the key to all of it is knowledge — of the other side and of yourself.",
 "If you know the enemy and know yourself, you need not fear the result of a hundred battles.")

card("Chapter IV", "Tactical Dispositions", "First, become unbeatable",
 "Good fighters of old first made themselves impossible to defeat, then waited for the enemy to make a mistake. Your own safety is in your hands; the chance to win is given by the enemy. So you can know how to win without being able to force it.",
 "The good fighters of old first put themselves beyond the possibility of defeat, and then waited for an opportunity of defeating the enemy.")
card("Chapter IV", "Tactical Dispositions", "Win before the battle",
 "A true master wins so easily that nobody calls him brave or brilliant — the victory looks obvious. That is because the win was set up before the fight began. The loser does the opposite: he fights first and hopes to find a way to win along the way.",
 "the victorious strategist only seeks battle after the victory has been won")

card("Chapter V", "Energy", "Direct and indirect",
 "There are only two kinds of attack: the direct (the obvious, head-on force) and the indirect (the surprise). Yet, like five musical notes making endless melodies, these two combine into endless manoeuvres. Use the direct to engage, the indirect to win.",
 "In battle, there are not more than two methods of attack—the direct and the indirect; yet these two in combination give rise to an endless series of manœuvers.")
card("Chapter V", "Energy", "Rolling stones",
 "Energy is like a crossbow drawn tight; timing is the release. A skilled general does not demand too much from each soldier — he puts the whole army in a position where momentum does the work, like round stones rolling down a steep mountain.",
 "the onset of troops is like the rush of a torrent which will even roll stones along in its course.")

card("Chapter VI", "Weak Points and Strong", "Be first in the field",
 "Whoever arrives first and waits is fresh; whoever rushes in late is tired. A clever fighter makes the enemy come to him, on his terms. Tempt him with an advantage, or block him with a threat — but always set the stage yourself.",
 "the clever combatant imposes his will on the enemy, but does not allow the enemy's will to be imposed on him.")
card("Chapter VI", "Weak Points and Strong", "Be like water",
 "Water flows away from high places and rushes into low ones. An army should do the same: avoid what is strong, strike what is weak. Water has no fixed shape — it follows the ground. In war, too, there are no fixed conditions; adapt to the enemy and you seem almost divine.",
 "Military tactics are like unto water; for water in its natural course runs away from high places and hastens downwards.")

card("Chapter VII", "Manœuvering", "Wind, forest, fire, mountain",
 "Manœuvering is the hardest part of war: turning the long way round into the short way. Sun Tzŭ's motto for movement: swift as the wind, compact as the forest, raiding like fire, still as a mountain. And keep your plans hidden until the moment you strike.",
 "Let your plans be dark and impenetrable as night, and when you move, fall like a thunderbolt.")
card("Chapter VII", "Manœuvering", "Leave an escape route",
 "Don't attack troops eager to fight, or chase an army pretending to flee. And a classic warning: when you surround an enemy, leave a way out. Men with no exit become desperate — and a desperate foe fights to the death.",
 "When you surround an army, leave an outlet free.")

card("Chapter VIII", "Variation of Tactics", "Not every road",
 "A general must bend rules to circumstances. Some roads should not be taken, some towns not attacked, some orders from the sovereign not obeyed. Wise leaders weigh both gain and harm in every choice — hope in danger, danger in hope.",
 "There are roads which must not be followed")
card("Chapter VIII", "Variation of Tactics", "Five fatal faults",
 "Sun Tzŭ lists five faults that ruin a general: recklessness, which leads to destruction; cowardice, which leads to capture; a hasty temper, easily provoked by insults; a pride in honour that is easily shamed; and too much worry for his men, which leaves him exhausted.",
 "Recklessness, which leads to destruction")

card("Chapter IX", "The Army on the March", "Read the signs",
 "This chapter is a field guide to reading the enemy. Birds rising mean an ambush. Dust high and narrow means chariots; low and wide means infantry. Soldiers leaning on spears are hungry. Polite words with hidden preparations mean an attack is coming.",
 "Humble words and increased preparations are signs that the enemy is about to advance.")
card("Chapter IX", "The Army on the March", "Discipline with kindness",
 "Soldiers must first be bonded to you by kindness; only then will punishment work. Punish them before they are attached to you and they will not obey; never punish once they are, and they become useless. Treat them with humanity, but keep them in control with iron discipline.",
 "Soldiers must be treated in the first instance with humanity, but kept under control by means of iron discipline.")

card("Chapter X", "Terrain", "Know the ground",
 "Sun Tzŭ names six kinds of terrain — open, entangling, temporising, narrow passes, steep heights and distant positions — each with its own rules. A general who knows the terrain, the enemy and himself has a complete picture of victory.",
 "If you know the enemy and know yourself, your victory will not stand in doubt")
card("Chapter X", "Terrain", "Soldiers as children",
 "A leader who cares for his soldiers like his own children will be followed into the deepest valleys. But kindness without authority spoils them, like indulged children who are useless when it matters.",
 "Regard your soldiers as your children, and they will follow you into the deepest valleys")

card("Chapter XI", "The Nine Situations", "No way back",
 "Deep in enemy land, soldiers with no road home fight with full morale and unity. Sun Tzŭ even advises placing troops where escape is impossible — then they stop fearing and fight as one, like the snake that strikes back with head and tail.",
 "Throw your soldiers into positions whence there is no escape, and they will prefer death to flight.")
card("Chapter XI", "The Nine Situations", "Seize what they love",
 "Speed is the essence of war: reach the enemy before he is ready, go by unexpected routes, hit where he is unguarded. And the shortcut to control: grab the thing your opponent values most, and he will follow your lead.",
 "Begin by seizing something which your opponent holds dear; then he will be amenable to your will.")

card("Chapter XII", "The Attack by Fire", "Fire and patience",
 "Sun Tzŭ explains five ways to attack with fire — burning soldiers, stores, baggage, arsenals, and supply lines — and the right winds and dry seasons for them. But the chapter turns into a warning: never move unless there is real advantage.",
 "Move not unless you see an advantage; use not your troops unless there is something to be gained; fight not unless the position is critical.")
card("Chapter XII", "The Attack by Fire", "Never fight from anger",
 "Anger fades and happiness returns, but a ruined kingdom stays ruined and the dead stay dead. So the enlightened ruler is careful, and the good general cautious. A war started from pique or spleen is a war already half lost.",
 "No ruler should put troops into the field merely to gratify his own spleen; no general should fight a battle simply out of pique.")

card("Chapter XIII", "The Use of Spies", "Foreknowledge",
 "Armies cost fortunes and years. It is foolish to save a little money on information and lose everything. Foreknowledge cannot come from spirits or guesses — only from people who know the enemy's situation.",
 "what enables the wise sovereign and the good general to strike and conquer, and achieve things beyond the reach of ordinary men, is foreknowledge.")
card("Chapter XIII", "The Use of Spies", "Five kinds of spies",
 "Local spies, inward spies (officials of the enemy), converted spies (the enemy's own spies, turned), doomed spies (fed false news on purpose), and surviving spies (who bring news back). The converted spy matters most: through him you learn how to use all the others.",
 "Hence the use of spies, of whom there are five classes")

ABOUT = {
 "version": 4, "id": "artofwar", "title": "The Art of War", "short": "Sun Tzu", "author": "Sun Tzu", "year": "c. 500 BC",
 "translator": "Lionel Giles, 1910 (public domain, Project Gutenberg #132)", "era": "eastern", "shelf": "Power & Strategy", "cover": "artofwar_cover",
 "self_src": "sunzi",
 "blurb": "Thirteen short chapters from ancient China on winning — ideally without a fight. Read by generals, CEOs and chess players for 2,500 years.",
}

if __name__ == "__main__":
    build(ABOUT, G, C, credits_file="commons/credits.json")
