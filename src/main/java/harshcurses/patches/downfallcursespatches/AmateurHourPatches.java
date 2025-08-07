package harshcurses.patches.downfallcursespatches;

import basemod.BaseMod;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import harshcurses.cards.AmateurHour;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AmateurHourPatches {

    // Helper method to check if Amateur Hour is in hand
    private static boolean hasAmateurHourInHand() {
        if (AbstractDungeon.player == null || AbstractDungeon.player.hand == null) {
            return false;
        }

        for (AbstractCard card : AbstractDungeon.player.hand.group) {
            if (card.cardID.equals(AmateurHour.ID)) {
                return true;
            }
        }
        return false;
    }

    // Patch AbstractChampCard stance detection methods
    @SpirePatch(
            optional = true,
            cls = "champ.cards.AbstractChampCard",
            method = "bcombo"
    )
    public static class BComboPatches {
        @SpirePostfixPatch
        public static boolean Postfix(boolean __result) {
            if (hasAmateurHourInHand()) {
                return false;
            }
            return __result;
        }
    }

    @SpirePatch(
            optional = true,
            cls = "champ.cards.AbstractChampCard",
            method = "dcombo"
    )
    public static class DComboPatches {
        @SpirePostfixPatch
        public static boolean Postfix(boolean __result) {
            if (hasAmateurHourInHand()) {
                return false;
            }
            return __result;
        }
    }

    @SpirePatch(
            optional = true,
            cls = "champ.cards.AbstractChampCard",
            method = "inBerserker"
    )
    public static class InBerserkerPatches {
        @SpirePostfixPatch
        public static boolean Postfix(boolean __result) {
            if (hasAmateurHourInHand()) {
                return false;
            }
            return __result;
        }
    }

    @SpirePatch(
            optional = true,
            cls = "champ.cards.AbstractChampCard",
            method = "inDefensive"
    )
    public static class InDefensivePatches {
        @SpirePostfixPatch
        public static boolean Postfix(boolean __result) {
            if (hasAmateurHourInHand()) {
                return false;
            }
            return __result;
        }
    }

    @SpirePatch(
            optional = true,
            cls = "champ.cards.AbstractChampCard",
            method = "canUse"
    )
    public static class CanUseFinisherPatch {
        @SpirePostfixPatch
        public static boolean Postfix(boolean __result, Object __instance, Object p, Object m) {
            if (!hasAmateurHourInHand()) {
                return __result;
            }

            try {
                // Use reflection to access AbstractChampCard methods and fields
                Class<?> champCardClass = Class.forName("champ.cards.AbstractChampCard");
                Class<?> champModClass = Class.forName("champ.ChampMod");
                Class<?> signatureFinisherClass = Class.forName("champ.relics.SignatureFinisher");
                Class<?> champCharClass = Class.forName("champ.ChampChar");

                // Check if card has FINISHER tag
                Field finisherTagField = champModClass.getDeclaredField("FINISHER");
                finisherTagField.setAccessible(true);
                Object finisherTag = finisherTagField.get(null);

                // Get the correct hasTag method - it should take the enum type, not Object
                Class<?> tagEnumClass = finisherTag.getClass();
                Method hasTagMethod = AbstractCard.class.getDeclaredMethod("hasTag", tagEnumClass);
                boolean hasFinisherTag = (boolean) hasTagMethod.invoke(__instance, finisherTag);

                if (hasFinisherTag) {
                    // Check if it's bottled (Signature Finisher relic exception)
                    boolean bottled = false;
                    Method hasRelicMethod = AbstractPlayer.class.getDeclaredMethod("hasRelic", String.class);
                    Field signatureIdField = signatureFinisherClass.getDeclaredField("ID");
                    signatureIdField.setAccessible(true);
                    String signatureId = (String) signatureIdField.get(null);

                    if ((boolean) hasRelicMethod.invoke(p, signatureId)) {
                        Method getRelicMethod = AbstractPlayer.class.getDeclaredMethod("getRelic", String.class);
                        Object signatureRelic = getRelicMethod.invoke(p, signatureId);

                        Field cardField = signatureFinisherClass.getDeclaredField("card");
                        cardField.setAccessible(true);
                        AbstractCard relicCard = (AbstractCard) cardField.get(signatureRelic);

                        Field uuidField = AbstractCard.class.getDeclaredField("uuid");
                        uuidField.setAccessible(true);
                        String instanceUuid = (String) uuidField.get(__instance);
                        String relicUuid = (String) uuidField.get(relicCard);

                        if (instanceUuid.equals(relicUuid)) {
                            bottled = true;
                        }
                    }

                    if (!bottled) {
                        // Set cant use message
                        Field characterStringsField = champCharClass.getDeclaredField("characterStrings");
                        characterStringsField.setAccessible(true);
                        Object characterStrings = characterStringsField.get(null);

                        Field textField = characterStrings.getClass().getDeclaredField("TEXT");
                        textField.setAccessible(true);
                        String[] textArray = (String[]) textField.get(characterStrings);

                        Field cantUseMessageField = AbstractCard.class.getDeclaredField("cantUseMessage");
                        cantUseMessageField.setAccessible(true);
                        cantUseMessageField.set(__instance, textArray[61]);

                        return false;
                    }
                }
            } catch (Exception e) {
                // Silently fail if ChampMod isn't loaded or structure changed
            }

            return __result;
        }
    }

    // Patch technique triggers from playing skill cards
    @SpirePatch(
            optional = true,
            cls = "champ.stances.AbstractChampStance",
            method = "onPlayCard"
    )
    public static class TechniqueTriggerPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(Object __instance, Object card) {
            if (hasAmateurHourInHand()) {
                return SpireReturn.Return(); // Skip technique trigger
            }
            return SpireReturn.Continue();
        }
    }

    // Patch finisher effects
    @SpirePatch(
            optional = true,
            cls = "champ.cards.AbstractChampCard",
            method = "finisher",
            paramtypes = {"boolean"}
    )
    public static class FinisherEffectPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(Object __instance, boolean noExit) {
            if (hasAmateurHourInHand()) {
                return SpireReturn.Return(); // Skip finisher effects entirely
            }
            return SpireReturn.Continue();
        }
    }

    // Patch glow effect for finisher cards
    @SpirePatch(
            optional = true,
            cls = "champ.cards.AbstractChampCard",
            method = "triggerOnGlowCheck"
    )
    public static class GlowCheckPatch {
        @SpirePostfixPatch
        public static void Postfix(Object __instance) {
            if (!hasAmateurHourInHand()) {
                return;
            }

            try {
                // Use reflection to check if card has FINISHER tag and set glow color
                Class<?> champModClass = Class.forName("champ.ChampMod");
                Field finisherTagField = champModClass.getDeclaredField("FINISHER");
                finisherTagField.setAccessible(true);
                Object finisherTag = finisherTagField.get(null);

                Method hasTagMethod = AbstractCard.class.getDeclaredMethod("hasTag", Object.class);
                boolean hasFinisherTag = (boolean) hasTagMethod.invoke(__instance, finisherTag);

                if (hasFinisherTag) {
                    // Use reflection to access BLUE_BORDER_GLOW_COLOR
                    Field glowColorField = AbstractCard.class.getDeclaredField("BLUE_BORDER_GLOW_COLOR");
                    glowColorField.setAccessible(true);
                    Color blueGlow = (Color) glowColorField.get(null);

                    Field instanceGlowColorField = AbstractCard.class.getDeclaredField("glowColor");
                    instanceGlowColorField.setAccessible(true);
                    instanceGlowColorField.set(__instance, blueGlow);
                }
            } catch (Exception e) {
                // Fallback to a default blue color if reflection fails
                try {
                    Field instanceGlowColorField = AbstractCard.class.getDeclaredField("glowColor");
                    instanceGlowColorField.setAccessible(true);
                    instanceGlowColorField.set(__instance, Color.BLUE.cpy());
                } catch (Exception ex) {
                    // Silently fail if ChampMod isn't loaded or structure changed
                }
            }
        }
    }
}