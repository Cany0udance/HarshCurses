package harshcurses.cards;

import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.status.Wound;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import harshcurses.HarshCurses;
import harshcurses.util.CardStats;
import io.chaofan.sts.bladegunner.cards.Jam;

public class PainfulLogistics extends BaseCard {
    public static final String ID = HarshCurses.makeID("PainfulLogistics");
    private static final CardStats info = new CardStats(
            CardColor.CURSE,
            CardType.CURSE,
            CardRarity.SPECIAL,
            CardTarget.NONE,
            1
    );

    public PainfulLogistics() {
        super(ID, info);
        SoulboundField.soulbound.set(this, true);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        this.addToBot(new MakeTempCardInDrawPileAction(new Jam(), 1, true, true));
    }

    @Override
    public AbstractCard makeCopy() {
        return new PainfulLogistics();
    }
}