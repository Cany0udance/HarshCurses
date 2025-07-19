package harshcurses.patches.basecursespatches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.stances.AbstractStance;
import harshcurses.stances.DepressionStance;

@SpirePatch(clz = AbstractStance.class, method = "getStanceFromName")
public class GetStanceFromNamePatch {
    @SpirePostfixPatch
    public static AbstractStance Postfix(AbstractStance __result, String name) {
        if (__result == null && name.equals(DepressionStance.STANCE_ID)) {
            return new DepressionStance();
        }
        return __result;
    }
}