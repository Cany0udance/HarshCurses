package harshcurses.patches.othermodpatches;

import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.*;
import harshcurses.cards.ShareTheLove;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

@SpirePatch(
        clz = ApplyPowerAction.class,
        method = "update"
)
public class ShareTheLovePatch {
    private static final Field powerToApplyField;
    private static final Class<?> loseThornsPowerClass;

    static {
        try {
            powerToApplyField = ApplyPowerAction.class.getDeclaredField("powerToApply");
            powerToApplyField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not access powerToApply field", e);
        }

        // Try to load LoseThornsPower class, will be null if mod isn't loaded
        Class<?> tempClass = null;
        try {
            tempClass = Class.forName("brainlets.powers.LoseThornsPower");
        } catch (ClassNotFoundException e) {
            // Mod not loaded, that's fine
        }
        loseThornsPowerClass = tempClass;
    }

    @SpirePostfixPatch
    public static void Postfix(ApplyPowerAction __instance) {
        try {
            // Only trigger when the action is actually done
            if (!__instance.isDone) {
                return;
            }
            // Only trigger when power is being applied to the player
            if (!(__instance.target instanceof AbstractPlayer)) {
                return;
            }
            AbstractPlayer player = (AbstractPlayer) __instance.target;

            // Check if ShareTheLove is in hand
            boolean hasShareTheLove = false;
            for (AbstractCard card : player.hand.group) {
                if (card instanceof ShareTheLove) {
                    hasShareTheLove = true;
                    break;
                }
            }
            if (!hasShareTheLove) {
                return;
            }

            // Get the private powerToApply field using reflection
            AbstractPower powerToApply = (AbstractPower) powerToApplyField.get(__instance);

            // Check if this is a power we want to redirect
            boolean shouldRedirect = powerToApply instanceof LoseStrengthPower ||
                    powerToApply instanceof LoseDexterityPower ||
                    powerToApply instanceof PlatedArmorPower ||
                    (loseThornsPowerClass != null && loseThornsPowerClass.isInstance(powerToApply));

            if (shouldRedirect) {
                // Flash all ShareTheLove cards red when triggered
                for (AbstractCard card : player.hand.group) {
                    if (card instanceof ShareTheLove) {
                        card.flash(Color.RED);
                    }
                }

                // Give the same power to all living enemies
                for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
                    if (!monster.isDead && !monster.escaped) {
                        // Create appropriate power for the monster
                        AbstractPower enemyPower = null;
                        AbstractPower positivePower = null;

                        if (powerToApply instanceof LoseStrengthPower) {
                            enemyPower = new LoseStrengthPower(monster, powerToApply.amount);
                            positivePower = new StrengthPower(monster, powerToApply.amount);
                        } else if (powerToApply instanceof LoseDexterityPower) {
                            enemyPower = new LoseDexterityPower(monster, powerToApply.amount);
                            positivePower = new DexterityPower(monster, powerToApply.amount);
                        } else if (powerToApply instanceof PlatedArmorPower) {
                            enemyPower = new PlatedArmorPower(monster, powerToApply.amount);
                        } else if (loseThornsPowerClass != null && loseThornsPowerClass.isInstance(powerToApply)) {
                            // Handle LoseThornsPower via reflection
                            try {
                                Constructor<?> constructor = loseThornsPowerClass.getConstructor(AbstractCreature.class, int.class);
                                enemyPower = (AbstractPower) constructor.newInstance(monster, powerToApply.amount);
                                positivePower = new ThornsPower(monster, powerToApply.amount);
                            } catch (Exception e) {
                                System.err.println("ShareTheLovePatch: Failed to create LoseThornsPower: " + e.getMessage());
                            }
                        }

                        // Apply powers using actions to avoid conflicts
                        if (enemyPower != null) {
                            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(monster, player, enemyPower));
                        }
                        if (positivePower != null) {
                            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(monster, player, positivePower));
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            // Log error but don't crash the game
            System.err.println("ShareTheLovePatch: Could not access powerToApply field: " + e.getMessage());
        }
    }
}