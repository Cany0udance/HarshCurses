package harshcurses.patches.othermodpatches;

import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import harshcurses.cards.GumOnShoe;

public class GumOnShoePatch {

    // Patch AbstractPlayer.gainGold to reduce gold gained by 50% when GumOnShoe is in deck
    @SpirePatch(
            clz = AbstractPlayer.class,
            method = "gainGold"
    )
    public static class ReduceGoldGainPatch {
        @SpirePrefixPatch
        public static void reduceGoldWithGumOnShoe(AbstractPlayer __instance, @ByRef int[] amount) {
            // Check if GumOnShoe is in the entire deck (master deck)
            if (hasGumOnShoe(__instance)) {
                // Reduce gold gain by 50%
                amount[0] = amount[0] / 2;
            }
        }
    }

    private static boolean hasGumOnShoe(AbstractPlayer player) {
        return player.masterDeck.group.stream()
                .anyMatch(card -> card instanceof GumOnShoe);
    }
}