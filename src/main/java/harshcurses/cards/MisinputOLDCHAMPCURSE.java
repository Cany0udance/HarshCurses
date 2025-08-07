
/*

package harshcurses.cards;

import champ.powers.CounterPower;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.watcher.VigorPower;
import harshcurses.HarshCurses;
import harshcurses.cards.BaseCard;
import harshcurses.util.CardStats;

public class Misinput extends BaseCard {
    public static final String ID = HarshCurses.makeID("Misinput");
    private static final CardStats info = new CardStats(
            CardColor.CURSE,
            CardType.CURSE,
            CardRarity.SPECIAL,
            CardTarget.NONE,
            -2
    );

    public Misinput() {
        super(ID, info);
        SoulboundField.soulbound.set(this, true);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // This method will never be called as the card is unplayable
    }

    public void triggerWhenDrawn() {
        this.flash(Color.RED);

        // Remove Vigor
        if (AbstractDungeon.player.hasPower(VigorPower.POWER_ID)) {
            AbstractDungeon.player.powers.removeIf(power -> power.ID.equals(VigorPower.POWER_ID));
        }

        // Remove Counter
        if (AbstractDungeon.player.hasPower(CounterPower.POWER_ID)) {
            AbstractDungeon.player.powers.removeIf(power -> power.ID.equals(CounterPower.POWER_ID));
        }
    }

    @Override
    public AbstractCard makeCopy() {
        return new Misinput();
    }
}

 */