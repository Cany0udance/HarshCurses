package harshcurses.patches.basecursespatches;

import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import harshcurses.cards.SatansGrudge;
import harshcurses.powers.BegrudgedPower;

@SpirePatch(clz = CardGroup.class, method = "moveToExhaustPile")
public class SatansGrudgePatch {
    @SpirePostfixPatch
    public static void postfixMoveToExhaustPile(CardGroup __instance, AbstractCard card) {
        // Only trigger during combat and if a card was actually exhausted
        if (AbstractDungeon.player != null && AbstractDungeon.getCurrRoom() instanceof MonsterRoom) {
            // Check if any Satan's Grudge cards are in hand
            for (AbstractCard handCard : AbstractDungeon.player.hand.group) {
                if (handCard instanceof SatansGrudge) {
                    // Apply 2 temporary damage
                    applyTemporaryDamage(AbstractDungeon.player, 2);
                    // Flash the card red to show it triggered
                    handCard.flash(Color.RED);
                }
            }
        }
    }
    private static void applyTemporaryDamage(AbstractPlayer player, int amount) {
        // First apply actual HP damage
        player.damage(new DamageInfo(null, amount, DamageInfo.DamageType.HP_LOSS));
        // Then add/stack the TemporaryDamagePower
        BegrudgedPower existingPower = null;
        for (AbstractPower power : player.powers) {
            if (power instanceof BegrudgedPower) {
                existingPower = (BegrudgedPower) power;
                break;
            }
        }
        if (existingPower != null) {
            existingPower.amount += amount;
            existingPower.updateDescription();
        } else {
            player.powers.add(new BegrudgedPower(player, amount));
        }
    }
}