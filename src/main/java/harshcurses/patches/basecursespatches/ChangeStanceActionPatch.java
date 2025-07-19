package harshcurses.patches.basecursespatches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import harshcurses.helpers.StanceTransitionHelper;
import harshcurses.stances.DepressionStance;

import java.lang.reflect.Field;

@SpirePatch(
        clz = ChangeStanceAction.class,
        method = "update"
)
public class ChangeStanceActionPatch {
    @SpirePrefixPatch
    public static void Prefix(ChangeStanceAction __instance) {
        try {
            Field idField = ChangeStanceAction.class.getDeclaredField("id");
            idField.setAccessible(true);
            String newStanceId = (String) idField.get(__instance);

            // Set flag if we're entering Depression
            StanceTransitionHelper.isEnteringDepression = newStanceId.equals(DepressionStance.STANCE_ID);
        } catch (Exception e) {
            StanceTransitionHelper.isEnteringDepression = false;
        }
    }

    @SpirePostfixPatch
    public static void Postfix(ChangeStanceAction __instance) {
        // Clean up the flag after the action is done
        StanceTransitionHelper.isEnteringDepression = false;
    }
}
