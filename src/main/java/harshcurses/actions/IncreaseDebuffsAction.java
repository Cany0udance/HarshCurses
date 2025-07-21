package harshcurses.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;

import java.util.Iterator;

public class IncreaseDebuffsAction extends AbstractGameAction {
    private AbstractCreature c;

    public IncreaseDebuffsAction(AbstractCreature c, int amount) {
        this.c = c;
        this.duration = 0.5F;
        this.amount = amount;
    }

    public void update() {
        if (this.amount <= 0) {
            this.isDone = true;
        } else {
            Iterator<AbstractPower> var1 = this.c.powers.iterator();

            while (var1.hasNext()) {
                final AbstractPower p = var1.next();

                if (p.type == AbstractPower.PowerType.DEBUFF) {
                    if (p.amount > 0) {
                        // Increase positive debuff amounts by directly stacking the power
                        this.addToTop(new AbstractGameAction() {
                            public void update() {
                                p.stackPower(IncreaseDebuffsAction.this.amount);
                                p.updateDescription();
                                AbstractDungeon.onModifyPower();
                                this.isDone = true;
                            }
                        });
                    } else if (p.amount < 0) {
                        // Make negative debuffs more negative
                        this.addToTop(new AbstractGameAction() {
                            public void update() {
                                p.stackPower(-IncreaseDebuffsAction.this.amount);
                                p.updateDescription();
                                AbstractDungeon.onModifyPower();
                                this.isDone = true;
                            }
                        });
                    }
                }
            }
            this.isDone = true;
        }
    }
}