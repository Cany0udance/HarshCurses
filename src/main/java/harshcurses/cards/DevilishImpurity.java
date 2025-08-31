package harshcurses.cards;

import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import harshcurses.HarshCurses;
import harshcurses.powers.BegrudgedPower;
import harshcurses.util.CardStats;
import theprismatic.ThePrismatic;

public class DevilishImpurity extends BaseCard {
    public static final String ID = HarshCurses.makeID("DevilishImpurity");
    private static final CardStats info = new CardStats(
            CardColor.CURSE,
            CardType.CURSE,
            CardRarity.SPECIAL,
            CardTarget.NONE,
            -2
    );

    public DevilishImpurity() {
        super(ID, info);
        SoulboundField.soulbound.set(this, true);
        this.isEthereal = true;
    }

    public void triggerOnOtherCardPlayed(AbstractCard c) {
        // Check if the played card is NOT an Ironclad card and NOT ThePrismatic Red
        if (c.color != AbstractCard.CardColor.RED &&
                c.color != ThePrismatic.Enums.Red) {

            AbstractDungeon.actionManager.addToBottom(
                    new LoseHPAction(AbstractDungeon.player, AbstractDungeon.player, 2));

            AbstractDungeon.actionManager.addToBottom(
                    new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player,
                            new BegrudgedPower(AbstractDungeon.player, 2), 2)
            );
        }
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // This method will never be called as the card is unplayable
    }

    @Override
    public AbstractCard makeCopy() {
        return new DevilishImpurity();
    }
}