package harshcurses.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.curses.AscendersBane;
import com.megacrit.cardcrawl.cards.curses.Normality;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import harshcurses.cards.*;
import javassist.CtBehavior;

@SpirePatch(
        clz = AbstractDungeon.class,
        method = "dungeonTransitionSetup"
)
public class HarshCursesPatch {

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static void replaceAscendersBane() {
        if (AbstractDungeon.ascensionLevel >= 10) {
            // Remove Ascender's Bane if it exists (direct collection manipulation)
            AbstractDungeon.player.masterDeck.group.removeIf(card -> card.cardID.equals(AscendersBane.ID));

            // Add the appropriate harsh curse based on character
            AbstractCard harshCurse = getHarshCurseForCharacter(AbstractDungeon.player);
            if (harshCurse != null) {
                AbstractDungeon.player.masterDeck.addToTop(harshCurse);
                UnlockTracker.markCardAsSeen(harshCurse.cardID);
            }
            if (AbstractDungeon.player.chosenClass.name().equals("THE_THORT")) {
                AbstractDungeon.player.masterDeck.group.removeIf(card -> card.cardID.equals(Normality.ID));
                AbstractCard gumOnShoe = new GumOnShoe();
                AbstractDungeon.player.masterDeck.addToTop(gumOnShoe);
                UnlockTracker.markCardAsSeen(gumOnShoe.cardID);
            }
        }
    }

    private static AbstractCard getHarshCurseForCharacter(AbstractPlayer player) {
        String className = player.chosenClass.name();

        if (className.endsWith("Prismatic")) {
            AbstractCard[] prismaticCurses = {
                    new DevilishImpurity(),
                    new ToxicImpurity(),
                    new MoroseImpurity(),
                    new ProgrammedImpurity()
            };
            return prismaticCurses[AbstractDungeon.cardRandomRng.random(prismaticCurses.length - 1)];
        }

        switch (className) {
            case "IRONCLAD":
                return new SatansGrudge();
            case "THE_SILENT":
                return new Clog();
            case "DEFECT":
                return new BadCode();
            case "WATCHER":
                return new TheBigSad();
            case "HERMIT":
                return new DeadOff();
            case "SLIMEBOUND":
                return new SplitCrap();
            case "GUARDIAN":
                return new BlackDiamond();
            case "THE_SPIRIT":
                return new FaultyGyroscope();
            case "THE_CHAMP":
                return new AmateurHour();
            case "THE_AUTOMATON":
                return new CoconutJPG();
            case "THE_COLLECTOR":
                return new LichsSoul();
            case "GREMLIN":
                return new BathroomBreak();
            case "THE_SNECKO":
                return new Eyepatch();
            case "THE_FISHING":
                return new GrizzlyBear();
            case "BLADE_GUNNER":
                return new BulletEatingSquirrel();
            case "TheBrainlets":
                return new ShareTheLove();
            case "THE_PACKMASTER":
                return new FourthRateDeck();
            case "RESEARCHERS":
                return new VeryVeryInteresting();
            case "THE_EPHEMERAL":
                return new Butterfingers();
            case "THE_VACANT":
                return new ForcedFullness();
            case "BLACKBEARD_CLASS":
                return new ScourgeOfTheCaribbean();
            case "THE_BOGWARDEN_OCEAN":
                return new Trespasser();
            case "THE_THORT":
                return new AnkleMonitor();
            default:
                return new CultistMicrophone();
        }
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(
                    UnlockTracker.class, "markCardAsSeen"
            );
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }
}