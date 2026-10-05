"""The 48 Laws of Power — a Folio digest. Greene's book (1998) is copyrighted: summaries here are original,
and every quote comes from the public-domain classics the laws grew out of. Run: python3 laws_content.py"""
from folio_build import build

G = {
 "courtier": ("A person who attends a king's court, often seeking favour.", "Office courtiers laugh loudest at the boss's jokes.", ["courtiers"]),
 "sovereign": ("The highest ruler.", "The sovereign's word was final.", []),
 "envy": ("A painful wish to have what someone else has.", "Her new car filled the neighbours with envy.", ["envious"]),
 "insecure": ("Not confident; easily worried about your place.", "The insecure manager hated being corrected in meetings.", ["insecurity"]),
 "rival": ("Someone competing with you for the same thing.", "The two cafés on the street are rivals.", ["rivals", "rivalry"]),
 "conceal": ("To hide.", "She concealed the gift under the bed.", ["concealed", "concealing"]),
 "decoy": ("Something used to distract or mislead.", "The empty box on the porch was a decoy for parcel thieves.", ["decoys"]),
 "mystique": ("An air of mystery that makes someone fascinating.", "The chef never gives interviews, which adds to her mystique.", []),
 "reputation": ("What people generally think of you.", "One rude review can dent a café's reputation.", []),
 "spectacle": ("A big, striking public show.", "The fireworks were a spectacle the whole city came to see.", ["spectacles"]),
 "indispensable": ("So needed that you cannot do without it.", "The only person who knows the old billing system is indispensable.", []),
 "ingratitude": ("Not being thankful.", "After he fixed her laptop for free, her ingratitude hurt.", []),
 "disarm": ("To make someone less hostile or suspicious.", "His honest smile disarmed the angry customer.", ["disarms", "disarming"]),
 "self-interest": ("Caring about what benefits yourself.", "Free samples work because they appeal to self-interest.", []),
 "aloof": ("Distant, not friendly or involved.", "The new neighbour seemed aloof, never stopping to chat.", []),
 "unpredictable": ("Impossible to guess in advance.", "The weather this week is unpredictable — carry an umbrella.", ["unpredictability"]),
 "isolation": ("Being cut off from others.", "Months of isolation during lockdown made people restless.", []),
 "gullible": ("Easily tricked into believing things.", "The gullible tourist paid triple for a 'genuine' antique.", []),
 "concentrate": ("To gather in one place or on one thing.", "Concentrate your effort on one exam at a time.", []),
 "flattery": ("Too much praise, often insincere.", "Flattery got him a better seat but no real respect.", []),
 "reinvent": ("To change yourself or something into something new.", "The singer reinvented herself with a new style every album.", ["reinvented"]),
 "scapegoat": ("Someone blamed for the mistakes of others.", "The intern became the scapegoat for the manager's error.", []),
 "audacity": ("Bold daring that surprises people.", "She had the audacity to ask the CEO for a meeting — and got it.", []),
 "effortless": ("Seeming to need no effort at all.", "The dancer's spins looked effortless after years of practice.", []),
 "illusion": ("Something that seems real but is not.", "The mirror wall gives the illusion of a bigger room.", []),
 "fantasy": ("Something imagined, often too good to be true.", "Getting rich overnight is a fantasy most ads sell.", ["fantasies"]),
 "dignity": ("Self-respect and a calm, worthy manner.", "She lost the match with dignity, shaking every hand.", []),
 "disdain": ("The feeling that something is not worth your attention.", "He looked at the instant coffee with disdain.", []),
 "conform": ("To behave the way others expect.", "At the formal dinner, he conformed and wore a tie.", ["conformity"]),
 "provoke": ("To deliberately make someone react, often angrily.", "Don't provoke the dog by pulling its tail.", ["provoked"]),
 "successor": ("The person who comes after someone in a role.", "The founder's successor struggled to fill his shoes.", ["successors"]),
 "predecessor": ("The person who held a role before.", "Her predecessor left the files in a mess.", []),
 "allegiance": ("Loyalty to a person, group or cause.", "Fans switch allegiance when a star player moves club.", []),
 "formless": ("Without a fixed shape.", "Water is formless — it takes the shape of its cup.", ["formlessness"]),
 "complacent": ("Too satisfied to see danger coming.", "The leading team grew complacent and lost in the last minute.", []),
 "ostracism": ("Being shut out of a group on purpose.", "Ancient Athens used ostracism to exile powerful men for ten years.", []),
 "veneration": ("Deep respect, almost worship.", "Pilgrims touched the old stone with veneration.", []),
}

