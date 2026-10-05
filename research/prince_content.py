"""Source of truth for The Prince flashcards. Run: python3 prince_content.py
Writes ../app/src/main/assets/books/prince.json and verifies every quote is verbatim in prince.txt."""
import json, re, sys, pathlib

STYLE = ("16th-century woodcut engraving, black ink on aged off-white paper, fine cross-hatching, "
         "Renaissance Italy, monochrome, no text, no letters, no color, antique book plate, ")

G = {  # word -> (meaning, everyday example, [other forms])
 "principality": ("A country or state ruled by a prince.", "Monaco is a tiny principality on the coast of France.", ["principalities"]),
 "hereditary": ("Passed down from parent to child.", "The family shop is hereditary — Grandpa gave it to Dad, and Dad will give it to her.", []),
 "dominion": ("Land or people that someone controls.", "The cat acts like the whole sofa is her dominion.", ["dominions"]),
 "sovereign": ("The highest ruler, with power over everyone else.", "In the group project, Riya acted like the sovereign and gave everyone tasks.", ["sovereignty"]),
 "prudence": ("Careful good sense; thinking ahead before acting.", "Out of prudence, he saved some money before quitting his job.", ["prudent", "prudently"]),
 "usurper": ("Someone who takes power that is not rightfully theirs.", "The younger brother grabbed the TV remote like a usurper.", ["usurp", "usurped"]),
 "patrimony": ("Property or wealth inherited from your father or family.", "He sold the old farm, his only patrimony, to start a business.", []),
 "fortune": ("Luck or chance — the events you cannot control.", "By good fortune, the rain stopped just before the match.", []),
 "virtù": ("Machiavelli's word for skill, courage and drive — the ability to shape events.", "Starting a company from nothing in a recession takes real virtù.", []),
 "mercenaries": ("Soldiers who fight only for money, not loyalty.", "The club bought star players who acted like mercenaries and left once the pay stopped.", ["mercenary"]),
 "auxiliaries": ("Troops borrowed from another, stronger ruler.", "Calling your big cousin to win your fight is using auxiliaries — now you owe him.", ["auxiliary"]),
 "liberality": ("Generosity, especially giving or spending freely.", "His liberality with free pizza made him popular — until the money ran out.", ["liberal"]),
 "parsimony": ("Extreme carefulness with money; stinginess.", "Her parsimony meant she reused tea bags twice.", []),
 "clemency": ("Mercy; choosing to be gentle when you could punish.", "The teacher showed clemency and let him resubmit the late homework.", []),
 "contempt": ("The feeling that someone is worthless or not worth respecting.", "The team treated the lazy captain with contempt.", ["contemptible"]),
 "conspiracy": ("A secret plan by a group to do something harmful.", "The kids formed a conspiracy to hide the last cookie.", ["conspiracies", "conspirator", "conspirators"]),
 "flatterers": ("People who praise you too much, usually to get something.", "The new manager was surrounded by flatterers laughing at his bad jokes.", ["flattery", "flatterer"]),
 "feign": ("To pretend.", "He feigned a cough to skip the meeting.", ["feigned"]),
 "dissembler": ("Someone who hides their real feelings or plans.", "She was a good dissembler — smiling while planning to quit.", ["dissemble"]),
 "magnanimous": ("Generous and noble, especially toward a rival.", "The champion was magnanimous and praised the player she beat.", []),
 "ecclesiastical": ("Belonging to the Church or the clergy.", "The old monastery was an ecclesiastical building, run by monks.", []),
 "condottiere": ("A hired captain who led mercenary armies in Renaissance Italy.", "Think of a condottiere like a contractor general — hired, paid, and gone.", ["condottieri"]),
 "arbiter": ("The one who decides or judges the outcome.", "Mum was the final arbiter of who got the front seat.", []),
 "avarice": ("Extreme greed for money or things.", "His avarice showed when he counted the change twice.", ["avaricious"]),
 "ingratitude": ("Not being thankful for help you received.", "After all her help moving house, his ingratitude stung.", ["ungrateful"]),
 "rapacious": ("Greedy and grabbing, taking by force.", "The rapacious landlord raised rent every six months.", []),
 "vacillate": ("To keep changing your mind; to waver.", "She vacillated between pizza and pasta for ten minutes.", ["vacillating"]),
 "irresolute": ("Unable to decide; lacking firmness.", "The irresolute captain couldn't pick a strategy before halftime.", ["irresolution"]),
 "affable": ("Friendly and easy to talk to.", "The affable shopkeeper remembers everyone's name.", []),
 "barbarous": ("Savage, cruel or uncivilised.", "Tearing up someone's homework is barbarous.", ["barbarians"]),
 "emancipate": ("To set free from control.", "Getting his first salary helped him emancipate himself from his parents' rules.", ["emancipated"]),
 "rancour": ("Bitter, long-lasting resentment.", "Years later, there was still rancour over who broke the vase.", []),
 "posterity": ("All the people who will live in the future.", "She planted the oak tree for posterity.", []),
 "nobles": ("Rich, powerful families of high rank.", "In the office, the senior partners act like nobles with private parking.", []),
 "fortress": ("A strongly built castle or defended place.", "His bedroom, with the door locked and headphones on, was his fortress.", ["fortresses"]),
 "colonies": ("Settlements sent to live in and hold a conquered land.", "The café chain opened 'colonies' in every new mall to keep its hold.", []),
 "provident": ("Planning ahead for the future.", "A provident student starts the assignment the day it's set.", []),
 "inveterate": ("Long-established and hard to change; deep-rooted.", "He is an inveterate snoozer — five alarms, every day.", []),
 "malcontents": ("People who are unhappy and want change, often trouble-makers.", "The malcontents in the group chat kept complaining about the trip plan.", []),
 "neutrality": ("Not taking any side in a fight.", "Her neutrality in her friends' argument annoyed both of them.", ["neutral"]),
 "subjects": ("People who live under a ruler's authority.", "The king's subjects lined the streets to see him.", []),
 "pontiff": ("The Pope.", "The pontiff spoke to the crowd from the balcony.", ["pontificate"]),
 "dukedom": ("Land ruled by a duke.", "Borgia's dukedom was the Romagna region of Italy.", []),
 "perfidy": ("Breaking a promise or betraying trust.", "Copying his friend's idea and pitching it as his own was pure perfidy.", ["perfidious"]),
 "expedient": ("Useful for a goal, even if not fully right or fair.", "Lying about traffic was expedient, but not honest.", []),
 "ambition": ("A strong wish to gain power or success.", "Her ambition was to run her own bakery by thirty.", ["ambitious"]),
 "magistrates": ("Officials who govern or judge.", "The town magistrates decided where the new market would go.", []),
 "intrepid": ("Fearless and adventurous.", "The intrepid hiker went up the trail in the storm.", []),
 "impetuous": ("Acting quickly without thinking; rash.", "In an impetuous moment, he bought a guitar he can't play.", []),
 "circumspect": ("Very careful; considering all risks.", "Be circumspect about what you post online.", []),
 "redeemer": ("Someone who rescues or saves others.", "The new coach was seen as the redeemer of the losing team.", []),
 "oppression": ("Cruel or unfair use of power over people.", "The workers protested years of oppression by the factory owners.", []),
 "republic": ("A state ruled by elected citizens, not a monarch.", "India is a republic — it elects its leaders.", ["republics"]),
 "esteem": ("Respect and admiration.", "She was held in high esteem by her coworkers.", ["esteemed"]),
 "reputation": ("What people generally think of you.", "The restaurant's reputation for slow service spread fast.", []),
 "deceive": ("To make someone believe something false.", "The ad deceived buyers with a photo of a much bigger burger.", ["deceived", "deceit", "deceives"]),
 "artifices": ("Clever tricks used to fool people.", "The magician's artifices kept the kids guessing.", ["artifice"]),
 "despotic": ("Ruling with total, often cruel, power.", "The despotic hostel warden banned phones after 9 pm.", []),
 "plebeian": ("An ordinary common person, not noble.", "He called his plain T-shirt plebeian but loved it anyway.", []),
 "indignation": ("Anger at something unfair.", "She felt indignation when the queue-jumper got served first.", []),
}

