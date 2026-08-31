package gdx.assets;

import java.util.HashMap;
import java.util.Map;

/**
 * تمام مسیرهای فایل‌های گرافیکی (Assets) در این کلاس نگه‌داری می‌شوند.
 * منابع گرافیکی این پروژه از دامپ رسمی اسپرایت‌های Plants vs. Zombies 2 استخراج شده‌اند.
 */
public final class AssetPaths {

    private AssetPaths() {
    }

    private static String atlas(String atlasName, String regionName) {
        return "atlas:" + atlasName + ":" + regionName;
    }

    // ==================== فونت‌ها ====================
    public static final String FONT_DEFAULT = "";
    public static final String FONT_TITLE = "";
    public static final String UI_SKIN_JSON = "";
    public static final String UI_SKIN_ATLAS = "";

    // ==================== پس‌زمینه‌ها ====================
    public static final String BG_REGISTER = atlas("DELAYLOAD_BACKGROUND_FRONTLAWN", "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE");
    public static final String BG_LOGIN = atlas("DELAYLOAD_BACKGROUND_FRONTLAWN", "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE");
    public static final String BG_MAIN_MENU = "backgrounds/mainmenu_bg.png";
    public static final String BG_SETTINGS = atlas("DELAYLOAD_BACKGROUND_FRONTLAWN", "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE");
    public static final String BG_NEWS = atlas("DELAYLOAD_BACKGROUND_FRONTLAWN", "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE");
    public static final String BG_PROFILE = atlas("DELAYLOAD_BACKGROUND_FRONTLAWN", "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE");
    public static final String BG_SHOP = atlas("DELAYLOAD_BACKGROUND_FRONTLAWN", "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE");
    public static final String BG_QUEST = atlas("DELAYLOAD_BACKGROUND_FRONTLAWN", "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE");
    public static final String BG_LEADERBOARD = atlas("DELAYLOAD_BACKGROUND_FRONTLAWN", "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE");
    public static final String BG_GREENHOUSE = atlas("DELAYLOAD_BACKGROUND_ZEN", "IMAGE_BACKGROUNDS_ZEN_GARDEN");
    public static final String BG_PLANT_SELECTION = atlas("DELAYLOAD_BACKGROUND_FRONTLAWN", "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE");
    public static final String BG_CHAPTER_SELECT = atlas("DELAYLOAD_BACKGROUND_FRONTLAWN", "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE");
    public static final String BG_LEVEL_SELECT = atlas("DELAYLOAD_BACKGROUND_FRONTLAWN", "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE");
    public static final String BG_COLLECTION = atlas("DELAYLOAD_BACKGROUND_FRONTLAWN", "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE");

    public static final String LOGO_PVZ2 = "";
    public static final String CARD_BACKGROUND = "ui/card_bg.png";

    // ==================== آیکون‌های نوار بالا (HUD) ====================
    public static final String ICON_COIN = atlas("UI_ALWAYSLOADED", "IMAGE_UI_COINS_STACK_1");
    public static final String ICON_DIAMOND = atlas("UI_ALWAYSLOADED", "IMAGE_UI_GEMS_STACK_3");
    public static final String ICON_STAR = "";
    public static final String ICON_LOCK = "";
    public static final String ICON_NEWS_BELL = atlas("UI_MAINMENU", "IMAGE_UI_MAINMENU_MM_NEWSICON");
    public static final String ICON_SETTINGS_GEAR = "";
    public static final String ICON_BACK_ARROW = atlas("UI_MAINMENU", "IMAGE_UI_MAINMENU_BACK_BTN_NORMAL");
    public static final String ICON_LOGOUT = "";
    public static final String ICON_SUN = atlas("UI_ALWAYSLOADED", "IMAGE_UI_SUNFLOWER");
    public static final String ICON_PLANT_FOOD = atlas("UI_ALWAYSLOADED", "IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON");

    // ==================== صحنه‌ی اصلی گیم‌پلی ====================
    public static final String BG_LAWN_NORMAL = atlas("DELAYLOAD_BACKGROUND_FRONTLAWN", "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE");
    public static final String BG_LAWN_ANCIENT_EGYPT = atlas("DELAYLOAD_BACKGROUND_EGYPT_COMPRESSED", "IMAGE_BACKGROUNDS_EGYPT_TEXTURE");
    public static final String BG_LAWN_FROSTBITE_CAVES = atlas("DELAYLOAD_BACKGROUND_ICEAGE_COMPRESSED", "IMAGE_BACKGROUNDS_ICEAGE_TEXTURE");
    public static final String BG_LAWN_BIG_WAVE_BEACH = atlas("DELAYLOAD_BACKGROUND_BEACH_COMPRESSED", "IMAGE_BACKGROUNDS_BEACH_TEXTURE");
    public static final String BG_LAWN_DARK_AGES = atlas("DELAYLOAD_BACKGROUND_DARK_COMPRESSED", "IMAGE_BACKGROUNDS_DARK_TEXTURE");

    public static final String SUN_NORMAL = atlas("UI_ALWAYSLOADED", "IMAGE_UI_SUNFLOWER");
    public static final String SUN_SPECIAL = atlas("UI_ALWAYSLOADED", "IMAGE_UI_SUNFLOWER");
    public static final String SUN_RADIOACTIVE = atlas("UI_ALWAYSLOADED", "IMAGE_UI_SUNFLOWER");

    public static final String DROP_COIN = atlas("UI_ALWAYSLOADED", "IMAGE_UI_COINS_STACK_1");
    public static final String DROP_GREENHOUSE_POT = "";
    public static final String DROP_DIAMOND = atlas("UI_ALWAYSLOADED", "IMAGE_UI_GEMS_STACK_3");

