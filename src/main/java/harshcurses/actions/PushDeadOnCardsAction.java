package harshcurses.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class PushDeadOnCardsAction extends AbstractGameAction {

    public PushDeadOnCardsAction() {
        this.actionType = ActionType.SPECIAL;
        this.duration = Settings.ACTION_DUR_FAST;
    }

    @Override
    public void update() {
        if (this.duration == Settings.ACTION_DUR_FAST) {
            // Get all Dead On cards from hand
            ArrayList<AbstractCard> deadOnCards = new ArrayList<>();
            for (AbstractCard card : AbstractDungeon.player.hand.group) {
                if (hasDeadOnTag(card)) {
                    deadOnCards.add(card);
                }
            }

            // Remove Dead On cards from hand
            for (AbstractCard card : deadOnCards) {
                AbstractDungeon.player.hand.removeCard(card);
            }

            // Add them back at the beginning (left side) by inserting at index 0
            for (int i = deadOnCards.size() - 1; i >= 0; i--) {
                AbstractDungeon.player.hand.group.add(0, deadOnCards.get(i));
            }

            // Refresh hand layout to update positions
            AbstractDungeon.player.hand.refreshHandLayout();
        }

        this.tickDuration();
    }

    // Helper method to check if a card has the Dead On tag
    private boolean hasDeadOnTag(AbstractCard card) {
        try {
            // Get the DEADON tag from AbstractHermitCard
            Class<?> abstractHermitCard = Class.forName("hermit.cards.AbstractHermitCard");
            Field deadOnField = abstractHermitCard.getField("DEADON");
            AbstractCard.CardTags deadOnTag = (AbstractCard.CardTags) deadOnField.get(null);
            return card.hasTag(deadOnTag);
        } catch (Exception e) {
            // Hermit not loaded or field not accessible, fall back to name checking
            for (AbstractCard.CardTags tag : card.tags) {
                if (tag.name().contains("DEADON")) {
                    return true;
                }
            }
        }

        return false;
    }
}