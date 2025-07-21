package harshcurses.patches.downfallcursespatches;

import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import harshcurses.cards.FaultyGyroscope;
import harshcurses.helpers.GyroscopeHelper;

import java.lang.reflect.Method;

public class GyroscopePatches {
    @SpirePatch(
            optional = true,
            cls = "theHexaghost.GhostflameHelper",
            method = "advance",
            paramtypez = {boolean.class}
    )
    public static class AdvancePatch {
        @SpirePostfixPatch
        public static void Postfix(boolean endTurn) {
            if (GyroscopeHelper.isCursedAdvancingOrRetracting) {
                return;
            }
            // Check if FaultyGyroscope is in hand
            AbstractCard faultyGyroscope = AbstractDungeon.player.hand.group.stream()
                    .filter(card -> card instanceof FaultyGyroscope)
                    .findFirst()
                    .orElse(null);
            if (faultyGyroscope != null) {
                faultyGyroscope.flash(Color.RED);
                GyroscopeHelper.isCursedAdvancingOrRetracting = true;
                // Use reflection to call GhostflameHelper.retract()
                try {
                    Class<?> ghostflameHelperClass = Class.forName("theHexaghost.GhostflameHelper");
                    Method retractMethod = ghostflameHelperClass.getMethod("retract");
                    retractMethod.invoke(null);
                } catch (Exception e) {
                    // Silently fail if the method doesn't exist
                }
                GyroscopeHelper.isCursedAdvancingOrRetracting = false;
            }
        }
    }
    @SpirePatch(
            optional = true,
            cls = "theHexaghost.GhostflameHelper",
            method = "retract"
    )
    public static class RetractPatch {
        @SpirePostfixPatch
        public static void Postfix() {
            if (GyroscopeHelper.isCursedAdvancingOrRetracting) {
                return;
            }
            // Check if FaultyGyroscope is in hand
            AbstractCard faultyGyroscope = AbstractDungeon.player.hand.group.stream()
                    .filter(card -> card instanceof FaultyGyroscope)
                    .findFirst()
                    .orElse(null);
            if (faultyGyroscope != null) {
                faultyGyroscope.flash(Color.RED);
                GyroscopeHelper.isCursedAdvancingOrRetracting = true;
                // Use reflection to call GhostflameHelper.advance(false)
                try {
                    Class<?> ghostflameHelperClass = Class.forName("theHexaghost.GhostflameHelper");
                    Method advanceMethod = ghostflameHelperClass.getMethod("advance", boolean.class);
                    advanceMethod.invoke(null, false); // Don't end turn on cursed advance
                } catch (Exception e) {
                    // Silently fail if the method doesn't exist
                }
                GyroscopeHelper.isCursedAdvancingOrRetracting = false;
            }
        }
    }
}