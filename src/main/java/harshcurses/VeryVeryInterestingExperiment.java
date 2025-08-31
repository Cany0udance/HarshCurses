package harshcurses;

import com.evacipated.cardcrawl.mod.stslib.powers.interfaces.InvisiblePower;
import com.evacipated.cardcrawl.mod.stslib.powers.interfaces.NonStackablePower;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import harshcurses.cards.VeryVeryInteresting;
import researchersmod.fields.ExperimentPowerFields;
import researchersmod.powers.BasePower;
import researchersmod.powers.interfaces.ExperimentInterfaces;
import researchersmod.powers.interfaces.ExperimentPower;
import researchersmod.ui.ExperimentCardManager;
import researchersmod.util.Wiz;

public class VeryVeryInterestingExperiment extends BasePower implements InvisiblePower, NonStackablePower, ExperimentPower, ExperimentInterfaces.OnTerminateInterface {
    public static final String POWER_ID = HarshCurses.makeID(VeryVeryInterestingExperiment.class.getSimpleName());
    public static final AbstractPower.PowerType TYPE = AbstractPower.PowerType.DEBUFF;
    private static final boolean TURNBASED = false;

    public VeryVeryInterestingExperiment(AbstractCreature owner, int amount, AbstractCard card) {
        super(POWER_ID, TYPE, false, owner, amount, card);
        ExperimentPowerFields.instantImmunity.set(this, true);
    }

    @Override
    public void terminateEffect() {
        // When experiment concludes, add a copy of VeryVeryInteresting to the deck
        AbstractDungeon.topLevelEffectsQueue.add(new ShowCardAndObtainEffect(new VeryVeryInteresting(),
                Settings.WIDTH / 2f, Settings.HEIGHT / 2f));
        ExperimentCardManager.remExp(this);
    }

    @Override
    public void completionEffect() {
        // Each time this experiment "completes" a step, player takes 2 damage
        Wiz.atb(new DamageAction(this.owner, new DamageInfo(this.owner, 2, DamageInfo.DamageType.THORNS),
                AbstractGameAction.AttackEffect.FIRE));
        ExperimentCardManager.tickExperiment(this);
    }

    @Override
    public void onTerminate(AbstractPower power) {
        // When another experiment concludes, complete this experiment (deals damage and ticks down)
        if (power != this && this.amount > 0) { // Don't tick ourselves and make sure we're still active
            ExperimentCardManager.complete(this);
        }
    }
}