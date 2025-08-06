package harshcurses.patches.downfallcursespatches;

import basemod.abstracts.CustomCard;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import guardian.GuardianMod;
import guardian.cards.*;
import harshcurses.HarshCurses;
import harshcurses.actions.IncreaseDebuffsAction;
import harshcurses.cards.BlackDiamond;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BlackDiamondPatches {

    // Helper method to add objects to the sockets list bypassing type checking
    private static void addToSockets(AbstractGuardianCard card, Object socketType) {
        try {
            Field socketsField = AbstractGuardianCard.class.getField("sockets");
            ArrayList sockets = (ArrayList) socketsField.get(card);
            sockets.add(socketType);
        } catch (Exception e) {
            GuardianMod.logger.error("Failed to add socket type: " + e.getMessage());
        }
    }

    // Patch the sockets field to accept Object type instead of GuardianMod.socketTypes
    @SpirePatch(clz = AbstractGuardianCard.class, method = SpirePatch.CLASS)
    public static class SocketsFieldPatch {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();

            // Remove the old sockets field
            try {
                CtField oldSocketsField = ctClass.getField("sockets");
                ctClass.removeField(oldSocketsField);
            } catch (NotFoundException e) {
                // Field might not exist, continue
            }

            // Add new sockets field with Object type
            CtField newSocketsField = CtField.make("public java.util.ArrayList sockets = new java.util.ArrayList();", ctClass);
            ctClass.addField(newSocketsField);
        }
    }

    @SpirePatch(clz = AbstractGuardianCard.class, method = "saveGemMisc")
    public static class SaveGemMiscPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(AbstractGuardianCard __instance) {
            if (AbstractDungeon.player != null && AbstractDungeon.player.masterDeck.contains(__instance)) {
                __instance.misc = 10 + __instance.socketCount;
                if (__instance.sockets.size() > 0) {
                    for (int i = 0; i < __instance.sockets.size(); ++i) {
                        __instance.misc *= 100;
                        int gemindex = 0;
                        Object socketType = __instance.sockets.get(i);

                        if (socketType == null) {
                            // Handle null socket - shouldn't happen but let's be safe
                            gemindex = 0; // Default to RED
                        } else if (socketType instanceof HarshCurses.BlackDiamondType) {
                            gemindex = 12; // Assign index 12 for BLACK
                        } else if (socketType instanceof GuardianMod.socketTypes) {
                            // Handle original GuardianMod socket types
                            GuardianMod.socketTypes guardianSocketType = (GuardianMod.socketTypes) socketType;
                            switch (guardianSocketType) {
                                case RED: gemindex = 0; break;
                                case GREEN: gemindex = 1; break;
                                case ORANGE: gemindex = 2; break;
                                case WHITE: gemindex = 3; break;
                                case CYAN: gemindex = 4; break;
                                case BLUE: gemindex = 5; break;
                                case CRIMSON: gemindex = 6; break;
                                case FRAGMENTED: gemindex = 7; break;
                                case PURPLE: gemindex = 8; break;
                                case SYNTHETIC: gemindex = 9; break;
                                case YELLOW: gemindex = 10; break;
                                case LIGHTBLUE: gemindex = 11; break;
                            }
                        } else {
                            // Unknown socket type - default to RED
                            gemindex = 0;
                        }
                        __instance.misc += 10 + gemindex;
                    }
                }
            }
            return SpireReturn.Return();
        }
    }

    @SpirePatch(clz = AbstractGuardianCard.class, method = "loadGemMisc")
    public static class LoadGemMiscPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(AbstractGuardianCard __instance) {
            __instance.sockets.clear();
            if (__instance.misc > 0 && Integer.toString(__instance.misc).length() % 2 == 0) {
                String miscString = Integer.toString(__instance.misc);
                String socketCountString = miscString.substring(0, 2);
                __instance.socketCount = Integer.parseInt(socketCountString) - 10;
                if (miscString.length() <= 2) {
                    __instance.updateDescription();
                    return SpireReturn.Return();
                }

                miscString = miscString.substring(2);
                int loops = miscString.length() / 2;

                for (int i = 0; i < loops; ++i) {
                    String gemCode = miscString.substring(0, 2);
                    switch (gemCode) {
                        case "10": __instance.sockets.add(GuardianMod.socketTypes.RED); break;
                        case "11": __instance.sockets.add(GuardianMod.socketTypes.GREEN); break;
                        case "12": __instance.sockets.add(GuardianMod.socketTypes.ORANGE); break;
                        case "13": __instance.sockets.add(GuardianMod.socketTypes.WHITE); break;
                        case "14": __instance.sockets.add(GuardianMod.socketTypes.CYAN); break;
                        case "15": __instance.sockets.add(GuardianMod.socketTypes.BLUE); break;
                        case "16": __instance.sockets.add(GuardianMod.socketTypes.CRIMSON); break;
                        case "17": __instance.sockets.add(GuardianMod.socketTypes.FRAGMENTED); break;
                        case "18": __instance.sockets.add(GuardianMod.socketTypes.PURPLE); break;
                        case "19": __instance.sockets.add(GuardianMod.socketTypes.SYNTHETIC); break;
                        case "20": __instance.sockets.add(GuardianMod.socketTypes.YELLOW); break;
                        case "21": __instance.sockets.add(GuardianMod.socketTypes.LIGHTBLUE); break;
                        case "22": addToSockets(__instance, HarshCurses.BlackDiamondType.BLACK); break;
                        default: __instance.sockets.add(GuardianMod.socketTypes.RED);
                    }

                    miscString = miscString.substring(2);
                    GuardianMod.logger.info("New misc gem load: " + __instance.name + " new misc = " + miscString);
                }
                __instance.updateDescription();
            }
            return SpireReturn.Return();
        }
    }

    @SpirePatch(clz = AbstractGuardianCard.class, method = "useGems")
    public static class UseGemsPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(AbstractGuardianCard __instance, AbstractPlayer p, AbstractMonster m) {
            for (Object gem : __instance.sockets) {
                if (gem instanceof HarshCurses.BlackDiamondType) {
                    BlackDiamond.gemEffect(p, m);
                } else {
                    GuardianMod.socketTypes guardianGem = (GuardianMod.socketTypes) gem;
                    switch (guardianGem) {
                        case RED: Gem_Red.gemEffect(p, m); break;
                        case GREEN: Gem_Green.gemEffect(p, m); break;
                        case ORANGE: Gem_Orange.gemEffect(p, m); break;
                        case WHITE: Gem_White.gemEffect(p, m); break;
                        case CYAN: Gem_Cyan.gemEffect(p, m); break;
                        case BLUE: Gem_Blue.gemEffect(p, m); break;
                        case CRIMSON: Gem_Crimson.gemEffect(p, m); break;
                        case FRAGMENTED: Gem_Fragmented.gemEffect(p, m); break;
                        case PURPLE: Gem_Purple.gemEffect(p, m); break;
                        case SYNTHETIC: Gem_Synthetic.gemEffect(p, m); break;
                        case YELLOW: Gem_Yellow.gemEffect(p, m); break;
                        case LIGHTBLUE: Gem_Lightblue.gemEffect(p, m); break;
                    }
                }
            }
            return SpireReturn.Return();
        }
    }

    @SpirePatch(clz = TwinSlam.class, method = "use")
    public static class TwinSlamUsePatch {
        private static boolean isProcessing = false;

        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(TwinSlam __instance, AbstractPlayer p, AbstractMonster m) {
            // Prevent recursive calls
            if (isProcessing) {
                System.out.println("PREVENTING RECURSIVE TWINSLAM CALL!");
                return SpireReturn.Continue(); // Let original method handle it
            }

            isProcessing = true;

            try {
                System.out.println("=== TwinSlam Use Patch Started ===");
                System.out.println("TwinSlam has " + __instance.sockets.size() + " sockets");

                // Handle the basic card use mechanics manually (energy, etc.)
                // Don't call super.use() at all to avoid any potential double effects

                // Deal damage (copied from original TwinSlam.use())
                AbstractDungeon.actionManager.addToBottom(new DamageAction(m, new DamageInfo(p, __instance.damage, __instance.damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HEAVY));

                // Create SecondStrike card
                AbstractGuardianCard q = new SecondStrike();
                if (__instance.upgraded) {
                    q.upgrade();
                }

                // Copy socket count first
                q.socketCount = 0; // Reset to avoid conflicts

                // Copy sockets with safe casting
                for (Object socketObj : __instance.sockets) {
                    if (socketObj instanceof GuardianMod.socketTypes) {
                        // Original Guardian gem - copy normally
                        q.sockets.add((GuardianMod.socketTypes) socketObj);
                        q.socketCount++;
                    } else if (socketObj instanceof HarshCurses.BlackDiamondType) {
                        // Our custom gem - add it safely using reflection
                        try {
                            Field socketsField = AbstractGuardianCard.class.getField("sockets");
                            ArrayList sockets = (ArrayList) socketsField.get(q);
                            sockets.add(HarshCurses.BlackDiamondType.BLACK);
                            q.socketCount++;
                        } catch (Exception e) {
                            System.err.println("Failed to add Black Diamond to SecondStrike: " + e.getMessage());
                        }
                    }
                }

                System.out.println("Created SecondStrike with " + q.socketCount + " sockets, actual list size: " + q.sockets.size());

                // Add the card to hand
                AbstractDungeon.actionManager.addToBottom(new MakeTempCardInHandAction(q, true));

                // Use gems with our custom logic (this replaces the super.useGems call)
                useGemsCustom(__instance, p, m);

                System.out.println("=== TwinSlam Use Patch Complete ===");
                return SpireReturn.Return(); // Skip original method
            } finally {
                isProcessing = false;
            }
        }

        private static void useGemsCustom(AbstractGuardianCard card, AbstractPlayer p, AbstractMonster m) {
            for (Object gem : card.sockets) {
                if (gem instanceof HarshCurses.BlackDiamondType) {
                    BlackDiamond.gemEffect(p, m);
                } else if (gem instanceof GuardianMod.socketTypes) {
                    GuardianMod.socketTypes guardianGem = (GuardianMod.socketTypes) gem;
                    switch (guardianGem) {
                        case RED: Gem_Red.gemEffect(p, m); break;
                        case GREEN: Gem_Green.gemEffect(p, m); break;
                        case ORANGE: Gem_Orange.gemEffect(p, m); break;
                        case WHITE: Gem_White.gemEffect(p, m); break;
                        case CYAN: Gem_Cyan.gemEffect(p, m); break;
                        case BLUE: Gem_Blue.gemEffect(p, m); break;
                        case CRIMSON: Gem_Crimson.gemEffect(p, m); break;
                        case FRAGMENTED: Gem_Fragmented.gemEffect(p, m); break;
                        case PURPLE: Gem_Purple.gemEffect(p, m); break;
                        case SYNTHETIC: Gem_Synthetic.gemEffect(p, m); break;
                        case YELLOW: Gem_Yellow.gemEffect(p, m); break;
                        case LIGHTBLUE: Gem_Lightblue.gemEffect(p, m); break;
                    }
                }
            }
        }
    }

    @SpirePatch(clz = AbstractCard.class, method = "triggerWhenDrawn")
    public static class TriggerWhenDrawnPatch {
        @SpirePostfixPatch
        public static void Postfix(AbstractCard __instance) {
            // Check if this is an AbstractGuardianCard with Black Diamond sockets
            if (__instance instanceof AbstractGuardianCard) {
                AbstractGuardianCard guardianCard = (AbstractGuardianCard) __instance;

                // Check each socket for Black Diamonds
                for (Object gem : guardianCard.sockets) {
                    if (gem instanceof HarshCurses.BlackDiamondType) {
                        System.out.println("Black Diamond socketed card drawn: " + __instance.name);
                        AbstractDungeon.actionManager.addToBottom(new IncreaseDebuffsAction(AbstractDungeon.player, 1));
                        break; // Only trigger once per card drawn, regardless of multiple Black Diamonds
                    }
                }
            }
        }
    }

    @SpirePatch(clz = AbstractGuardianCard.class, method = "updateGemDescription")
    public static class UpdateGemDescriptionPatch {
        private static final Set<AbstractGuardianCard> processingCards = new HashSet<>();

        @SpirePrefixPatch
        public static SpireReturn<String> Prefix(AbstractGuardianCard __instance, String desc, Boolean after) {
            // Prevent infinite recursion
            if (processingCards.contains(__instance)) {
                System.out.println("Preventing infinite recursion for card: " + __instance.name);
                return SpireReturn.Continue(); // Let original method handle it
            }

            processingCards.add(__instance);

            try {
                String addedDesc = "";

                // Debug: Print socket information (but less verbose)
                System.out.println("Updating description for: " + __instance.name + " (sockets: " + __instance.socketCount + "/" + __instance.sockets.size() + ")");

                // Check if this card has Black Diamond and needs soulbound text added
                boolean hasBlackDiamond = false;
                boolean isInherentlySoulbound = (__instance instanceof BlackDiamond);

                for (Object socket : __instance.sockets) {
                    if (socket instanceof HarshCurses.BlackDiamondType) {
                        hasBlackDiamond = true;
                        break;
                    }
                }

                for (int i = 0; i < __instance.socketCount; ++i) {
                    if (__instance.sockets.size() > i) {
                        Object gem = __instance.sockets.get(i);

                        if (after) {
                            addedDesc = addedDesc + " NL ";
                        }

                        if (gem == null) {
                            addedDesc = addedDesc + "Empty Socket";
                        } else if (gem instanceof HarshCurses.BlackDiamondType) {
                            if (BlackDiamond.UPGRADED_DESCRIPTION != null && !BlackDiamond.UPGRADED_DESCRIPTION.isEmpty()) {
                                addedDesc = addedDesc + BlackDiamond.UPGRADED_DESCRIPTION;
                            } else {
                                addedDesc = addedDesc + "When you draw this card, increase each of your debuffs by 1.";
                            }
                        } else if (gem instanceof GuardianMod.socketTypes) {
                            GuardianMod.socketTypes guardianGem = (GuardianMod.socketTypes) gem;
                            switch (guardianGem) {
                                case RED: addedDesc = addedDesc + Gem_Red.UPGRADED_DESCRIPTION; break;
                                case GREEN: addedDesc = addedDesc + Gem_Green.UPGRADED_DESCRIPTION; break;
                                case ORANGE: addedDesc = addedDesc + Gem_Orange.UPGRADED_DESCRIPTION; break;
                                case WHITE: addedDesc = addedDesc + Gem_White.UPGRADED_DESCRIPTION; break;
                                case CYAN: addedDesc = addedDesc + Gem_Cyan.UPGRADED_DESCRIPTION; break;
                                case BLUE: addedDesc = addedDesc + Gem_Blue.UPGRADED_DESCRIPTION; break;
                                case CRIMSON: addedDesc = addedDesc + Gem_Crimson.UPGRADED_DESCRIPTION; break;
                                case FRAGMENTED: addedDesc = addedDesc + Gem_Fragmented.UPGRADED_DESCRIPTION; break;
                                case PURPLE: addedDesc = addedDesc + Gem_Purple.UPGRADED_DESCRIPTION; break;
                                case SYNTHETIC: addedDesc = addedDesc + Gem_Synthetic.UPGRADED_DESCRIPTION; break;
                                case YELLOW: addedDesc = addedDesc + Gem_Yellow.UPGRADED_DESCRIPTION; break;
                                case LIGHTBLUE: addedDesc = addedDesc + Gem_Lightblue.UPGRADED_DESCRIPTION; break;
                            }
                        } else {
                            addedDesc = addedDesc + "Unknown Gem: " + gem.getClass().getSimpleName();
                        }

                        if (!after) {
                            addedDesc = addedDesc + " NL ";
                        }
                    } else {
                        if (after) {
                            addedDesc = addedDesc + " NL ";
                        }
                        addedDesc = addedDesc + CardCrawlGame.languagePack.getCharacterString("Guardian").TEXT[2];
                        if (!after) {
                            addedDesc = addedDesc + " NL ";
                        }
                    }
                }

                // Add soulbound text at the beginning for cards that gained soulbound from Black Diamond
                String finalDesc = desc;
                if (hasBlackDiamond && !isInherentlySoulbound && !desc.contains("Soulbound")) {
                    // Add "Soulbound. NL" at the very beginning of the description
                    finalDesc = "Soulbound. NL " + desc;
                }

                if (after) {
                    return SpireReturn.Return(finalDesc + addedDesc);
                } else {
                    return SpireReturn.Return(addedDesc + finalDesc);
                }
            } finally {
                processingCards.remove(__instance);
            }
        }
    }

    @SpirePatch(clz = AbstractGuardianCard.class, method = "render")
    public static class CompleteRenderOverride {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(AbstractGuardianCard __instance, SpriteBatch sb) {
            // Check if this card has any Black Diamond sockets
            boolean hasBlackDiamonds = false;
            for (Object socket : __instance.sockets) {
                if (socket instanceof HarshCurses.BlackDiamondType) {
                    hasBlackDiamonds = true;
                    break;
                }
            }

            if (!hasBlackDiamonds) {
                // No Black Diamonds, let the original method handle it
                return SpireReturn.Continue();
            }

            // This card has Black Diamonds - call base rendering with reflection
            try {
                // Call AbstractCard.renderCard()
                Method renderCardMethod = AbstractCard.class.getDeclaredMethod("renderCard", SpriteBatch.class, boolean.class, boolean.class);
                renderCardMethod.setAccessible(true);
                renderCardMethod.invoke(__instance, sb, __instance.hb.hovered, __instance.hb.clickStarted);

                // Render hitbox
                __instance.hb.render(sb);

            } catch (Exception e) {
                System.err.println("Failed to render base card: " + e.getMessage());
                // If base rendering fails, at least try to render sockets
            }

            // Now the socket rendering part - copied from original but with safe casting
            if (__instance.socketCount > 0 && !__instance.isFlipped) {
                Texture socketTexture = null;

                for (int i = 0; i < __instance.socketCount; ++i) {
                    socketTexture = null; // Reset each iteration

                    if (__instance.sockets.size() > i) {
                        Object socketObj = __instance.sockets.get(i);

                        // Safe casting - handle both our enum and Guardian's enum
                        GuardianMod.socketTypes socketType;
                        if (socketObj instanceof GuardianMod.socketTypes) {
                            socketType = (GuardianMod.socketTypes) socketObj;
                        } else if (socketObj instanceof HarshCurses.BlackDiamondType) {
                            // For rendering purposes, treat Black Diamond like SYNTHETIC
                            socketType = GuardianMod.socketTypes.SYNTHETIC;
                        } else {
                            // Fallback
                            socketType = GuardianMod.socketTypes.RED;
                        }

                        // Original switch logic from AbstractGuardianCard.render()
                        switch (socketType) {
                            case RED:
                                socketTexture = getSocketTexture(i, 1);
                                break;
                            case GREEN:
                                socketTexture = getSocketTexture(i, 2);
                                break;
                            case ORANGE:
                                socketTexture = getSocketTexture(i, 3);
                                break;
                            case WHITE:
                                socketTexture = getSocketTexture(i, 4);
                                break;
                            case CYAN:
                                socketTexture = getSocketTexture(i, 5);
                                break;
                            case BLUE:
                                socketTexture = getSocketTexture(i, 6);
                                break;
                            case CRIMSON:
                                socketTexture = getSocketTexture(i, 7);
                                break;
                            case FRAGMENTED:
                                socketTexture = getSocketTexture(i, 8);
                                break;
                            case PURPLE:
                                socketTexture = getSocketTexture(i, 9);
                                break;
                            case SYNTHETIC:
                                socketTexture = getSocketTexture(i, 10);
                                break;
                            case YELLOW:
                                socketTexture = getSocketTexture(i, 11);
                                break;
                            case LIGHTBLUE:
                                socketTexture = getSocketTexture(i, 12);
                                break;
                        }
                    } else {
                        // Empty socket
                        socketTexture = getSocketTexture(i, 0);
                    }

                    if (socketTexture != null) {
                        renderSocket(__instance, sb, socketTexture, i);
                    }
                }
            }

            return SpireReturn.Return(); // Skip original method
        }

        private static Texture getSocketTexture(int socketIndex, int textureIndex) {
            if (socketIndex == 0) {
                return GuardianMod.socketTextures.get(textureIndex);
            } else if (socketIndex == 1) {
                return GuardianMod.socketTextures2.get(textureIndex);
            } else if (socketIndex == 2) {
                return GuardianMod.socketTextures3.get(textureIndex);
            } else {
                return GuardianMod.socketTextures4.get(textureIndex);
            }
        }

        private static void renderSocket(AbstractGuardianCard card, SpriteBatch sb, Texture texture, int socketIndex) {
            try {
                Method renderSocketMethod = AbstractGuardianCard.class.getDeclaredMethod("renderSocket", SpriteBatch.class, Texture.class, Integer.class);
                renderSocketMethod.setAccessible(true);
                renderSocketMethod.invoke(card, sb, texture, socketIndex);
            } catch (Exception e) {
                // Fallback: manual render
                float drawX = card.current_x - 256.0F;
                float drawY = card.current_y - 256.0F;
                sb.draw(texture, drawX, drawY, 256.0F, 256.0F, 512.0F, 512.0F,
                        card.drawScale * Settings.scale, card.drawScale * Settings.scale,
                        card.angle, 0, 0, 512, 512, false, false);
            }
        }
    }

    @SpirePatch(clz = AbstractGuardianCard.class, method = "addGemToSocket", paramtypez = {AbstractGuardianCard.class, boolean.class})
    public static class AddGemToSocketPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(AbstractGuardianCard __instance, AbstractGuardianCard gem, boolean removeFromDeck) {
            // Only handle Black Diamond gems
            if (!(gem instanceof BlackDiamond)) {
                return SpireReturn.Continue(); // Let original method handle other gems
            }

            // Handle adding Black Diamond gems to sockets
            if (removeFromDeck) {
                AbstractDungeon.player.masterDeck.removeCard(gem);
            }

            // Find the next available socket slot
            int targetSocketIndex = -1;
            for (int i = 0; i < __instance.socketCount; i++) {
                if (__instance.sockets.size() <= i || __instance.sockets.get(i) == null) {
                    targetSocketIndex = i;
                    break;
                }
            }

            if (targetSocketIndex == -1) {
                // No available sockets - shouldn't happen but let's be safe
                System.err.println("No available socket slots!");
                return SpireReturn.Return();
            }

            // Ensure the sockets list is big enough
            while (__instance.sockets.size() <= targetSocketIndex) {
                __instance.sockets.add(null);
            }

            // Set the Black Diamond at the correct index instead of adding to end
            try {
                Field socketsField = AbstractGuardianCard.class.getField("sockets");
                ArrayList sockets = (ArrayList) socketsField.get(__instance);
                sockets.set(targetSocketIndex, HarshCurses.BlackDiamondType.BLACK);

                System.out.println("Successfully added Black Diamond to socket " + targetSocketIndex);
            } catch (Exception e) {
                System.err.println("Failed to add Black Diamond to socket: " + e.getMessage());
            }

            __instance.updateDescription();
            __instance.saveGemMisc();

            // Update soulbound status after adding Black Diamond
            updateSoulboundTag(__instance);

            return SpireReturn.Return(); // Skip original method
        }

        private static void updateSoulboundTag(AbstractGuardianCard card) {
            boolean hasBlackDiamond = false;

            // Check if any socket contains a Black Diamond
            for (Object socket : card.sockets) {
                if (socket instanceof HarshCurses.BlackDiamondType) {
                    hasBlackDiamond = true;
                    break;
                }
            }

            // Add Soulbound tag if Black Diamond is present
            if (hasBlackDiamond) {
                SoulboundField.soulbound.set(card, true);
                System.out.println("Added Soulbound tag to: " + card.name);
            }
        }
    }


    @SpirePatch(clz = AbstractGuardianCard.class, method = "updateDescription")
    public static class UpdateSoulboundTagPatch {
        @SpirePostfixPatch
        public static void Postfix(AbstractGuardianCard __instance) {
            updateSoulboundTag(__instance);
        }

        private static void updateSoulboundTag(AbstractGuardianCard card) {
            boolean hasBlackDiamond = false;

            // Check if any socket contains a Black Diamond
            for (Object socket : card.sockets) {
                if (socket instanceof HarshCurses.BlackDiamondType) {
                    hasBlackDiamond = true;
                    break;
                }
            }

            // Add or remove Soulbound tag based on Black Diamond presence
            if (hasBlackDiamond) {
                if (!SoulboundField.soulbound.get(card)) {
                    SoulboundField.soulbound.set(card, true);
                    System.out.println("Added Soulbound tag to: " + card.name);
                }
            } else {
                // Only remove if the card isn't inherently soulbound
                // (Check if it's not a BlackDiamond card itself)
                if (!(card instanceof BlackDiamond) && SoulboundField.soulbound.get(card)) {
                    SoulboundField.soulbound.set(card, false);
                    System.out.println("Removed Soulbound tag from: " + card.name);
                }
            }
        }
    }

    // Also patch the loadGemMisc method to ensure soulbound status is updated after loading
    @SpirePatch(clz = AbstractGuardianCard.class, method = "loadGemMisc")
    public static class LoadGemMiscSoulboundPatch {
        @SpirePostfixPatch
        public static void Postfix(AbstractGuardianCard __instance) {
            // Update soulbound status after loading gems from save data
            updateSoulboundTagAfterLoad(__instance);
        }

        private static void updateSoulboundTagAfterLoad(AbstractGuardianCard card) {
            boolean hasBlackDiamond = false;

            // Check if any socket contains a Black Diamond
            for (Object socket : card.sockets) {
                if (socket instanceof HarshCurses.BlackDiamondType) {
                    hasBlackDiamond = true;
                    break;
                }
            }

            // Set soulbound status based on Black Diamond presence
            if (hasBlackDiamond) {
                SoulboundField.soulbound.set(card, true);
            } else if (!(card instanceof BlackDiamond)) {
                // Only remove soulbound if it's not a BlackDiamond card itself
                SoulboundField.soulbound.set(card, false);
            }
        }
    }

    @SpirePatch(clz = GuardianMod.class, method = "getGemCards")
    public static class GetGemCardsPatch {
        @SpirePostfixPatch
        public static CardGroup Postfix(CardGroup result) {
            // Check if player has Black Diamond gems in their deck
            for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
                if (c instanceof BlackDiamond && !result.group.contains(c)) {
                    result.group.add(c);
                }
            }
            return result;
        }
    }

    @SpirePatch(clz = GuardianMod.class, method = "initializeSocketTextures")
    public static class InitializeSocketTexturesPatch {
        @SpirePostfixPatch
        public static void Postfix(GuardianMod __instance) {
            // No need to add new textures - we'll reuse existing SYNTHETIC gem textures
        }
    }
}