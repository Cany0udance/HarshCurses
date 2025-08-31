package harshcurses.cards;

import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import harshcurses.HarshCurses;
import harshcurses.VeryVeryInterestingExperiment;
import researchersmod.cards.ExperimentCard;
import researchersmod.powers.interfaces.ExperimentInterfaces;
import researchersmod.ui.ExperimentCardManager;
import researchersmod.util.CardStats;
import researchersmod.util.Wiz;

public class VeryVeryInteresting extends ExperimentCard {
    public static final String ID = HarshCurses.makeID("VeryVeryInteresting");
    private static final CardStats info = new CardStats(
            CardColor.CURSE,
            CardType.CURSE,
            CardRarity.SPECIAL,
            CardTarget.NONE,
            -2
    );

    public VeryVeryInteresting() {
        super(ID, info, 17, 0, "harshcurses/images/cards/curse/VeryVeryInteresting.png"); // Explicitly specify image path
        SoulboundField.soulbound.set(this, true);
    }

    @Override
    public void triggerWhenDrawn() {
        // Start the experiment when drawn
        AbstractPlayer p = AbstractDungeon.player;
        Wiz.atb(new ApplyPowerAction(p, p, new VeryVeryInterestingExperiment(p, this.trial, this)));
        // Remove from hand and add to experiment manager
        Wiz.atb(new AbstractGameAction() {
            @Override
            public void update() {
                // Remove from hand first
                AbstractDungeon.player.hand.removeCard(VeryVeryInteresting.this);
                // Then add to experiments
                ExperimentCardManager.addExp(VeryVeryInteresting.this, false);
                this.isDone = true;
            }
        });
        super.triggerWhenDrawn();
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // This method will never be called as the card is unplayable (cost -2)
    }

    @Override
    public AbstractCard makeCopy() {
        return new VeryVeryInteresting();
    }
}