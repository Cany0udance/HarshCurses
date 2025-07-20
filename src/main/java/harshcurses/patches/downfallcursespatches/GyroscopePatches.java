package harshcurses.patches.downfallcursespatches;

import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import harshcurses.cards.FaultyGyroscope;
import harshcurses.helpers.GyroscopeHelper;
import theHexaghost.GhostflameHelper;

public class GyroscopePatches {

    @SpirePatch(
            clz = GhostflameHelper.class,
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
                GhostflameHelper.retract();
                GyroscopeHelper.isCursedAdvancingOrRetracting = false;
            }
        }
    }

    @SpirePatch(
            clz = GhostflameHelper.class,
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
                GhostflameHelper.advance(false); // Don't end turn on cursed advance
                GyroscopeHelper.isCursedAdvancingOrRetracting = false;
            }
        }
    }
}