package harshcurses.patches.othermodpatches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.ThoughtBubble;
import harshcurses.cards.AnkleMonitor;

import java.lang.reflect.Field;

public class AnkleMonitorPatch {

    // Patch TryToFleeAction to prevent fleeing when AnkleMonitor is in hand
    @SpirePatch(
            optional = true,
            cls = "theThorton.actions.TryToFleeAction",
            method = "update"
    )
    public static class PreventFleeingPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> preventFleeingWithAnkleMonitor(AbstractGameAction __instance) {
            // Check if AnkleMonitor is in hand
            if (hasAnkleMonitor()) {
                // Add thought bubble effect
                showAnkleMonitorThought();

                // Mark the action as done and prevent the original update from running
                __instance.isDone = true;
                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }
    }

    private static boolean hasAnkleMonitor() {
        return AbstractDungeon.player.hand.group.stream()
                .anyMatch(card -> card instanceof AnkleMonitor);
    }

    private static void showAnkleMonitorThought() {
        // Create thought bubble at player position
        AbstractDungeon.effectList.add(new ThoughtBubble(
                AbstractDungeon.player.hb.cX,
                AbstractDungeon.player.hb.cY,
                "They'll know!!",
                true
        ));
    }
}