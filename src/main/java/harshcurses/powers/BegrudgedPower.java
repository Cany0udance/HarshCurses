package harshcurses.powers;

import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import harshcurses.HarshCurses;
import harshcurses.helpers.BegrudgedHelper;

public class BegrudgedPower extends BasePower {
    public static final String POWER_ID = HarshCurses.makeID("BegrudgedPower");

    public BegrudgedPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
        this.amount = amount;
        this.priority = -999; // Very low priority so it triggers last
        updateDescription();
    }

    @Override
    public void updateDescription() {
        description = DESCRIPTIONS[0] + amount + DESCRIPTIONS[1];
    }

    public void onVictory() {
        AbstractPlayer p = AbstractDungeon.player;
        if (p.currentHealth > 0) {
            BegrudgedHelper.bypassBegrudged = true;
            p.heal(this.amount);
            BegrudgedHelper.bypassBegrudged = false;
        }
    }

    // Helper method to reduce temporary damage
    public int reduceBegrudgedDamage(int healAmount) {
        if (this.amount <= 0) {
            return healAmount;
        }

        int reduction = Math.min(this.amount, healAmount);
        this.amount -= reduction;

        if (this.amount <= 0) {
            // Queue removal action instead of removing directly
            this.addToTop(new RemoveSpecificPowerAction(this.owner, this.owner, this));
        } else {
            updateDescription();
        }

        return healAmount - reduction;
    }
}