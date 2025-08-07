package harshcurses.patches.othermodpatches;

import basemod.BaseMod;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import harshcurses.cards.FourthRateDeck;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FourthRateDeckPatches {

    // Cache the reflection results to avoid repeated lookups
    private static Class<?> spireAnniv5Class = null;
    private static Method getMethod = null;
    private static Object cardParentMap = null;
    private static boolean reflectionInitialized = false;
    private static boolean packmasterAvailable = false;

    // Patch the canUse method to prevent playing cards from the most prevalent pack
    @SpirePatch(
            optional = true,
            cls = "com.megacrit.cardcrawl.cards.AbstractCard",
            method = "canUse"
    )
    public static class CanUsePatch {
        @SpirePostfixPatch
        public static boolean Postfix(boolean __result, AbstractCard __instance, AbstractPlayer p, AbstractMonster m) {
            // Only proceed if the card would normally be playable and we have the curse in our hand
            if (__result && hasFourthRateDeck()) {
                if (isFromMostPrevalentPack(__instance)) {
                    return false;
                }
            }
            return __result;
        }
    }

    private static boolean hasFourthRateDeck() {
        return AbstractDungeon.player.hand.group.stream()
                .anyMatch(card -> card instanceof FourthRateDeck);
    }

    private static void initializeReflection() {
        if (reflectionInitialized) return;

        reflectionInitialized = true;

        try {
            // Try different possible class names
            String[] possibleClassNames = {
                    "thePackmaster.SpireAnniversary5Mod",
                    "SpireAnniversary5Mod",
                    "com.megacrit.cardcrawl.mod.SpireAnniversary5Mod",
                    "spireanniversary5.SpireAnniversary5Mod",
                    "anniv5.SpireAnniversary5Mod"
            };

            for (String className : possibleClassNames) {
                try {
                    spireAnniv5Class = Class.forName(className);
                    break;
                } catch (ClassNotFoundException e) {
                    // Continue trying other names
                }
            }

            if (spireAnniv5Class == null) {
                packmasterAvailable = false;
                return;
            }

            Field cardParentMapField = spireAnniv5Class.getDeclaredField("cardParentMap");
            cardParentMapField.setAccessible(true);
            cardParentMap = cardParentMapField.get(null);
            getMethod = cardParentMap.getClass().getMethod("get", Object.class);

            packmasterAvailable = (cardParentMap != null);

        } catch (Exception e) {
            packmasterAvailable = false;
        }
    }

    private static boolean isFromMostPrevalentPack(AbstractCard card) {
        initializeReflection();

        if (!packmasterAvailable) {
            return false;
        }

        try {
            String cardPack = (String) getMethod.invoke(cardParentMap, card.cardID);

            if (cardPack == null) {
                return false; // Card is not from any pack
            }

            // Count cards in master deck by pack
            Map<String, Integer> packCounts = new HashMap<>();

            for (AbstractCard masterCard : AbstractDungeon.player.masterDeck.group) {
                String masterCardPack = (String) getMethod.invoke(cardParentMap, masterCard.cardID);
                if (masterCardPack != null) {
                    packCounts.put(masterCardPack, packCounts.getOrDefault(masterCardPack, 0) + 1);
                }
            }

            if (packCounts.isEmpty()) {
                return false; // No pack cards in deck
            }

            // Find the pack(s) with the most cards
            int maxCount = packCounts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
            int currentPackCount = packCounts.getOrDefault(cardPack, 0);

            // Return true if this card's pack has the maximum count
            return currentPackCount == maxCount;

        } catch (Exception e) {
            return false;
        }
    }
}