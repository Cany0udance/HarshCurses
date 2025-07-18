package harshcurses.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.stances.CalmStance;
import harshcurses.helpers.StanceTransitionHelper;

@SpirePatch(
        clz = CalmStance.class,
        method = "onExitStance"
)
public class CalmStanceExitPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(CalmStance __instance) {
        if (StanceTransitionHelper.isEnteringDepression) {
            // Skip energy gain when entering Depression
            return SpireReturn.Return();
        }
        return SpireReturn.Continue();
    }
}