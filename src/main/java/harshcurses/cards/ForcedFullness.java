
/*

package harshcurses.cards;

import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import harshcurses.HarshCurses;
import harshcurses.actions.RemoveAllGemsAction;
import harshcurses.util.CardStats;

public class ForcedFullness extends BaseCard {
    public static final String ID = HarshCurses.makeID("ForcedFullness");
    private static final CardStats info = new CardStats(
            CardColor.CURSE,
            CardType.CURSE,
            CardRarity.SPECIAL,
            CardTarget.NONE,
            1
    );

    public ForcedFullness() {
        super(ID, info);
        SoulboundField.soulbound.set(this, true);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        AbstractDungeon.actionManager.addToBottom(new RemoveAllGemsAction());
    }

    @Override
    public AbstractCard makeCopy() {
        return new ForcedFullness();
    }
}

 */