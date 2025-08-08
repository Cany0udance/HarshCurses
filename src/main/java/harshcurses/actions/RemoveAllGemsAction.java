package harshcurses.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import theVacant.orbs.AbstractGemOrb;

public class RemoveAllGemsAction extends AbstractGameAction {
    public RemoveAllGemsAction() {
        this.actionType = ActionType.SPECIAL;
    }

    public void update() {
        // Remove all gem orbs from the player's orb slots
        AbstractDungeon.player.orbs.removeIf(orb -> orb instanceof AbstractGemOrb);

        // Since gems create orb slots, we need to refresh the orb system
        // Reset max orbs to account for removed gem slots
        AbstractDungeon.player.maxOrbs = AbstractDungeon.player.masterMaxOrbs;

        // Add back orb slots for any remaining orbs that aren't gems
        for (AbstractOrb orb : AbstractDungeon.player.orbs) {
            if (!(orb instanceof AbstractGemOrb)) {
                AbstractDungeon.player.maxOrbs++;
            }
        }

        // Update orb positions and slots
        for (int i = 0; i < AbstractDungeon.player.orbs.size(); i++) {
            AbstractDungeon.player.orbs.get(i).setSlot(i, AbstractDungeon.player.maxOrbs);
        }

        this.isDone = true;
    }
}