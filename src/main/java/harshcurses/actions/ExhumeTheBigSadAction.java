package harshcurses.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import harshcurses.cards.TheBigSad;

public class ExhumeTheBigSadAction extends AbstractGameAction {
    public ExhumeTheBigSadAction() {
        this.actionType = ActionType.CARD_MANIPULATION;
        this.duration = Settings.ACTION_DUR_FAST;
    }

    public void update() {
        if (this.duration == Settings.ACTION_DUR_FAST) {
            // Look for TheBigSad in exhaust pile
            AbstractCard theBigSad = null;
            for (AbstractCard card : AbstractDungeon.player.exhaustPile.group) {
                if (card.cardID.equals(TheBigSad.ID)) {
                    theBigSad = card;
                    break;
                }
            }

            if (theBigSad != null) {
                // Reset card visual state before moving
                theBigSad.unfadeOut();
                theBigSad.unhover();
                theBigSad.fadingOut = false;
                theBigSad.stopGlowing();

                // Move to draw pile (use the CardGroup method for proper handling)
                AbstractDungeon.player.exhaustPile.moveToDeck(theBigSad, true);

                this.isDone = true;
            } else {
                this.isDone = true;
            }
        }
    }
}