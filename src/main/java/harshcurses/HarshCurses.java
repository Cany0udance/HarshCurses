package harshcurses;

import basemod.AutoAdd;
import basemod.BaseMod;
import basemod.interfaces.*;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import gremlin.characters.GremlinCharacter;
import gremlin.patches.GremlinMobState;
import harshcurses.cards.BaseCard;
import harshcurses.util.GeneralUtils;
import harshcurses.util.KeywordInfo;
import harshcurses.util.TextureLoader;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.Patcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.localization.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.clapper.util.classutil.ClassFilter;
import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;
import org.scannotation.AnnotationDB;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SpireInitializer
public class HarshCurses implements
        EditStringsSubscriber,
        EditKeywordsSubscriber,
        EditCardsSubscriber,
        PostBattleSubscriber,
        PostInitializeSubscriber {
    public static ModInfo info;
    public static String modID; //Edit your pom.xml to change this

    static {
        loadModInfo();
    }

    private static final String resourcesFolder = checkResourcesPath();
    public static final Logger logger = LogManager.getLogger(modID); //Used to output to the console.

    //This is used to prefix the IDs of various objects like cards and relics,
    //to avoid conflicts between different mods using the same name for things.
    public static String makeID(String id) {
        return modID + ":" + id;
    }

    //This will be called by ModTheSpire because of the @SpireInitializer annotation at the top of the class.
    public static void initialize() {
        new HarshCurses();
    }

    public HarshCurses() {
        BaseMod.subscribe(this); //This will make BaseMod trigger all the subscribers at their appropriate times.
        logger.info(modID + " subscribed to BaseMod.");
    }

    @Override
    public void receivePostInitialize() {
        //This loads the image used as an icon in the in-game mods menu.
        Texture badgeTexture = TextureLoader.getTexture(imagePath("badge.png"));
        //Set up the mod information displayed in the in-game mods menu.
        //The information used is taken from your pom.xml file.

        //If you want to set up a config panel, that will be done here.
        //The Mod Badges page has a basic example of this, but setting up config is overall a bit complex.
        BaseMod.registerModBadge(badgeTexture, info.Name, GeneralUtils.arrToString(info.Authors), info.Description, null);
    }

    /*----------Localization----------*/

    //This is used to load the appropriate localization files based on language.
    private static String getLangString() {
        return Settings.language.name().toLowerCase();
    }

    private static final String defaultLanguage = "eng";
    public static String bathroomBreakGremlin = null;
    public static int bathroomBreakGremlinHP = 0;
    public static final Map<String, KeywordInfo> keywords = new HashMap<>();

    @Override
    public void receiveEditStrings() {
        /*
            First, load the default localization.
            Then, if the current language is different, attempt to load localization for that language.
            This results in the default localization being used for anything that might be missing.
            The same process is used to load keywords slightly below.
        */
        loadLocalization(defaultLanguage); //no exception catching for default localization; you better have at least one that works.
        if (!defaultLanguage.equals(getLangString())) {
            try {
                loadLocalization(getLangString());
            } catch (GdxRuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadLocalization(String lang) {
        //While this does load every type of localization, most of these files are just outlines so that you can see how they're formatted.
        //Feel free to comment out/delete any that you don't end up using.
        BaseMod.loadCustomStringsFile(CardStrings.class,
                localizationPath(lang, "CardStrings.json"));
        BaseMod.loadCustomStringsFile(CharacterStrings.class,
                localizationPath(lang, "CharacterStrings.json"));
        BaseMod.loadCustomStringsFile(EventStrings.class,
                localizationPath(lang, "EventStrings.json"));
        BaseMod.loadCustomStringsFile(OrbStrings.class,
                localizationPath(lang, "OrbStrings.json"));
        BaseMod.loadCustomStringsFile(PotionStrings.class,
                localizationPath(lang, "PotionStrings.json"));
        BaseMod.loadCustomStringsFile(PowerStrings.class,
                localizationPath(lang, "PowerStrings.json"));
        BaseMod.loadCustomStringsFile(RelicStrings.class,
                localizationPath(lang, "RelicStrings.json"));
        BaseMod.loadCustomStringsFile(StanceStrings.class,
                localizationPath(lang, "StanceStrings.json"));
        BaseMod.loadCustomStringsFile(UIStrings.class,
                localizationPath(lang, "UIStrings.json"));
    }

    @Override
    public void receiveEditKeywords() {
        Gson gson = new Gson();
        String json = Gdx.files.internal(localizationPath(defaultLanguage, "Keywords.json")).readString(String.valueOf(StandardCharsets.UTF_8));
        KeywordInfo[] keywords = gson.fromJson(json, KeywordInfo[].class);
        for (KeywordInfo keyword : keywords) {
            keyword.prep();
            registerKeyword(keyword);
        }

        if (!defaultLanguage.equals(getLangString())) {
            try {
                json = Gdx.files.internal(localizationPath(getLangString(), "Keywords.json")).readString(String.valueOf(StandardCharsets.UTF_8));
                keywords = gson.fromJson(json, KeywordInfo[].class);
                for (KeywordInfo keyword : keywords) {
                    keyword.prep();
                    registerKeyword(keyword);
                }
            } catch (Exception e) {
                logger.warn(modID + " does not support " + getLangString() + " keywords.");
            }
        }
    }

    private void registerKeyword(KeywordInfo info) {
        BaseMod.addKeyword(modID.toLowerCase(), info.PROPER_NAME, info.NAMES, info.DESCRIPTION);
        if (!info.ID.isEmpty()) {
            keywords.put(info.ID, info);
        }
    }

    //These methods are used to generate the correct filepaths to various parts of the resources folder.
    public static String localizationPath(String lang, String file) {
        return resourcesFolder + "/localization/" + lang + "/" + file;
    }

    public static String imagePath(String file) {
        return resourcesFolder + "/images/" + file;
    }

    public static String characterPath(String file) {
        return resourcesFolder + "/images/character/" + file;
    }

    public static String powerPath(String file) {
        return resourcesFolder + "/images/powers/" + file;
    }

    public static String relicPath(String file) {
        return resourcesFolder + "/images/relics/" + file;
    }

    /**
     * Checks the expected resources path based on the package name.
     */
    private static String checkResourcesPath() {
        String name = HarshCurses.class.getName(); //getPackage can be iffy with patching, so class name is used instead.
        int separator = name.indexOf('.');
        if (separator > 0)
            name = name.substring(0, separator);

        FileHandle resources = new LwjglFileHandle(name, Files.FileType.Internal);
        if (resources.child("images").exists() && resources.child("localization").exists()) {
            return name;
        }

        throw new RuntimeException("\n\tFailed to find resources folder; expected it to be named \"" + name + "\"." +
                " Either make sure the folder under resources has the same name as your mod's package, or change the line\n" +
                "\t\"private static final String resourcesFolder = checkResourcesPath();\"\n" +
                "\tat the top of the " + HarshCurses.class.getSimpleName() + " java file.");
    }

    /**
     * This determines the mod's ID based on information stored by ModTheSpire.
     */
    private static void loadModInfo() {
        Optional<ModInfo> infos = Arrays.stream(Loader.MODINFOS).filter((modInfo) -> {
            AnnotationDB annotationDB = Patcher.annotationDBMap.get(modInfo.jarURL);
            if (annotationDB == null)
                return false;
            Set<String> initializers = annotationDB.getAnnotationIndex().getOrDefault(SpireInitializer.class.getName(), Collections.emptySet());
            return initializers.contains(HarshCurses.class.getName());
        }).findFirst();
        if (infos.isPresent()) {
            info = infos.get();
            modID = info.ID;
        } else {
            throw new RuntimeException("Failed to determine mod info/ID based on initializer.");
        }
    }

    @Override
    public void receiveEditCards() {
        new AutoAdd(modID)
                .packageFilter(BaseCard.class)
                .filter(new ClassFilter() {
                    @Override
                    public boolean accept(ClassInfo classInfo, ClassFinder classFinder) {
                        String className = classInfo.getClassName();

                        // Define conditional loading requirements
                        if (className.endsWith("DeadOff")) {
                            return Loader.isModLoaded("Hermit") || Loader.isModLoaded("downfall");
                        }

                        if (className.endsWith("SplitCrap")) {
                            return Loader.isModLoaded("downfall");
                        }

                        if (className.endsWith("BlackDiamond")) {
                            return Loader.isModLoaded("downfall");
                        }

                        if (className.endsWith("FaultyGyroscope")) {
                            return Loader.isModLoaded("downfall");
                        }

                        if (className.endsWith("Misinput")) {
                            return Loader.isModLoaded("downfall");
                        }

                        if (className.endsWith("CoconutJPG")) {
                            return Loader.isModLoaded("downfall");
                        }

                        if (className.endsWith("LichsSoul")) {
                            return Loader.isModLoaded("downfall");
                        }

                        if (className.endsWith("BathroomBreak")) {
                            return Loader.isModLoaded("downfall");
                        }

                        if (className.endsWith("Eyepatch")) {
                            return Loader.isModLoaded("downfall");
                        }

                        // Accept all other cards
                        return true;
                    }
                })
                .setDefaultSeen(true)
                .cards();
    }

    @Override
    public void receivePostBattle(AbstractRoom abstractRoom) {
        if (Loader.isModLoaded("downfall")) {
            if (HarshCurses.bathroomBreakGremlin != null && AbstractDungeon.player instanceof GremlinCharacter) {
                GremlinCharacter player = (GremlinCharacter) AbstractDungeon.player;

                try {
                    // Use reflection to access the private enslaved field
                    Field enslavedField = GremlinMobState.class.getDeclaredField("enslaved");
                    enslavedField.setAccessible(true);
                    ArrayList<String> enslaved = (ArrayList<String>) enslavedField.get(player.mobState);

                    // Remove the bathroom break gremlin from enslaved list
                    enslaved.remove(HarshCurses.bathroomBreakGremlin);

                    // Find the bathroom break gremlin in the mob state and restore their original HP
                    for (int i = 0; i < player.mobState.gremlins.size(); i++) {
                        if (player.mobState.gremlins.get(i).equals(HarshCurses.bathroomBreakGremlin)) {
                            // Restore them to their original HP when they left
                            player.mobState.gremlinHP.set(i, HarshCurses.bathroomBreakGremlinHP);
                            break;
                        }
                    }

                } catch (Exception e) {
                    // Log error if needed
                }

                // Clear the tracking
                HarshCurses.bathroomBreakGremlin = null;
                HarshCurses.bathroomBreakGremlinHP = 0;
            }
        }
    }
}