C = []  # (chapter label, chapter title, card title, text, quote, image prompt)
def card(ch, cht, title, text, quote, prompt):
    C.append(dict(ch=ch, chTitle=cht, title=title, text=text, quote=quote, prompt=prompt))

card("Dedication", "To Lorenzo de' Medici", "A gift worth more than gold",
 "People usually win a prince's favour with horses, jewels and fine cloth. Machiavelli, out of a job and out of favour, has nothing like that. So he offers what he values most: everything he has learned about how great men rise and fall, boiled down into one small book. He says the people see the prince clearly, and the prince sees the people clearly — each from a distance.",
 "to understand the nature of the people it needs to be a prince, and to understand that of princes it needs to be of the people",
 "a humble scholar kneeling and offering a small leather book to a richly dressed Medici lord in a palace hall")
card("Dedication", "To Lorenzo de' Medici", "Looking up the mountain",
 "His image: a painter who wants to draw mountains stands low in the plain; one who wants to draw valleys climbs high. Machiavelli, a lowly man, claims his low position lets him see the shape of power that rulers, standing at the top, often miss. He hopes Lorenzo will read with care and one day notice the unfair blows of fortune he has suffered.",
 "those who draw landscapes place themselves below in the plain to contemplate the nature of the mountains",
 "an artist with easel in a valley sketching tall mountains with a castle on top, Tuscan hills")

