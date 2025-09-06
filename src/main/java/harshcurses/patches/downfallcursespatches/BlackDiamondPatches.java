package harshcurses.patches.downfallcursespatches;

import basemod.BaseMod;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import downfall.actions.ForceWaitAction;
import harshcurses.HarshCurses;
import harshcurses.actions.IncreaseDebuffsAction;
import harshcurses.cards.BlackDiamond;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BlackDiamondPatches {
    // Helper method to add objects to the sockets list bypassing type checking
    private static void addToSockets(Object card, Object socketType) {
        try {
            Class<?> guardianCardClass = Class.forName("guardian.cards.AbstractGuardianCard");
            Field socketsField = guardianCardClass.getField("sockets");
            ArrayList sockets = (ArrayList) socketsField.get(card);
            sockets.add(socketType);
        } catch (Exception e) {
            // Silently fail if GuardianMod isn't loaded
        }
    }

    // Patch the sockets field to accept Object type instead of GuardianMod.socketTypes
    @SpirePatch(
            optional = true,
            cls = "guardian.cards.AbstractGuardianCard",
            method = SpirePatch.CLASS
    )
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

    @SpirePatch(
            optional = true,
            cls = "guardian.cards.AbstractGuardianCard",
            method = "saveGemMisc"
    )
    public static class SaveGemMiscPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(Object __instance) {
            try {
                if (AbstractDungeon.player != null && AbstractDungeon.player.masterDeck.contains((AbstractCard) __instance)) {
                    Class<?> guardianCardClass = Class.forName("guardian.cards.AbstractGuardianCard");
                    Field socketCountField = guardianCardClass.getDeclaredField("socketCount");
                    Field socketsField = guardianCardClass.getField("sockets");

                    int socketCount = (int) socketCountField.get(__instance);
                    ArrayList sockets = (ArrayList) socketsField.get(__instance);

                    ((AbstractCard) __instance).misc = 10 + socketCount;

                    if (sockets.size() > 0) {
                        for (int i = 0; i < sockets.size(); ++i) {
                            ((AbstractCard) __instance).misc *= 100;
                            int gemindex = 0;
                            Object socketType = sockets.get(i);

                            if (socketType instanceof HarshCurses.BlackDiamondType) {
                                gemindex = 12; // Assign index 12 for BLACK (22 - 10 = 12)
                            } else if (socketType != null) {
                                // Handle original GuardianMod socket types
                                String enumName = socketType.toString();
                                switch (enumName) {
                                    case "RED": gemindex = 0; break;
                                    case "GREEN": gemindex = 1; break;
                                    case "ORANGE": gemindex = 2; break;
                                    case "WHITE": gemindex = 3; break;
                                    case "CYAN": gemindex = 4; break;
                                    case "BLUE": gemindex = 5; break;
                                    case "CRIMSON": gemindex = 6; break;
                                    case "FRAGMENTED": gemindex = 7; break;
                                    case "PURPLE": gemindex = 8; break;
                                    case "SYNTHETIC": gemindex = 9; break;
                                    case "YELLOW": gemindex = 10; break;
                                    case "LIGHTBLUE": gemindex = 11; break;
                                    default: gemindex = 0;
                                }
                            }

                            ((AbstractCard) __instance).misc += 10 + gemindex;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return SpireReturn.Continue(); // Let original method handle it if we fail
            }
            return SpireReturn.Return();
        }
    }

    @SpirePatch(
            optional = true,
            cls = "guardian.cards.AbstractGuardianCard",
            method = "loadGemMisc"
    )
    public static class LoadGemMiscPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(Object __instance) {
            try {
                Class<?> guardianCardClass = Class.forName("guardian.cards.AbstractGuardianCard");
                Field socketCountField = guardianCardClass.getDeclaredField("socketCount");
                Field socketsField = guardianCardClass.getField("sockets");

                ArrayList sockets = (ArrayList) socketsField.get(__instance);
                sockets.clear();

                int misc = ((AbstractCard) __instance).misc;

                if (misc > 0 && Integer.toString(misc).length() % 2 == 0) {
                    String miscString = Integer.toString(misc);
                    String socketCountString = miscString.substring(0, 2);
                    int loadedSocketCount = Integer.parseInt(socketCountString) - 10;
                    socketCountField.set(__instance, loadedSocketCount);

                    if (miscString.length() <= 2) {
                        Method updateDescMethod = guardianCardClass.getDeclaredMethod("updateDescription");
                        updateDescMethod.invoke(__instance);
                        return SpireReturn.Return();
                    }

                    miscString = miscString.substring(2);
                    int loops = miscString.length() / 2;

                    Class<?> socketTypesClass = Class.forName("guardian.GuardianMod$socketTypes");

                    for (int i = 0; i < loops; ++i) {
                        String gemCode = miscString.substring(0, 2);
                        Object socketToAdd = null;

                        switch (gemCode) {
                            case "10": socketToAdd = getSocketType(socketTypesClass, "RED"); break;
                            case "11": socketToAdd = getSocketType(socketTypesClass, "GREEN"); break;
                            case "12": socketToAdd = getSocketType(socketTypesClass, "ORANGE"); break;
                            case "13": socketToAdd = getSocketType(socketTypesClass, "WHITE"); break;
                            case "14": socketToAdd = getSocketType(socketTypesClass, "CYAN"); break;
                            case "15": socketToAdd = getSocketType(socketTypesClass, "BLUE"); break;
                            case "16": socketToAdd = getSocketType(socketTypesClass, "CRIMSON"); break;
                            case "17": socketToAdd = getSocketType(socketTypesClass, "FRAGMENTED"); break;
                            case "18": socketToAdd = getSocketType(socketTypesClass, "PURPLE"); break;
                            case "19": socketToAdd = getSocketType(socketTypesClass, "SYNTHETIC"); break;
                            case "20": socketToAdd = getSocketType(socketTypesClass, "YELLOW"); break;
                            case "21": socketToAdd = getSocketType(socketTypesClass, "LIGHTBLUE"); break;
                            case "22":
                                socketToAdd = HarshCurses.BlackDiamondType.BLACK;
                                break;
                            default: socketToAdd = getSocketType(socketTypesClass, "RED");
                        }

                        if (socketToAdd != null) {
                            sockets.add(socketToAdd);
                        } else {
                        }
                        miscString = miscString.substring(2);
                    }

                    Method updateDescMethod = guardianCardClass.getDeclaredMethod("updateDescription");
                    updateDescMethod.invoke(__instance);
                } else {
                }
            } catch (Exception e) {
                e.printStackTrace();
                return SpireReturn.Continue(); // Let original method handle it if we fail
            }
            return SpireReturn.Return();
        }

        private static Object getSocketType(Class<?> socketTypesClass, String typeName) {
            try {
                return Enum.valueOf((Class<Enum>) socketTypesClass, typeName);
            } catch (Exception e) {
                return null;
            }
        }
    }

    @SpirePatch(
            optional = true,
            cls = "guardian.cards.AbstractGuardianCard",
            method = "loadGemMisc"
    )
    public static class LoadGemMiscSoulboundPatch {
        @SpirePostfixPatch
        public static void Postfix(Object __instance) {
            updateSoulboundTagAfterLoad(__instance);
        }

        private static void updateSoulboundTagAfterLoad(Object card) {
            try {
                Class<?> guardianCardClass = Class.forName("guardian.cards.AbstractGuardianCard");
                Field socketsField = guardianCardClass.getField("sockets");
                ArrayList sockets = (ArrayList) socketsField.get(card);
                boolean hasBlackDiamond = false;
                for (Object socket : sockets) {
                    if (socket instanceof HarshCurses.BlackDiamondType) {
                        hasBlackDiamond = true;
                        break;
                    }
                }
                if (hasBlackDiamond) {
                    SoulboundField.soulbound.set(card, true);
                } else if (!(card instanceof BlackDiamond)) {
                    SoulboundField.soulbound.set(card, false);
                }
            } catch (Exception e) {
            }
        }
    }

    @SpirePatch(
            optional = true,
            cls = "guardian.cards.AbstractGuardianCard",
            method = "useGems",
            paramtypes = {"com.megacrit.cardcrawl.characters.AbstractPlayer", "com.megacrit.cardcrawl.monsters.AbstractMonster"}
    )
    public static class UseGemsPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(Object __instance, Object p, Object m) {
            try {
                Class<?> guardianCardClass = Class.forName("guardian.cards.AbstractGuardianCard");
                Field socketsField = guardianCardClass.getField("sockets");
                ArrayList sockets = (ArrayList) socketsField.get(__instance);
                for (Object gem : sockets) {
                    if (gem instanceof HarshCurses.BlackDiamondType) {
                        BlackDiamond.gemEffect((AbstractPlayer) p, (AbstractMonster) m);
                    } else {
                        // Handle Guardian gem types using reflection
                        try {
                            String gemType = gem.toString();
                            Class<?> gemClass = getGemClass(gemType);
                            if (gemClass != null) {
                                Method gemEffectMethod = gemClass.getDeclaredMethod("gemEffect",
                                        AbstractPlayer.class, AbstractMonster.class);
                                gemEffectMethod.invoke(null, p, m);
                            }
                        } catch (Exception e) {
                            // Silently fail for unknown gem types
                        }
                    }
                }
            } catch (Exception e) {
                // Silently fail if GuardianMod isn't loaded or structure changed
            }
            return SpireReturn.Return();
        }

        private static Class<?> getGemClass(String gemType) {
            try {
                String className = "guardian.cards.";
                switch (gemType) {
                    case "RED": return Class.forName(className + "Gem_Red");
                    case "GREEN": return Class.forName(className + "Gem_Green");
                    case "ORANGE": return Class.forName(className + "Gem_Orange");
                    case "WHITE": return Class.forName(className + "Gem_White");
                    case "CYAN": return Class.forName(className + "Gem_Cyan");
                    case "BLUE": return Class.forName(className + "Gem_Blue");
                    case "CRIMSON": return Class.forName(className + "Gem_Crimson");
                    case "FRAGMENTED": return Class.forName(className + "Gem_Fragmented");
                    case "PURPLE": return Class.forName(className + "Gem_Purple");
                    case "SYNTHETIC": return Class.forName(className + "Gem_Synthetic");
                    case "YELLOW": return Class.forName(className + "Gem_Yellow");
                    case "LIGHTBLUE": return Class.forName(className + "Gem_Lightblue");
                    default: return null;
                }
            } catch (Exception e) {
                return null;
            }
        }
    }

    @SpirePatch(
            optional = true,
            cls = "guardian.cards.TwinSlam",
            method = "use",
            paramtypes = {"com.megacrit.cardcrawl.characters.AbstractPlayer", "com.megacrit.cardcrawl.monsters.AbstractMonster"}
    )
    public static class TwinSlamUsePatch {
        private static boolean isProcessing = false;

        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(Object __instance, Object p, Object m) {
            if (isProcessing) {
                return SpireReturn.Continue();
            }
            isProcessing = true;
            try {
                // Deal damage
                DamageInfo damageInfo = new DamageInfo((AbstractCreature) p,
                        ((AbstractCard) __instance).damage,
                        ((AbstractCard) __instance).damageTypeForTurn);
                AbstractDungeon.actionManager.addToBottom(new DamageAction((AbstractCreature) m, damageInfo, AbstractGameAction.AttackEffect.SLASH_HEAVY));

                // Create SecondStrike card
                Class<?> secondStrikeClass = Class.forName("guardian.cards.SecondStrike");
                Object q = secondStrikeClass.getConstructor().newInstance();
                if (((AbstractCard) __instance).upgraded) {
                    Method upgradeMethod = q.getClass().getDeclaredMethod("upgrade");
                    upgradeMethod.invoke(q);
                }

                // Copy sockets
                copySocketsToCard(__instance, q);

                // Add card to hand
                AbstractDungeon.actionManager.addToBottom(new MakeTempCardInHandAction((AbstractCard) q, true));

                // Use gems
                useGemsCustom(__instance, p, m);
                return SpireReturn.Return();
            } catch (Exception e) {
                // Silently fail if GuardianMod isn't loaded or structure changed
                return SpireReturn.Continue();
            } finally {
                isProcessing = false;
            }
        }

        private static void copySocketsToCard(Object source, Object target) {
            try {
                Class<?> guardianCardClass = Class.forName("guardian.cards.AbstractGuardianCard");
                Field socketsField = guardianCardClass.getField("sockets");
                Field socketCountField = guardianCardClass.getDeclaredField("socketCount");
                ArrayList sourceSockets = (ArrayList) socketsField.get(source);
                ArrayList targetSockets = (ArrayList) socketsField.get(target);
                socketCountField.set(target, 0);
                targetSockets.clear();
                for (Object socketObj : sourceSockets) {
                    targetSockets.add(socketObj);
                    int currentCount = (int) socketCountField.get(target);
                    socketCountField.set(target, currentCount + 1);
                }
            } catch (Exception e) {
                // Silently fail
            }
        }

        private static void useGemsCustom(Object card, Object p, Object m) {
            try {
                Class<?> guardianCardClass = Class.forName("guardian.cards.AbstractGuardianCard");
                Field socketsField = guardianCardClass.getField("sockets");
                ArrayList sockets = (ArrayList) socketsField.get(card);
                for (Object gem : sockets) {
                    if (gem instanceof HarshCurses.BlackDiamondType) {
                        BlackDiamond.gemEffect((AbstractPlayer) p, (AbstractMonster) m);
                    } else {
                        // Handle Guardian gem types
                        try {
                            String gemType = gem.toString();
                            Class<?> gemClass = getGemClassForUse(gemType);
                            if (gemClass != null) {
                                Method gemEffectMethod = gemClass.getDeclaredMethod("gemEffect",
                                        AbstractPlayer.class, AbstractMonster.class);
                                gemEffectMethod.invoke(null, p, m);
                            }
                        } catch (Exception e) {
                            // Silently fail for unknown gem types
                        }
                    }
                }
            } catch (Exception e) {
                // Silently fail
            }
        }

        private static Class<?> getGemClassForUse(String gemType) {
            try {
                String className = "guardian.cards.";
                switch (gemType) {
                    case "RED": return Class.forName(className + "Gem_Red");
                    case "GREEN": return Class.forName(className + "Gem_Green");
                    case "ORANGE": return Class.forName(className + "Gem_Orange");
                    case "WHITE": return Class.forName(className + "Gem_White");
                    case "CYAN": return Class.forName(className + "Gem_Cyan");
                    case "BLUE": return Class.forName(className + "Gem_Blue");
                    case "CRIMSON": return Class.forName(className + "Gem_Crimson");
                    case "FRAGMENTED": return Class.forName(className + "Gem_Fragmented");
                    case "PURPLE": return Class.forName(className + "Gem_Purple");
                    case "SYNTHETIC": return Class.forName(className + "Gem_Synthetic");
                    case "YELLOW": return Class.forName(className + "Gem_Yellow");
                    case "LIGHTBLUE": return Class.forName(className + "Gem_Lightblue");
                    default: return null;
                }
            } catch (Exception e) {
                return null;
            }
        }
    }

    @SpirePatch(
            optional = true,
            cls = "com.megacrit.cardcrawl.cards.AbstractCard",
            method = "triggerWhenDrawn"
    )
    public static class TriggerWhenDrawnPatch {
        @SpirePostfixPatch
        public static void Postfix(AbstractCard __instance) {
            try {
                Class<?> guardianCardClass = Class.forName("guardian.cards.AbstractGuardianCard");
                if (guardianCardClass.isInstance(__instance)) {
                    Field socketsField = guardianCardClass.getField("sockets");
                    ArrayList sockets = (ArrayList) socketsField.get(__instance);
                    for (Object gem : sockets) {
                        if (gem instanceof HarshCurses.BlackDiamondType) {
                            AbstractDungeon.actionManager.addToBottom(new IncreaseDebuffsAction(AbstractDungeon.player, 1));
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                // Silently fail if GuardianMod isn't loaded or structure changed
            }
        }
    }


    @SpirePatch(
            optional = true,
            cls = "guardian.cards.AbstractGuardianCard",
            method = "updateGemDescription",
            paramtypes = {"java.lang.String", "java.lang.Boolean"}
    )
    public static class UpdateGemDescriptionPatch {
        private static final Set<Object> processingCards = new HashSet<>();

        @SpirePrefixPatch
        public static SpireReturn<String> Prefix(Object __instance, String desc, Boolean after) {
            if (processingCards.contains(__instance)) {
                return SpireReturn.Continue();
            }

            processingCards.add(__instance);
            try {
                String addedDesc = "";

                Class<?> guardianCardClass = Class.forName("guardian.cards.AbstractGuardianCard");
                Field socketCountField = guardianCardClass.getDeclaredField("socketCount");
                Field socketsField = guardianCardClass.getField("sockets");

                int socketCount = (int) socketCountField.get(__instance);
                ArrayList sockets = (ArrayList) socketsField.get(__instance);

                // Check for Black Diamond and soulbound status
                boolean hasBlackDiamond = false;
                boolean isInherentlySoulbound = (__instance instanceof BlackDiamond);

                for (Object socket : sockets) {
                    if (socket instanceof HarshCurses.BlackDiamondType) {
                        hasBlackDiamond = true;
                        break;
                    }
                }

                for (int i = 0; i < socketCount; ++i) {
                    if (sockets.size() > i) {
                        Object gem = sockets.get(i);
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
                        } else {
                            // Handle Guardian gem descriptions using reflection
                            String gemDesc = getGemDescription(gem);
                            addedDesc = addedDesc + gemDesc;
                        }

                        if (!after) {
                            addedDesc = addedDesc + " NL ";
                        }
                    } else {
                        if (after) {
                            addedDesc = addedDesc + " NL ";
                        }

                        // Get empty socket text
                        try {
                            Class<?> cardCrawlGameClass = Class.forName("com.megacrit.cardcrawl.core.CardCrawlGame");
                            Field languagePackField = cardCrawlGameClass.getDeclaredField("languagePack");
                            Object languagePack = languagePackField.get(null);

                            Method getCharacterStringMethod = languagePack.getClass().getDeclaredMethod("getCharacterString", String.class);
                            Object characterString = getCharacterStringMethod.invoke(languagePack, "Guardian");

                            Field textField = characterString.getClass().getDeclaredField("TEXT");
                            String[] textArray = (String[]) textField.get(characterString);

                            addedDesc = addedDesc + textArray[2];
                        } catch (Exception e) {
                            addedDesc = addedDesc + "Empty Socket";
                        }

                        if (!after) {
                            addedDesc = addedDesc + " NL ";
                        }
                    }
                }

                // Add soulbound text
                String finalDesc = desc;
                if (hasBlackDiamond && !isInherentlySoulbound && !desc.contains("Soulbound")) {
                    finalDesc = "Soulbound. NL " + desc;
                }

                if (after) {
                    return SpireReturn.Return(finalDesc + addedDesc);
                } else {
                    return SpireReturn.Return(addedDesc + finalDesc);
                }
            } catch (Exception e) {
                // Silently fail if GuardianMod isn't loaded or structure changed
                return SpireReturn.Continue();
            } finally {
                processingCards.remove(__instance);
            }
        }

        private static String getGemDescription(Object gem) {
            try {
                String gemType = gem.toString();
                String className = "guardian.cards.";
                Class<?> gemClass = null;

                switch (gemType) {
                    case "RED": gemClass = Class.forName(className + "Gem_Red"); break;
                    case "GREEN": gemClass = Class.forName(className + "Gem_Green"); break;
                    case "ORANGE": gemClass = Class.forName(className + "Gem_Orange"); break;
                    case "WHITE": gemClass = Class.forName(className + "Gem_White"); break;
                    case "CYAN": gemClass = Class.forName(className + "Gem_Cyan"); break;
                    case "BLUE": gemClass = Class.forName(className + "Gem_Blue"); break;
                    case "CRIMSON": gemClass = Class.forName(className + "Gem_Crimson"); break;
                    case "FRAGMENTED": gemClass = Class.forName(className + "Gem_Fragmented"); break;
                    case "PURPLE": gemClass = Class.forName(className + "Gem_Purple"); break;
                    case "SYNTHETIC": gemClass = Class.forName(className + "Gem_Synthetic"); break;
                    case "YELLOW": gemClass = Class.forName(className + "Gem_Yellow"); break;
                    case "LIGHTBLUE": gemClass = Class.forName(className + "Gem_Lightblue"); break;
                }

                if (gemClass != null) {
                    Field upgradedDescField = gemClass.getDeclaredField("UPGRADED_DESCRIPTION");
                    return (String) upgradedDescField.get(null);
                }
            } catch (Exception e) {
                // Silently fail
            }
            return "Unknown Gem";
        }
    }

    @SpirePatch(
            optional = true,
            cls = "guardian.cards.AbstractGuardianCard",
            method = "addGemToSocket",
            paramtypes = {"guardian.cards.AbstractGuardianCard", "boolean"}
    )
    public static class AddGemToSocketPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(Object __instance, Object gem, boolean removeFromDeck) {
            if (!(gem instanceof BlackDiamond)) {
                return SpireReturn.Continue();
            }
            try {
                if (removeFromDeck) {
                    AbstractDungeon.player.masterDeck.removeCard((AbstractCard) gem);
                }
                Class<?> guardianCardClass = Class.forName("guardian.cards.AbstractGuardianCard");
                Field socketCountField = guardianCardClass.getDeclaredField("socketCount");
                Field socketsField = guardianCardClass.getField("sockets");
                int socketCount = (int) socketCountField.get(__instance);
                ArrayList sockets = (ArrayList) socketsField.get(__instance);

                // Find available socket slot
                int targetSocketIndex = -1;
                for (int i = 0; i < socketCount; i++) {
                    if (sockets.size() <= i || sockets.get(i) == null) {
                        targetSocketIndex = i;
                        break;
                    }
                }
                if (targetSocketIndex == -1) {
                    return SpireReturn.Return();
                }

                // Ensure sockets list is big enough
                while (sockets.size() <= targetSocketIndex) {
                    sockets.add(null);
                }

                // Set the Black Diamond at the correct index
                sockets.set(targetSocketIndex, HarshCurses.BlackDiamondType.BLACK);
                Method updateDescMethod = guardianCardClass.getDeclaredMethod("updateDescription");
                updateDescMethod.invoke(__instance);
                Method saveGemMiscMethod = guardianCardClass.getDeclaredMethod("saveGemMisc");
                saveGemMiscMethod.invoke(__instance);

                // Update soulbound status after adding Black Diamond
                updateSoulboundTag(__instance);
                return SpireReturn.Return();
            } catch (Exception e) {
                // Silently fail if GuardianMod isn't loaded or structure changed
            }
            return SpireReturn.Return();
        }

        private static void updateSoulboundTag(Object card) {
            try {
                Class<?> guardianCardClass = Class.forName("guardian.cards.AbstractGuardianCard");
                Field socketsField = guardianCardClass.getField("sockets");
                ArrayList sockets = (ArrayList) socketsField.get(card);
                boolean hasBlackDiamond = false;
                for (Object socket : sockets) {
                    if (socket instanceof HarshCurses.BlackDiamondType) {
                        hasBlackDiamond = true;
                        break;
                    }
                }
                if (hasBlackDiamond) {
                    SoulboundField.soulbound.set(card, true);
                }
            } catch (Exception e) {
                // Silently fail
            }
        }
    }


    @SpirePatch(
            optional = true,
            cls = "guardian.cards.AbstractGuardianCard",
            method = "updateDescription"
    )
    public static class UpdateSoulboundTagPatch {
        @SpirePostfixPatch
        public static void Postfix(Object __instance) {
            updateSoulboundTag(__instance);
        }

        private static void updateSoulboundTag(Object card) {
            try {
                Class<?> guardianCardClass = Class.forName("guardian.cards.AbstractGuardianCard");
                Field socketsField = guardianCardClass.getField("sockets");
                ArrayList sockets = (ArrayList) socketsField.get(card);

                boolean hasBlackDiamond = false;
                for (Object socket : sockets) {
                    if (socket instanceof HarshCurses.BlackDiamondType) {
                        hasBlackDiamond = true;
                        break;
                    }
                }

                if (hasBlackDiamond) {
                    if (!SoulboundField.soulbound.get(card)) {
                        SoulboundField.soulbound.set(card, true);
                    }
                } else {
                    // Only remove if the card isn't inherently soulbound
                    if (!(card instanceof BlackDiamond) && SoulboundField.soulbound.get(card)) {
                        SoulboundField.soulbound.set(card, false);
                    }
                }
            } catch (Exception e) {
                // Silently fail if GuardianMod isn't loaded or structure changed
            }
        }
    }

    @SpirePatch(
            optional = true,
            cls = "guardian.GuardianMod",
            method = "getGemCards"
    )
    public static class GetGemCardsPatch {
        @SpirePostfixPatch
        public static Object Postfix(Object result) {
            try {
                if (AbstractDungeon.player != null) {
                    Field resultGroupField = result.getClass().getDeclaredField("group");
                    ArrayList resultGroup = (ArrayList) resultGroupField.get(result);
                    for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
                        if (c instanceof BlackDiamond && !resultGroup.contains(c)) {
                            resultGroup.add(c);
                        }
                    }
                }
            } catch (Exception e) {
                // Silently fail if GuardianMod isn't loaded or structure changed
            }
            return result;
        }
    }

    @SpirePatch(
            optional = true,
            cls = "guardian.cards.AbstractGuardianCard",
            method = "render",
            paramtypes = {"com.badlogic.gdx.graphics.g2d.SpriteBatch"}
    )
    public static class CompleteRenderOverride {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(Object __instance, Object sb) {
            try {
                Class<?> guardianCardClass = Class.forName("guardian.cards.AbstractGuardianCard");
                Field socketsField = guardianCardClass.getField("sockets");
                ArrayList sockets = (ArrayList) socketsField.get(__instance);

                // Check if this card has any Black Diamond sockets
                boolean hasBlackDiamonds = false;
                for (Object socket : sockets) {
                    if (socket instanceof HarshCurses.BlackDiamondType) {
                        hasBlackDiamonds = true;
                        break;
                    }
                }
                if (!hasBlackDiamonds) {
                    return SpireReturn.Continue();
                }

                // Call base rendering with reflection
                Method renderCardMethod = AbstractCard.class.getDeclaredMethod("renderCard",
                        SpriteBatch.class, boolean.class, boolean.class);
                renderCardMethod.setAccessible(true);
                renderCardMethod.invoke(__instance, sb,
                        ((AbstractCard) __instance).hb.hovered,
                        ((AbstractCard) __instance).hb.clickStarted);

                // Render hitbox
                ((AbstractCard) __instance).hb.render((SpriteBatch) sb);

                // Render sockets
                Field socketCountField = guardianCardClass.getDeclaredField("socketCount");
                int socketCount = (int) socketCountField.get(__instance);
                if (socketCount > 0 && !((AbstractCard) __instance).isFlipped) {
                    renderSocketsWithBlackDiamond(__instance, sb, socketCount, sockets);
                }
                return SpireReturn.Return();
            } catch (Exception e) {
                // Silently fail if GuardianMod isn't loaded or structure changed
                return SpireReturn.Continue();
            }
        }

        private static void renderSocketsWithBlackDiamond(Object card, Object sb, int socketCount, ArrayList sockets) {
            try {
                Class<?> guardianModClass = Class.forName("guardian.GuardianMod");
                Field socketTexturesField = guardianModClass.getDeclaredField("socketTextures");
                Field socketTextures2Field = guardianModClass.getDeclaredField("socketTextures2");
                Field socketTextures3Field = guardianModClass.getDeclaredField("socketTextures3");
                Field socketTextures4Field = guardianModClass.getDeclaredField("socketTextures4");
                ArrayList socketTextures = (ArrayList) socketTexturesField.get(null);
                ArrayList socketTextures2 = (ArrayList) socketTextures2Field.get(null);
                ArrayList socketTextures3 = (ArrayList) socketTextures3Field.get(null);
                ArrayList socketTextures4 = (ArrayList) socketTextures4Field.get(null);

                for (int i = 0; i < socketCount; ++i) {
                    Object socketTexture = null;
                    if (sockets.size() > i && sockets.get(i) != null) {
                        Object socketObj = sockets.get(i);
                        int textureIndex;
                        if (socketObj instanceof HarshCurses.BlackDiamondType) {
                            textureIndex = 10; // Use SYNTHETIC texture for Black Diamond
                        } else {
                            String socketType = socketObj.toString();
                            switch (socketType) {
                                case "RED":
                                    textureIndex = 1;
                                    break;
                                case "GREEN":
                                    textureIndex = 2;
                                    break;
                                case "ORANGE":
                                    textureIndex = 3;
                                    break;
                                case "WHITE":
                                    textureIndex = 4;
                                    break;
                                case "CYAN":
                                    textureIndex = 5;
                                    break;
                                case "BLUE":
                                    textureIndex = 6;
                                    break;
                                case "CRIMSON":
                                    textureIndex = 7;
                                    break;
                                case "FRAGMENTED":
                                    textureIndex = 8;
                                    break;
                                case "PURPLE":
                                    textureIndex = 9;
                                    break;
                                case "SYNTHETIC":
                                    textureIndex = 10;
                                    break;
                                case "YELLOW":
                                    textureIndex = 11;
                                    break;
                                case "LIGHTBLUE":
                                    textureIndex = 12;
                                    break;
                                default:
                                    textureIndex = 1;
                            }
                        }
                        socketTexture = getSocketTexture(i, textureIndex, socketTextures, socketTextures2, socketTextures3, socketTextures4);
                    } else {
                        // Empty socket
                        socketTexture = getSocketTexture(i, 0, socketTextures, socketTextures2, socketTextures3, socketTextures4);
                    }
                    if (socketTexture != null) {
                        renderSocket(card, sb, socketTexture, i);
                    }
                }
            } catch (Exception e) {
                // Silently fail
            }
        }

        private static Object getSocketTexture(int socketIndex, int textureIndex, ArrayList textures1, ArrayList textures2, ArrayList textures3, ArrayList textures4) {
            try {
                if (socketIndex == 0) {
                    return textures1.get(textureIndex);
                } else if (socketIndex == 1) {
                    return textures2.get(textureIndex);
                } else if (socketIndex == 2) {
                    return textures3.get(textureIndex);
                } else {
                    return textures4.get(textureIndex);
                }
            } catch (Exception e) {
                return null;
            }
        }

        private static void renderSocket(Object card, Object sb, Object texture, int socketIndex) {
            try {
                Class<?> guardianCardClass = Class.forName("guardian.cards.AbstractGuardianCard");
                Method renderSocketMethod = guardianCardClass.getDeclaredMethod("renderSocket",
                        SpriteBatch.class, Texture.class, Integer.class);
                renderSocketMethod.setAccessible(true);
                renderSocketMethod.invoke(card, sb, texture, socketIndex);
            } catch (Exception e) {
                // Fallback: manual render if reflection fails
                try {
                    AbstractCard cardInstance = (AbstractCard) card;
                    float drawX = cardInstance.current_x - 256.0F;
                    float drawY = cardInstance.current_y - 256.0F;
                    ((SpriteBatch) sb).draw((Texture) texture, drawX, drawY, 256.0F, 256.0F, 512.0F, 512.0F,
                            cardInstance.drawScale * Settings.scale, cardInstance.drawScale * Settings.scale,
                            cardInstance.angle, 0, 0, 512, 512, false, false);
                } catch (Exception ex) {
                    // Silently fail
                }
            }
        }
    }

    @SpirePatch(
            optional = true,
            cls = "guardian.cards.GemFire",
            method = "use",
            paramtypes = {"com.megacrit.cardcrawl.characters.AbstractPlayer", "com.megacrit.cardcrawl.monsters.AbstractMonster"}
    )
    public static class gemFireUsePatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(Object __instance, Object p, Object m) {
            try {
                // Collect all socket objects (mixed types)
                ArrayList<Object> tempSockets = new ArrayList<>();
                collectSocketsFromGroupSafe(__instance, ((AbstractPlayer)p).hand, tempSockets);
                collectSocketsFromGroupSafe(__instance, ((AbstractPlayer)p).drawPile, tempSockets);
                collectSocketsFromGroupSafe(__instance, ((AbstractPlayer)p).discardPile, tempSockets);
                collectSocketsFromStasisSafe(__instance, p, tempSockets);

                // Create visual effects for each socket
                for (int i = 0; i < tempSockets.size(); i++) {
                    Object socket = tempSockets.get(i);
                    // Create the visual effect - we'll use reflection to call the original effect
                    createGemShootEffect(socket, i, m, tempSockets.size());
                }

                if (tempSockets.size() > 0) {
                    AbstractDungeon.actionManager.addToBottom(new ForceWaitAction(1.25F + 0.05F * (float)tempSockets.size()));
                }

                // Deal damage
                DamageInfo damageInfo = new DamageInfo((AbstractCreature)p,
                        ((AbstractCard)__instance).damage,
                        ((AbstractCard)__instance).damageTypeForTurn);
                AbstractDungeon.actionManager.addToBottom(new DamageAction((AbstractCreature)m, damageInfo, AbstractGameAction.AttackEffect.FIRE));

                // Separate synthetic sockets (including Black Diamond) from others
                ArrayList<Object> nonSyntheticSockets = new ArrayList<>();
                ArrayList<Object> syntheticSockets = new ArrayList<>();

                for (Object socket : tempSockets) {
                    if (isSyntheticType(socket)) {
                        syntheticSockets.add(socket);
                    } else {
                        nonSyntheticSockets.add(socket);
                    }
                }

                // Process non-synthetic first, then synthetic
                nonSyntheticSockets.addAll(syntheticSockets);

                for (Object socket : nonSyntheticSockets) {
                    processSocketSafe(__instance, p, m, socket);
                    AbstractDungeon.actionManager.addToBottom(new ForceWaitAction(0.02F));
                }

                return SpireReturn.Return();
            } catch (Exception e) {
                // Silently fail and let original method handle it
                return SpireReturn.Continue();
            }
        }

        private static void collectSocketsFromGroupSafe(Object gemFireInstance, CardGroup group, ArrayList<Object> tempSockets) {
            try {
                Class<?> guardianCardClass = Class.forName("guardian.cards.AbstractGuardianCard");
                Field socketsField = guardianCardClass.getField("sockets");

                for (AbstractCard card : group.group) {
                    if (guardianCardClass.isInstance(card)) {
                        ArrayList sockets = (ArrayList) socketsField.get(card);
                        for (Object socket : sockets) {
                            if (socket != null) {
                                tempSockets.add(socket);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Silently fail
            }
        }

        private static void collectSocketsFromStasisSafe(Object gemFireInstance, Object player, ArrayList<Object> tempSockets) {
            try {
                // Try to call the original collectSocketsFromStasis method via reflection
                Class<?> gemFireClass = Class.forName("guardian.cards.GemFire");
                Method collectStasisMethod = gemFireClass.getDeclaredMethod("collectSocketsFromStasis",
                        AbstractPlayer.class, ArrayList.class);
                collectStasisMethod.setAccessible(true);

                // Create a temporary ArrayList for the original method
                ArrayList<Object> originalSockets = new ArrayList<>();
                collectStasisMethod.invoke(gemFireInstance, player, originalSockets);
                tempSockets.addAll(originalSockets);
            } catch (Exception e) {
                // If reflection fails, try manual stasis collection
                try {
                    Class<?> stasisManagerClass = Class.forName("guardian.stances.StasisManager");
                    Field instanceField = stasisManagerClass.getDeclaredField("instance");
                    Object stasisManager = instanceField.get(null);
                    Field stasisCardsField = stasisManagerClass.getDeclaredField("stasisCards");
                    ArrayList stasisCards = (ArrayList) stasisCardsField.get(stasisManager);

                    Class<?> guardianCardClass = Class.forName("guardian.cards.AbstractGuardianCard");
                    Field socketsField = guardianCardClass.getField("sockets");

                    for (Object card : stasisCards) {
                        if (guardianCardClass.isInstance(card)) {
                            ArrayList sockets = (ArrayList) socketsField.get(card);
                            for (Object socket : sockets) {
                                if (socket != null) {
                                    tempSockets.add(socket);
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    // Silently fail
                }
            }
        }

        private static void createGemShootEffect(Object socket, int index, Object monster, int totalSockets) {
            try {
                // Try to create the original GemShootEffect
                Class<?> gemFireActionClass = Class.forName("guardian.actions.GemFireAction");
                Class<?> gemShootEffectClass = null;

                // Find the inner GemShootEffect class
                for (Class<?> innerClass : gemFireActionClass.getDeclaredClasses()) {
                    if (innerClass.getSimpleName().equals("GemShootEffect")) {
                        gemShootEffectClass = innerClass;
                        break;
                    }
                }

                if (gemShootEffectClass != null) {
                    Constructor<?> constructor = gemShootEffectClass.getDeclaredConstructor(
                            Object.class, Integer.TYPE, AbstractCreature.class, Integer.TYPE);
                    constructor.setAccessible(true);
                    Object effect = constructor.newInstance(socket, index, monster, totalSockets);
                    AbstractDungeon.actionManager.addToBottom(new VFXAction((AbstractGameEffect) effect, 0.0F));
                }
            } catch (Exception e) {
                // Silently fail - visual effect just won't show
            }
        }

        private static boolean isSyntheticType(Object socket) {
            if (socket instanceof HarshCurses.BlackDiamondType) {
                return true; // Treat Black Diamond as synthetic for processing order
            }

            try {
                Class<?> socketTypesClass = Class.forName("guardian.GuardianMod$socketTypes");
                if (socketTypesClass.isInstance(socket)) {
                    return socket.toString().equals("SYNTHETIC");
                }
            } catch (Exception e) {
                // Silently fail
            }
            return false;
        }

        private static void processSocketSafe(Object gemFireInstance, Object player, Object monster, Object socket) {
            try {
                if (socket instanceof HarshCurses.BlackDiamondType) {
                    // Handle Black Diamond
                    BlackDiamond.gemEffect((AbstractPlayer) player, (AbstractMonster) monster);
                } else {
                    // Try to call the original processSocket method
                    Class<?> gemFireClass = Class.forName("guardian.cards.gemFire");
                    Method processSocketMethod = gemFireClass.getDeclaredMethod("processSocket",
                            AbstractPlayer.class, AbstractMonster.class, Object.class);
                    processSocketMethod.setAccessible(true);
                    processSocketMethod.invoke(gemFireInstance, player, monster, socket);
                }
            } catch (Exception e) {
                // Try alternative approach - call gem effect directly
                try {
                    String gemType = socket.toString();
                    Class<?> gemClass = getGemClassForEffect(gemType);
                    if (gemClass != null) {
                        Method gemEffectMethod = gemClass.getDeclaredMethod("gemEffect",
                                AbstractPlayer.class, AbstractMonster.class);
                        gemEffectMethod.invoke(null, player, monster);
                    }
                } catch (Exception ex) {
                    // Silently fail
                }
            }
        }

        private static Class<?> getGemClassForEffect(String gemType) {
            try {
                String className = "guardian.cards.";
                switch (gemType) {
                    case "RED": return Class.forName(className + "Gem_Red");
                    case "GREEN": return Class.forName(className + "Gem_Green");
                    case "ORANGE": return Class.forName(className + "Gem_Orange");
                    case "WHITE": return Class.forName(className + "Gem_White");
                    case "CYAN": return Class.forName(className + "Gem_Cyan");
                    case "BLUE": return Class.forName(className + "Gem_Blue");
                    case "CRIMSON": return Class.forName(className + "Gem_Crimson");
                    case "FRAGMENTED": return Class.forName(className + "Gem_Fragmented");
                    case "PURPLE": return Class.forName(className + "Gem_Purple");
                    case "SYNTHETIC": return Class.forName(className + "Gem_Synthetic");
                    case "YELLOW": return Class.forName(className + "Gem_Yellow");
                    case "LIGHTBLUE": return Class.forName(className + "Gem_Lightblue");
                    default: return null;
                }
            } catch (Exception e) {
                return null;
            }
        }
    }

}