    public static final String GRAVE_TYPE_1 = atlas("TOMBSTONE_DARK_BASE", "IMAGE_GRAVESTONES_DARK_NOOP_DARK_NOOP_132X160");
    public static final String GRAVE_TYPE_2 = atlas("TOMBSTONE_DARK_BASE", "IMAGE_GRAVESTONES_DARK_NOOP_DARK_NOOP_132X160_2");
    public static final String GRAVE_TYPE_3 = atlas("TOMBSTONE_DARK_BASE", "IMAGE_GRAVESTONES_DARK_NOOP_DARK_NOOP_132X156");
    public static final String NECROMANCY_TILE_MARKER = "";

    public static final String LAWN_MOWER_IDLE_NORMAL = atlas("FRONTLAWNMOWERGROUP", "IMAGE_MOWERS_MOWER_TUTORIAL_MOWER_TUTORIAL_114X67");
    public static final String LAWN_MOWER_USED_NORMAL = atlas("FRONTLAWNMOWERGROUP", "IMAGE_MOWERS_MOWER_TUTORIAL_MOWER_TUTORIAL_67X52");
    public static final String LAWN_MOWER_IDLE_EGYPT = atlas("EGYPTMOWERGROUP", "IMAGE_MOWERS_MOWER_EGYPT_MOWER_EGYPT_76X120");
    public static final String LAWN_MOWER_USED_EGYPT = atlas("EGYPTMOWERGROUP", "IMAGE_MOWERS_MOWER_EGYPT_MOWER_EGYPT_57X90_2");
    public static final String LAWN_MOWER_IDLE_ICEAGE = atlas("ICEAGEMOWERGROUP", "IMAGE_MOWERS_MOWER_ICEAGE_MOWER_ICEAGE_147X160");
    public static final String LAWN_MOWER_USED_ICEAGE = atlas("ICEAGEMOWERGROUP", "IMAGE_MOWERS_MOWER_ICEAGE_MOWER_ICEAGE_70X67");
    public static final String LAWN_MOWER_IDLE_BEACH = atlas("BEACHMOWERGROUP", "IMAGE_MOWERS_MOWER_BEACH_MOWER_BEACH_166X175");
    public static final String LAWN_MOWER_USED_BEACH = atlas("BEACHMOWERGROUP", "IMAGE_MOWERS_MOWER_BEACH_MOWER_BEACH_206X83");
    public static final String LAWN_MOWER_IDLE_DARK = atlas("DARKMOWERGROUP", "IMAGE_MOWERS_MOWER_DARK_MOWER_DARK_130X103");
    public static final String LAWN_MOWER_USED_DARK = atlas("DARKMOWERGROUP", "IMAGE_MOWERS_MOWER_DARK_MOWER_DARK_101X53");

    public static final String LAWN_MOWER_IDLE = LAWN_MOWER_IDLE_NORMAL;
    public static final String LAWN_MOWER_USED = LAWN_MOWER_USED_NORMAL;

    public static final String ICON_PAUSE_BUTTON = "";
    public static final String ICON_FAST_FORWARD = "";
    public static final String ICON_PLANT_FOOD_LEAF = atlas("UI_ALWAYSLOADED", "IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON");
    public static final String ICON_SHOVEL = atlas("UI_ALWAYSLOADED", "IMAGE_UI_HUD_INGAME_SHOVEL_ICON");
    public static final String CONVEYOR_BELT_BG = "";

    public static final String ZOMBIE_PROGRESS_BAR_BG = "";
    public static final String ZOMBIE_PROGRESS_BAR_FILL = "";
    public static final String ZOMBIE_PROGRESS_BAR_ICON = "";
    public static final String PLANT_FOOD_COUNTER_BG = atlas("UI_ALWAYSLOADED", "IMAGE_UI_HUD_INGAME_PLANTFOOD_BANK");

    public static final String PROJECTILE_NORMAL = "";
    public static final String PROJECTILE_FIRE = "";
    public static final String PROJECTILE_ICE = "";

    // ==================== دکمه‌ها ====================
    public static final String BTN_DEFAULT_UP = "";
    public static final String BTN_DEFAULT_DOWN = "";
    public static final String BTN_PLAY_UP = "";
    public static final String BTN_PLAY_DOWN = "";
    public static final String BTN_CLOSE = "";