card("Chapter I", "How Many Kinds of Principalities", "Two kinds of rule",
 "Every state that has dominion over men is either a republic or a principality. Principalities are either hereditary — passed down in an old ruling family — or new. New ones are either completely new, or are pieces added to a prince's existing lands. They are won either with your own arms or other people's, by fortune or by ability.",
 "All states, all powers, that have held and hold rule over men have been and are either republics or principalities.",
 "a map of Renaissance Italy split into city-states with tiny castles, a compass rose, decorative border")

card("Chapter II", "Hereditary Principalities", "The easy throne",
 "A prince who inherits his throne has it easy. The people are used to his family, so he only needs to keep the old customs and handle surprises calmly. Even if pushed out, he usually gets back in at the first mistake of the usurper. Long rule makes people forget why things ever changed.",
 "For the hereditary prince has less cause and less necessity to offend; hence it happens that he will be more loved",
 "an old king on a throne with his young son beside him, family coat of arms and ancestral portraits on the wall")

card("Chapter III", "Mixed Principalities", "New lands, old trouble",
 "Conquering a new land is the hard part. The people who helped you in, hoping life would improve, quickly feel let down. The malcontents you pushed out become enemies. So a new prince makes enemies of all he has hurt and can't fully satisfy his friends. If the new land shares your language and customs, it is far easier to hold.",
 "men change their rulers willingly, hoping to better themselves",
 "soldiers marching through an Italian town gate while townspeople watch suspiciously from windows")
card("Chapter III", "Mixed Principalities", "Crush or care — never half",
 "To hold a foreign land, the best move is to go and live there yourself, so you can spot trouble early. The next best is to send colonies — cheap, and they only hurt a few. Machiavelli's coldest rule appears here: either treat people well or crush them completely. A small injury leaves them able to take revenge; a total one does not.",
 "men ought either to be well treated or crushed, because they can avenge themselves of lighter injuries, of more serious ones they cannot",
 "a prince on horseback with settlers and wagons arriving to build houses in a conquered valley")
card("Chapter III", "Mixed Principalities", "Treat problems like a fever",
 "The Romans saw troubles from far away and fixed them early. Machiavelli compares this to a fever: at first easy to cure but hard to notice; later easy to notice but impossible to cure. A provident prince does not wait. He also warns: whoever makes another man powerful ruins himself, because that power was given with cleverness or force — both of which the new strongman distrusts.",
 "he who is the cause of another becoming powerful is ruined",
 "a Renaissance physician checking the pulse of a sick man in bed, with a Roman legion seen through the window")

card("Chapter IV", "Why Alexander's Kingdom Did Not Rebel", "Two ways to rule a country",
 "Some kingdoms are ruled by one prince with servants (like the Turk); others by a prince together with powerful nobles who have their own lands and followers (like France). The first type is hard to conquer but easy to keep. The second is easy to enter — some unhappy noble will always open the door — but very hard to hold.",
 "the principalities of which one has record are found to be governed in two different ways",
 "two scenes side by side: a sultan alone on a throne with bowing servants, and a French king surrounded by armoured nobles")

card("Chapter V", "Governing Cities That Had Their Own Laws", "The memory of liberty",
 "A city used to freedom never truly forgets it. You can let it keep its laws, set up a friendly small group to run it, or destroy it. Machiavelli says the safest way is ruin, because the name of liberty and its old ways will always be a rallying cry for rebellion — no matter how much time passes or what benefits you give.",
 "in republics there is more vitality, greater hatred, and more desire for vengeance, which will never permit them to allow the memory of their former liberty to rest",
 "citizens of a free Italian republic gathered in a town square around a bell tower, a banner flying")

card("Chapter VI", "New Principalities Won by One's Own Ability", "Walk in great footsteps",
 "A wise man should copy the greatest people, like an archer who aims higher than the target to hit it. Moses, Cyrus, Romulus and Theseus got from fortune only an opportunity; their own virtù did the rest. Those who rise by ability gain power with difficulty but keep it with ease.",
 "A wise man ought always to follow the paths beaten by great men, and to imitate those who have been supreme",
 "an archer aiming an arrow high into the sky above a distant target, classical columns in background")
card("Chapter VI", "New Principalities Won by One's Own Ability", "Armed prophets win",
 "Changing the system is the hardest job there is. Everyone who gained from the old order fights you; the people who would gain from the new order only half-heartedly help, because they doubt new things until they see them work. So a reformer needs force to back up persuasion — because people are easy to convince but hard to keep convinced.",
 "there is nothing more difficult to take in hand, more perilous to conduct, or more uncertain in its success, than to take the lead in the introduction of a new order of things",
 "a robed preacher in a crowded square with armoured guards beside him holding pikes")