C = []
def law(n, name, title, text, quote, src):
    C.append(dict(ch=f"Law {n}", chTitle=name, title=title, text=text, quote=quote, src=src))

law(1, "Never Outshine the Master", "Let the boss shine",
 "People above you want to feel superior. If you look more brilliant than them, you stir envy and insecurity. In 1661 Nicolas Fouquet threw a party so dazzling that King Louis XIV felt upstaged — within weeks Fouquet was arrested and spent the rest of his life in prison. Make your superiors look good; let your talent serve them.",
 "All victories breed hate, and that over your superior is foolish or fatal.", "gracian")
law(2, "Never Put Too Much Trust in Friends, Learn How to Use Enemies", "Friends and enemies",
 "Friends can turn on you quickly because envy hides inside friendship, and they expect favours. A former enemy you win over has more to prove and works harder to show loyalty. Keep an eye on friends; don't be afraid to hire a rival.",
 "A wise man gets more use from his enemies than a fool from his friends.", "gracian")
law(3, "Conceal Your Intentions", "Hide your hand",
 "If people know exactly what you want, they can block you. Keep them guessing. Use a decoy goal or a show of sincerity to throw them off. By the time your real aim is clear, it is too late for them to stop it.",
 "It is both useless and insipid to play with the cards on the table.", "gracian")
law(4, "Always Say Less Than Necessary", "Silence is power",
 "The more you talk, the more likely you are to say something foolish, and the more ordinary you seem. Short, vague answers make people wonder what you really think. Powerful people often impress by how little they say.",
 "Cautious silence is the holy of holies of worldly wisdom.", "gracian")
law(5, "So Much Depends on Reputation — Guard It with Your Life", "Your name is your shield",
 "Reputation works before you even enter the room. A strong one makes people respect and fear you; a damaged one invites attacks. Guard it, notice early threats, and never let one mocking nickname stick to you.",
 "if it clings to you with a nickname, your reputation is in danger.", "gracian")
law(6, "Court Attention at All Costs", "Be seen",
 "People judge by appearances, and what goes unseen is forgotten. Stand out — with a striking style, a mystery, even a little scandal. P. T. Barnum built a business on drawing crowds. Attention brings opportunity; being ignored brings nothing.",
 "Display startling novelties, rise afresh like the sun every day.", "gracian")
law(7, "Get Others to Do the Work for You, but Always Take the Credit", "Borrow others' brains",
 "Use the skill, knowledge and labour of others to move faster. History's examples are many — inventors whose ideas were sold under someone else's name. It is ruthless, so know when it is fair to ask for help and give credit, and know that others may try this on you.",
 "you thus obtain the fame of an oracle by others' toil.", "gracian")
law(8, "Make Other People Come to You — Use Bait if Necessary", "Make them come to you",
 "Whoever acts first, on the other's ground, is at a disadvantage. Lure opponents into coming to you, with a tempting offer if needed. Then you control the place and the rules. Napoleon's enemies, believing him beaten, came to him — and lost.",
 "the clever combatant imposes his will on the enemy, but does not allow the enemy's will to be imposed on him.", "sunzi")
law(9, "Win Through Your Actions, Never Through Argument", "Show, don't argue",
 "Winning an argument often makes the loser resent you. Demonstrate instead. When an official insisted a statue's nose was too large, Michelangelo pretended to chip at it, dropping marble dust — and the official was delighted with the 'change'. Results persuade; words provoke.",
 "To dissent from others' views is regarded as an insult", "gracian")
law(10, "Infection: Avoid the Unhappy and Unlucky", "Misery is contagious",
 "Some people carry bad luck and bad moods with them, and pull everyone close to them down. Their problems spread like a disease. Don't try to rescue everyone — keep company with the cheerful and the fortunate.",
 "Ill-luck is generally the penalty of folly, and there is no disease so contagious to those who share in it.", "gracian")
law(11, "Learn to Keep People Dependent on You", "Be needed",
 "To stay secure, make yourself indispensable. If others need your skills or knowledge, they will protect you. Teach them everything and they no longer need you; gratitude fades much faster than need.",
 "The wise man would rather see men needing him than thanking him.", "gracian")
