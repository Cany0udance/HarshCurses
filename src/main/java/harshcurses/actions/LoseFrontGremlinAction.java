package harshcurses.actions;

import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.orbs.EmptyOrbSlot;
import gremlin.GremlinMod;
import gremlin.characters.GremlinCharacter;
import gremlin.orbs.GremlinStandby;
import gremlin.powers.GremlinNobPower;
import harshcurses.HarshCurses;

public class LoseFrontGremlinAction extends AbstractGameAction {

    private static final float WALK_SPEED = 400.0f * Settings.scale;
    private static final float ANIMATION_DURATION = 2.5f;

    private boolean hasStarted = false;
    private boolean animationComplete = false;
    private float originalDrawX;
    private boolean originalFlipState;
    private float walkDistance;

    public LoseFrontGremlinAction() {
        this.actionType = ActionType.SPECIAL;
        this.duration = ANIMATION_DURATION;
        this.walkDistance = 0.0f;
    }

    @Override
    public void update() {
        // Early exit if not playing as Gremlin character
        if (!(AbstractDungeon.player instanceof GremlinCharacter)) {
            this.isDone = true;
            return;
        }


        if (AbstractDungeon.player.hasPower(GremlinNobPower.POWER_ID)) {
            this.isDone = true;
            return;
        }

        GremlinCharacter player = (GremlinCharacter) AbstractDungeon.player;

        // Initialize animation on first update
        if (!hasStarted) {
            initializeAnimation(player);
        }

        // Continue walking animation until complete
        if (!animationComplete) {
            updateWalkAnimation(player);
        }

        // Check if animation duration is complete OR if walk animation is complete
        this.duration -= Gdx.graphics.getDeltaTime();
        if ((this.duration <= 0.0f || animationComplete) && !this.isDone) {
            // Animation is complete, now handle the gremlin "death"
            completeGremlinDeath(player);
        }
    }

    private void initializeAnimation(GremlinCharacter player) {
        hasStarted = true;

        // Store original state
        originalDrawX = player.drawX;
        originalFlipState = player.flipHorizontal;

        // Flip gremlin to face left (exit direction)
        player.flipHorizontal = true;

        // Play death sound effect (gremlin saying they need to leave)
        player.gremlinDeathSFX();
    }

    private void updateWalkAnimation(GremlinCharacter player) {
        // Move gremlin left off screen
        float deltaMovement = Gdx.graphics.getDeltaTime() * WALK_SPEED;
        player.drawX -= deltaMovement;
        walkDistance += deltaMovement;

        // Check if gremlin has walked far enough off screen
        if (walkDistance >= 1000.0f * Settings.scale) {
            animationComplete = true;
        }
    }

    private void completeGremlinDeath(GremlinCharacter player) {
        // Reset the character's position and flip state for the next gremlin
        player.drawX = originalDrawX;
        player.flipHorizontal = originalFlipState;

        // Update the mob state to mark this gremlin as dead
        player.mobState.gremlinHP.set(0, 0);

        // Mark the gremlin as temporarily enslaved (bathroom break)
        String departedGremlin = player.currentGremlin;
        int departedGremlinHP = player.currentHealth;
        player.mobState.enslave(departedGremlin);

        // Track which gremlin took a bathroom break and their HP
        HarshCurses.bathroomBreakGremlin = departedGremlin;
        HarshCurses.bathroomBreakGremlinHP = departedGremlinHP;

        // Find the next living gremlin to swap to
        String nextGremlin = null;
        String nextAnimation = null;
        int nextGremlinHP = 0;

        // Look through the orbs to find a living gremlin
        for (int i = 0; i < player.orbs.size(); i++) {
            if (player.orbs.get(i) instanceof GremlinStandby) {
                GremlinStandby standbyGremlin = (GremlinStandby) player.orbs.get(i);
                if (standbyGremlin.hp > 0) {
                    nextGremlin = standbyGremlin.assetFolder;
                    nextAnimation = standbyGremlin.animationName;
                    nextGremlinHP = standbyGremlin.hp;

                    // Remove this gremlin from orbs and shift remaining orbs down
                    player.orbs.remove(i);

                    // Add an empty orb slot at the end to maintain proper array size
                    player.orbs.add(new EmptyOrbSlot());

                    // Re-position all orbs after the removal
                    for (int j = 0; j < player.orbs.size(); j++) {
                        player.orbs.get(j).setSlot(j, player.maxOrbs);
                    }
                    break;
                }
            }
        }

        if (nextGremlin != null) {
            // Swap the character body to the next gremlin
            player.swapBody(nextGremlin, nextAnimation);

            // Set the new health
            player.currentHealth = nextGremlinHP;
            player.healthBarUpdatedEvent();

            // Update mob state to reflect the new front gremlin
            // Move the new gremlin to position 0 in the mob state
            for (int i = 1; i < player.mobState.gremlins.size(); i++) {
                if (player.mobState.gremlins.get(i).equals(nextGremlin)) {
                    // Swap positions in the mob state
                    String tempGremlin = player.mobState.gremlins.get(0);
                    player.mobState.gremlins.set(0, nextGremlin);
                    player.mobState.gremlins.set(i, tempGremlin);

                    // Update HP
                    player.mobState.gremlinHP.set(0, nextGremlinHP);
                    player.mobState.gremlinHP.set(i, 0);
                    break;
                }
            }

            // Apply the new gremlin's power
            GremlinStandby newGremlinOrb = GremlinMod.getGremlinOrb(nextGremlin);
            if (newGremlinOrb != null && newGremlinOrb.getPower() != null) {
                AbstractDungeon.actionManager.addToTop(new ApplyPowerAction(player, player, newGremlinOrb.getPower(), 1));
            }
        } else {
            // No living gremlins left - the player should be defeated
            player.currentHealth = 0;
            player.healthBarUpdatedEvent();

            // Trigger the actual death
            DamageInfo deathDamage = new DamageInfo(null, 1, DamageInfo.DamageType.HP_LOSS);
            player.damage(deathDamage);
        }

        // Fix orb positioning after state change
        fixOrbPositions(player);

        this.isDone = true;
    }

    private void fixOrbPositions(GremlinCharacter player) {
        for (int i = 0; i < player.orbs.size(); i++) {
            player.orbs.get(i).setSlot(i, player.maxOrbs);
        }
    }
}