card("Chapter VI", "New Principalities Won by One's Own Ability", "The fate of the unarmed",
 "His example is Savonarola, the friar who ruled Florence through sermons. When the crowd stopped believing, he had no way to make them keep believing, and he was destroyed. The lesson: ideas without power behind them do not last.",
 "all armed prophets have conquered, and the unarmed ones have been destroyed",
 "a friar preaching from a pulpit to an empty crowd as night falls, a lone candle")

card("Chapter VII", "Principalities Won by Fortune or Others' Arms", "Easy come, easy go",
 "Those who become princes through luck or someone else's money rise easily but struggle to stay. They depend on the goodwill of whoever raised them — two things that are very fickle. Like plants that grow too fast, they have no roots, and the first storm knocks them down.",
 "Those who solely by good fortune become princes from being private citizens have little trouble in rising, but much in keeping atop",
 "a young sapling with shallow roots being torn up by a storm wind, a castle behind")
card("Chapter VII", "Principalities Won by Fortune or Others' Arms", "Cesare Borgia's model",
 "Cesare Borgia got his dukedom through his father, Pope Alexander VI, yet worked hard to build his own roots. He won over the people of the Romagna by giving them a harsh but effective governor, Remirro de Orco, who restored order. Then, to shift the blame for that cruelty, Borgia had Remirro cut in two and left in the town square.",
 "The barbarity of this spectacle caused the people to be at once satisfied and dismayed",
 "Cesare Borgia in armour with a confident gaze, a Renaissance duke on horseback overlooking the Romagna")
card("Chapter VII", "Principalities Won by Fortune or Others' Arms", "Old wounds don't forget",
 "Borgia's one great mistake: he let Julius II become Pope — a man full of rancour against him. Machiavelli warns that great men do not forget old injuries just because you now do them favours. That choice ruined Borgia.",
 "He who believes that new benefits will cause great personages to forget old injuries is deceived",
 "cardinals in red robes gathering in a Vatican chamber to elect a new pope, smoke rising")

card("Chapter VIII", "Those Who Gained Power by Wickedness", "Power is not glory",
 "Agathocles rose from a potter's son to King of Syracuse by calling the senate and rich men to a meeting and having them all killed. He held power for years. Yet Machiavelli refuses to call that excellence: killing fellow citizens and betraying friends can win you an empire, but never glory.",
 "Yet it cannot be called talent to slay fellow-citizens, to deceive friends, to be without faith, without mercy, without religion; such methods may gain empire, but not glory.",
 "an ancient Greek tyrant standing among toppled senators' chairs in a marble hall, soldiers in shadow")
card("Chapter VIII", "Those Who Gained Power by Wickedness", "Cruelty used well and badly",
 "Machiavelli's chilling distinction: cruelty is 'well used' when done all at once for safety and then stopped, turning to the people's benefit. It is badly used when it starts small and grows. Hurt people all at once so they feel it less; give benefits slowly so they enjoy them more.",
 "injuries ought to be done all at one time, so that, being tasted less, they offend less; benefits ought to be given little by little, so that the flavour of them may last longer",
 "a scale balancing a sword on one side and a basket of bread on the other, chiaroscuro engraving")

card("Chapter IX", "The Civil Principality", "Climb with the people",
 "Some become prince through the favour of fellow citizens. In every city there are two moods: the people want freedom from oppression, and the nobles want to oppress them. A prince made by the nobles is surrounded by equals who won't obey. One raised by the people stands alone and is much safer, because the people only want not to be crushed.",
 "the nobles wish to rule and oppress the people",
 "a prince on a balcony waving to a cheering crowd of townspeople below, nobles frowning to the side")
card("Chapter IX", "The Civil Principality", "Keep the people friendly",
 "The people's wishes are more honest than the nobles', since the people only want not to be oppressed. A prince can never secure himself against a hostile people because there are too many of them. So even if the nobles made you, win over the people — it is cheap and essential in hard times.",
 "it is necessary for a prince to have the people friendly, otherwise he has no security in adversity",
 "farmers and craftsmen bringing grain to a castle gate where a prince greets them warmly")

card("Chapter X", "Measuring a Principality's Strength", "Walls, stores and goodwill",
 "Strong princes can raise an army and fight anyone. Weaker ones must hide behind walls. Then they need good fortifications, a year's supply of food and fuel, and a people who are not hostile. Machiavelli notes the German free cities: well stocked, well walled, and nobody dares attack them.",
 "a prince who has a strong city, and had not made himself odious, will not be attacked",
 "a walled German city with thick towers, granaries and townsfolk carrying firewood inside")

card("Chapter XI", "Ecclesiastical Principalities", "Held by heaven",
 "Ecclesiastical states, run by the Church, are won by ability or luck but kept without either, because ancient religious customs hold them up. These princes have lands they do not defend and subjects they do not govern — yet no one takes them away. Machiavelli says it would be presumptuous to discuss them, then explains how the pontiff Alexander VI and Julius II made the Church a great temporal power.",
 "they are acquired either by capacity or good fortune, and they can be held without either",
 "Saint Peter's basilica dome under construction with a pope's procession in front")

