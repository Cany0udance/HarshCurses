package harshcurses.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.combat.HealEffect;
import harshcurses.powers.BegrudgedPower;
import harshcurses.helpers.BegrudgedHelper;

import java.util.Iterator;

@SpirePatch(clz = AbstractCreature.class, method = "heal", paramtypez = {int.class, boolean.class})
public class BegrudgedHealPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> prefixHeal(AbstractCreature __instance, int healAmount, boolean showEffect) {
        // Skip if flag is set or not player
        if (BegrudgedHelper.bypassBegrudged || !__instance.isPlayer) {
            return SpireReturn.Continue();
        }
        // Check if player has BegrudgedPower
        BegrudgedPower begrudgedPower = null;
        for (AbstractPower power : __instance.powers) {
            if (power instanceof BegrudgedPower) {
                begrudgedPower = (BegrudgedPower) power;
                break;
            }
        }
        if (begrudgedPower == null) {
            // No begrudged damage, proceed with normal healing
            return SpireReturn.Continue();
        }
        // Reduce begrudged damage first
        int remainingHeal = begrudgedPower.reduceBegrudgedDamage(healAmount);
        if (remainingHeal > 0) {
            // Call the original heal method with remaining amount
            callOriginalHeal(__instance, remainingHeal, showEffect);
        } else if (showEffect && healAmount > 0) {
            // Show heal effect even if all healing went to begrudged damage
            AbstractDungeon.topPanel.panelHealEffect();
            AbstractDungeon.effectsQueue.add(new HealEffect(
                    __instance.hb.cX - __instance.animX,
                    __instance.hb.cY,
                    healAmount
            ));
        }
        return SpireReturn.Return(null);
    }
    // Helper method to call original heal logic
    private static void callOriginalHeal(AbstractCreature creature, int healAmount, boolean showEffect) {
        if (Settings.isEndless && creature.isPlayer && AbstractDungeon.player.hasBlight("FullBelly")) {
            healAmount /= 2;
            if (healAmount < 1) {
                healAmount = 1;
            }
        }
        if (!creature.isDying) {
            Iterator var3 = AbstractDungeon.player.relics.iterator();
            AbstractRelic r2;
            while(var3.hasNext()) {
                r2 = (AbstractRelic)var3.next();
                if (creature.isPlayer) {
                    healAmount = r2.onPlayerHeal(healAmount);
                }
            }
            AbstractPower p;
            for(var3 = creature.powers.iterator(); var3.hasNext(); healAmount = p.onHeal(healAmount)) {
                p = (AbstractPower)var3.next();
            }
            creature.currentHealth += healAmount;
            if (creature.currentHealth > creature.maxHealth) {
                creature.currentHealth = creature.maxHealth;
            }
            if ((float)creature.currentHealth > (float)creature.maxHealth / 2.0F && creature.isBloodied) {
                creature.isBloodied = false;
                var3 = AbstractDungeon.player.relics.iterator();
                while(var3.hasNext()) {
                    r2 = (AbstractRelic)var3.next();
                    r2.onNotBloodied();
                }
            }
            if (healAmount > 0) {
                if (showEffect && creature.isPlayer) {
                    AbstractDungeon.topPanel.panelHealEffect();
                    AbstractDungeon.effectsQueue.add(new HealEffect(creature.hb.cX - creature.animX, creature.hb.cY, healAmount));
                }
                creature.healthBarUpdatedEvent();
            }
        }
    }
}