package harshcurses.cards;

import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import harshcurses.HarshCurses;
import harshcurses.util.CardStats;

public class GumOnShoe extends BaseCard {
    public static final String ID = HarshCurses.makeID("GumOnShoe");
    private static final CardStats info = new CardStats(
            CardColor.CURSE,
            CardType.CURSE,
            CardRarity.SPECIAL,
            CardTarget.NONE,
            -2
    );

    public GumOnShoe() {
        super(ID, info);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // This method will never be called as the card is unplayable
    }

    @Override
    public AbstractCard makeCopy() {
        return new GumOnShoe();
    }
}
