package harshcurses.patches.downfallcursespatches;

import automaton.AutomatonMod;
import automaton.FunctionHelper;
import automaton.cardmods.CardEffectsCardMod;
import automaton.cards.AbstractBronzeCard;
import automaton.cards.FunctionCard;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import harshcurses.cards.CoconutJPG;

import java.util.ArrayList;
import java.util.Iterator;

public class CoconutPatches {

    @SpirePatch(
            clz = CardEffectsCardMod.class,
            method = "modifyDamage",
            paramtypez = {float.class, DamageInfo.DamageType.class, AbstractCard.class, AbstractMonster.class}
    )
    public static class FunctionCardDamagePatch {
        @SpirePrefixPatch
        public static SpireReturn<Float> Prefix(CardEffectsCardMod __instance, float damage, DamageInfo.DamageType type, AbstractCard card, AbstractMonster target) {
            // If stored() would return null, just return the original damage
            if (__instance.stored() == null) {
                return SpireReturn.Return(damage);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = CardEffectsCardMod.class,
            method = "modifyDescription",
            paramtypez = {String.class, AbstractCard.class}
    )
    public static class FunctionCardDescPatch {
        @SpirePrefixPatch
        public static SpireReturn<String> Prefix(CardEffectsCardMod __instance, String rawDescription, AbstractCard card) {
            // If stored() would return null, just return the original description
            if (__instance.stored() == null) {
                return SpireReturn.Return(rawDescription);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = FunctionCard.class,
            method = "onMoveToDiscard"
    )
    public static class FunctionCardDiscardPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(FunctionCard __instance) {
            // Replace the method to handle null stored cards safely
            if (CardModifierManager.hasModifier(__instance, CardEffectsCardMod.ID)) {
                Iterator var1 = CardModifierManager.getModifiers(__instance, CardEffectsCardMod.ID).iterator();
                while(var1.hasNext()) {
                    AbstractCardModifier m = (AbstractCardModifier)var1.next();
                    if (m instanceof CardEffectsCardMod) {
                        AbstractBronzeCard stored = ((CardEffectsCardMod)m).stored();
                        if (stored != null) {
                            stored.resetAttributes();
                        }
                    }
                }
                __instance.initializeDescription();
            }
            return SpireReturn.Return(null);
        }
    }

    @SpirePatch(
            clz = CardEffectsCardMod.class,
            method = "isFinalCardEffectsFunction",
            paramtypez = {AbstractCard.class}
    )
    public static class FunctionCardFinalPatch {
        @SpirePrefixPatch
        public static SpireReturn<Boolean> Prefix(CardEffectsCardMod __instance, AbstractCard card) {
            // Completely replace the method to handle null stored cards safely
            boolean yesIAmTheFinalCardWoo = false;
            Iterator var3 = CardModifierManager.getModifiers(card, CardEffectsCardMod.ID).iterator();
            while(var3.hasNext()) {
                AbstractCardModifier c = (AbstractCardModifier)var3.next();
                if (!(c instanceof CardEffectsCardMod)) continue;
                CardEffectsCardMod cardMod = (CardEffectsCardMod)c;
                if (cardMod == __instance) {
                    yesIAmTheFinalCardWoo = true;
                } else {
                    // Safely check if stored card exists and has the tag
                    AbstractBronzeCard stored = cardMod.stored();
                    if (stored == null || !stored.hasTag(AutomatonMod.ADDS_NO_CARDTEXT) || !yesIAmTheFinalCardWoo) {
                        yesIAmTheFinalCardWoo = false;
                    }
                }
            }
            return SpireReturn.Return(yesIAmTheFinalCardWoo);
        }
    }

    @SpirePatch(
            clz = CardEffectsCardMod.class,
            method = "onInitialApplication",
            paramtypez = {AbstractCard.class}
    )
    public static class FunctionCardInitPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(CardEffectsCardMod __instance, AbstractCard card) {
            // If stored() would return null, skip the initialization
            if (__instance.stored() == null) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = CardEffectsCardMod.class,
            method = "onApplyPowers",
            paramtypez = {AbstractCard.class}
    )
    public static class FunctionCardPowersPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(CardEffectsCardMod __instance, AbstractCard card) {
            // If stored() would return null, skip applying powers
            if (__instance.stored() == null) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = FunctionCard.class,
            method = "triplicateCheck"
    )
    public static class FunctionCardTriplicatePatch {
        @SpirePrefixPatch
        public static SpireReturn<Boolean> Prefix(FunctionCard __instance) {
            // If any stored card is null, can't do triplicate check
            ArrayList<AbstractBronzeCard> cards = __instance.cards();
            if (cards.isEmpty() || cards.contains(null)) {
                return SpireReturn.Return(false);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = CardEffectsCardMod.class,
            method = "stored"
    )
    public static class FunctionCardTypePatch {
        @SpirePrefixPatch
        public static SpireReturn<AbstractBronzeCard> Prefix(CardEffectsCardMod __instance) {
            if (FunctionHelper.cardModsInfo.containsKey(__instance)) {
                AbstractCard card = (AbstractCard) FunctionHelper.cardModsInfo.get(__instance);
                if (!(card instanceof AbstractBronzeCard)) {
                    // For non-Bronze cards, return null - other patches will handle this
                    return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = CardEffectsCardMod.class,
            method = "onUse",
            paramtypez = {AbstractCard.class, AbstractCreature.class, UseCardAction.class}
    )
    public static class FunctionCardUsePatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(CardEffectsCardMod __instance, AbstractCard card, AbstractCreature target, UseCardAction action) {
            // If stored() would return null, skip the use effect
            if (__instance.stored() == null) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = FunctionCard.class,
            method = "getEncodablePortrait",
            paramtypez = {AbstractCard.class}
    )
    public static class FunctionCardPortraitPatch {
        @SpirePrefixPatch
        public static SpireReturn<TextureAtlas.AtlasRegion> Prefix(FunctionCard __instance, AbstractCard c) {
            // If the card is null, try to find the original non-Bronze card
            if (c == null) {
                // Look through the function's modifiers to find a non-Bronze card
                for (AbstractCardModifier mod : CardModifierManager.getModifiers(__instance, CardEffectsCardMod.ID)) {
                    if (mod instanceof CardEffectsCardMod) {
                        CardEffectsCardMod cardMod = (CardEffectsCardMod) mod;
                        if (FunctionHelper.cardModsInfo.containsKey(cardMod)) {
                            AbstractCard originalCard = FunctionHelper.cardModsInfo.get(cardMod);
                            if (!(originalCard instanceof AbstractBronzeCard)) {
                                // Found a non-Bronze card, use its portrait
                                boolean betaArt = UnlockTracker.betaCardPref.getBoolean(originalCard.cardID, false) || Settings.PLAYTESTER_ART_MODE;
                                return SpireReturn.Return(betaArt ? originalCard.jokePortrait : originalCard.portrait);
                            }
                        }
                    }
                }
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = FunctionCard.class,
            method = "doNothingSpecificInParticular"
    )
    public static class FunctionCardNamingPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(FunctionCard __instance) {
            // Check if we have any non-Bronze cards and handle naming
            boolean hasNonBronze = false;
            String nonBronzeName = "";

            for (AbstractCardModifier mod : CardModifierManager.getModifiers(__instance, CardEffectsCardMod.ID)) {
                if (mod instanceof CardEffectsCardMod) {
                    CardEffectsCardMod cardMod = (CardEffectsCardMod) mod;
                    if (FunctionHelper.cardModsInfo.containsKey(cardMod)) {
                        AbstractCard originalCard = FunctionHelper.cardModsInfo.get(cardMod);
                        if (!(originalCard instanceof AbstractBronzeCard)) {
                            hasNonBronze = true;
                            // Extract a reasonable name from the non-Bronze card
                            String cardName = originalCard.name;
                            if (cardName != null && !cardName.isEmpty()) {
                                // Clean up the card name (remove file extensions, etc.)
                                nonBronzeName = cardName.replaceAll("\\..*", ""); // Remove file extensions like .jpg
                            }
                            break; // Use the first non-Bronze card found
                        }
                    }
                }
            }

            if (hasNonBronze && !nonBronzeName.isEmpty()) {
                if (__instance.textPrefix.equals("")) {
                    __instance.name = nonBronzeName;
                } else {
                    __instance.name = __instance.textPrefix + nonBronzeName;
                }
                // Just call initializeDescription since we can't access initializeTitle
                __instance.initializeDescription();
                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            method = "hover"
    )
    public static class CoconutSequenceHoverPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(AbstractCard __instance) {
            // Only apply to CoconutJPG cards
            if (!(__instance instanceof CoconutJPG)) {
                return SpireReturn.Continue();
            }

            // Check if this card is in the function sequence
            if (FunctionHelper.held != null && FunctionHelper.held.contains(__instance)) {
                // Use Bronze card sequence hover behavior - don't expand normally
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            method = "unhover"
    )
    public static class CoconutSequenceUnhoverPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(AbstractCard __instance) {
            // Only apply to CoconutJPG cards
            if (!(__instance instanceof CoconutJPG)) {
                return SpireReturn.Continue();
            }

            // Check if this card is in the function sequence
            if (FunctionHelper.held != null && FunctionHelper.held.contains(__instance)) {
                // Use Bronze card sequence unhover behavior
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}