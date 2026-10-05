"""Draft varied-shelf expansion. Builds to research/staging, never the live catalog."""
import json
from pathlib import Path

from folio_build import SOURCES, build
from shelf2_content import C

SOURCES.update({
    "wealth": ("wealth.txt", "Adam Smith, The Wealth of Nations (1776)"),
    "walden": ("walden.txt", "Henry David Thoreau, Walden (1854)"),
    "gitanjali": ("gitanjali.txt", "Rabindranath Tagore, Gitanjali (English version, 1913)"),
})

CAPS = {"walden_02": "Thoreau approaches his cabin at Walden Pond"}

GLOSS = {
    "wealth": {
        "labour": ("Work that makes goods or provides services.", "The labour of many people builds a house.", []),
        "specialization": ("Concentrating on one kind of work.", "Her specialization is repairing old watches.", []),
        "benevolence": ("Kindness and desire to help.", "The neighbour's benevolence brought meals to the sick family.", []),
        "producer": ("A person or business that makes something to sell.", "The farmer is a producer of wheat.", ["producers"]),
    },
    "walden": {
        "desperation": ("Feeling that there is no good way out.", "He spoke in desperation after losing his job.", []),
        "deliberately": ("With conscious care and intention.", "She deliberately kept Sunday free of work.", []),
        "possessions": ("Things someone owns.", "He gave away possessions he never used.", []),
    },
    "gitanjali": {
        "vessel": ("A container; here, a symbol for a human life.", "The clay vessel held water for the journey.", []),
        "divine": ("Related to God or a sacred power.", "She felt the divine in the music.", []),
        "indulgence": ("Patient permission for a small request.", "He asked the teacher's indulgence for one more minute.", []),
    },
}