    // ==================== گیاهان ====================
    private static final Map<String, String> PLANT_ATLAS = new HashMap<>();
    private static final Map<String, String> PLANT_REGION = new HashMap<>();
    private static final Map<String, String> PLANT_SEED_REGION = new HashMap<>();
    static {
        PLANT_ATLAS.put("appeasemint", "PLANTAPPEASEMINT");
        PLANT_REGION.put("appeasemint", "IMAGE_EMPOWERMINTS_PLANT_APPEASEMINT_APPEASEMINT_153X145");
        PLANT_SEED_REGION.put("appeasemint", "IMAGE_UI_PACKETS_APPEASEMINT");
        PLANT_ATLAS.put("armamint", "PLANTARMAMINT");
        PLANT_REGION.put("armamint", "IMAGE_EMPOWERMINTS_PLANT_ARMAMINT_EXPLOSION_ARMAMINT_EXPLOSION_194X181");
        PLANT_SEED_REGION.put("armamint", "IMAGE_UI_PACKETS_ARMAMINT");
        PLANT_ATLAS.put("bombardmint", "PLANTBOMBARDMINT");
        PLANT_REGION.put("bombardmint", "IMAGE_EMPOWERMINTS_PLANT_BOMBARDMINT_BOMBARDMINT_430X405");
        PLANT_SEED_REGION.put("bombardmint", "IMAGE_UI_PACKETS_BOMBARDMINT");
        PLANT_ATLAS.put("bonkchoy", "PLANTBONKCHOY");
        PLANT_REGION.put("bonkchoy", "IMAGE_PLANT_BONKCHOY_BONKCHOY_255X73");
        PLANT_SEED_REGION.put("bonkchoy", "IMAGE_UI_PACKETS_BONKCHOY");
        PLANT_ATLAS.put("bowlingbulb", "PLANTBOWLINGBULB");
        PLANT_REGION.put("bowlingbulb", "IMAGE_EFFECTS_BOWLINGBULB_PLANTFOOD_PROJECTILE_BOWLINGBULB_PLANTFOOD_PROJECTILE_199X188");
        PLANT_SEED_REGION.put("bowlingbulb", "IMAGE_UI_PACKETS_BOWLINGBULB");
        PLANT_ATLAS.put("cabbagepult", "PLANTCABBAGEPULT");
        PLANT_REGION.put("cabbagepult", "IMAGE_EFFECTS_T_CABBAGEPULT_PROJECTILE_T_CABBAGEPULT_PROJECTILE_215X218");
        PLANT_SEED_REGION.put("cabbagepult", "IMAGE_UI_PACKETS_CABBAGEPULT");
        PLANT_ATLAS.put("cactus", "PLANTCACTUS");
        PLANT_REGION.put("cactus", "IMAGE_PLANT_CACTUS_CACTUS_333X300");
        PLANT_SEED_REGION.put("cactus", "IMAGE_UI_PACKETS_CACTUS");
        PLANT_ATLAS.put("caulipower", "PLANTCAULIPOWER");
        PLANT_REGION.put("caulipower", "IMAGE_PLANT_CAULIPOWER_CAULIPOWER_183X94");
        PLANT_SEED_REGION.put("caulipower", "IMAGE_UI_PACKETS_CAULIPOWER");
        PLANT_ATLAS.put("cherrybomb", "PLANTCHERRYBOMB");
        PLANT_REGION.put("cherrybomb", "IMAGE_EFFECTS_CHERRYBOMB_EXPLOSION_TOP_CHERRYBOMB_EXPLOSION_TOP_236X223");
        PLANT_SEED_REGION.put("cherrybomb", "IMAGE_UI_PACKETS_CHERRY_BOMB");
        PLANT_ATLAS.put("chomper", "PLANTCHOMPER");
        PLANT_REGION.put("chomper", "IMAGE_PLANT_CHOMPER_CHOMPER_146X232");
        PLANT_SEED_REGION.put("chomper", "IMAGE_UI_PACKETS_CHOMPER");
        PLANT_ATLAS.put("citron", "PLANTCITRON");
        PLANT_REGION.put("citron", "IMAGE_EFFECTS_CITRON_PLANTFOOD_LIGHTNING_CHARGE_CITRON_PLANTFOOD_LIGHTNING_CHARGE_1416X184");
        PLANT_SEED_REGION.put("citron", "IMAGE_UI_PACKETS_CITRON");
        PLANT_ATLAS.put("doomshroom", "PLANTDOOMSHROOM");
        PLANT_REGION.put("doomshroom", "IMAGE_PLANT_DOOMSHROOM_DOOMSHROOM_722X389");
        PLANT_SEED_REGION.put("doomshroom", "IMAGE_UI_PACKETS_DOOMSHROOM");
        PLANT_ATLAS.put("electricblueberry", "PLANTELECTRICBLUEBERRY");
        PLANT_REGION.put("electricblueberry", "IMAGE_EFFECTS_ELECTRICBLUEBERRY_CLOUD_PROJECTILE_ELECTRICBLUEBERRY_CLOUD_PROJECTILE_67X155");
        PLANT_SEED_REGION.put("electricblueberry", "IMAGE_UI_PACKETS_ELECTRICBLUEBERRY");
        PLANT_ATLAS.put("enchantmint", "PLANTENCHANTMINT");
        PLANT_REGION.put("enchantmint", "IMAGE_EMPOWERMINTS_PLANT_ENCHANTMINT_ENCHANTMINT_1356X216");
        PLANT_SEED_REGION.put("enchantmint", "IMAGE_UI_PACKETS_ENCHANTMINT");
        PLANT_ATLAS.put("endurian", "PLANTENDURIAN");
        PLANT_REGION.put("endurian", "IMAGE_PLANT_ENDURIAN_ENDURIAN_232X243");
        PLANT_SEED_REGION.put("endurian", "IMAGE_UI_PACKETS_ENDURIAN");
        PLANT_ATLAS.put("enforcemint", "PLANTENFORCEMINT");
        PLANT_REGION.put("enforcemint", "IMAGE_EMPOWERMINTS_PLANT_ENFORCEMINT_ENFORCEMINT_89X327");
        PLANT_SEED_REGION.put("enforcemint", "IMAGE_UI_PACKETS_ENFORCEMINT");
        PLANT_ATLAS.put("enlightenmint", "PLANTENLIGHTENMINT");
        PLANT_REGION.put("enlightenmint", "IMAGE_EMPOWERMINTS_PLANT_ENLIGHTENMINT_ENLIGHTENMINT_123X609");
        PLANT_SEED_REGION.put("enlightenmint", "IMAGE_UI_PACKETS_ENLIGHTENMINT");
        PLANT_ATLAS.put("explodeonut", "PLANTEXPLODEONUT");
        PLANT_REGION.put("explodeonut", "IMAGE_PLANT_EXPLODEONUT_EXPLODEONUT_179X212");
        PLANT_SEED_REGION.put("explodeonut", "IMAGE_UI_PACKETS_EXPLODEONUT");
        PLANT_ATLAS.put("firepeashooter", "PLANTFIREPEASHOOTER");
        PLANT_REGION.put("firepeashooter", "IMAGE_PLANT_FIREPEASHOOTER_FIREPEASHOOTER_113X125");
        PLANT_SEED_REGION.put("firepeashooter", "IMAGE_UI_PACKETS_FIREPEASHOOTER");
        PLANT_ATLAS.put("fumeshroom", "PLANTFUMESHROOM");
        PLANT_REGION.put("fumeshroom", "IMAGE_PLANT_FUMESHROOM_FUMESHROOM_116X89");
        PLANT_SEED_REGION.put("fumeshroom", "IMAGE_UI_PACKETS_FUMESHROOM");
        PLANT_ATLAS.put("garlic", "PLANTGARLIC");
        PLANT_REGION.put("garlic", "IMAGE_PLANT_GARLIC_GARLIC_120X87");
        PLANT_SEED_REGION.put("garlic", "IMAGE_UI_PACKETS_GARLIC");
        PLANT_ATLAS.put("goldbloom", "PLANTGOLDBLOOM");
        PLANT_REGION.put("goldbloom", "IMAGE_PLANT_GOLDBLOOM_GOLDBLOOM_69X78");
        PLANT_SEED_REGION.put("goldbloom", "IMAGE_UI_PACKETS_GOLDBLOOM");
        PLANT_ATLAS.put("grapeshot", "PLANTGRAPESHOT");
        PLANT_REGION.put("grapeshot", "IMAGE_PLANT_GRAPESHOT_GRAPESHOT_222X210");
        PLANT_SEED_REGION.put("grapeshot", "IMAGE_UI_PACKETS_GRAPESHOT");
        PLANT_ATLAS.put("gravebuster", "PLANTGRAVEBUSTER");
        PLANT_REGION.put("gravebuster", "IMAGE_EFFECTS_GRAVEBUSTER_EXPLOSION_POTATOMINE_GRAVEBUSTER_EXPLOSION_POTATOMINE_424X145");
        PLANT_SEED_REGION.put("gravebuster", "IMAGE_UI_PACKETS_GRAVEBUSTER");
        PLANT_ATLAS.put("hotpotato", "PLANTHOTPOTATO");
        PLANT_REGION.put("hotpotato", "IMAGE_EFFECTS_HOTPOTATO_ICEBLOCK_PUDDLE_HOTPOTATO_ICEBLOCK_PUDDLE_203X63");
        PLANT_SEED_REGION.put("hotpotato", "IMAGE_UI_PACKETS_HOTPOTATO");
        PLANT_ATLAS.put("hypnoshroom", "PLANTHYPNOSHROOM");
        PLANT_REGION.put("hypnoshroom", "IMAGE_PLANT_HYPNOSHROOM_HYPNOSHROOM_173X161");
        PLANT_SEED_REGION.put("hypnoshroom", "IMAGE_UI_PACKETS_HYPNOSHROOM");
        PLANT_ATLAS.put("iceshroom", "PLANTICESHROOM");
        PLANT_REGION.put("iceshroom", "IMAGE_PLANT_ICESHROOM_ICESHROOM_190X958");
        PLANT_SEED_REGION.put("iceshroom", "IMAGE_UI_PACKETS_ICESHROOM");
        PLANT_ATLAS.put("imitater", "PLANTIMITATER");
        PLANT_REGION.put("imitater", "IMAGE_PLANT_IMITATER_IMITATER_113X92");
        PLANT_SEED_REGION.put("imitater", "IMAGE_UI_PACKETS_IMITATER");
        PLANT_ATLAS.put("jalapeno", "PLANTJALAPENO");
        PLANT_REGION.put("jalapeno", "IMAGE_PLANT_JALAPENO_JALAPENO_111X185");
        PLANT_SEED_REGION.put("jalapeno", "IMAGE_UI_PACKETS_JALAPENO");
        PLANT_ATLAS.put("kernelpult", "PLANTKERNELPULT");
        PLANT_REGION.put("kernelpult", "IMAGE_PLANT_KERNALPULT_KERNALPULT_132X96");
        PLANT_SEED_REGION.put("kernelpult", "IMAGE_UI_PACKETS_KERNELPULT");
        PLANT_ATLAS.put("kiwibeast", "PLANTKIWIBEAST");
        PLANT_REGION.put("kiwibeast", "IMAGE_EFFECTS_KIWIBEAST_ATTACK_PULSE_KIWIBEAST_ATTACK_PULSE_184X184");
        PLANT_SEED_REGION.put("kiwibeast", "IMAGE_UI_PACKETS_KIWIBEAST");
        PLANT_ATLAS.put("lilypad", "PLANTLILYPAD");
        PLANT_REGION.put("lilypad", "IMAGE_PLANT_LILYPAD_LILYPAD_214X95");
        PLANT_SEED_REGION.put("lilypad", "IMAGE_UI_PACKETS_LILYPAD");
        PLANT_ATLAS.put("magnetshroom", "PLANTMAGNETSHROOM");
        PLANT_REGION.put("magnetshroom", "IMAGE_PLANT_MAGNETSHROOM_MAGNETSHROOM_269X269");
        PLANT_SEED_REGION.put("magnetshroom", "IMAGE_UI_PACKETS_MAGNETSHROOM");
        PLANT_ATLAS.put("megagatlingpea", "PLANTMEGAGATLING");
        PLANT_REGION.put("megagatlingpea", "IMAGE_PLANT_MEGAGATLING_MEGAGATLING_121X87");
        PLANT_SEED_REGION.put("megagatlingpea", "IMAGE_UI_PACKETS_MEGAGATLING");
        PLANT_ATLAS.put("melonpult", "PLANTMELONPULT");
        PLANT_REGION.put("melonpult", "IMAGE_PLANT_MELONPULT_MELONPULT_203X193");
        PLANT_SEED_REGION.put("melonpult", "IMAGE_UI_PACKETS_MELONPULT");
        PLANT_ATLAS.put("peapod", "PLANTPEAPOD");
        PLANT_REGION.put("peapod", "IMAGE_PLANT_PEAPOD_PEAPOD_329X278");
        PLANT_SEED_REGION.put("peapod", "IMAGE_UI_PACKETS_PEAPOD");
        PLANT_ATLAS.put("peashooter", "PLANTPEASHOOTER");
        PLANT_REGION.put("peashooter", "IMAGE_PLANT_PEASHOOTER_PEASHOOTER_99X88");
        PLANT_SEED_REGION.put("peashooter", "IMAGE_UI_PACKETS_PEASHOOTER");
        PLANT_ATLAS.put("pepperpult", "PLANTPEPPERPULT");
        PLANT_REGION.put("pepperpult", "IMAGE_PLANT_PEPPERPULT_PEPPERPULT_293X263");
        PLANT_SEED_REGION.put("pepperpult", "IMAGE_UI_PACKETS_PEPPERPULT");
        PLANT_ATLAS.put("phatbeet", "PLANTPHATBEET");
        PLANT_REGION.put("phatbeet", "IMAGE_EFFECTS_PHATBEETS_IDLE_PULSE_PHATBEETS_IDLE_PULSE_184X184");
        PLANT_SEED_REGION.put("phatbeet", "IMAGE_UI_PACKETS_PHATBEET");
        PLANT_ATLAS.put("potatomine", "PLANTPOTATOMINE");
        PLANT_REGION.put("potatomine", "IMAGE_EFFECTS_POTATOMINE_EXPLOSION_POTATOMINE_EXPLOSION_238X226");
        PLANT_SEED_REGION.put("potatomine", "IMAGE_UI_PACKETS_POTATOMINE");
        PLANT_ATLAS.put("primalpotatomine", "PLANTPRIMALPOTATOMINE");
        PLANT_REGION.put("primalpotatomine", "IMAGE_PLANT_PRIMAL_POTATOMINE_PRIMAL_POTATOMINE_119X113");
        PLANT_SEED_REGION.put("primalpotatomine", "IMAGE_UI_PACKETS_PRIMALPOTATOMINE");
        PLANT_ATLAS.put("primalsunflower", "PLANTPRIMALSUNFLOWER");
        PLANT_REGION.put("primalsunflower", "IMAGE_PLANT_PRIMAL_SUNFLOWER_PRIMAL_SUNFLOWER_121X121");
        PLANT_SEED_REGION.put("primalsunflower", "IMAGE_UI_PACKETS_PRIMALSUNFLOWER");
        PLANT_ATLAS.put("puffshroom", "PLANTPUFFSHROOM");
        PLANT_REGION.put("puffshroom", "IMAGE_EFFECTS_MUSHROOM_EXPIRATION_MUSHROOM_EXPIRATION_293X263");
        PLANT_SEED_REGION.put("puffshroom", "IMAGE_UI_PACKETS_PUFFSHROOM");
        PLANT_ATLAS.put("pumpkin", "PLANTPUMPKIN");
        PLANT_REGION.put("pumpkin", "IMAGE_PLANT_PUMPKIN_PUMPKIN_179X86");
        PLANT_SEED_REGION.put("pumpkin", "IMAGE_UI_PACKETS_PUMPKIN");
        PLANT_ATLAS.put("reinforcemint", "PLANTREINFORCEMINT");
        PLANT_REGION.put("reinforcemint", "IMAGE_EMPOWERMINTS_PLANT_REINFORCEMINT_REINFORCEMINT_221X210");
        PLANT_SEED_REGION.put("reinforcemint", "IMAGE_UI_PACKETS_REINFORCEMINT");
        PLANT_ATLAS.put("repeater", "PLANTREPEATER");
        PLANT_REGION.put("repeater", "IMAGE_PLANT_REPEATER_REPEATER_149X131");
        PLANT_SEED_REGION.put("repeater", "IMAGE_UI_PACKETS_REPEATER");
        PLANT_ATLAS.put("seashroom", "PLANTSEASHROOM");
        PLANT_REGION.put("seashroom", "IMAGE_PLANT_SEASHROOM_SEASHROOM_98X89");
        PLANT_SEED_REGION.put("seashroom", "IMAGE_UI_PACKETS_SEASHROOM");
        PLANT_ATLAS.put("snowpea", "PLANTSNOWPEA");
        PLANT_REGION.put("snowpea", "IMAGE_EFFECTS_SNOWPEA_PLANTFOOD_SNOWPEA_PLANTFOOD_546X260");
        PLANT_SEED_REGION.put("snowpea", "IMAGE_UI_PACKETS_SNOWPEA");
        PLANT_ATLAS.put("splitpea", "PLANTSPLITPEA");
        PLANT_REGION.put("splitpea", "IMAGE_PLANT_SPLITPEA_SPLITPEA_68X63");
        PLANT_SEED_REGION.put("splitpea", "IMAGE_UI_PACKETS_SPLITPEA");
        PLANT_ATLAS.put("squash", "PLANTSQUASH");
        PLANT_REGION.put("squash", "IMAGE_PLANT_SQUASH_SQUASH_148X122");
        PLANT_SEED_REGION.put("squash", "IMAGE_UI_PACKETS_SQUASH");
        PLANT_ATLAS.put("starfruit", "PLANTSTARFRUIT");
        PLANT_REGION.put("starfruit", "IMAGE_EFFECTS_STARFRUIT_PROJECTILE_PLANTFOOD_STARFRUIT_PROJECTILE_PLANTFOOD_121X124");
        PLANT_SEED_REGION.put("starfruit", "IMAGE_UI_PACKETS_STARFRUIT");
        PLANT_ATLAS.put("sunbean", "PLANTSUNBEAN");
        PLANT_REGION.put("sunbean", "IMAGE_PLANT_SUNBEAN_SUNBEAN_213X213");
        PLANT_SEED_REGION.put("sunbean", "IMAGE_UI_PACKETS_SUNBEAN");
        PLANT_ATLAS.put("sunflower", "PLANTSUNFLOWER");
        PLANT_REGION.put("sunflower", "IMAGE_PLANT_SUNFLOWER_SUNFLOWER_132X115");
        PLANT_SEED_REGION.put("sunflower", "IMAGE_UI_PACKETS_SUNFLOWER");
        PLANT_ATLAS.put("sunshroom", "PLANTSUNSHROOM");
        PLANT_REGION.put("sunshroom", "IMAGE_PLANT_SUNSHROOM_SUNSHROOM_169X125");
        PLANT_SEED_REGION.put("sunshroom", "IMAGE_UI_PACKETS_SUNSHROOM");
        PLANT_ATLAS.put("sweetpotato", "PLANTSWEETPOTATO");
        PLANT_REGION.put("sweetpotato", "IMAGE_PLANT_SWEETPOTATO_SWEETPOTATO_101X100");
        PLANT_SEED_REGION.put("sweetpotato", "IMAGE_UI_PACKETS_SWEETPOTATO");
        PLANT_ATLAS.put("tallnut", "PLANTTALLNUT");
        PLANT_REGION.put("tallnut", "IMAGE_PLANT_TALLNUT_TALLNUT_157X160");
        PLANT_SEED_REGION.put("tallnut", "IMAGE_UI_PACKETS_TALLNUT");
        PLANT_ATLAS.put("tanglekelp", "PLANTTANGLEKELP");
        PLANT_REGION.put("tanglekelp", "IMAGE_PLANT_TANGLEKELP_TANGLEKELP_264X412");
        PLANT_SEED_REGION.put("tanglekelp", "IMAGE_UI_PACKETS_TANGLEKELP");
        PLANT_ATLAS.put("threepeater", "PLANTTHREEPEATER");
        PLANT_REGION.put("threepeater", "IMAGE_PLANT_THREEPEATER_THREEPEATER_132X124");
        PLANT_SEED_REGION.put("threepeater", "IMAGE_UI_PACKETS_THREEPEATER");
        PLANT_ATLAS.put("torchwood", "PLANTTORCHWOOD");
        PLANT_REGION.put("torchwood", "IMAGE_PLANT_TORCHWOOD_TORCHWOOD_418X446");
        PLANT_SEED_REGION.put("torchwood", "IMAGE_UI_PACKETS_TORCHWOOD");
        PLANT_ATLAS.put("twinsunflower", "PLANTTWINSUNFLOWER");
        PLANT_REGION.put("twinsunflower", "IMAGE_PLANT_SUNFLOWER_TWIN_SUNFLOWER_TWIN_107X90");
        PLANT_SEED_REGION.put("twinsunflower", "IMAGE_UI_PACKETS_TWINSUNFLOWER");
        PLANT_ATLAS.put("wallnut", "PLANTWALLNUT");
        PLANT_REGION.put("wallnut", "IMAGE_PLANT_WALLNUT_WALLNUT_169X187");
        PLANT_SEED_REGION.put("wallnut", "IMAGE_UI_PACKETS_WALLNUT");
        PLANT_ATLAS.put("wasabiwhip", "PLANTWASABIWHIP");
        PLANT_REGION.put("wasabiwhip", "IMAGE_PLANT_WASABIWHIP_WASABIWHIP_323X316");
        PLANT_SEED_REGION.put("wasabiwhip", "IMAGE_UI_PACKETS_WASABIWHIP");
        PLANT_ATLAS.put("wintermelon", "PLANTWINTERMELON");
        PLANT_REGION.put("wintermelon", "IMAGE_EFFECTS_T_WINTERMELON_PROJECTILE_T_WINTERMELON_PROJECTILE_235X269");
        PLANT_SEED_REGION.put("wintermelon", "IMAGE_UI_PACKETS_WINTERMELON");
    }

