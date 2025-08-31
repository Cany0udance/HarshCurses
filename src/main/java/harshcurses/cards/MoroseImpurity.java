package harshcurses.cards;

import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import harshcurses.HarshCurses;
import harshcurses.powers.BegrudgedPower;
import harshcurses.stances.DepressionStance;
import harshcurses.util.CardStats;
import theprismatic.ThePrismatic;

public class MoroseImpurity extends BaseCard {
    public static final String ID = HarshCurses.makeID("MoroseImpurity");
    private static final CardStats info = new CardStats(
            CardColor.CURSE,
            CardType.CURSE,
            CardRarity.SPECIAL,
            CardTarget.NONE,
            -2
    );

    public MoroseImpurity() {
        super(ID, info);
        SoulboundField.soulbound.set(this, true);
        this.isEthereal = true;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // This method will never be called as the card is unplayable
    }

    public void triggerOnOtherCardPlayed(AbstractCard c) {
        if (c.color != CardColor.PURPLE &&
                c.color != ThePrismatic.Enums.Purple) {

            AbstractDungeon.actionManager.addToBottom(
                    new ChangeStanceAction(DepressionStance.STANCE_ID)
            );
        }
    }

    @Override
    public AbstractCard makeCopy() {
        return new MoroseImpurity();
    }
}