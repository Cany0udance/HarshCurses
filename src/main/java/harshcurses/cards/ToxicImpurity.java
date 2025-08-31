package harshcurses.cards;

import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import harshcurses.HarshCurses;
import harshcurses.orbs.Brick;
import harshcurses.util.CardStats;
import theprismatic.ThePrismatic;

public class ToxicImpurity extends BaseCard {
    public static final String ID = HarshCurses.makeID("ToxicImpurity");
    private static final CardStats info = new CardStats(
            CardColor.CURSE,
            CardType.CURSE,
            CardRarity.SPECIAL,
            CardTarget.NONE,
            -2
    );

    public ToxicImpurity() {
        super(ID, info);
        SoulboundField.soulbound.set(this, true);
        this.isEthereal = true;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // This method will never be called as the card is unplayable
    }

    public void triggerOnOtherCardPlayed(AbstractCard c) {
        if (c.color != CardColor.GREEN &&
                c.color != ThePrismatic.Enums.Green) {

            AbstractDungeon.actionManager.addToBottom(
                    new MakeTempCardInDrawPileAction(
                            new ToxicImpurity(),
                            1,
                            true,
                            true
                    )
            );
        }
    }

    @Override
    public AbstractCard makeCopy() {
        return new ToxicImpurity();
    }
}