law(12, "Use Selective Honesty and Generosity to Disarm Your Victim", "One honest act",
 "A single sincere gesture can lower someone's guard for a long time. Con men often begin with an honest deal or a small gift. Recognise the move: one surprise kindness is not proof of good intent.",
 "benefits ought to be given little by little, so that the flavour of them may last longer.", "prince")
law(13, "When Asking for Help, Appeal to Self-Interest", "What's in it for them",
 "Don't remind people of what you did for them, and don't beg for mercy. Show how helping you helps them. Most people respond to self-interest far more readily than to gratitude or pity.",
 "Interest speaks all sorts of tongues and plays all sorts of characters; even that of disinterestedness.", "roche")
law(14, "Pose as a Friend, Work as a Spy", "Gather intelligence",
 "Knowing your rival's plans is a huge advantage. In friendly conversation, people reveal their hopes, weaknesses and next moves. Ask, listen, and let them talk. Know too that others may do the same to you.",
 "what enables the wise sovereign and the good general to strike and conquer, and achieve things beyond the reach of ordinary men, is foreknowledge.", "sunzi")
law(15, "Crush Your Enemy Totally", "No half measures",
 "A beaten enemy who still has strength will recover and seek revenge. History is full of leaders who spared a rival and were destroyed later. This law is harsh — but its point is that half-finished conflicts tend to return.",
 "men ought either to be well treated or crushed, because they can avenge themselves of lighter injuries, of more serious ones they cannot", "prince")
law(16, "Use Absence to Increase Respect and Honor", "Leave them wanting",
 "Too much presence makes you common. Once people value you, step back a little; scarcity raises worth. Deioces of the Medes withdrew from public view, and his mystique grew — people began to treat him like a king.",
 "Change too the scene on which you shine, so that your loss may be felt in the old scenes of your triumph", "gracian")
law(17, "Keep Others in Suspended Terror: Cultivate an Air of Unpredictability", "Be hard to read",
 "People are creatures of habit and like to predict others. If you are unpredictable, they stay off balance and wary. Chess champion Bobby Fischer unsettled Boris Spassky in 1972 with endless odd demands before and during their match.",
 "Vary the Mode of Action ; not always the same way, so as to distract attention, especially if there be a rival.", "gracian")
law(18, "Do Not Build Fortresses to Protect Yourself — Isolation Is Dangerous", "Don't hide away",
 "Walling yourself off feels safe but cuts you off from information and allies. Isolated rulers fall to plots they never heard coming. Stay among people, keep moving, and stay informed.",
 "the best possible fortress is—not to be hated by the people", "prince")
law(19, "Know Who You're Dealing With — Do Not Offend the Wrong Person", "Read the person",
 "People react differently to the same move. Some are proud and will never forgive a slight; some are suspicious; some are dangerous when crossed. Study who someone really is before you test or trick them.",
 "If you know the enemy and know yourself, you need not fear the result of a hundred battles.", "sunzi")
law(20, "Do Not Commit to Anyone", "Stay free",
 "Taking sides too early makes you a pawn in someone else's quarrel. Stay independent, and people will compete for your support. Queen Elizabeth I kept her many suitors hoping for years and kept her power by never marrying.",
 "You should aim to be independent of any one vote, of any one fashion, of any one century.", "gracian")
law(21, "Play a Sucker to Catch a Sucker — Seem Dumber Than Your Mark", "Look less clever",
 "Nobody likes feeling stupid. If you seem a bit slower than others, they relax, and their guard drops. Appearing harmless can be a smart disguise.",
 "there are times when the greatest wisdom lies in seeming not to be wise.", "gracian")
law(22, "Use the Surrender Tactic: Transform Weakness into Power", "Bend, don't break",
 "When you are weaker, don't fight for honour's sake. Give in for now; it buys time, calms the winner, and lets you prepare. Surrender can make an opponent arrogant and careless.",
 "Pretend to be weak, that he may grow arrogant.", "sunzi")
law(23, "Concentrate Your Forces", "Focus",
 "Spreading yourself across many goals weakens all of them. Pour your energy into one strong point — one project, one patron, one skill. Depth beats scattered effort.",
 "we can form a single united body, while the enemy must split up into fractions.", "sunzi")
law(24, "Play the Perfect Courtier", "Master the room",
 "Every workplace is a kind of court with its own manners. The skilled courtier never shows off, flatters with care, notices moods, and stays graceful. Respect is earned by giving it.",
 "pay respect that you may be respected, and know that to be esteemed you must show esteem.", "gracian")
