package harshcurses.cards;

import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import harshcurses.HarshCurses;
import harshcurses.orbs.CrapSlime;
import harshcurses.util.CardStats;
import slimebound.actions.SlimeSpawnAction;

public class SplitCrap extends BaseCard {
    public static final String ID = HarshCurses.makeID("SplitCrap");
    private static final CardStats info = new CardStats(
            CardColor.CURSE,
            CardType.CURSE,
            CardRarity.SPECIAL,
            CardTarget.NONE,
            -2
    );

    public SplitCrap() {
        super(ID, info);
        SoulboundField.soulbound.set(this, true);
        this.isEthereal = true;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // This method will never be called as the card is unplayable
    }

    public void triggerWhenDrawn() {
        // Create a new Crap Slime and spawn it using the SlimeSpawnAction
        this.addToBot(new SlimeSpawnAction(new CrapSlime(), false, false, 0, 0));
    }

    @Override
    public AbstractCard makeCopy() {
        return new SplitCrap();
    }
}