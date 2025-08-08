package harshcurses.patches.othermodpatches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import harshcurses.cards.ForcedFullness;

public class ForcedFullnessPatch {

    // Patch AbstractVacantCard.getHollow to prevent being Hollow when ForcedFullness is in hand
    @SpirePatch(
            optional = true,
            cls = "theVacant.cards.AbstractVacantCard",
            method = "getHollow"
    )
    public static class PreventHollowPatch {
        @SpirePostfixPatch
        public static boolean preventHollowWithForcedFullness(boolean __result) {
            // If ForcedFullness is in hand, cannot be Hollow
            if (hasForcedFullnessInHand()) {
                return false;
            }

            return __result;
        }
    }

    private static boolean hasForcedFullnessInHand() {
        return AbstractDungeon.player.hand.group.stream()
                .anyMatch(card -> card instanceof ForcedFullness);
    }
}