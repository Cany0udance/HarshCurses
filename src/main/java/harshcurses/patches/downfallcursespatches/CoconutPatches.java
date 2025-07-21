package harshcurses.patches.downfallcursespatches;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.Loader;
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CoconutPatches {
    @SpirePatch(
            optional = true,
            cls = "automaton.cardmods.CardEffectsCardMod",
            method = "modifyDamage",
            paramtypez = {float.class, DamageInfo.DamageType.class, AbstractCard.class, AbstractMonster.class}
    )
    public static class FunctionCardDamagePatch {
        @SpirePrefixPatch
        public static SpireReturn<Float> Prefix(Object __instance, float damage, DamageInfo.DamageType type, AbstractCard card, AbstractMonster target) {
            try {
                // Use reflection to call stored() method
                Method storedMethod = __instance.getClass().getMethod("stored");
                Object stored = storedMethod.invoke(__instance);
                if (stored == null) {
                    return SpireReturn.Return(damage);
                }
            } catch (Exception e) {
                return SpireReturn.Return(damage);
            }
            return SpireReturn.Continue();
        }
    }
    @SpirePatch(
            optional = true,
            cls = "automaton.cardmods.CardEffectsCardMod",
            method = "modifyDescription",
            paramtypez = {String.class, AbstractCard.class}
    )
    public static class FunctionCardDescPatch {
        @SpirePrefixPatch
        public static SpireReturn<String> Prefix(Object __instance, String rawDescription, AbstractCard card) {
            try {
                Method storedMethod = __instance.getClass().getMethod("stored");
                Object stored = storedMethod.invoke(__instance);
                if (stored == null) {
                    return SpireReturn.Return(rawDescription);
                }
            } catch (Exception e) {
                return SpireReturn.Return(rawDescription);
            }
            return SpireReturn.Continue();
        }
    }
    @SpirePatch(
            optional = true,
            cls = "automaton.cards.FunctionCard",
            method = "onMoveToDiscard"
    )
    public static class FunctionCardDiscardPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(Object __instance) {
            try {
                // Use reflection to safely handle the discard logic
                Class<?> cardModManagerClass = Class.forName("basemod.helpers.CardModifierManager");
                Method hasModifierMethod = cardModManagerClass.getMethod("hasModifier", AbstractCard.class, String.class);
                Method getModifiersMethod = cardModManagerClass.getMethod("getModifiers", AbstractCard.class, String.class);

                String cardEffectsId = "automaton:CardEffectsCardMod"; // Assuming this is the ID
                boolean hasModifier = (Boolean) hasModifierMethod.invoke(null, __instance, cardEffectsId);

                if (hasModifier) {
                    @SuppressWarnings("unchecked")
                    List<Object> modifiers = (List<Object>) getModifiersMethod.invoke(null, __instance, cardEffectsId);

                    for (Object m : modifiers) {
                        Class<?> cardEffectsClass = Class.forName("automaton.cardmods.CardEffectsCardMod");
                        if (cardEffectsClass.isInstance(m)) {
                            Method storedMethod = m.getClass().getMethod("stored");
                            Object stored = storedMethod.invoke(m);
                            if (stored != null) {
                                Method resetMethod = stored.getClass().getMethod("resetAttributes");
                                resetMethod.invoke(stored);
                            }
                        }
                    }
                    Method initDescMethod = __instance.getClass().getMethod("initializeDescription");
                    initDescMethod.invoke(__instance);
                }
            } catch (Exception e) {
                // Silently fail
            }
            return SpireReturn.Return(null);
        }
    }
    @SpirePatch(
            optional = true,
            cls = "automaton.cardmods.CardEffectsCardMod",
            method = "isFinalCardEffectsFunction",
            paramtypez = {AbstractCard.class}
    )
    public static class FunctionCardFinalPatch {
        @SpirePrefixPatch
        public static SpireReturn<Boolean> Prefix(Object __instance, AbstractCard card) {
            try {
                Class<?> cardModManagerClass = Class.forName("basemod.helpers.CardModifierManager");
                Method getModifiersMethod = cardModManagerClass.getMethod("getModifiers", AbstractCard.class, String.class);

                String cardEffectsId = "bronze:CardEffectsCardMod";
                @SuppressWarnings("unchecked")
                List<Object> modifiers = (List<Object>) getModifiersMethod.invoke(null, card, cardEffectsId);

                boolean yesIAmTheFinalCardWoo = false;
                for (Object c : modifiers) {
                    Class<?> cardEffectsClass = Class.forName("automaton.cardmods.CardEffectsCardMod");
                    if (!cardEffectsClass.isInstance(c)) continue;

                    if (c == __instance) {
                        yesIAmTheFinalCardWoo = true;
                    } else {
                        Method storedMethod = c.getClass().getMethod("stored");
                        Object stored = storedMethod.invoke(c);
                        if (stored == null) {
                            yesIAmTheFinalCardWoo = false;
                        } else {
                            // Check if stored card has ADDS_NO_CARDTEXT tag
                            try {
                                Class<?> automatonModClass = Class.forName("automaton.AutomatonMod");
                                Field addsNoCardtextField = automatonModClass.getField("ADDS_NO_CARDTEXT");
                                Object addsNoCardtextTag = addsNoCardtextField.get(null);
                                Method hasTagMethod = stored.getClass().getMethod("hasTag", Object.class);
                                Boolean hasTag = (Boolean) hasTagMethod.invoke(stored, addsNoCardtextTag);
                                if (!hasTag || !yesIAmTheFinalCardWoo) {
                                    yesIAmTheFinalCardWoo = false;
                                }
                            } catch (Exception e) {
                                // If we can't check the tag, assume it doesn't have it
                                yesIAmTheFinalCardWoo = false;
                            }
                        }
                    }
                }
                return SpireReturn.Return(yesIAmTheFinalCardWoo);
            } catch (Exception e) {
                return SpireReturn.Return(false);
            }
        }
    }
    @SpirePatch(
            optional = true,
            cls = "automaton.cardmods.CardEffectsCardMod",
            method = "onInitialApplication",
            paramtypez = {AbstractCard.class}
    )
    public static class FunctionCardInitPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(Object __instance, AbstractCard card) {
            try {
                Method storedMethod = __instance.getClass().getMethod("stored");
                Object stored = storedMethod.invoke(__instance);
                if (stored == null) {
                    return SpireReturn.Return(null);
                }
            } catch (Exception e) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
    @SpirePatch(
            optional = true,
            cls = "automaton.cardmods.CardEffectsCardMod",
            method = "onApplyPowers",
            paramtypez = {AbstractCard.class}
    )
    public static class FunctionCardPowersPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(Object __instance, AbstractCard card) {
            try {
                Method storedMethod = __instance.getClass().getMethod("stored");
                Object stored = storedMethod.invoke(__instance);
                if (stored == null) {
                    return SpireReturn.Return(null);
                }
            } catch (Exception e) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
    @SpirePatch(
            optional = true,
            cls = "automaton.cards.FunctionCard",
            method = "triplicateCheck"
    )
    public static class FunctionCardTriplicatePatch {
        @SpirePrefixPatch
        public static SpireReturn<Boolean> Prefix(Object __instance) {
            try {
                Method cardsMethod = __instance.getClass().getMethod("cards");
                @SuppressWarnings("unchecked")
                ArrayList<Object> cards = (ArrayList<Object>) cardsMethod.invoke(__instance);
                if (cards.isEmpty() || cards.contains(null)) {
                    return SpireReturn.Return(false);
                }
            } catch (Exception e) {
                return SpireReturn.Return(false);
            }
            return SpireReturn.Continue();
        }
    }
    @SpirePatch(
            optional = true,
            cls = "automaton.cardmods.CardEffectsCardMod",
            method = "stored"
    )
    public static class FunctionCardTypePatch {
        @SpirePrefixPatch
        public static SpireReturn<Object> Prefix(Object __instance) {
            try {
                Class<?> functionHelperClass = Class.forName("automaton.FunctionHelper");
                Field cardModsInfoField = functionHelperClass.getField("cardModsInfo");
                @SuppressWarnings("unchecked")
                Map<Object, Object> cardModsInfo = (Map<Object, Object>) cardModsInfoField.get(null);

                if (cardModsInfo.containsKey(__instance)) {
                    Object card = cardModsInfo.get(__instance);
                    Class<?> bronzeCardClass = Class.forName("automaton.cards.AbstractBronzeCard");
                    if (!(bronzeCardClass.isInstance(card))) {
                        return SpireReturn.Return(null);
                    }
                }
            } catch (Exception e) {
                // Silently fail
            }
            return SpireReturn.Continue();
        }
    }
    @SpirePatch(
            optional = true,
            cls = "automaton.cardmods.CardEffectsCardMod",
            method = "onUse",
            paramtypez = {AbstractCard.class, AbstractCreature.class, UseCardAction.class}
    )
    public static class FunctionCardUsePatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(Object __instance, AbstractCard card, AbstractCreature target, UseCardAction action) {
            try {
                Method storedMethod = __instance.getClass().getMethod("stored");
                Object stored = storedMethod.invoke(__instance);
                if (stored == null) {
                    return SpireReturn.Return(null);
                }
            } catch (Exception e) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
    @SpirePatch(
            optional = true,
            cls = "automaton.cards.FunctionCard",
            method = "getEncodablePortrait",
            paramtypez = {AbstractCard.class}
    )
    public static class FunctionCardPortraitPatch {
        @SpirePrefixPatch
        public static SpireReturn<TextureAtlas.AtlasRegion> Prefix(Object __instance, AbstractCard c) {
            try {
                if (c == null) {
                    Class<?> cardModManagerClass = Class.forName("basemod.helpers.CardModifierManager");
                    Method getModifiersMethod = cardModManagerClass.getMethod("getModifiers", AbstractCard.class, String.class);

                    String cardEffectsId = "bronze:CardEffectsCardMod";
                    @SuppressWarnings("unchecked")
                    List<Object> modifiers = (List<Object>) getModifiersMethod.invoke(null, __instance, cardEffectsId);

                    for (Object mod : modifiers) {
                        Class<?> cardEffectsClass = Class.forName("automaton.cardmods.CardEffectsCardMod");
                        if (cardEffectsClass.isInstance(mod)) {
                            Class<?> functionHelperClass = Class.forName("automaton.FunctionHelper");
                            Field cardModsInfoField = functionHelperClass.getField("cardModsInfo");
                            @SuppressWarnings("unchecked")
                            Map<Object, Object> cardModsInfo = (Map<Object, Object>) cardModsInfoField.get(null);

                            if (cardModsInfo.containsKey(mod)) {
                                Object originalCard = cardModsInfo.get(mod);
                                Class<?> bronzeCardClass = Class.forName("automaton.cards.AbstractBronzeCard");
                                if (!(bronzeCardClass.isInstance(originalCard)) && originalCard instanceof AbstractCard) {
                                    AbstractCard card = (AbstractCard) originalCard;
                                    boolean betaArt = UnlockTracker.betaCardPref.getBoolean(card.cardID, false) || Settings.PLAYTESTER_ART_MODE;
                                    return SpireReturn.Return(betaArt ? card.jokePortrait : card.portrait);
                                }
                            }
                        }
                    }
                    return SpireReturn.Return(null);
                }
            } catch (Exception e) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
    @SpirePatch(
            optional = true,
            cls = "automaton.cards.FunctionCard",
            method = "doNothingSpecificInParticular"
    )
    public static class FunctionCardNamingPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(Object __instance) {
            try {
                Class<?> cardModManagerClass = Class.forName("basemod.helpers.CardModifierManager");
                Method getModifiersMethod = cardModManagerClass.getMethod("getModifiers", AbstractCard.class, String.class);

                String cardEffectsId = "bronze:CardEffectsCardMod";
                @SuppressWarnings("unchecked")
                List<Object> modifiers = (List<Object>) getModifiersMethod.invoke(null, __instance, cardEffectsId);

                boolean hasNonBronze = false;
                String nonBronzeName = "";

                for (Object mod : modifiers) {
                    Class<?> cardEffectsClass = Class.forName("automaton.cardmods.CardEffectsCardMod");
                    if (cardEffectsClass.isInstance(mod)) {
                        Class<?> functionHelperClass = Class.forName("automaton.FunctionHelper");
                        Field cardModsInfoField = functionHelperClass.getField("cardModsInfo");
                        @SuppressWarnings("unchecked")
                        Map<Object, Object> cardModsInfo = (Map<Object, Object>) cardModsInfoField.get(null);

                        if (cardModsInfo.containsKey(mod)) {
                            Object originalCard = cardModsInfo.get(mod);
                            Class<?> bronzeCardClass = Class.forName("automaton.cards.AbstractBronzeCard");
                            if (!(bronzeCardClass.isInstance(originalCard)) && originalCard instanceof AbstractCard) {
                                hasNonBronze = true;
                                String cardName = ((AbstractCard) originalCard).name;
                                if (cardName != null && !cardName.isEmpty()) {
                                    nonBronzeName = cardName.replaceAll("\\..*", "");
                                }
                                break;
                            }
                        }
                    }
                }

                if (hasNonBronze && !nonBronzeName.isEmpty()) {
                    Field textPrefixField = __instance.getClass().getField("textPrefix");
                    Field nameField = AbstractCard.class.getField("name");
                    String textPrefix = (String) textPrefixField.get(__instance);

                    if (textPrefix.equals("")) {
                        nameField.set(__instance, nonBronzeName);
                    } else {
                        nameField.set(__instance, textPrefix + nonBronzeName);
                    }

                    Method initDescMethod = __instance.getClass().getMethod("initializeDescription");
                    initDescMethod.invoke(__instance);
                    return SpireReturn.Return(null);
                }
            } catch (Exception e) {
                // Silently fail
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
            // Only apply if Downfall mod is loaded
            if (!Loader.isModLoaded("downfall")) {
                return SpireReturn.Continue();
            }

            // Only apply to CoconutJPG cards
            if (!(__instance instanceof CoconutJPG)) {
                return SpireReturn.Continue();
            }

            try {
                // Check if this card is in the function sequence
                Class<?> functionHelperClass = Class.forName("automaton.FunctionHelper");
                Field heldField = functionHelperClass.getField("held");
                Object held = heldField.get(null);
                if (held != null) {
                    Method containsMethod = held.getClass().getMethod("contains", AbstractCard.class);
                    Boolean contains = (Boolean) containsMethod.invoke(held, __instance);
                    if (contains) {
                        return SpireReturn.Return(null);
                    }
                }
            } catch (Exception e) {
                // Silently fail
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
            // Only apply if Downfall mod is loaded
            if (!Loader.isModLoaded("downfall")) {
                return SpireReturn.Continue();
            }

            // Only apply to CoconutJPG cards
            if (!(__instance instanceof CoconutJPG)) {
                return SpireReturn.Continue();
            }

            try {
                // Check if this card is in the function sequence
                Class<?> functionHelperClass = Class.forName("automaton.FunctionHelper");
                Field heldField = functionHelperClass.getField("held");
                Object held = heldField.get(null);
                if (held != null) {
                    Method containsMethod = held.getClass().getMethod("contains", AbstractCard.class);
                    Boolean contains = (Boolean) containsMethod.invoke(held, __instance);
                    if (contains) {
                        return SpireReturn.Return(null);
                    }
                }
            } catch (Exception e) {
                // Silently fail
            }
            return SpireReturn.Continue();
        }
    }
}