package harshcurses.actions;

import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.FleetingField;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.ThoughtBubble;
import harshcurses.patches.othermodpatches.ScourgeOfTheCaribbeanPatches;

import java.util.ArrayList;

public class MakeRarestCardFleetingAction extends AbstractGameAction {

    public MakeRarestCardFleetingAction() {
        this.actionType = ActionType.CARD_MANIPULATION;
        this.duration = Settings.ACTION_DUR_FAST;
    }

    @Override
    public void update() {
        if (this.duration == Settings.ACTION_DUR_FAST) {
            AbstractCard rarestCard = findRarestCard();

            if (rarestCard != null) {
                // Set the card as fleeting
                FleetingField.fleeting.set(rarestCard, true);

                // Track that this card was made fleeting by our curse
                ScourgeOfTheCaribbeanPatches.addCursedFleetingCard(rarestCard);

                // Force description update to show "Fleeting"
                rarestCard.initializeDescription();

                // Flash the card to show it was affected
                rarestCard.flash(Color.RED);
            }
        }

        this.tickDuration();
    }

    private AbstractCard findRarestCard() {
        AbstractPlayer player = AbstractDungeon.player;

        // Only check cards in hand
        ArrayList<AbstractCard> handCards = new ArrayList<>(player.hand.group);

        // Remove cards that are already fleeting, curses, or status cards
        handCards.removeIf(card ->
                FleetingField.fleeting.get(card) ||
                        card.type == AbstractCard.CardType.CURSE ||
                        card.type == AbstractCard.CardType.STATUS
        );

        if (handCards.isEmpty()) {
            return null;
        }

        // Find the rarest rarity present in hand
        AbstractCard.CardRarity rarestRarity = findRarestRarity(handCards);

        // Filter to only cards of that rarity
        ArrayList<AbstractCard> rarestCards = new ArrayList<>();
        for (AbstractCard card : handCards) {
            if (card.rarity == rarestRarity) {
                rarestCards.add(card);
            }
        }

        // Prioritize upgraded cards
        ArrayList<AbstractCard> upgradedCards = new ArrayList<>();
        ArrayList<AbstractCard> nonUpgradedCards = new ArrayList<>();

        for (AbstractCard card : rarestCards) {
            if (card.upgraded) {
                upgradedCards.add(card);
            } else {
                nonUpgradedCards.add(card);
            }
        }

        // Pick randomly from upgraded first, then non-upgraded
        if (!upgradedCards.isEmpty()) {
            return upgradedCards.get(AbstractDungeon.cardRandomRng.random(upgradedCards.size() - 1));
        } else {
            return nonUpgradedCards.get(AbstractDungeon.cardRandomRng.random(nonUpgradedCards.size() - 1));
        }
    }

    private AbstractCard.CardRarity findRarestRarity(ArrayList<AbstractCard> cards) {
        boolean hasRare = false;
        boolean hasUncommon = false;
        boolean hasCommon = false;
        boolean hasBasic = false;

        for (AbstractCard card : cards) {
            switch (card.rarity) {
                case RARE:
                    hasRare = true;
                    break;
                case UNCOMMON:
                    hasUncommon = true;
                    break;
                case COMMON:
                    hasCommon = true;
                    break;
                case BASIC:
                    hasBasic = true;
                    break;
            }
        }

        // Return rarest rarity found (Rare > Uncommon > Common > Basic)
        if (hasRare) return AbstractCard.CardRarity.RARE;
        if (hasUncommon) return AbstractCard.CardRarity.UNCOMMON;
        if (hasCommon) return AbstractCard.CardRarity.COMMON;
        return AbstractCard.CardRarity.BASIC;
    }
}