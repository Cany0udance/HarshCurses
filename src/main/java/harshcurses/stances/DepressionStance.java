package harshcurses.stances;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.StanceStrings;
import com.megacrit.cardcrawl.stances.AbstractStance;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.stance.StanceAuraEffect;
import harshcurses.HarshCurses;
import harshcurses.effects.DepressionAuraEffect;
import harshcurses.effects.DepressionParticleEffect;

public class DepressionStance extends AbstractStance {
    public static final String STANCE_ID = HarshCurses.makeID("Depression");
    private static final StanceStrings stanceStrings = CardCrawlGame.languagePack.getStanceString(STANCE_ID);
    private static final String NAME = stanceStrings.NAME;
    private static final String[] DESCRIPTION = stanceStrings.DESCRIPTION;
    private static long sfxId;

    public DepressionStance() {
        this.ID = STANCE_ID;
        this.name = NAME;
        this.updateDescription();
    }

    public void updateDescription() {
        this.description = stanceStrings.DESCRIPTION[0];
    }

    public void updateAnimation() {
        if (!Settings.DISABLE_EFFECTS) {
            this.particleTimer -= Gdx.graphics.getDeltaTime();
            if (this.particleTimer < 0.0F) {
                this.particleTimer = 0.2F; // Much slower particle generation
                AbstractDungeon.effectsQueue.add(new DepressionParticleEffect());
            }
        }
        this.particleTimer2 -= Gdx.graphics.getDeltaTime();
        if (this.particleTimer2 < 0.0F) {
            this.particleTimer2 = MathUtils.random(0.45F, 0.55F);
            AbstractDungeon.effectsQueue.add(new DepressionAuraEffect());
        }
    }

    public float atDamageGive(float damage, DamageInfo.DamageType type) {
        return type == DamageInfo.DamageType.NORMAL ? damage * 0.5F : damage;
    }

    public void onEnterStance() {
        if (sfxId != -1L) {
            this.stopIdleSfx();
        }
        CardCrawlGame.sound.play("STANCE_ENTER_CALM", -0.4F); // heavily pitched down
        AbstractDungeon.effectsQueue.add(new BorderFlashEffect(Color.PURPLE, true));
    }
}