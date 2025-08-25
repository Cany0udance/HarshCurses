package harshcurses.actions;

import basemod.BaseMod;
import com.badlogic.gdx.math.Interpolation;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.GremlinThief;
import com.megacrit.cardcrawl.powers.MinionPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.Iterator;

public class SummonGremlinThiefAction extends AbstractGameAction {
    private AbstractMonster m;

    public SummonGremlinThiefAction() {
        this.actionType = ActionType.SPECIAL;
        if (Settings.FAST_MODE) {
            this.startDuration = Settings.ACTION_DUR_FAST;
        } else {
            this.startDuration = Settings.ACTION_DUR_LONG;
        }
        this.duration = this.startDuration;

        // Calculate position
        float[] position = this.calculatePosition();
        float x = position[0];
        float y = position[1];

        // Always spawn a GremlinThief
        this.m = MonsterHelper.getGremlin(GremlinThief.ID, x, y);

        // Force the position since MonsterHelper.getGremlin seems to ignore our coordinates
        this.m.drawX = x;
        this.m.drawY = y;

        // Trigger relic effects
        Iterator var3 = AbstractDungeon.player.relics.iterator();
        while(var3.hasNext()) {
            AbstractRelic r = (AbstractRelic)var3.next();
            r.onSpawnMonster(this.m);
        }
    }

    private float[] calculatePosition() {
        float leftmostX = Float.MAX_VALUE;
        float playerY = AbstractDungeon.player.drawY;

        // Find the leftmost existing enemy
        for (AbstractMonster monster : AbstractDungeon.getCurrRoom().monsters.monsters) {
            if (!monster.isDying && !monster.isDead) {
                if (monster.drawX < leftmostX) {
                    leftmostX = monster.drawX;
                }
            }
        }

        float randomOffset = AbstractDungeon.aiRng.random(150.0F, 200.0F);
        float x = leftmostX - (randomOffset * Settings.scale);
        float y = playerY;

        return new float[]{x, y};
    }

    private int getSmartPosition() {
        int position = 0;
        for(Iterator var2 = AbstractDungeon.getCurrRoom().monsters.monsters.iterator(); var2.hasNext(); ++position) {
            AbstractMonster mo = (AbstractMonster)var2.next();
            if (!(this.m.drawX > mo.drawX)) {
                break;
            }
        }
        return position;
    }

    public void update() {
        if (this.duration == this.startDuration) {
            // Get smart position BEFORE setting animX
            int smartPos = this.getSmartPosition();

            // Now set up the animation
            this.m.animX = 1200.0F * Settings.xScale;
            this.m.init();
            this.m.applyPowers();

            // Force the monster to roll its move for this turn so it shows intent immediately
            this.m.rollMove();
            this.m.createIntent();

            AbstractDungeon.getCurrRoom().monsters.addMonster(smartPos, this.m);

            if (AbstractDungeon.ascensionLevel >= 2) {
                this.addToBot(new ApplyPowerAction(this.m, this.m, new StrengthPower(this.m, -9), -9));
            } else {
                this.addToBot(new ApplyPowerAction(this.m, this.m, new StrengthPower(this.m, -8), -8));
            }

            this.addToBot(new ApplyPowerAction(this.m, this.m, new MinionPower(this.m)));
        }

        this.tickDuration();

        if (this.isDone) {
            this.m.animX = 0.0F;
            this.m.showHealthBar();
            this.m.usePreBattleAction();
        } else {
            this.m.animX = Interpolation.fade.apply(0.0F, 1200.0F * Settings.xScale, this.duration);
        }
    }
}