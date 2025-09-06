package harshcurses.patches;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.curses.AscendersBane;
import com.megacrit.cardcrawl.cards.curses.Normality;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import harshcurses.HarshCurses;
import harshcurses.cards.*;
import javassist.CtBehavior;

@SpirePatch(
        clz = AbstractDungeon.class,
        method = "dungeonTransitionSetup"
)
public class HarshCursesPatch {
    @SpirePostfixPatch
    public static void replaceAscendersBane() {

        if (AbstractDungeon.actNum != 1) {
            return;
        }

        if (AbstractDungeon.ascensionLevel >= 10 || HarshCurses.alwaysGiveCurse) {
            // Remove Ascender's Bane if it exists (direct collection manipulation)
            AbstractDungeon.player.masterDeck.group.removeIf(card -> card.cardID.equals(AscendersBane.ID));
            // Add the appropriate harsh curse based on character
            AbstractCard harshCurse = getHarshCurseForCharacter(AbstractDungeon.player);
            if (harshCurse != null) {
                AbstractDungeon.player.masterDeck.addToTop(harshCurse);
                UnlockTracker.markCardAsSeen(harshCurse.cardID);
            }

            if (Loader.isModLoaded("Bundle_Of_Peglin")) {
                // Remove any existing Terriball cards from the deck
                AbstractDungeon.player.masterDeck.group.removeIf(card ->
                        card.cardID.equals("Bundle_Of_Peglin:TerriballCurseSoulbound"));
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
                if (Loader.isModLoaded("downfall")) {
                    return createCardByReflection("harshcurses.cards.BlackDiamond");
                }
                break;
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
                if (Loader.isModLoaded("researchersmod")) {
                    return createCardByReflection("harshcurses.cards.VeryVeryInteresting");
                }
                break;
            case "THE_EPHEMERAL":
                return new Butterfingers();
            case "BLACKBEARD_CLASS":
                return new ScourgeOfTheCaribbean();
            case "THE_BOGWARDEN_OCEAN":
                return new Trespasser();
            case "THE_THORT":
                return new AnkleMonitor();
            default:
                return new CultistMicrophone();
        }
        return new CultistMicrophone();
    }
    private static AbstractCard createCardByReflection(String className) {
        try {
            Class<?> cardClass = Class.forName(className);
            return (AbstractCard) cardClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return new CultistMicrophone();
        }
    }
}