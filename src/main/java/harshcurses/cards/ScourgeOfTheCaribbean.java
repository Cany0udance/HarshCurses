package harshcurses.cards;

import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import harshcurses.HarshCurses;
import harshcurses.actions.MakeRarestCardFleetingAction;
import harshcurses.util.CardStats;

public class ScourgeOfTheCaribbean extends BaseCard {
    public static final String ID = HarshCurses.makeID("ScourgeOfTheCaribbean");
    private static final CardStats info = new CardStats(
            CardColor.CURSE,
            CardType.CURSE,
            CardRarity.SPECIAL,
            CardTarget.NONE,
            -2
    );

    public ScourgeOfTheCaribbean() {
        super(ID, info);
        SoulboundField.soulbound.set(this, true);
        this.isEthereal = true;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // This method will never be called as the card is unplayable
    }

    public void triggerWhenDrawn() {
        // Queue the action to make the rarest card fleeting
        AbstractDungeon.actionManager.addToBottom(new MakeRarestCardFleetingAction());

        // Flash this card to show it triggered
        this.flash(Color.RED);
    }


    @Override
    public AbstractCard makeCopy() {
        return new ScourgeOfTheCaribbean();
    }
}