    public static String plantIcon(String plantName) {
        if (plantName == null) return "";
        String key = plantName.toLowerCase();
        String atlas = PLANT_ATLAS.get(key);
        String region = PLANT_REGION.get(key);
        if (atlas == null || region == null) {
            return "";
        }
        return atlas(atlas, region);
    }

    public static String plantSeedPacket(String plantName) {
        if (plantName == null) return "";
        String key = plantName.toLowerCase();
        String atlas = PLANT_ATLAS.get(key);
        String seedRegion = PLANT_SEED_REGION.get(key);
        if (atlas != null && seedRegion != null) {
            return atlas("UI_SEEDPACKETS", seedRegion);
        }
        return plantIcon(plantName);
    }

    // ==================== زامبی‌ها ====================
    private static final Map<String, String> ZOMBIE_ATLAS = new HashMap<>();
    private static final Map<String, String> ZOMBIE_REGION = new HashMap<>();
    static {
        ZOMBIE_ATLAS.put("normal", "ZOMBIEMODERNBASICGROUP");
        ZOMBIE_REGION.put("normal", "IMAGE_ZOMBIE_ZOMBIE_MODERN_VET_FLAG_ZOMBIE_MODERN_VET_FLAG_125X143");
        ZOMBIE_ATLAS.put("conehead", "ZOMBIECARNIECONEHEADGROUP");
        ZOMBIE_REGION.put("conehead", "IMAGE_ZOMBIE_ZOMBIE_CARNIE_CONEHEAD_ZOMBIE_CARNIE_CONEHEAD_95X77");
        ZOMBIE_ATLAS.put("buckethead", "ZOMBIECARNIEBUCKETHEADGROUP");
        ZOMBIE_REGION.put("buckethead", "IMAGE_ZOMBIE_ZOMBIE_CARNIE_BUCKETHEAD_ZOMBIE_CARNIE_BUCKETHEAD_99X137");
        ZOMBIE_ATLAS.put("allstar", "ZOMBIEMODERNALLSTARGROUP");
        ZOMBIE_REGION.put("allstar", "IMAGE_ZOMBIE_ZOMBIE_MODERN_ALLSTAR_ZOMBIE_MODERN_ALLSTAR_117X101");
        ZOMBIE_ATLAS.put("arcade", "ZOMBIEEIGHTIESARCADEGROUP");
        ZOMBIE_REGION.put("arcade", "IMAGE_EFFECTS_80S_ARCADE_CABINET_BREAK_80S_ARCADE_CABINET_BREAK_293X263");
        ZOMBIE_ATLAS.put("barrelroller", "ZOMBIEPIRATEBARRELPUSHERGROUP");
        ZOMBIE_REGION.put("barrelroller", "IMAGE_ZOMBIE_ZOMBIE_PIRATE_BARREL_PUSHER_BARREL_BIRTHDAY_ZOMBIE_PIRATE_BARREL_PUSHER_BARREL_BIRTHDAY_127X126");
        ZOMBIE_ATLAS.put("dodorider", "ZOMBIEICEAGEDODOGROUP");
        ZOMBIE_REGION.put("dodorider", "IMAGE_ZOMBIE_ZOMBIE_ICEAGE_DODORIDER_ZOMBIE_ICEAGE_DODORIDER_293X263");
        ZOMBIE_ATLAS.put("explorer", "ZOMBIEEGYPTEXPLORERGROUP");
        ZOMBIE_REGION.put("explorer", "IMAGE_ZOMBIE_ZOMBIE_EXPLORER_ZOMBIE_EXPLORER_159X106");
        ZOMBIE_ATLAS.put("fisherman", "ZOMBIEBEACHFISHERMANGROUP");
        ZOMBIE_REGION.put("fisherman", "IMAGE_ZOMBIE_ZOMBIE_BEACH_FISHERMAN_ZOMBIE_BEACH_FISHERMAN_264X412");
        ZOMBIE_ATLAS.put("gargantuar", "ZOMBIEDARKGARGANTUARGROUP");
        ZOMBIE_REGION.put("gargantuar", "IMAGE_ZOMBIE_DARK_GARGANTUAR_DARK_GARGANTUAR_317X277");
        ZOMBIE_ATLAS.put("hunter", "ZOMBIEICEAGEHUNTERGROUP");
        ZOMBIE_REGION.put("hunter", "IMAGE_ZOMBIE_ZOMBIE_ICEAGE_HUNTER_ZOMBIE_ICEAGE_HUNTER_117X128");
        ZOMBIE_ATLAS.put("imp", "ZOMBIEDARKIMPGROUP");
        ZOMBIE_REGION.put("imp", "IMAGE_ZOMBIE_ZOMBIE_DARK_IMP_MONK_ZOMBIE_DARK_IMP_MONK_69X82");
        ZOMBIE_ATLAS.put("imp_dragon", "ZOMBIEDARKIMPDRAGONGROUP");
        ZOMBIE_REGION.put("imp_dragon", "IMAGE_ZOMBIE_ZOMBIE_DARK_IMP_DRAGON_ZOMBIE_DARK_IMP_DRAGON_121X88");
        ZOMBIE_ATLAS.put("jester", "ZOMBIEDARKJESTERGROUP");
        ZOMBIE_REGION.put("jester", "IMAGE_ZOMBIE_ZOMBIE_DARK_JESTER_ZOMBIE_DARK_JESTER_224X224");
        ZOMBIE_ATLAS.put("king", "ZOMBIEDARKKINGGROUP");
        ZOMBIE_REGION.put("king", "IMAGE_ZOMBIE_ZOMBIE_DARK_KING_ZOMBIE_DARK_KING_545X514");
        ZOMBIE_ATLAS.put("newspaper", "ZOMBIEMODERNNEWSPAPERGROUP");
        ZOMBIE_REGION.put("newspaper", "IMAGE_ZOMBIE_ZOMBIE_MODERN_NEWSPAPER_ZOMBIE_MODERN_NEWSPAPER_117X138");
        ZOMBIE_ATLAS.put("octopus", "ZOMBIEBEACHOCTOPUSGROUP");
        ZOMBIE_REGION.put("octopus", "IMAGE_EFFECTS_ZOMBIE_OCTOPUS_PROJECTILE_ZOMBIE_OCTOPUS_PROJECTILE_396X357");
        ZOMBIE_ATLAS.put("parasol", "ZOMBIEBEACHSURFERGROUP");
        ZOMBIE_REGION.put("parasol", "IMAGE_ZOMBIE_ZOMBIE_BEACH_SURFER_ZOMBIE_BEACH_SURFER_264X412");
        ZOMBIE_ATLAS.put("pianist", "ZOMBIEWESTPIANOGROUP");
        ZOMBIE_REGION.put("pianist", "IMAGE_ZOMBIE_PIANO_PIANO_265X306");
        ZOMBIE_ATLAS.put("prospector", "ZOMBIEWESTPROSPECTORGROUP");
        ZOMBIE_REGION.put("prospector", "IMAGE_EFFECTS_ZOMBIE_PROSPECTOR_BLAST_OFF_ZOMBIE_PROSPECTOR_BLAST_OFF_327X182");
        ZOMBIE_ATLAS.put("ra", "ZOMBIEEGYPTRAGROUP");
        ZOMBIE_REGION.put("ra", "IMAGE_ZOMBIE_ZOMBIE_EGYPT_RA_ZOMBIE_EGYPT_RA_100X96");
        ZOMBIE_ATLAS.put("snorkel", "ZOMBIEBEACHSNORKELGROUP");
        ZOMBIE_REGION.put("snorkel", "IMAGE_ZOMBIE_ZOMBIE_BEACH_SNORKELER_ZOMBIE_BEACH_SNORKELER_55X140");
        ZOMBIE_ATLAS.put("sun_producer_zombie", "ZOMBIEEGYPTRAGROUP");
        ZOMBIE_REGION.put("sun_producer_zombie", "IMAGE_ZOMBIE_ZOMBIE_EGYPT_RA_ZOMBIE_EGYPT_RA_100X96");
        ZOMBIE_ATLAS.put("tombraiser", "ZOMBIEEGYPTTOMBRAISERGROUP");
        ZOMBIE_REGION.put("tombraiser", "IMAGE_EFFECTS_ZOMBIE_EGYPT_TOMBRAISER_BONE_HIT_ZOMBIE_EGYPT_TOMBRAISER_DISC_01");
        ZOMBIE_ATLAS.put("troglobite", "ZOMBIEICEAGETROGLOBITEGROUP");
        ZOMBIE_REGION.put("troglobite", "IMAGE_ZOMBIE_ZOMBIE_ICEAGE_TROGLOBITE_ZOMBIE_ICEAGE_TROGLOBITE_112X104");
        ZOMBIE_ATLAS.put("wizard", "ZOMBIEDARKWIZARDGROUP");
        ZOMBIE_REGION.put("wizard", "IMAGE_ZOMBIE_ZOMBIE_DARK_WIZARD_VETERAN_ZOMBIE_DARK_WIZARD_VETERAN_384X417");
    }

