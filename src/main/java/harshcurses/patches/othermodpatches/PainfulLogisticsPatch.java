package harshcurses.patches.othermodpatches;

import basemod.BaseMod;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import harshcurses.cards.BulletEatingSquirrel;
import io.chaofan.sts.bladegunner.actions.gun.UpdateBulletAction;
import io.chaofan.sts.bladegunner.guns.Guns;

import java.lang.reflect.Method;

@SpirePatch(
        optional = true,
        cls = "io.chaofan.sts.bladegunner.actions.shooting.ReloadAction",
        method = "update"
)
public class PainfulLogisticsPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> modifyReload(AbstractGameAction __instance) {
        // If PainfulLogistics is in hand, load 1 less bullet than normal
        if (hasPainfulLogisticsInHand()) {
            try {
                // Flash PainfulLogistics cards red
                AbstractDungeon.player.hand.group.stream()
                        .filter(card -> card instanceof BulletEatingSquirrel)
                        .forEach(card -> card.flash(Color.RED));

                // Calculate magazine capacity - 1
                int magazineCapacity = Guns.instance.magazineCapacity;
                int reducedCapacity = Math.max(0, magazineCapacity - 1);

                // Use reflection to access the protected addToTop method
                Method addToTopMethod = AbstractGameAction.class.getDeclaredMethod("addToTop", AbstractGameAction.class);
                addToTopMethod.setAccessible(true);

                // Add the modified reload sequence
                addToTopMethod.invoke(__instance, new WaitAction(0.1F));
                addToTopMethod.invoke(__instance, new SFXAction("bladegunner:Reload", 0.05F, true));
                addToTopMethod.invoke(__instance, new UpdateBulletAction(false, reducedCapacity));
                __instance.isDone = true;
                return SpireReturn.Return(null);
            } catch (Exception e) {
                // If reflection fails, fall back to normal behavior
                return SpireReturn.Continue();
            }
        }
        return SpireReturn.Continue();
    }

    private static boolean hasPainfulLogisticsInHand() {
        return AbstractDungeon.player.hand.group.stream()
                .anyMatch(card -> card instanceof BulletEatingSquirrel);
    }
}