law(25, "Re-Create Yourself", "Write your own role",
 "Don't accept the identity others hand you. Shape your own image, like an actor building a character. Julius Caesar and many others crafted public personas. Keep renewing yourself so you never grow stale.",
 "Renew your Brilliance. 'Tis the privilege of the Phoenix.", "gracian")
law(26, "Keep Your Hands Clean", "Let others take the blame",
 "Leaders need to look clean even when hard things must happen. Historically, they used a scapegoat — like Cesare Borgia's harsh governor in the Romagna — or a go-between to carry unpopular tasks. Notice when you are being made someone's scapegoat.",
 "princes ought to leave affairs of reproach to the management of others, and keep those of grace in their own hands.", "prince")
law(27, "Play on People's Need to Believe to Create a Cultlike Following", "People want to believe",
 "Many people crave something to believe in. Charlatans in history gathered followers with vague, grand promises, rituals, and a sense of belonging. Recognising this pattern protects you from it.",
 "the vulgar are always taken by what a thing seems to be and by what comes of it", "prince")
law(28, "Enter Action with Boldness", "Hesitation is fatal",
 "Doubt shows, and it invites others to push back. Bold moves often succeed because they surprise and impress. If you act, act fully; timid half-measures get the worst of both worlds.",
 "it is better to be adventurous than cautious", "prince")
law(29, "Plan All the Way to the End", "See the ending first",
 "Many plans fail because people only think about the start. Picture the full path — the obstacles, the reactions, the finish. Then you won't be surprised halfway, and you'll know when to stop.",
 "the victorious strategist only seeks battle after the victory has been won", "sunzi")
law(30, "Make Your Accomplishments Seem Effortless", "Hide the sweat",
 "Showing how hard you worked makes your success look ordinary. Grace under pressure creates an illusion of natural talent. Practise in private; perform with ease in public.",
 "What the ancients called a clever fighter is one who not only wins, but excels in winning with ease.", "sunzi")
law(31, "Control the Options: Get Others to Play with the Cards You Deal", "Choose the choices",
 "The best way to steer someone is to let them choose — between options you designed. They feel free, yet every path leads where you want. Watch for this in sales and politics.",
 "By holding out advantages to him, he can cause the enemy to approach of his own accord", "sunzi")
law(32, "Play to People's Fantasies", "Sell the dream",
 "Plain truth is often dull and painful; fantasy is attractive. People are drawn to whoever promises a better story — instant wealth, a magic cure. This explains many famous frauds; it also explains why hope sells.",
 "he who seeks to deceive will always find someone who will allow himself to be deceived.", "prince")
law(33, "Discover Each Man's Thumbscrew", "Find the lever",
 "Everyone has a soft spot — an insecurity, a need, a secret pleasure. Find it and you know how to move them. The phrase comes straight from the 17th-century Spanish writer Baltasar Gracián.",
 "Find out each Man's Thumbscrew. 'Tis the art of setting their wills in action.", "gracian")
law(34, "Be Royal in Your Own Fashion: Act Like a King to Be Treated Like One", "Carry yourself like royalty",
 "How you carry yourself teaches others how to treat you. Calm dignity and quiet confidence make people respect you. Act cheap or desperate and you will be treated that way.",
 "Let each deed of a man in its degree, though he be not a king, be worthy of a prince", "gracian")
law(35, "Master the Art of Timing", "Wait, then strike",
 "Never seem in a hurry — haste shows you are not in control. Be patient while the moment ripens, then move fast. Fouché, Napoleon's police chief, survived every regime in France by reading the times.",
 "Time and I against any two.", "gracian")
law(36, "Disdain Things You Cannot Have: Ignoring Them Is the Best Revenge", "Shrug it off",
 "Reacting to a small irritation makes it bigger and shows it hurt you. Often the most powerful response is to seem not to care. Attention is fuel — starve trivial attacks of it.",
 "Fools depreciate all men", "gracian")
law(37, "Create Compelling Spectacles", "Put on a show",
 "Images and symbols move people more than arguments. Louis XIV made himself the 'Sun King', staging his whole life at Versailles like a theatre. A striking show creates awe and silences doubt.",
 "Nothing makes a prince so much esteemed as great enterprises and setting a fine example.", "prince")