card("Chapter XII", "Kinds of Soldiery and Mercenaries", "Good laws need good arms",
 "Every state rests on two foundations: good laws and good arms. You cannot have good laws without good arms, and where there are good arms, good laws follow. Mercenaries are useless and dangerous: disunited, ambitious, disloyal, brave among friends and cowards in front of enemies.",
 "The chief foundations of all states, new as well as old or composite, are good laws and good arms",
 "a group of hired soldiers counting gold coins at a table while their banner lies forgotten on the floor")
card("Chapter XII", "Kinds of Soldiery and Mercenaries", "Paid to avoid battle",
 "Mercenaries have no reason to stay in a fight except a little pay — not enough to make them want to die for you. They love being your soldiers in peacetime; when war comes they run or leave. Italy's ruin, he says, came from relying on hired condottieri for many years.",
 "They are ready enough to be your soldiers whilst you do not make war, but if war comes they take themselves off or run from the foe",
 "mercenary soldiers fleeing a battlefield with their pikes, leaving a lone captain behind")

card("Chapter XIII", "Auxiliaries, Mixed Soldiery, and One's Own", "Borrowed armies own you",
 "Auxiliaries — troops borrowed from a powerful neighbour — are even worse than mercenaries. If they lose, you are undone; if they win, you are their captive. A wise prince prefers to lose with his own men than win with others', because a victory gained with foreign arms is not a real victory.",
 "The wise prince, therefore, has always avoided these arms and turned to his own",
 "David refusing the heavy armour of King Saul, holding only his sling and stones")
card("Chapter XIII", "Auxiliaries, Mixed Soldiery, and One's Own", "David's armour",
 "Machiavelli recalls David, who refused Saul's armour to fight Goliath because it would weigh him down; he chose his own sling. Other people's arms either fall off you, weigh you down, or bind you. No principality is secure without its own forces; without them it depends entirely on fortune.",
 "the arms of others either fall from your back, or they weigh you down, or they bind you fast",
 "young David with a sling facing giant Goliath in a valley, armies on both hillsides")

card("Chapter XIV", "A Prince and Military Affairs", "Think of war in peacetime",
 "A prince should have no other aim or study than war and its discipline. Being unarmed makes you contemptible. In peace he should train even harder — hunting to learn terrain, reading history to study great captains. Then, when fortune turns, he is ready to resist her.",
 "A prince ought to have no other aim or thought, nor select anything else for his study, than war and its rules and discipline",
 "a prince hunting on horseback with dogs through hills and forest, studying the land")

card("Chapter XV", "What Men, Especially Princes, Are Praised or Blamed For", "Real, not imagined",
 "Here Machiavelli breaks with all earlier writers. Many have imagined perfect republics and princes that never existed. He wants to describe things as they really are. The gap between how people live and how they ought to live is so wide that whoever ignores what is done for what should be done learns his own ruin.",
 "how one lives is so far distant from how one ought to live, that he who neglects what is done for what ought to be done, sooner effects his ruin than his preservation",
 "a scholar at a desk tearing a page from a book of utopian drawings while looking out at a real crowded street")
card("Chapter XV", "What Men, Especially Princes, Are Praised or Blamed For", "Learn how not to be good",
 "A man who wants to be good in everything will be destroyed among so many who are not good. So a prince must learn how not to be good, and use or not use that knowledge as needed. He should avoid the vices that would cost him his state, but not worry too much about others if they help keep it.",
 "it is necessary for a prince wishing to hold his own to know how to do wrong, and to make use of it or not according to necessity",
 "a theatre mask split in two, one half calm and saintly, the other half cunning, on a draped table")

card("Chapter XVI", "Liberality and Meanness", "The trap of generosity",
 "Everyone wants to be called generous. But a prince who spends lavishly to look generous will burn through his treasury and then must tax his people heavily, becoming hated. Better to accept being called mean (stingy): over time, people see he defends them without new taxes, and his parsimony looks like prudence.",
 "there is nothing wastes so rapidly as liberality, for even whilst you exercise it you lose the power to do so",
 "an overflowing treasure chest tipped over with coins spilling onto the floor as hands grab at them")
card("Chapter XVI", "Liberality and Meanness", "Spend other people's money",
 "There is one exception. A prince can be generous with what he takes from enemies through war — loot and ransom — because that money was never his own people's. Caesar and Alexander were generous this way. But spending your own wealth or your subjects' only leads to poverty or to being hated.",
 "a prince should guard himself, above all things, against being despised and hated; and liberality leads you to both",
 "a Roman general distributing captured treasure to his soldiers outside a tent")

