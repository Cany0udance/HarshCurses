package harshcurses.patches.downfallcursespatches;

import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import harshcurses.cards.Eyepatch;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class EyepatchPatches {
    private static int storedCost = -1;

    @SpirePatch(
            optional = true,
            cls = "sneckomod.actions.MuddleAction",
            method = "update"
    )
    public static class MuddleActionPrefixPatch {
        @SpirePrefixPatch
        public static void Prefix(Object __instance) {
            try {
                // Store the current cost before muddling happens
                Class<?> muddleActionClass = Class.forName("sneckomod.actions.MuddleAction");
                Field cardField = muddleActionClass.getDeclaredField("card");
                cardField.setAccessible(true);
                AbstractCard card = (AbstractCard) cardField.get(__instance);
                storedCost = card.costForTurn;
            } catch (Exception e) {
                storedCost = -1;
            }
        }
    }

    @SpirePatch(
            optional = true,
            cls = "sneckomod.actions.MuddleAction",
            method = "update"
    )
    public static class MuddleActionPostfixPatch {
        @SpirePostfixPatch
        public static void Postfix(Object __instance) {
            // Check if Eyepatch is in hand
            AbstractCard eyepatch = AbstractDungeon.player.hand.group.stream()
                    .filter(card -> card instanceof Eyepatch) // Replace with your actual Eyepatch class
                    .findFirst()
                    .orElse(null);

            if (eyepatch != null) {
                try {
                    // Get the MuddleAction class and access the card field
                    Class<?> muddleActionClass = Class.forName("sneckomod.actions.MuddleAction");
                    Field cardField = muddleActionClass.getDeclaredField("card");
                    cardField.setAccessible(true);
                    AbstractCard card = (AbstractCard) cardField.get(__instance);

                    // Get the cost that was stored before muddling
                    int costBeforeMuddling = storedCost;
                    int currentCost = card.costForTurn;

                    // Only intervene if the muddling made the cost lower than it was before
                    if (costBeforeMuddling >= 0 && currentCost < costBeforeMuddling) {
                        // Reroll, but only allow costs >= the cost before muddling
                        ArrayList<Integer> validCosts = new ArrayList<>();
                        for (int i = costBeforeMuddling; i <= 3; i++) {
                            validCosts.add(i);
                        }

                        // Pick randomly from valid costs
                        if (!validCosts.isEmpty()) {
                            int newCost = validCosts.get(AbstractDungeon.cardRandomRng.random(validCosts.size() - 1));
                            card.setCostForTurn(newCost);

                            eyepatch.flash(Color.RED);
                        }
                    }
                } catch (Exception e) {
                    // Silently fail if SneckoMod isn't loaded or structure changed
                }
            }
        }
    }
}