package harshcurses.cards;

import automaton.actions.AddToFuncAction;
import collector.actions.GainReservesAction;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import harshcurses.HarshCurses;
import harshcurses.util.CardStats;

public class LichsSoul extends BaseCard {
    public static final String ID = HarshCurses.makeID("LichsSoul");
    private static final CardStats info = new CardStats(
            CardColor.CURSE,
            CardType.CURSE,
            CardRarity.SPECIAL,
            CardTarget.NONE,
            -2
    );

    public LichsSoul() {
        super(ID, info);
        SoulboundField.soulbound.set(this, true);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // This method will never be called as the card is unplayable
    }

    public void triggerWhenDrawn() {
        // Encode this card (add to function sequence)
        AbstractDungeon.actionManager.addToBottom(new GainReservesAction(-1));
    }

    public static boolean isLichsSoul(AbstractCard card) {
        return card instanceof LichsSoul;
    }

    @Override
    public AbstractCard makeCopy() {
        return new LichsSoul();
    }
}