law(38, "Think as You Like but Behave Like Others", "Blend in outside",
 "Showing off unusual opinions or habits makes others feel judged. Keep your most radical thoughts for trusted friends, and speak like the people around you in public. You keep freedom of thought without making enemies.",
 "Think with the Few and speak with the Many.", "gracian")
law(39, "Stir Up Waters to Catch Fish", "Stay calm, unsettle them",
 "Angry people make mistakes. Keep your temper, and notice when someone is trying to provoke you. Losing control hands the advantage to the calmer side.",
 "If your opponent is of choleric temper, seek to irritate him.", "sunzi")
law(40, "Despise the Free Lunch", "Nothing is free",
 "What comes free often carries a hidden cost — obligation, guilt, strings attached. Paying your own way keeps you independent. Generosity, used wisely, wins goodwill.",
 "What costs little is little worth.", "gracian")
law(41, "Avoid Stepping into a Great Man's Shoes", "Make your own name",
 "Following a famous predecessor is a trap: everything you do gets compared to a legend. Create your own space and style instead of copying theirs. Alexander the Great feared his father Philip would leave him nothing to conquer.",
 "It is no great gain if a poor successor makes the predecessor seem good", "gracian")
law(42, "Strike the Shepherd, and the Sheep Will Scatter", "Find the source",
 "Trouble in a group often grows from one person — a schemer, a ringleader. Remove or win over that one source, and the rest settles. Don't waste effort on the followers.",
 "Begin by seizing something which your opponent holds dear; then he will be amenable to your will.", "sunzi")
law(43, "Work on the Hearts and Minds of Others", "Win their hearts",
 "Force makes people resentful; persuasion makes them loyal. Learn what people feel and value, and speak to that. People who feel understood follow willingly.",
 "it is necessary for a prince to have the people friendly, otherwise he has no security in adversity.", "prince")
law(44, "Disarm and Infuriate with the Mirror Effect", "Mirror them",
 "Reflecting someone's behaviour back at them is powerful: it can calm them by showing you understand, or unsettle them by showing them their own tactics. Mirroring builds rapport — and it can also be a weapon.",
 "Success in warfare is gained by carefully accommodating ourselves to the enemy's purpose.", "sunzi")
law(45, "Preach the Need for Change, but Never Reform Too Much at Once", "Change slowly",
 "People say they want change but cling to familiar habits. Reform too fast and you create a backlash. Wrap new ideas in old, comforting forms, and move step by step.",
 "there is nothing more difficult to take in hand, more perilous to conduct, or more uncertain in its success, than to take the lead in the introduction of a new order of things", "prince")
law(46, "Never Appear Too Perfect", "Show a small flaw",
 "Seeming flawless makes others envious and suspicious. A small, harmless weakness makes you human and likable. In ancient Athens, people even voted to exile a man partly because they were tired of hearing him called 'the Just' — ostracism.",
 "Allow Yourself some venial Fault. Some such carelessness is often the greatest recommendation of talent.", "gracian")
law(47, "Do Not Go Past the Mark You Aimed For; In Victory, Learn When to Stop", "Know when to stop",
 "Success can make people complacent and greedy. After a win, pause and consolidate. Many have lost everything by pushing their luck one step too far.",
 "Leave your Luck while Winning. All the best players do it. A fine retreat is as good as a gallant attack.", "gracian")
law(48, "Assume Formlessness", "Be like water",
 "Rigid people and rigid plans break. Stay flexible, adapt to circumstances, and avoid being pinned down. The old image is water — it fits any container and wears away stone.",
 "Military tactics are like unto water; for water in its natural course runs away from high places and hastens downwards.", "sunzi")

ABOUT = {
 "version": 2, "id": "laws", "title": "The 48 Laws of Power", "short": "Robert Greene", "author": "Robert Greene", "year": "1998",
 "translator": "A Folio digest: original summaries of each law, with quotes from the public-domain classics behind them. Greene's book is copyrighted — read the full text in print.",
 "era": "baroque", "cover": "laws_cover", "self_src": "none",
 "blurb": "Forty-eight rules of power, from royal courts to modern offices — each law in a minute, paired with a line from Machiavelli, Sun Tzŭ, Gracián or La Rochefoucauld. Know them to use them, or to spot them used on you.",
}

if __name__ == "__main__":
    build(ABOUT, G, C, credits_file="commons/credits.json")