card("Chapter XVII", "Cruelty and Clemency", "Mercy that causes chaos",
 "Every prince wants a reputation for clemency, not cruelty. But Cesare Borgia, called cruel, brought peace and order to the Romagna, while the Florentines, wanting to avoid looking cruel, let the city of Pistoia be destroyed by riots. A few harsh examples can be more merciful than letting disorder spread murder and theft.",
 "a prince, so long as he keeps his subjects united and loyal, ought not to mind the reproach of cruelty",
 "an orderly Renaissance town square at peace with merchants and guards, a single gallows on a far hill")
card("Chapter XVII", "Cruelty and Clemency", "Feared or loved?",
 "The famous question: is it better to be loved than feared, or the reverse? Ideally both — but since that is hard, it is much safer to be feared. Love is a chain of obligation that men break whenever it suits them. Fear is kept by a dread of punishment that never fails.",
 "it is much safer to be feared than loved, when, of the two, either must be dispensed with",
 "a stern prince on a high throne with a lion at his feet while courtiers bow in fear and awe")
card("Chapter XVII", "Cruelty and Clemency", "Never take their property",
 "Being feared is fine; being hated is not. The prince must keep his hands off the property and the women of his subjects. If he must execute someone, he needs a clear reason. Above all, he must not seize property, because men forget the death of their father sooner than the loss of their inheritance.",
 "men more quickly forget the death of their father than the loss of their patrimony",
 "a grieving son in a hallway holding a deed scroll tightly, a family estate behind him")

card("Chapter XVIII", "How Princes Should Keep Faith", "The fox and the lion",
 "There are two ways to fight: by law, which is human, and by force, which is for beasts. Since the first is often not enough, a prince must know how to use the beast. He should be both a fox and a lion: the lion cannot defend himself from traps, the fox cannot defend himself from wolves.",
 "it is necessary to be a fox to discover the snares and a lion to terrify the wolves",
 "a fox and a lion side by side in a forest clearing, a hunter's snare hidden on the ground")
card("Chapter XVIII", "How Princes Should Keep Faith", "Promises are tools",
 "A wise ruler cannot and should not keep his word when doing so would hurt him and when the reasons he gave it are gone. If all men were good, this would be bad advice — but they are not, and they will not keep faith with you. Princes have never lacked good excuses to cover their perfidy.",
 "a wise lord cannot, nor ought he to, keep faith when such observance may be turned against him",
 "two Renaissance lords shaking hands while one holds a dagger behind his back")
card("Chapter XVIII", "How Princes Should Keep Faith", "Seem, don't need to be",
 "A prince does not need to have all the good qualities — mercy, faith, kindness, honesty, religion — but he must seem to have them — a great pretender and dissembler. Everyone sees what you appear to be; few touch what you are. And those few dare not oppose the opinion of the many.",
 "Every one sees what you appear to be, few really know what you are",
 "a prince holding an ornate mask in front of his face, a crowd seeing only the mask")
card("Chapter XVIII", "How Princes Should Keep Faith", "Judged by results",
 "Men judge more by the eye than by the hand: everyone can see, few can feel. In the actions of princes, where there is no court to appeal to, people look at results. Keep and win the state, and your methods will always be called honourable and praised by everyone.",
 "men judge generally more by the eye than by the hand",
 "a crowd looking up at a grand triumphal parade, a prince on a chariot under an arch")

card("Chapter XIX", "Avoiding Contempt and Hatred", "Two things to avoid",
 "A prince must avoid being hated or despised. He becomes hated through avarice and seizing property; he falls into contempt by being fickle, frivolous, cowardly, weak or irresolute. Instead his actions should show greatness, courage, seriousness and strength, and his decisions should be final.",
 "It makes him contemptible to be considered fickle, frivolous, effeminate, mean-spirited, irresolute",
 "a weak king shrinking on a throne while courtiers laugh behind their hands")
card("Chapter XIX", "Avoiding Contempt and Hatred", "Two fears, one cure",
 "A prince faces two dangers: rebellion inside and attack from outside. Against outside threats, good arms and good allies. Against conspiracy inside, the best defence is not being hated by the people — a conspirator always hopes the people will be happy at the prince's death. If they would be outraged, he does not dare.",
 "one of the most efficacious remedies that a prince can have against conspiracies is not to be hated and despised by the people",
 "hooded conspirators whispering in a dark stone corridor lit by a single torch")
card("Chapter XIX", "Avoiding Contempt and Hatred", "Let others deliver bad news",
 "Princes should give unpopular tasks to others and keep the pleasing ones for themselves. France built a parliament to keep the nobles in check, so the king never had to take the blame. Hatred, he adds, is earned by good works as well as bad ones.",
 "princes ought to leave affairs of reproach to the management of others, and keep those of grace in their own hands",
 "a king smiling and handing out gifts at the front while an official reads a harsh decree behind him")