    public static String zombieIcon(String zombieName) {
        if (zombieName == null) return "";
        String key = zombieName.toLowerCase();
        String atlas = ZOMBIE_ATLAS.get(key);
        String region = ZOMBIE_REGION.get(key);
        if (atlas == null || region == null) {
            return "";
        }
        return atlas(atlas, region);
    }

    // ==================== گلخانه ====================
    public static final String GREENHOUSE_POT_EMPTY = atlas("ZENGARDENGROUP", "IMAGE_ZEN_GARDEN_ZEN_POT_WATER_ZEN_POT_WATER_160X97");
    public static final String GREENHOUSE_POT_LOCKED = atlas("ZENGARDENGROUP", "IMAGE_ZEN_GARDEN_LOCKED_POT_ICON");
    public static final String GREENHOUSE_POT_GROWING = atlas("ZENGARDENGROUP", "IMAGE_ZEN_GARDEN_GROWING_PLANT_SLOT_GROWING_PLANT_SLOT_184X161");
    public static final String GREENHOUSE_POT_READY = atlas("ZENGARDENGROUP", "IMAGE_ZEN_GARDEN_READYTOWATER");

    // ==================== صدا / موسیقی ====================
    public static final String MUSIC_MENU = "audio/menu.ogg";
    public static final String MUSIC_MINIGAME = "audio/minigame.ogg";
    public static final String MUSIC_BOSS = "audio/boss.ogg";
    public static final String MUSIC_WIN = "audio/win.ogg";
    public static final String MUSIC_LOSE = "audio/lose.ogg";
    public static final String MUSIC_ZEN = "audio/zen.ogg";
    public static final String MUSIC_DUEL = "audio/duel.ogg";

