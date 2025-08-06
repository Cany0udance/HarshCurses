package harshcurses.cards;

import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import guardian.GuardianMod;
import guardian.cards.AbstractGemCard;
import guardian.cards.AbstractGuardianCard;
import harshcurses.HarshCurses;
import harshcurses.actions.IncreaseDebuffsAction;

import java.lang.reflect.Field;

public class BlackDiamond extends AbstractGemCard {
    public static final String ID = HarshCurses.makeID("BlackDiamond");
    public static final String NAME;
    public static final String IMG_PATH = "cards/blackDiamond.png";
    private static final AbstractCard.CardType TYPE;
    private static final AbstractCard.CardRarity RARITY;
    private static final AbstractCard.CardTarget TARGET;
    private static final CardStrings cardStrings;
    private static final int COST = 0;
    public static String DESCRIPTION;
    public static String UPGRADED_DESCRIPTION;

    public BlackDiamond() {
        super(ID, NAME, "harshcurses/images/cards/curse/BlackDiamond.png", 0, DESCRIPTION, TYPE, CardColor.CURSE, RARITY, TARGET);
        this.tags.add(GuardianMod.GEM);

        // Use reflection to set thisGemsType to bypass type checking
        try {
            Field thisGemsTypeField = AbstractGuardianCard.class.getField("thisGemsType");
            thisGemsTypeField.set(this, HarshCurses.BlackDiamondType.BLACK);
        } catch (Exception e) {
            System.err.println("Failed to set thisGemsType: " + e.getMessage());
        }

        SoulboundField.soulbound.set(this, true);
        this.isEthereal = true;
    }

    public static void gemEffect(AbstractPlayer p, AbstractMonster m) {
        AbstractDungeon.actionManager.addToBottom(new IncreaseDebuffsAction(p, 1));
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        super.use(p, m);
        // This card does nothing when played, but we still call gemEffect for consistency
        // when it's socketed into other cards
        gemEffect(p, m);
    }

    public void triggerWhenDrawn() {
        AbstractDungeon.actionManager.addToBottom(new IncreaseDebuffsAction(AbstractDungeon.player, 1));
    }

    public AbstractCard makeCopy() {
        return new BlackDiamond();
    }

    public void upgrade() {
        // Curses typically don't upgrade
    }

    public static boolean isBlackDiamond(AbstractCard card) {
        return card instanceof BlackDiamond;
    }

    public boolean canUpgrade() {
        return false;
    }

    static {
        TYPE = CardType.CURSE;
        RARITY = CardRarity.SPECIAL;
        TARGET = CardTarget.NONE;
        cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
        NAME = cardStrings.NAME;
        DESCRIPTION = cardStrings.DESCRIPTION;
        UPGRADED_DESCRIPTION = cardStrings.UPGRADE_DESCRIPTION;
    }
}