card("Chapter XX", "Fortresses and Other Tools of Princes", "The best fortress",
 "Should a prince disarm his people, divide cities by factions, build castles? New princes should arm their subjects, because arms given to people make them yours. Fortresses help against the people only if you fear them more than foreigners. In the end, the best fortress is not to be hated by your people.",
 "the best possible fortress is—not to be hated by the people",
 "a lonely abandoned castle on a crag, while below a happy town thrives around its market")

card("Chapter XXI", "How a Prince Should Act to Gain Renown", "Great deeds, big name",
 "Nothing gives a prince so much renown as great undertakings and rare examples. Ferdinand of Aragon went from a weak king to the foremost sovereign in Christendom by keeping people busy with one bold campaign after another, so no one had time to plot against him.",
 "Nothing makes a prince so much esteemed as great enterprises and setting a fine example",
 "a Spanish king on horseback leading a grand army toward a Moorish fortress city")
card("Chapter XXI", "How a Prince Should Act to Gain Renown", "Never stay neutral",
 "When two neighbours fight, pick a side openly. If you stay neutral, the winner will see you as a coward who didn't help, and the loser will have no reason to shelter you. Irresolute princes take the neutral path to avoid present danger and are usually ruined by it.",
 "irresolute princes, to avoid present dangers, generally follow the neutral path, and are generally ruined",
 "a lone figure standing at a fork in a road, two armies clashing on either side")

card("Chapter XXII", "A Prince's Secretaries", "Judge a prince by his staff",
 "The first way to judge a ruler's intelligence is to look at the people around him. Capable and loyal advisers mean he is wise. To test a minister: if he thinks more of himself than of you, he will never be trustworthy. Hold a good one in high esteem, with honour and riches, so he does not want more elsewhere.",
 "the first opinion which one forms of a prince, and of his understanding, is by observing the men he has around him",
 "a prince at a long table surrounded by attentive ministers and scribes with ledgers")

card("Chapter XXIII", "How Flatterers Should Be Avoided", "Make truth safe",
 "Courts are full of flatterers, and men enjoy flattery so much it is hard to resist. The only defence is to make people understand that telling you the truth will not offend you. But if everyone can tell you the truth, you lose respect — so pick a few wise advisers, let them speak freely, but only when you ask.",
 "there is no other way of guarding oneself from flatterers except letting men understand that to tell you the truth does not offend you",
 "a court jester whispering praise into a king's ear while an honest old adviser stands apart")
card("Chapter XXIII", "How Flatterers Should Be Avoided", "Good advice needs a wise ruler",
 "A prince who is not wise himself cannot be well advised, unless by luck he hands everything to one very prudent man — who would soon take his state. Good advice, wherever it comes from, comes from the wisdom of the prince, not the wisdom of the prince from good advice.",
 "good counsels, whencesoever they come, are born of the wisdom of the prince, and not the wisdom of the prince from good counsels",
 "a prince reading many letters by candlelight, weighing scrolls in each hand")

card("Chapter XXIV", "Why Italian Princes Lost Their States", "Don't blame fortune",
 "The Italian princes who lost their lands should not blame fortune, but their own laziness. In good times they never thought the weather would change — a common mistake of men, never to plan for storms in calm. When bad times came, they ran instead of defending themselves.",
 "it is a common defect in man not to make any provision in the calm against the tempest",
 "a ship in calm sea with dark storm clouds gathering on the horizon, sailors idle on deck")

card("Chapter XXV", "Fortune in Human Affairs", "Fortune is a river",
 "Many believe God and fortune control everything, so effort is pointless. Machiavelli says fortune controls about half of what we do and leaves the other half to us. He compares fortune to a raging river that floods the plain — but in calm weather, men can build dykes and banks to hold it back.",
 "Fortune is the arbiter of one-half of our actions",
 "a raging flooded river tearing through fields while workers build stone dykes and embankments")
card("Chapter XXV", "Fortune in Human Affairs", "Match the times",
 "Two people with the same methods can get opposite results, because one acts in times that suit him and the other does not. A cautious man succeeds when cautious times come and fails when they need boldness. The trouble is, people can't change their nature, so fortune changes and they stay the same.",
 "he will be successful who directs his actions according to the spirit of the times",
 "a giant wheel of fortune with figures rising and falling around its rim, a blindfolded woman turning it")
card("Chapter XXV", "Fortune in Human Affairs", "Better bold than cautious",
 "Pope Julius II was impetuous in everything, and because the times suited him, he always succeeded. Machiavelli concludes it is better to be bold than circumspect, because fortune tends to favour those who are young, daring and less careful.",
 "it is better to be adventurous than cautious",
 "an elderly pope in armour leading troops on horseback through a city gate")

