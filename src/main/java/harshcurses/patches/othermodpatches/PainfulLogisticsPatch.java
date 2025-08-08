package harshcurses.patches.othermodpatches;

import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import harshcurses.cards.PainfulLogistics;
import io.chaofan.sts.bladegunner.actions.gun.UpdateBulletAction;

import java.lang.reflect.Method;

@SpirePatch(
        optional = true,
        cls = "io.chaofan.sts.bladegunner.actions.shooting.ReloadAction",
        method = "update"
)
public class PainfulLogisticsPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> modifyReload(AbstractGameAction __instance) {
        // If PainfulLogistics is in hand, load only 1 bullet instead of all
        if (hasPainfulLogisticsInHand()) {
            try {
                // Flash PainfulLogistics cards red
                AbstractDungeon.player.hand.group.stream()
                        .filter(card -> card instanceof PainfulLogistics)
                        .forEach(card -> card.flash(Color.RED));

                // Use reflection to access the protected addToTop method
                Method addToTopMethod = AbstractGameAction.class.getDeclaredMethod("addToTop", AbstractGameAction.class);
                addToTopMethod.setAccessible(true);

                // Add the modified reload sequence
                addToTopMethod.invoke(__instance, new WaitAction(0.1F));
                addToTopMethod.invoke(__instance, new SFXAction("bladegunner:Reload", 0.05F, true));
                addToTopMethod.invoke(__instance, new UpdateBulletAction(true, 1)); // Load 1 instead of all

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
                .anyMatch(card -> card instanceof PainfulLogistics);
    }
}