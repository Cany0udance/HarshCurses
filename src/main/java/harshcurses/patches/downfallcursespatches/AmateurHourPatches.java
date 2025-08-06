package harshcurses.patches.downfallcursespatches;

import champ.ChampChar;
import champ.ChampMod;
import champ.cards.AbstractChampCard;
import champ.relics.SignatureFinisher;
import champ.stances.AbstractChampStance;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import harshcurses.cards.AmateurHour;

import java.lang.reflect.Field;

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
    @SpirePatch(clz = AbstractChampCard.class, method = "bcombo")
    public static class BComboPatches {
        @SpirePostfixPatch
        public static boolean Postfix(boolean __result) {
            if (hasAmateurHourInHand()) {
                return false;
            }
            return __result;
        }
    }

    @SpirePatch(clz = AbstractChampCard.class, method = "dcombo")
    public static class DComboPatches {
        @SpirePostfixPatch
        public static boolean Postfix(boolean __result) {
            if (hasAmateurHourInHand()) {
                return false;
            }
            return __result;
        }
    }

    @SpirePatch(clz = AbstractChampCard.class, method = "inBerserker")
    public static class InBerserkerPatches {
        @SpirePostfixPatch
        public static boolean Postfix(boolean __result) {
            if (hasAmateurHourInHand()) {
                return false;
            }
            return __result;
        }
    }

    @SpirePatch(clz = AbstractChampCard.class, method = "inDefensive")
    public static class InDefensivePatches {
        @SpirePostfixPatch
        public static boolean Postfix(boolean __result) {
            if (hasAmateurHourInHand()) {
                return false;
            }
            return __result;
        }
    }

    // Patch finisher card playability
    @SpirePatch(clz = AbstractChampCard.class, method = "canUse")
    public static class CanUseFinisherPatch {
        @SpirePostfixPatch
        public static boolean Postfix(boolean __result, AbstractChampCard __instance, AbstractPlayer p, AbstractMonster m) {
            if (hasAmateurHourInHand() && __instance.hasTag(ChampMod.FINISHER)) {
                // Check if it's bottled (Signature Finisher relic exception)
                boolean bottled = false;
                if (p.hasRelic(SignatureFinisher.ID) && ((SignatureFinisher)p.getRelic(SignatureFinisher.ID)).card.uuid == __instance.uuid) {
                    bottled = true;
                }

                if (!bottled) {
                    __instance.cantUseMessage = ChampChar.characterStrings.TEXT[61]; // Same message as neutral stance
                    return false;
                }
            }
            return __result;
        }
    }

    // Patch technique triggers from playing skill cards
    @SpirePatch(clz = AbstractChampStance.class, method = "onPlayCard")
    public static class TechniqueTriggerPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(AbstractChampStance __instance, AbstractCard card) {
            if (hasAmateurHourInHand()) {
                return SpireReturn.Return(); // Skip technique trigger
            }
            return SpireReturn.Continue();
        }
    }

    // Patch finisher effects
    @SpirePatch(clz = AbstractChampCard.class, method = "finisher", paramtypes = {"boolean"})
    public static class FinisherEffectPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(AbstractChampCard __instance, boolean noExit) {
            if (hasAmateurHourInHand()) {
                return SpireReturn.Return(); // Skip finisher effects entirely
            }
            return SpireReturn.Continue();
        }
    }

    // Patch glow effect for finisher cards
    @SpirePatch(clz = AbstractChampCard.class, method = "triggerOnGlowCheck")
    public static class GlowCheckPatch {
        @SpirePostfixPatch
        public static void Postfix(AbstractChampCard __instance) {
            if (hasAmateurHourInHand() && __instance.hasTag(ChampMod.FINISHER)) {
                try {
                    // Use reflection to access BLUE_BORDER_GLOW_COLOR
                    Field glowColorField = AbstractCard.class.getDeclaredField("BLUE_BORDER_GLOW_COLOR");
                    glowColorField.setAccessible(true);
                    Color blueGlow = (Color) glowColorField.get(null);
                    __instance.glowColor = blueGlow;
                } catch (Exception e) {
                    // Fallback to a default blue color if reflection fails
                    __instance.glowColor = Color.BLUE.cpy();
                }
            }
        }
    }
}