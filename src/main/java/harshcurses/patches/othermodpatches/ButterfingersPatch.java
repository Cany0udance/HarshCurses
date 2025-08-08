package harshcurses.patches.othermodpatches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import harshcurses.cards.Butterfingers;

import java.util.ArrayList;

public class ButterfingersPatch {
    private static boolean processingEthereal = false; // Prevent recursive processing

    @SpirePatch(
            clz = CardGroup.class,
            method = "refreshHandLayout"
    )
    public static class RefreshHandLayoutPatch {
        @SpirePostfixPatch
        public static void Postfix(CardGroup __instance) {
            // Only check if this is the player's hand
            if (__instance != AbstractDungeon.player.hand) {
                return;
            }

            // Prevent recursive calls while we're already processing ethereal cards
            if (processingEthereal) {
                return;
            }

            if (hasButterfingers() && hasAnyEtherealCard()) {
                processingEthereal = true;
                // Add to TOP of queue to block other actions
                AbstractDungeon.actionManager.addToTop(new ButterfingersDiscardAction());
            }
        }
    }

    private static boolean hasButterfingers() {
        return AbstractDungeon.player.hand.group.stream()
                .anyMatch(card -> card instanceof Butterfingers);
    }

    private static boolean hasAnyEtherealCard() {
        return AbstractDungeon.player.hand.group.stream()
                .anyMatch(card -> card.isEthereal);
    }

    // Main action that handles the entire ethereal discard sequence
    private static class ButterfingersDiscardAction extends AbstractGameAction {
        private AbstractCard etherealCard;
        private Phase phase;
        private float timer;

        private enum Phase {
            WAIT_FOR_SETTLE,    // Wait for hand animation to complete
            FLASH_EFFECT,       // Flash Butterfingers cards
            DISCARD_CARD        // Discard the ethereal card
        }

        public ButterfingersDiscardAction() {
            this.actionType = ActionType.SPECIAL;
            this.phase = Phase.WAIT_FOR_SETTLE;
            this.timer = 0.3F; // Wait for hand to settle
            findFirstEtherealCard();
        }

        private void findFirstEtherealCard() {
            this.etherealCard = AbstractDungeon.player.hand.group.stream()
                    .filter(card -> card.isEthereal)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void update() {
            switch (phase) {
                case WAIT_FOR_SETTLE:
                    timer -= Gdx.graphics.getDeltaTime();
                    if (timer <= 0.0F) {
                        if (etherealCard != null && AbstractDungeon.player.hand.contains(etherealCard)) {
                            // Flash all Butterfingers cards red
                            AbstractDungeon.player.hand.group.stream()
                                    .filter(card -> card instanceof Butterfingers)
                                    .forEach(card -> card.flash(Color.RED));

                            phase = Phase.FLASH_EFFECT;
                            timer = 0.3F; // Duration of flash effect
                        } else {
                            // Ethereal card disappeared, finish action
                            finishAction();
                        }
                    }
                    break;

                case FLASH_EFFECT:
                    timer -= Gdx.graphics.getDeltaTime();
                    if (timer <= 0.0F) {
                        phase = Phase.DISCARD_CARD;
                    }
                    break;

                case DISCARD_CARD:
                    if (etherealCard != null && AbstractDungeon.player.hand.contains(etherealCard)) {
                        AbstractDungeon.player.hand.moveToDiscardPile(etherealCard);
                    }
                    finishAction();
                    break;
            }
        }

        private void finishAction() {
            processingEthereal = false;
            this.isDone = true;
            // When this action completes, refreshHandLayout will be called automatically
            // and will process the next ethereal card if any remain
        }
    }
}