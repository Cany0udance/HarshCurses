package harshcurses.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.curses.AscendersBane;
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
        }
    }

    private static AbstractCard getHarshCurseForCharacter(AbstractPlayer player) {
        switch (player.chosenClass) {
            case IRONCLAD:
                return new SatansGrudge();
            case THE_SILENT:
                 return new Clog();
             case DEFECT:
                 return new BadCode();
             case WATCHER:
                 return new TheBigSad();
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