card("Chapter XXVI", "An Exhortation to Liberate Italy", "Italy waits for a redeemer",
 "In the final chapter Machiavelli drops his cool tone. Italy is leaderless, beaten, robbed, torn and overrun — the perfect moment for a new prince. Like the Israelites waiting for Moses, Italy waits for a redeemer to heal her wounds. He begs the Medici family to take up the cause.",
 "To all of us this barbarous dominion stinks.",
 "a ruined Italian landscape with broken columns, a lone banner raised on a hilltop at dawn")
card("Chapter XXVI", "An Exhortation to Liberate Italy", "Courage against fury",
 "He says Italians are strong and clever one-on-one but fail in armies, because their leaders are weak. A national army of its own people, led well, could drive out the barbarians. He ends with a line from the poet Petrarch: the ancient Italian valour is not yet dead.",
 "Virtue against fury shall advance the fight",
 "Italian citizen-soldiers marching with pikes under a rising sun, laurel wreath in the sky")

ABOUT = {
 "version": 2, "id": "prince", "title": "The Prince", "author": "Niccolò Machiavelli", "short": "Machiavelli", "year": "1532",
 "translator": "W. K. Marriott, 1908 (public domain, Project Gutenberg #1232)",
 "era": "renaissance", "cover": "prince_cover",
 "blurb": "A banished Florentine diplomat's handbook on how power is won, held and lost. Five hundred years later it still names the game: appearances, fear, loyalty, luck.",
}

CAP = {  # hand-written captions where the Commons title is Dutch or catalogue-speak
 "prince_11": "Sermon on the Art of Dying Well, Florentine woodcut", "prince_13": "A Gust of Wind, engraving",
 "prince_15": "Plan of the Vatican during a conclave", "prince_17": "Justice with scales and sword",
 "prince_23": "Battle scene, 16th-century print", "prince_24": "David receives Saul's armour",
 "prince_28": "Janus and the Four Seasons", "prince_29": "Quentin Massys, The Moneylender and his Wife, 1514",
 "prince_31": "Beheading of John the Baptist, woodcut", "prince_34": "A fox, engraving",
 "prince_37": "Burgkmair, The Triumphal Procession of Emperor Maximilian I", "prince_45": "The Fool, engraving",
 "prince_48": "Virgil Solis, The Deluge", "prince_49": "Dürer, Time and a Fox Turning the Wheel of Fortune",
 "prince_16": "Agathocles, bust in the Vatican Museums", "prince_19": "Peasants at work, after Holbein",
}

def caption(f):
    """Commons file title -> short museum-style caption."""
    f = re.sub(r"^File:|\.(jpe?g|png|tiff?)$", "", f, flags=re.I)
    f = re.sub(r"\s*[-,]?\s*(WGA\d+|MET DP\d+|RP-P-[\w.-]+|NGA \d+|Google Art Project|Walters \d+|LACMA [\d.]+|RMG \w+|\(BM [^)]*\)|[\d.]+ - Cleveland Museum of Art|\(cropped\)|\(titel op object\)|\(serietitel\))", "", f)
    f = re.sub(r"\s+", " ", f).strip(" -,")
    return f + " · public domain"

def norm(s):
    s = s.replace("’", "'").replace("‘", "'").replace("“", '"').replace("”", '"')
    return re.sub(r"\s+", " ", s).strip()

def main():
    here = pathlib.Path(__file__).parent
    src = norm((here / "prince.txt").read_text()).lower()
    bad = [c["quote"] for c in C if norm(c["quote"]).lower() not in src]
    credits = json.loads((here / "commons/credits.json").read_text())
    for i, c in enumerate(C):
        c["img"] = f"prince_{i+1:02d}"
        cr = credits.get(c["img"])
        c["cap"] = (CAP[c["img"]] + " · public domain") if c["img"] in CAP else caption(cr["file"]) if cr else "Engraving made for Folio"
    gl = {}
    for w, (m, e, forms) in G.items():
        gl[w] = {"m": m, "e": e}
        for f in forms:
            gl[f] = {"m": m, "e": e, "root": w}
    # every glossary headword should appear somewhere, else it is dead weight
    alltext = " ".join(c["text"] + " " + c["quote"] for c in C).lower()
    unused = [w for w in gl if not re.search(r"\b" + re.escape(w.lower()) + r"\b", alltext)]
    gl = {w: v for w, v in gl.items() if w not in unused}  # only ship words that actually appear
    out = dict(ABOUT, glossary=gl, cards=[{k: v for k, v in c.items()} for c in C])
    import meanings
    meanings.apply("prince", out["cards"])
    dst = here.parent / "app/src/main/assets/books/prince.json"
    dst.parent.mkdir(parents=True, exist_ok=True)
    dst.write_text(json.dumps(out, ensure_ascii=False, indent=1))
    print(f"{len(C)} cards, {len(G)} glossary words, wrote {dst}")
    print(len(gl), "glossary forms shipped")
    if bad:
        print("QUOTES NOT FOUND:"); [print("  -", q) for q in bad]; sys.exit(1)

if __name__ == "__main__":
    main()
