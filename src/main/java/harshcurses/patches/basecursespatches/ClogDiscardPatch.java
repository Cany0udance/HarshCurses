package harshcurses.patches.basecursespatches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import harshcurses.cards.Clog;

public class ClogDiscardPatch {

    @SpirePatch(
            clz = AbstractCard.class,
            method = "triggerOnManualDiscard"
    )
    public static class ManualDiscardPatch {

        @SpirePostfixPatch
        public static void postfix(AbstractCard __instance) {
            // Check if any Clog cards are in hand
            int clogCount = 0;
            for (AbstractCard card : AbstractDungeon.player.hand.group) {
                if (card instanceof Clog) {
                    clogCount++;
                }
            }

            // If we have Clog cards in hand, add copies to draw pile
            if (clogCount > 0) {
                // Add one copy per Clog in hand (they stack)
                for (int i = 0; i < clogCount; i++) {
                    AbstractDungeon.actionManager.addToTop(
                            new MakeTempCardInDrawPileAction(
                                    new Clog(),
                                    1,
                                    true,
                                    true
                            )
                    );
                }
            }
        }
    }
}