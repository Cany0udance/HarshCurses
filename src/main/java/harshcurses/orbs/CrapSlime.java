package harshcurses.orbs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.cards.status.Slimed;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import harshcurses.HarshCurses;
import slimebound.orbs.SpawnedSlime;
import slimebound.vfx.SlimeFlareEffect;

public class CrapSlime extends SpawnedSlime {
    public static final String ID = HarshCurses.makeID("CrapSlime"); // Replace with your mod prefix

    public CrapSlime() {
        this(false);
    }

    public CrapSlime(boolean topLevelVFX) {
        super(
                ID,
                new Color(0.6F, 0.4F, 0.2F, 100.0F), // Brown color for crap theme
                "harshcurses/images/orbs/CrapSlime.atlas",
                "images/monsters/theBottom/slimeS/skeleton.json",
                false, // not medium scale
                false, // not alt positioning
                0, // passive amount (not used for this slime)
                0, // secondary amount
                false, // doesn't move to attack
                new Color(0.6F, 0.4F, 0.2F, 1.0F), // death color
                SlimeFlareEffect.OrbFlareColor.BRONZE, // VFX color
                new Texture("slimeboundResources/SlimeboundImages/orbs/debuff2.png") // Replace with your intent image
        );
        this.topSpawnVFX = topLevelVFX;
        this.spawnVFX();
    }

    @Override
    public void updateDescription() {
        this.name = "Crap Slime";
        // Update description to show it adds Slimed to discard pile
        this.description = "Adds a #ySlimed into your discard pile each turn.";
    }

    @Override
    public void renderText(SpriteBatch sb) {
        // Override to render no text/numbers under the slime
    }

    @Override
    public void activateEffectUnique() {
        // Add a Slimed card to the discard pile
        AbstractDungeon.actionManager.addToBottom(new MakeTempCardInDiscardAction(new Slimed(), 1));
    }

    @Override
    public AbstractOrb makeCopy() {
        return new CrapSlime();
    }
}