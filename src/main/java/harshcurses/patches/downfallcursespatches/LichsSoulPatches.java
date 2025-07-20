package harshcurses.patches.downfallcursespatches;

import basemod.ReflectionHacks;
import collector.cardmods.PyreMod;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.ExhaustAction;
import com.megacrit.cardcrawl.actions.common.ExhaustSpecificCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.unique.BlockPerNonAttackAction;
import com.megacrit.cardcrawl.actions.unique.FiendFireAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;
import harshcurses.cards.BaseCard;
import harshcurses.cards.LichsSoul;

import java.util.ArrayList;
import java.util.Random;

public class LichsSoulPatches {

    // Prevents LichsSoul from being moved to exhaust pile at the fundamental level
    @SpirePatch(clz = CardGroup.class, method = "moveToExhaustPile")
    public static class PreventMoveToExhaustPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> preventLichsSoulMoveToExhaust(CardGroup __instance, AbstractCard card) {
            // If the card is LichsSoul, flash red and prevent the move entirely
            if (LichsSoul.isLichsSoul(card)) {
                card.flash(Color.RED);
                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }
    }

    // Patch ExhaustAction to filter out LichsSoul from all exhaust operations
    @SpirePatch(clz = ExhaustAction.class, method = "update")
    public static class FilterLichsSoulFromExhaustPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> filterLichsSoulFromExhaust(ExhaustAction __instance,
                                                                   @ByRef AbstractPlayer[] ___p, @ByRef boolean[] ___isRandom, @ByRef boolean[] ___anyNumber,
                                                                   @ByRef boolean[] ___canPickZero, @ByRef int[] ___amount) {

            AbstractPlayer p = ___p[0];

            // Flash any LichsSoul cards to show they're being "considered"
            for (AbstractCard c : p.hand.group) {
                if (LichsSoul.isLichsSoul(c)) {
                    c.flash(Color.RED);
                }
            }

            // For random exhausts, we don't need to modify the action
            // The moveToExhaustPile patch will handle LichsSoul rejection
            // and the random selection will just pick another card

            return SpireReturn.Continue();
        }
    }

    // Patch CardGroup.getRandomCard to skip LichsSoul when called during exhaust
    @SpirePatch(clz = CardGroup.class, method = "getRandomCard", paramtypez = {com.megacrit.cardcrawl.random.Random.class})
    public static class SkipLichsSoulInRandomSelectionPatch {
        @SpirePostfixPatch
        public static AbstractCard skipLichsSoulForExhaust(AbstractCard __result, CardGroup __instance, com.megacrit.cardcrawl.random.Random rng) {
            // Only interfere if we're in an exhaust context and the result is LichsSoul
            if (__result != null && LichsSoul.isLichsSoul(__result)) {
                // Check if we're being called from an ExhaustAction
                boolean isExhaustContext = false;
                for (AbstractGameAction action : AbstractDungeon.actionManager.actions) {
                    if (action instanceof ExhaustAction) {
                        isExhaustContext = true;
                        break;
                    }
                }

                if (isExhaustContext) {
                    // Try to find a different card that isn't LichsSoul
                    ArrayList<AbstractCard> validCards = new ArrayList<>();
                    for (AbstractCard card : __instance.group) {
                        if (!LichsSoul.isLichsSoul(card)) {
                            validCards.add(card);
                        }
                    }

                    // If there are other cards, pick one of those instead
                    if (!validCards.isEmpty()) {
                        return validCards.get(rng.random(validCards.size() - 1));
                    }
                    // If only LichsSoul cards remain, return null
                    return null;
                }
            }

            return __result;
        }
    }

    // Prevents specific targeting of LichsSoul for exhausting (backup)
    @SpirePatch(clz = ExhaustSpecificCardAction.class, method = "update")
    public static class PreventSpecificExhaustPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> preventLichsSoulSpecificExhaust(ExhaustSpecificCardAction __instance,
                                                                        @ByRef AbstractCard[] ___targetCard) {

            // If the target card is LichsSoul, complete the action silently without exhausting
            if (LichsSoul.isLichsSoul(___targetCard[0])) {
                // Flash red to show resistance
                ___targetCard[0].flash(Color.RED);
                // Mark the action as complete and skip to next action
                __instance.isDone = true;
                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }
    }

    // Prevents LichsSoul from being selectable ONLY for exhaust in hand selection
    @SpirePatch(clz = HandCardSelectScreen.class, method = "selectHoveredCard")
    public static class PreventHandSelectionPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> preventLichsSoulSelection(HandCardSelectScreen __instance) {
            // Check if the hovered card is LichsSoul
            if (__instance.hoveredCard != null && LichsSoul.isLichsSoul(__instance.hoveredCard)) {
                // We need to determine if this is an exhaust selection
                // Check if there are any ExhaustAction or ExhaustSpecificCardAction in the action queue
                boolean isExhaustContext = false;
                for (AbstractGameAction action : AbstractDungeon.actionManager.actions) {
                    if (action instanceof ExhaustAction || action instanceof ExhaustSpecificCardAction) {
                        isExhaustContext = true;
                        break;
                    }
                }

                // Also check the selection reason text for exhaust-related keywords
                String selectionReason = __instance.selectionReason;
                if (selectionReason != null) {
                    String lowerReason = selectionReason.toLowerCase();
                    if (lowerReason.contains("exhaust") || lowerReason.contains("second wind") ||
                            lowerReason.contains("fiend fire") || lowerReason.contains("burn")) {
                        isExhaustContext = true;
                    }
                }

                // Only prevent selection if this is an exhaust context
                if (isExhaustContext) {
                    // Flash red and prevent selection
                    __instance.hoveredCard.flash(Color.RED);
                    return SpireReturn.Return(null);
                }
            }

            // Allow normal selection for non-exhaust contexts
            return SpireReturn.Continue();
        }
    }

    // Patch BlockPerNonAttackAction (Second Wind) to exclude LichsSoul from counting
    @SpirePatch(clz = BlockPerNonAttackAction.class, method = "update")
    public static class FixSecondWindCountingPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> fixSecondWindCounting(BlockPerNonAttackAction __instance) {
            ArrayList<AbstractCard> cardsToExhaust = new ArrayList<>();

            // Count only non-attack cards that aren't LichsSoul
            for (AbstractCard c : AbstractDungeon.player.hand.group) {
                if (c.type != AbstractCard.CardType.ATTACK && !LichsSoul.isLichsSoul(c)) {
                    cardsToExhaust.add(c);
                }
            }

            // Flash any LichsSoul cards that were skipped
            for (AbstractCard c : AbstractDungeon.player.hand.group) {
                if (c.type != AbstractCard.CardType.ATTACK && LichsSoul.isLichsSoul(c)) {
                    c.flash(Color.RED);
                }
            }

            // Get the block amount per card
            int blockPerCard = ReflectionHacks.getPrivate(__instance, BlockPerNonAttackAction.class, "blockPerCard");

            // Add block actions for valid cards only
            for (AbstractCard c : cardsToExhaust) {
                ReflectionHacks.privateMethod(AbstractGameAction.class, "addToTop", AbstractGameAction.class)
                        .invoke(__instance, new GainBlockAction(AbstractDungeon.player, AbstractDungeon.player, blockPerCard));
            }

            // Add exhaust actions for valid cards
            for (AbstractCard c : cardsToExhaust) {
                ReflectionHacks.privateMethod(AbstractGameAction.class, "addToTop", AbstractGameAction.class)
                        .invoke(__instance, new ExhaustSpecificCardAction(c, AbstractDungeon.player.hand));
            }

            __instance.isDone = true;
            return SpireReturn.Return(null);
        }
    }

    // Patch FiendFireAction to exclude LichsSoul from counting
    @SpirePatch(clz = FiendFireAction.class, method = "update")
    public static class FixFiendFireCountingPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> fixFiendFireCounting(FiendFireAction __instance) {
            // Count cards that aren't LichsSoul
            int validCardCount = 0;
            for (AbstractCard c : AbstractDungeon.player.hand.group) {
                if (!LichsSoul.isLichsSoul(c)) {
                    validCardCount++;
                } else {
                    c.flash(Color.RED);
                }
            }

            // Get the damage info
            DamageInfo info = ReflectionHacks.getPrivate(__instance, FiendFireAction.class, "info");

            // Add damage actions for valid cards only
            for (int i = 0; i < validCardCount; i++) {
                ReflectionHacks.privateMethod(AbstractGameAction.class, "addToTop", AbstractGameAction.class)
                        .invoke(__instance, new DamageAction(__instance.target, info, AbstractGameAction.AttackEffect.FIRE));
            }

            // Add exhaust actions for valid cards only
            for (int i = 0; i < validCardCount; i++) {
                if (Settings.FAST_MODE) {
                    ReflectionHacks.privateMethod(AbstractGameAction.class, "addToTop", AbstractGameAction.class)
                            .invoke(__instance, new ExhaustAction(1, true, true, false, Settings.ACTION_DUR_XFAST));
                } else {
                    ReflectionHacks.privateMethod(AbstractGameAction.class, "addToTop", AbstractGameAction.class)
                            .invoke(__instance, new ExhaustAction(1, true, true));
                }
            }

            __instance.isDone = true;
            return SpireReturn.Return(null);
        }
    }

    // Patch PyreMod to exclude LichsSoul from hand size calculation
    @SpirePatch(clz = PyreMod.class, method = "canPlayCard")
    public static class FixPyreModCountingPatch {
        @SpirePrefixPatch
        public static SpireReturn<Boolean> fixPyreModCounting(PyreMod __instance, AbstractCard card) {
            // Count cards that aren't LichsSoul
            int validCardCount = 0;
            for (AbstractCard c : AbstractDungeon.player.hand.group) {
                if (!LichsSoul.isLichsSoul(c)) {
                    validCardCount++;
                }
            }

            // Check if we have more than 1 valid card (excluding LichsSoul)
            if (validCardCount - 1 <= 0) {
                UIStrings uiStrings = ReflectionHacks.getPrivateStatic(PyreMod.class, "uiStrings");
                card.cantUseMessage = uiStrings.TEXT[0];
                return SpireReturn.Return(false);
            } else {
                return SpireReturn.Return(true);
            }
        }
    }
}