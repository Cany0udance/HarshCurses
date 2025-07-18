package harshcurses.cards;

import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.actions.common.ExhaustSpecificCardAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.tempCards.Miracle;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.Frost;
import harshcurses.HarshCurses;
import harshcurses.orbs.Brick;
import harshcurses.util.CardStats;

public class BadCode extends BaseCard {
    public static final String ID = HarshCurses.makeID("BadCode");
    private static final CardStats info = new CardStats(
            CardColor.CURSE,
            CardType.CURSE,
            CardRarity.SPECIAL,
            CardTarget.NONE,
            -2
    );

    public BadCode() {
        super(ID, info);
        SoulboundField.soulbound.set(this, true);
        this.isEthereal = true;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // This method will never be called as the card is unplayable
    }

    public void triggerWhenDrawn() {
        this.addToBot(new ChannelAction(new Brick()));
    }

    @Override
    public AbstractCard makeCopy() {
        return new BadCode();
    }
}