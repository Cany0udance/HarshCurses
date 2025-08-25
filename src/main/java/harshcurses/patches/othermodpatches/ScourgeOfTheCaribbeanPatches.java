package harshcurses.patches.othermodpatches;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.FleetingField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.common.ExhaustSpecificCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.util.HashSet;

public class ScourgeOfTheCaribbeanPatches {

    // Static set to track cards that were made fleeting by our curse
    private static final HashSet<String> cursedFleetingCards = new HashSet<>();

    // Method to add a card to our tracking
    public static void addCursedFleetingCard(AbstractCard card) {
        cursedFleetingCards.add(card.uuid.toString());
    }

    // Patch to update description when card is hovered/viewed
    @SpirePatch(
            clz = AbstractCard.class,
            method = "update"
    )
    public static class UpdateFleetingDescription {
        @SpirePostfixPatch
        public static void Postfix(AbstractCard __instance) {
            // Force description update for fleeting cards to ensure they show the fleeting text
            if (FleetingField.fleeting.get(__instance)) {
                boolean needsUpdate = false;
                if (__instance.rawDescription == null || !__instance.rawDescription.startsWith("Fleeting.")) {
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    String fleetingText = "Fleeting. NL ";
                    if (__instance.rawDescription == null) {
                        __instance.rawDescription = fleetingText;
                    } else {
                        __instance.rawDescription = fleetingText + __instance.rawDescription;
                    }
                    __instance.initializeDescription();
                }
            }
        }
    }

    // Patch to clear fleeting status at end of turn
    @SpirePatch(
            clz = AbstractRoom.class,
            method = "applyEndOfTurnRelics"
    )
    public static class ClearFleetingAtTurnEnd {
        @SpirePostfixPatch
        public static void Postfix(AbstractRoom __instance) {
            AbstractPlayer player = AbstractDungeon.player;

            // Clear fleeting from all cards that were made fleeting by our curse
            clearCursedFleetingFromCardGroup(player.hand);
            clearCursedFleetingFromCardGroup(player.drawPile);
            clearCursedFleetingFromCardGroup(player.discardPile);

            // Clear our tracking set for the next turn
            cursedFleetingCards.clear();
        }

        private static void clearCursedFleetingFromCardGroup(CardGroup cardGroup) {
            for (AbstractCard card : cardGroup.group) {
                // Only clear fleeting if this card was made fleeting by our curse
                if (FleetingField.fleeting.get(card) && cursedFleetingCards.contains(card.uuid.toString())) {
                    FleetingField.fleeting.set(card, false);

                    // Remove fleeting text from rawDescription
                    if (card.rawDescription != null && card.rawDescription.startsWith("Fleeting. NL ")) {
                        card.rawDescription = card.rawDescription.substring("Fleeting. NL ".length());
                    }

                    // Force description update
                    card.initializeDescription();
                }
            }
        }
    }
}