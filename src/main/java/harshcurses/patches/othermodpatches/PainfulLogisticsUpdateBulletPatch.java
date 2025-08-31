package harshcurses.patches.othermodpatches;

import basemod.BaseMod;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import harshcurses.cards.BulletEatingSquirrel;
import io.chaofan.sts.bladegunner.actions.gun.UpdateBulletAction;

import java.lang.reflect.Field;

@SpirePatch(
        optional = true,
        cls = "io.chaofan.sts.bladegunner.actions.gun.UpdateBulletAction",
        method = "update"
)
public class PainfulLogisticsUpdateBulletPatch {
    @SpirePrefixPatch
    public static void reduceBulletGain(UpdateBulletAction __instance) {
        if (hasPainfulLogisticsInHand()) {
            try {
                // Use reflection to access private fields
                Field isIncrementField = UpdateBulletAction.class.getDeclaredField("isIncrement");
                Field valueField = UpdateBulletAction.class.getDeclaredField("value");
                isIncrementField.setAccessible(true);
                valueField.setAccessible(true);

                boolean isIncrement = isIncrementField.getBoolean(__instance);
                int value = valueField.getInt(__instance);


                // Only affect increment operations (not set operations like reload)
                // and only if value is positive
                if (isIncrement && value > 0) {

                    // Flash PainfulLogistics cards red
                    AbstractDungeon.player.hand.group.stream()
                            .filter(card -> card instanceof BulletEatingSquirrel)
                            .forEach(card -> card.flash(Color.RED));

                    // Reduce bullet gain by 1 (minimum 0)
                    int reducedValue = Math.max(0, value - 1);
                    valueField.setInt(__instance, reducedValue);
                } else {

                }
            } catch (Exception e) {

            }
        }
    }

    private static boolean hasPainfulLogisticsInHand() {
        return AbstractDungeon.player.hand.group.stream()
                .anyMatch(card -> card instanceof BulletEatingSquirrel);
    }
}