    public static final String SFX_CLICK = "audio/click.wav";
    public static final String SFX_ERROR = "audio/splat.wav";
    public static final String SFX_PLANT = "audio/plant.wav";
    public static final String SFX_PLANT_DIES = "audio/plant-dies.wav";
    public static final String SFX_SHOOT = "audio/shoot.wav";
    public static final String SFX_SHOVEL = "audio/shovel.wav";
    public static final String SFX_SUN = "audio/sun.wav";
    public static final String SFX_MOWER = "audio/mower.wav";
    public static final String SFX_ZOMBIE_DIES = "audio/zombie-dies.wav";
    public static final String SFX_EXPLODE = "audio/explode.wav";
    public static final String SFX_GRAVE_BREAKS = "audio/grave-breaks.wav";
    public static final String SFX_GULP = "audio/gulp.wav";
    public static final String SFX_LOB = "audio/lob.wav";
    public static final String SFX_MINT = "audio/mint.wav";
    public static final String SFX_CHIME = "audio/chime.ogg";
    public static final String SFX_RISE = "audio/rise.wav";
    public static final String SFX_STORM = "audio/storm.wav";
    public static final String SFX_TIDE = "audio/tide.wav";
    public static final String SFX_WAVE = "audio/wave.wav";
    public static final String SFX_HUGE_WAVE = "audio/huge-wave.wav";
    public static final String SFX_BOSS_HURT = "audio/boss-hurt.wav";
    public static final String SFX_BOSS_MOVE = "audio/boss-move.wav";
}