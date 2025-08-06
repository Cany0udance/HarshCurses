package harshcurses.patches.downfallcursespatches;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.BlockPerNonAttackAction;
import com.megacrit.cardcrawl.actions.unique.FiendFireAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;
import harshcurses.cards.BlackDiamond;
import harshcurses.cards.LichsSoul;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class LichsSoulPatches {
    // Helper method to check if a card is either LichsSoul or BlackDiamond
    private static boolean isProtectedFromExhaust(AbstractCard card) {
        return LichsSoul.isLichsSoul(card) || BlackDiamond.isBlackDiamond(card);
    }

    // Prevents LichsSoul and BlackDiamond from being moved to exhaust pile at the fundamental level
    @SpirePatch(clz = CardGroup.class, method = "moveToExhaustPile")
    public static class PreventMoveToExhaustPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> preventProtectedCardsMoveToExhaust(CardGroup __instance, AbstractCard card) {
            // Only apply if Downfall mod is loaded
            if (!Loader.isModLoaded("downfall")) {
                return SpireReturn.Continue();
            }
            // If the card is LichsSoul or BlackDiamond, flash red and prevent the move entirely
            if (isProtectedFromExhaust(card)) {
                card.flash(Color.RED);
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    // Patch ExhaustAction to filter out LichsSoul and BlackDiamond from all exhaust operations
    @SpirePatch(clz = ExhaustAction.class, method = "update")
    public static class FilterProtectedCardsFromExhaustPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> filterProtectedCardsFromExhaust(ExhaustAction __instance,
                                                                        @ByRef AbstractPlayer[] ___p, @ByRef boolean[] ___isRandom, @ByRef boolean[] ___anyNumber,
                                                                        @ByRef boolean[] ___canPickZero, @ByRef int[] ___amount) {
            // Only apply if Downfall mod is loaded
            if (!Loader.isModLoaded("downfall")) {
                return SpireReturn.Continue();
            }
            AbstractPlayer p = ___p[0];
            // Flash any protected cards to show they're being "considered"
            for (AbstractCard c : p.hand.group) {
                if (isProtectedFromExhaust(c)) {
                    c.flash(Color.RED);
                }
            }
            // For random exhausts, we don't need to modify the action
            // The moveToExhaustPile patch will handle protected card rejection
            // and the random selection will just pick another card
            return SpireReturn.Continue();
        }
    }

    // Patch CardGroup.getRandomCard to skip protected cards when called during exhaust
    @SpirePatch(clz = CardGroup.class, method = "getRandomCard", paramtypez = {com.megacrit.cardcrawl.random.Random.class})
    public static class SkipProtectedCardsInRandomSelectionPatch {
        @SpirePostfixPatch
        public static AbstractCard skipProtectedCardsForExhaust(AbstractCard __result, CardGroup __instance, com.megacrit.cardcrawl.random.Random rng) {
            // Only apply if Downfall mod is loaded
            if (!Loader.isModLoaded("downfall")) {
                return __result;
            }
            // Only interfere if we're in an exhaust context and the result is protected
            if (__result != null && isProtectedFromExhaust(__result)) {
                // Check if we're being called from an ExhaustAction
                boolean isExhaustContext = false;
                for (AbstractGameAction action : AbstractDungeon.actionManager.actions) {
                    if (action instanceof ExhaustAction) {
                        isExhaustContext = true;
                        break;
                    }
                }
                if (isExhaustContext) {
                    // Try to find a different card that isn't protected
                    ArrayList<AbstractCard> validCards = new ArrayList<>();
                    for (AbstractCard card : __instance.group) {
                        if (!isProtectedFromExhaust(card)) {
                            validCards.add(card);
                        }
                    }
                    // If there are other cards, pick one of those instead
                    if (!validCards.isEmpty()) {
                        return validCards.get(rng.random(validCards.size() - 1));
                    }
                    // If only protected cards remain, return null
                    return null;
                }
            }
            return __result;
        }
    }

    // Prevents specific targeting of protected cards for exhausting (backup)
    @SpirePatch(clz = ExhaustSpecificCardAction.class, method = "update")
    public static class PreventSpecificExhaustPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> preventProtectedCardSpecificExhaust(ExhaustSpecificCardAction __instance,
                                                                            @ByRef AbstractCard[] ___targetCard) {
            // Only apply if Downfall mod is loaded
            if (!Loader.isModLoaded("downfall")) {
                return SpireReturn.Continue();
            }
            // If the target card is protected, complete the action silently without exhausting
            if (isProtectedFromExhaust(___targetCard[0])) {
                // Flash red to show resistance
                ___targetCard[0].flash(Color.RED);
                // Mark the action as complete and skip to next action
                __instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    // Prevents protected cards from being selectable ONLY for exhaust in hand selection
    @SpirePatch(clz = HandCardSelectScreen.class, method = "selectHoveredCard")
    public static class PreventHandSelectionPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> preventProtectedCardSelection(HandCardSelectScreen __instance) {
            // Only apply if Downfall mod is loaded
            if (!Loader.isModLoaded("downfall")) {
                return SpireReturn.Continue();
            }
            // Check if the hovered card is protected
            if (__instance.hoveredCard != null && isProtectedFromExhaust(__instance.hoveredCard)) {
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

    // Patch BlockPerNonAttackAction (Second Wind) to exclude protected cards from counting
    @SpirePatch(clz = BlockPerNonAttackAction.class, method = "update")
    public static class FixSecondWindCountingPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> fixSecondWindCounting(BlockPerNonAttackAction __instance) {
            // Only apply if Downfall mod is loaded
            if (!Loader.isModLoaded("downfall")) {
                return SpireReturn.Continue();
            }
            ArrayList<AbstractCard> cardsToExhaust = new ArrayList<>();
            // Count only non-attack cards that aren't protected
            for (AbstractCard c : AbstractDungeon.player.hand.group) {
                if (c.type != AbstractCard.CardType.ATTACK && !isProtectedFromExhaust(c)) {
                    cardsToExhaust.add(c);
                }
            }
            // Flash any protected cards that were skipped
            for (AbstractCard c : AbstractDungeon.player.hand.group) {
                if (c.type != AbstractCard.CardType.ATTACK && isProtectedFromExhaust(c)) {
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

    // Patch FiendFireAction to exclude protected cards from counting
    @SpirePatch(clz = FiendFireAction.class, method = "update")
    public static class FixFiendFireCountingPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> fixFiendFireCounting(FiendFireAction __instance) {
            // Only apply if Downfall mod is loaded
            if (!Loader.isModLoaded("downfall")) {
                return SpireReturn.Continue();
            }
            // Collect all cards that aren't protected
            ArrayList<AbstractCard> cardsToExhaust = new ArrayList<>();
            for (AbstractCard c : AbstractDungeon.player.hand.group) {
                if (!isProtectedFromExhaust(c)) {
                    cardsToExhaust.add(c);
                } else {
                    c.flash(Color.RED);
                }
            }
            // Get the damage info
            DamageInfo info = ReflectionHacks.getPrivate(__instance, FiendFireAction.class, "info");
            // Add damage actions for valid cards only
            for (int i = 0; i < cardsToExhaust.size(); i++) {
                ReflectionHacks.privateMethod(AbstractGameAction.class, "addToTop", AbstractGameAction.class)
                        .invoke(__instance, new DamageAction(__instance.target, info, AbstractGameAction.AttackEffect.FIRE));
            }
            // Add specific exhaust actions for each valid card
            for (AbstractCard cardToExhaust : cardsToExhaust) {
                ReflectionHacks.privateMethod(AbstractGameAction.class, "addToTop", AbstractGameAction.class)
                        .invoke(__instance, new ExhaustSpecificCardAction(cardToExhaust, AbstractDungeon.player.hand));
            }
            __instance.isDone = true;
            return SpireReturn.Return(null);
        }
    }

    // Patch PyreMod to exclude protected cards from hand size calculation
    @SpirePatch(
            optional = true,
            cls = "collector.cardmods.PyreMod",
            method = "canPlayCard"
    )
    public static class FixPyreModCountingPatch {
        @SpirePrefixPatch
        public static SpireReturn<Boolean> fixPyreModCounting(Object __instance, AbstractCard card) {
            // Only apply if Downfall mod is loaded
            if (!Loader.isModLoaded("downfall")) {
                return SpireReturn.Continue();
            }
            try {
                // Count cards that aren't protected
                int validCardCount = 0;
                for (AbstractCard c : AbstractDungeon.player.hand.group) {
                    if (!isProtectedFromExhaust(c)) {
                        validCardCount++;
                    }
                }
                // Check if we have more than 1 valid card (excluding protected cards)
                if (validCardCount - 1 <= 0) {
                    Class<?> pyreModClass = Class.forName("collector.cardmods.PyreMod");
                    Field uiStringsField = pyreModClass.getDeclaredField("uiStrings");
                    uiStringsField.setAccessible(true);
                    Object uiStrings = uiStringsField.get(null);
                    Field textField = uiStrings.getClass().getField("TEXT");
                    String[] textArray = (String[]) textField.get(uiStrings);
                    card.cantUseMessage = textArray[0];
                    return SpireReturn.Return(false);
                } else {
                    return SpireReturn.Return(true);
                }
            } catch (Exception e) {
                return SpireReturn.Continue();
            }
        }
    }

    // Patch PlayTopCardAction to skip protected cards when exhausting is enabled
    @SpirePatch(clz = PlayTopCardAction.class, method = "update")
    public static class SkipProtectedCardsInPlayTopCardPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> skipProtectedCardsPlayTopCard(PlayTopCardAction __instance) {
            // Only apply if Downfall mod is loaded
            if (!Loader.isModLoaded("downfall")) {
                return SpireReturn.Continue();
            }
            // Get the exhaustCards field
            boolean exhaustCards = ReflectionHacks.getPrivate(__instance, PlayTopCardAction.class, "exhaustCards");
            // Only interfere if this action would exhaust the card
            if (!exhaustCards) {
                return SpireReturn.Continue();
            }
            // Get the duration field using reflection
            float duration = ReflectionHacks.getPrivate(__instance, AbstractGameAction.class, "duration");
            if (duration == Settings.ACTION_DUR_FAST) {
                if (AbstractDungeon.player.drawPile.size() + AbstractDungeon.player.discardPile.size() == 0) {
                    __instance.isDone = true;
                    return SpireReturn.Return(null);
                }
                if (AbstractDungeon.player.drawPile.isEmpty()) {
                    ReflectionHacks.privateMethod(AbstractGameAction.class, "addToTop", AbstractGameAction.class)
                            .invoke(__instance, new PlayTopCardAction(__instance.target, exhaustCards));
                    ReflectionHacks.privateMethod(AbstractGameAction.class, "addToTop", AbstractGameAction.class)
                            .invoke(__instance, new EmptyDeckShuffleAction());
                    __instance.isDone = true;
                    return SpireReturn.Return(null);
                }
                if (!AbstractDungeon.player.drawPile.isEmpty()) {
                    AbstractCard topCard = AbstractDungeon.player.drawPile.getTopCard();
                    // If the top card is protected and we would exhaust it, skip this action entirely
                    if (isProtectedFromExhaust(topCard)) {
                        topCard.flash(Color.RED);
                        __instance.isDone = true;
                        return SpireReturn.Return(null);
                    }
                }
            }
            // Continue with normal execution for non-protected cards
            return SpireReturn.Continue();
        }
    }
}