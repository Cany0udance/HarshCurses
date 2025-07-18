package harshcurses.orbs;

import basemod.abstracts.CustomOrb;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.localization.OrbStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.vfx.combat.OrbFlareEffect;
import harshcurses.HarshCurses;

public class Brick extends CustomOrb {
    public static final String ORB_ID = HarshCurses.makeID("Brick");
    private static final OrbStrings orbString = CardCrawlGame.languagePack.getOrbString(ORB_ID);
    public static final String NAME = orbString.NAME;
    public static final String[] DESCRIPTIONS = orbString.DESCRIPTION;

    private static final String IMG_PATH = "harshcurses/images/orbs/Brick.png";
    private static final float BRICK_WIDTH = 96.0f;
    private static final int BASE_PASSIVE = 1;
    private static final int BASE_EVOKE = 1;

    private float rotation = 0.0f;
    private static final float ROTATION_SPEED = 30.0f; // degrees per second

    public Brick() {
        super(ORB_ID, NAME, BASE_PASSIVE, BASE_EVOKE, "", "", IMG_PATH);
        updateDescription();
    }

    @Override
    public void applyFocus() {
        // This orb ignores Focus - passive and evoke amounts stay constant
        passiveAmount = BASE_PASSIVE;
        evokeAmount = BASE_EVOKE;
    }

    @Override
    public void onEndOfTurn() {
        // Deal 1 damage to the enemy with the most HP
        AbstractMonster target = getEnemyWithMostHP();
        if (target != null) {
            AbstractDungeon.actionManager.addToBottom(
                    new DamageAction(target, new DamageInfo(AbstractDungeon.player, passiveAmount, DamageInfo.DamageType.THORNS))
            );
            AbstractDungeon.effectList.add(new OrbFlareEffect(this, OrbFlareEffect.OrbFlareColor.PLASMA));
        }
    }

    @Override
    public void onEvoke() {
        // Channel a Brick when evoked
        AbstractDungeon.actionManager.addToBottom(new ChannelAction(new Brick()));
    }

    private AbstractMonster getEnemyWithMostHP() {
        AbstractMonster target = null;
        int highestHP = 0;

        for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
            if (!monster.isDeadOrEscaped() && monster.currentHealth > highestHP) {
                highestHP = monster.currentHealth;
                target = monster;
            }
        }

        return target;
    }

    @Override
    public void updateAnimation() {
        super.updateAnimation();

        // Simple rotation animation
        rotation += ROTATION_SPEED * com.badlogic.gdx.Gdx.graphics.getDeltaTime();
        rotation = rotation % 360.0f;

        // Standard orb positioning
        cX = MathHelper.orbLerpSnap(cX, AbstractDungeon.player.animX + tX);
        cY = MathHelper.orbLerpSnap(cY, AbstractDungeon.player.animY + tY);

        if (channelAnimTimer != 0.0F) {
            channelAnimTimer -= com.badlogic.gdx.Gdx.graphics.getDeltaTime();
            if (channelAnimTimer < 0.0F) {
                channelAnimTimer = 0.0F;
            }
        }

        c.a = com.badlogic.gdx.math.Interpolation.pow2In.apply(1.0F, 0.01F, channelAnimTimer / 0.5F);
        scale = com.badlogic.gdx.math.Interpolation.swingIn.apply(Settings.scale, 0.01F, channelAnimTimer / 0.5F);
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setBlendFunction(770, 771);
        sb.setColor(c);

        // Render the brick with rotation
        sb.draw(img,
                cX - BRICK_WIDTH / 2F, cY - BRICK_WIDTH / 2F,
                BRICK_WIDTH / 2F, BRICK_WIDTH / 2F,
                BRICK_WIDTH, BRICK_WIDTH,
                scale, scale,
                rotation,
                0, 0,
                (int) BRICK_WIDTH, (int) BRICK_WIDTH,
                false, false);

        sb.setColor(Color.WHITE);
        renderText(sb);
        hb.render(sb);
    }

    @Override
    public void playChannelSFX() {
        CardCrawlGame.sound.playA("DECK_OPEN", -0.1F);
    }

    @Override
    public void updateDescription() {
        description = DESCRIPTIONS[0] + passiveAmount + DESCRIPTIONS[1] + DESCRIPTIONS[2];
    }

    @Override
    public AbstractOrb makeCopy() {
        return new Brick();
    }
}