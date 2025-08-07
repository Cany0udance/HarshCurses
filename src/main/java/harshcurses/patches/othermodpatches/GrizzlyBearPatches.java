package harshcurses.patches.othermodpatches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.combat.BiteEffect;
import harshcurses.cards.GrizzlyBear;

import java.util.ArrayList;

public class GrizzlyBearPatches {
    private static boolean processingFish = false; // Prevent recursive processing

    @SpirePatch(
            optional = true,
            cls = "com.megacrit.cardcrawl.cards.CardGroup",
            method = "refreshHandLayout"
    )
    public static class RefreshHandLayoutPatch {
        @SpirePostfixPatch
        public static void Postfix(CardGroup __instance) {
            // Only check if this is the player's hand
            if (__instance != AbstractDungeon.player.hand) {
                return;
            }

            // Prevent recursive calls while we're already processing a fish
            if (processingFish) {
                return;
            }

            if (hasGrizzlyBear() && hasAnyFishCard()) {
                processingFish = true;
                // Add to TOP of queue to block other actions
                AbstractDungeon.actionManager.addToTop(new GrizzlyBearChompAction());
            }
        }
    }

    private static boolean hasGrizzlyBear() {
        return AbstractDungeon.player.hand.group.stream()
                .anyMatch(card -> card instanceof GrizzlyBear);
    }

    private static boolean hasAnyFishCard() {
        try {
            Class<?> abstractFishCardClass = Class.forName("theFishing.cards.fish.AbstractFishCard");
            return AbstractDungeon.player.hand.group.stream()
                    .anyMatch(abstractFishCardClass::isInstance);
        } catch (ClassNotFoundException e) {
            return false; // FishingMod not loaded
        } catch (Exception e) {
            return false; // Other error
        }
    }

    // Main action that handles the entire chomping sequence
    private static class GrizzlyBearChompAction extends AbstractGameAction {
        private AbstractCard fishCard;
        private Phase phase;
        private float timer;

        private enum Phase {
            WAIT_FOR_SETTLE,    // Wait for hand animation to complete
            BITE_EFFECT,        // Show bite effect and flash bears
            WAIT_FOR_CHOMP,     // Wait for bite effect to complete
            EXHAUST_FISH        // Remove the fish card
        }

        public GrizzlyBearChompAction() {
            this.actionType = ActionType.SPECIAL;
            this.phase = Phase.WAIT_FOR_SETTLE;
            this.timer = 0.3F; // Wait for hand to settle
            findFirstFishCard();
        }

        private void findFirstFishCard() {
            try {
                Class<?> abstractFishCardClass = Class.forName("theFishing.cards.fish.AbstractFishCard");
                this.fishCard = AbstractDungeon.player.hand.group.stream()
                        .filter(abstractFishCardClass::isInstance)
                        .findFirst()
                        .orElse(null);
            } catch (ClassNotFoundException e) {
                this.fishCard = null; // FishingMod not loaded
            } catch (Exception e) {
                this.fishCard = null; // Other error
            }
        }

        @Override
        public void update() {
            switch (phase) {
                case WAIT_FOR_SETTLE:
                    timer -= Gdx.graphics.getDeltaTime();
                    if (timer <= 0.0F) {
                        if (fishCard != null && AbstractDungeon.player.hand.contains(fishCard)) {
                            // Calculate bite effect position (now that card is settled)
                            float effectX = fishCard.hb.cX;
                            float effectY = fishCard.hb.cY + 50.0F * Settings.scale;

                            // Add bite effect
                            AbstractDungeon.topLevelEffects.add(new BiteEffect(effectX, effectY));

                            // Flash all grizzly bears
                            AbstractDungeon.player.hand.group.stream()
                                    .filter(card -> card instanceof GrizzlyBear)
                                    .forEach(card -> card.flash(Color.RED));

                            phase = Phase.BITE_EFFECT;
                            timer = 0.5F; // Duration of bite effect
                        } else {
                            // Fish card disappeared, finish action
                            finishAction();
                        }
                    }
                    break;

                case BITE_EFFECT:
                    timer -= Gdx.graphics.getDeltaTime();
                    if (timer <= 0.0F) {
                        phase = Phase.EXHAUST_FISH;
                    }
                    break;

                case EXHAUST_FISH:
                    if (fishCard != null && AbstractDungeon.player.hand.contains(fishCard)) {
                        AbstractDungeon.player.hand.moveToExhaustPile(fishCard);
                    }
                    finishAction();
                    break;
            }
        }

        private void finishAction() {
            processingFish = false;
            this.isDone = true;
            // When this action completes, refreshHandLayout will be called automatically
            // and will process the next fish if any remain
        }
    }
}