BOOKS = [
    (dict(id="wealth", title="The Wealth of Nations", short="Adam Smith", author="Adam Smith", year="1776",
          translator="Original text, Project Gutenberg #3300", era="enlightenment", shelf="Fortune & Wealth",
          blurb="Why work becomes productive, why people trade, and why an economy should serve those who use what it makes."),
     "wealth", [
        ("Introduction", "The wealth of a nation", "Where wealth begins",
         "Smith begins with work, not gold. A country's yearly labour produces what people use, whether made at home or obtained by trade. His question is how that work can supply more people well.",
         "The annual labour of every nation is the fund which originally supplies it with all the necessaries and conveniencies of life which it annually consumes",
         "A nation's real supply comes from its work.", "A town's bakers, builders and farmers matter more to daily life than its pile of coins."),
        ("Book I, Chapter I", "Of the Division of Labour", "The pin factory",
         "One worker could barely make a pin alone. In a small factory, different workers draw wire, straighten it, cut it and shape its head. Together they produce thousands. Smith uses this ordinary object to show why specialization multiplies output.",
         "One man draws out the wire; another straights it; a third cuts it; a fourth points it",
         "Dividing a job into steps lets a group make far more.", "In a bakery, one person mixes dough while another shapes and another bakes."),
        ("Book I, Chapter II", "Of the Principle Which Gives Occasion to the Division of Labour", "Why the baker feeds us",
         "People need help from strangers every day. Smith says exchange works because each side can offer the other something wanted. The baker does not have to know you personally to make your dinner.",
         "It is not from the benevolence of the butcher, the brewer, or the baker that we expect our dinner, but from their regard to their own interest.",
         "Trade works when both sides see a benefit.", "You pay a mechanic to fix your bike; you get a working bike and the mechanic earns a living."),
        ("Book I, Chapter III", "That the Division of Labour Is Limited by the Extent of the Market", "A wider market",
         "A specialist needs enough customers. A tiny village cannot support as many specialized trades as a large city. Roads, rivers and ships widen the market and let workers concentrate on narrower tasks.",
         "the division of labour is limited by the extent of the market",
         "Specialization grows when there are enough buyers.", "A niche bookshop can thrive in a large city or online but not in every small village."),
        ("Book IV, Chapter VIII", "Conclusion of the Mercantile System", "Production is for people",
         "Smith turns the argument back to its purpose: goods exist for the people who use them. Rules that protect producers at consumers' expense can defeat the point of production.",
         "Consumption is the sole end and purpose of all production",
         "Making goods matters because people need or enjoy them.", "A rule that protects one factory but makes essentials unaffordable deserves scrutiny."),
     ]),
    (dict(id="walden", title="Walden", short="Thoreau", author="Henry David Thoreau", year="1854",
          translator="Original text, Project Gutenberg #205", era="modern", shelf="Nature & Simple Living",
          blurb="Two years beside a Massachusetts pond became a challenge to hurry less and choose life deliberately."),
     "walden", [
        ("Economy", "What a life costs", "Quiet desperation",
         "Thoreau sees people working to maintain lives they did not truly choose. He asks whether habit, debt and status have made them resign themselves to unhappiness.",
         "The mass of men lead lives of quiet desperation.",
         "Many people endure lives that quietly make them unhappy.", "Keeping a costly job only to pay for a lifestyle you no longer want."),
        ("Where I Lived", "Choosing the essentials", "Why he went to the woods",
         "He moves to the pond to test what he actually needs. The cabin is an experiment in attention: leave room to notice the important parts of living before time runs out.",
         "I went to the woods because I wished to live deliberately",
         "He wants to make deliberate choices about his life.", "Taking a month away from constant notifications to find what you really care about."),
        ("Where I Lived", "Less, but clearer", "Simplify",
         "Thoreau thinks too many possessions and duties crowd out thought. His repeated command is practical: remove some of the needless details and see what becomes easier to understand.",
         "Simplify, simplify.",
         "Cut away needless complexity.", "Canceling subscriptions you never use and keeping an evening free."),
        ("Conclusion", "Your own pace", "A different drummer",
         "People mature and choose paths at different speeds. Thoreau warns against treating someone else's timetable as the measure of your life.",
         "If a man does not keep pace with his companions, perhaps it is because he hears a different drummer.",
         "Someone may have a sound reason to follow their own pace.", "Starting a career later because you spent time caring for family."),
        ("Conclusion", "Build under the dream", "Castles in the air",
         "Dreams alone are not enough, but neither must they be dismissed. Thoreau says to keep the vision and give it foundations through real work.",
         "If you have built castles in the air, your work need not be lost; that is where they should be.",
         "Keep the ambitious idea, then make it real.", "Sketching a community garden, then finding a plot and volunteers."),
     ]),
    (dict(id="gitanjali", title="Gitanjali", short="Tagore", author="Rabindranath Tagore", year="1913",
          translator="Tagore's English version, Project Gutenberg #7164", era="modern", shelf="Poetry of Life",
          blurb="Songs of devotion, work and shared life in the English collection that brought Tagore a worldwide readership."),
     "gitanjali", [
        ("Song 1", "A life refilled", "The little flute",
         "Tagore imagines human life as a small vessel that is emptied and filled again, and as a reed flute played by a larger breath. The image joins fragility with renewal.",
         "Thou hast made me endless, such is thy pleasure.",
         "Life feels repeatedly renewed by a power beyond the speaker.", "Finding new purpose after a difficult ending."),
        ("Song 11", "Prayer in the world", "Leave the closed temple",
         "The poem asks worshippers to look for the divine among people doing hard everyday work, not only inside a shut temple. Its spiritual lesson leads outward toward others.",
         "Leave this chanting and singing and telling of beads!",
         "Prayer also means attending to people outside the temple.", "Helping a worker in the heat instead of only talking about compassion."),
        ("Song 35", "Freedom of mind", "Where the mind is without fear",
         "Tagore prays for a country where thought is fearless, knowledge is open and truth is spoken plainly. He connects national freedom with the freedom to think.",
         "Where the mind is without fear and the head is held high",
         "A free society lets people think and speak without fear.", "A classroom where students can question an idea without being punished."),
        ("Song 5", "A moment beside you", "Pause the work",
         "The speaker asks to set his tasks aside briefly and sit in quiet presence. Without that pause, work stretches into joyless, endless effort.",
         "I ask for a moment’s indulgence to sit by thy side.",
         "Take a moment of stillness and connection.", "Stepping away from email to sit with someone you love."),
        ("Song 69", "One shared life", "The same stream",
         "A single current of life moves through the speaker, grass, flowers and the sea. Tagore's joy comes from feeling part of that larger living world.",
         "The same stream of life that runs through my veins night and day runs through the world",
         "Your life is connected with the life around you.", "Feeling renewed after time among trees and water."),
     ]),
]

if __name__ == "__main__":
    out = Path(__file__).resolve().parent / "staging/books"
    out.mkdir(parents=True, exist_ok=True)
    for about, src, rows in BOOKS:
        cards = C(src, rows)
        cover = f"{about['id']}_cover" if about['id'] in {'wealth', 'gitanjali'} else ''
        build(dict(about, version=1, self_src=src, cover=cover), GLOSS[about['id']], cards,
              credits_file="commons/credits.json", caps=CAPS, out_dir=out)
        path = out / f"{about['id']}.json"
        book = json.loads(path.read_text())
        for card in book['cards']:
            if card['img'] != 'walden_02':
                card['img'] = ''
                card['cap'] = ''
        path.write_text(json.dumps(book, ensure_ascii=False, indent=1) + '\n')
