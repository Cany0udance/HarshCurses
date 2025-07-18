package harshcurses.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.stances.WrathStance;
import harshcurses.actions.ExhumeTheBigSadAction;

@SpirePatch(
        clz = WrathStance.class,
        method = "onEnterStance"
)
public class WrathStanceEnterPatch {
    @SpirePostfixPatch
    public static void Postfix(WrathStance __instance) {
        // Check if TheBigSad is in exhaust pile and move it to draw pile
        AbstractDungeon.actionManager.addToBottom(new ExhumeTheBigSadAction());
    }
}