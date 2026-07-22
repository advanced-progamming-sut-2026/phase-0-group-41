package model.plant;

import model.plant.plants.*;
import java.util.Arrays;
import java.util.List;

/**
 * کارخانه تولید گیاهان (Plant Factory)
 * این کلاس وظیفه وهله‌سازی (Instantiation) تمام ۶۹ گیاه بازی را بر عهده دارد.
 */
public final class PlantFactory {

    private static final List<String> KNOWN_PLANTS = Arrays.asList(
            "appeasemint", "armamint", "bombardmint", "bonkchoy", "bowlingbulb",
            "cabbagepult", "cactus", "cattail", "cattailmint", "caulipower",
            "cherrybomb", "chomper", "citron", "doomshroom", "electricblueberry",
            "enchantmint", "endurian", "enforcemint", "enlightenmint", "explodeonut",
            "firepeashooter", "fumeshroom", "garlic", "goldbloom", "goopeashooter",
            "grapeshot", "gravebuster", "hotpotato", "hypnoshroom", "iceberglettuce",
            "iceshroom", "imitater", "jalapeno", "kernelpult", "kiwibeast",
            "lilypad", "magnetshroom", "megagatlingpea", "melonpult", "peapod",
            "peashooter", "pepperpult", "phatbeet", "piercemint", "potatomine",
            "primalpotatomine", "primalsunflower", "puffshroom", "pumpkin", "reinforcemint",
            "repeater", "rotobaga", "seashroom", "snowpea", "splitpea",
            "squash", "starfruit", "sunbean", "sunflower", "sunshroom",
            "sweetpotato", "tallnut", "tanglekelp", "threepeater", "torchwood",
            "twinsunflower", "wallnut", "wasabiwhip", "wintermelon"
    );

    private PlantFactory() {
        // جلوگیری از ساخته شدن آبجکت از این کلاس 유تیلیتی
    }

    public static boolean isKnown(String plantName) {
        if (plantName == null) return false;
        return KNOWN_PLANTS.contains(plantName.toLowerCase());
    }

    public static List<String> allPlantNames() {
        return KNOWN_PLANTS;
    }

    public static Plant create(String plantName) {
        if (plantName == null) {
            throw new IllegalArgumentException("نام گیاه نمی‌تواند null باشد.");
        }
//salam
        switch (plantName.toLowerCase()) {
            case "appeasemint": return new AppeaseMint();
            case "armamint": return new ArmaMint();
            case "bombardmint": return new BombardMint();
            case "bonkchoy": return new BonkChoy();
            case "bowlingbulb": return new BowlingBulb();
            case "cabbagepult": return new CabbagePult();
            case "cactus": return new Cactus();
            case "cattail": return new CatTail();
            case "cattailmint": return new CatTailMint();
            case "caulipower": return new Caulipower();
            case "cherrybomb": return new CherryBomb();
            case "chomper": return new Chomper();
            case "citron": return new Citron();
            case "doomshroom": return new DoomShroom();
            case "electricblueberry": return new ElectricBlueberry();
            case "enchantmint": return new EnchantMint();
            case "endurian": return new Endurian();
            case "enforcemint": return new EnforceMint();
            case "enlightenmint": return new EnlightenMint();
            case "explodeonut": return new ExplodeONut();
            case "firepeashooter": return new FirePeashooter();
            case "fumeshroom": return new FumeShroom();
            case "garlic": return new Garlic();
            case "goldbloom": return new GoldBloom();
            case "goopeashooter": return new GooPeashooter();
            case "grapeshot": return new Grapeshot();
            case "gravebuster": return new GraveBuster();
            case "hotpotato": return new HotPotato();
            case "hypnoshroom": return new HypnoShroom();
            case "iceberglettuce": return new IcebergLettuce();
            case "iceshroom": return new IceShroom();

            // نکته: از آنجایی که در سازنده Imitater یک ورودی برای گیاه کپی‌شونده گذاشته بودیم،
            // اینجا فعلاً null پاس می‌دهیم. در زمان پیاده‌سازی منطق انتخاب کارت‌ها، این بخش مدیریت می‌شود.
            case "imitater": return new Imitater(null);

            case "jalapeno": return new Jalapeno();
            case "kernelpult": return new KernelPult();
            case "kiwibeast": return new Kiwibeast();
            case "lilypad": return new LilyPad();
            case "magnetshroom": return new MagnetShroom();
            case "megagatlingpea": return new MegaGatlingPea();
            case "melonpult": return new MelonPult();
            case "peapod": return new PeaPod();
            case "peashooter": return new Peashooter();
            case "pepperpult": return new PepperPult();
            case "phatbeet": return new PhatBeet();
            case "piercemint": return new PierceMint();
            case "potatomine": return new PotatoMine();
            case "primalpotatomine": return new PrimalPotatoMine();
            case "primalsunflower": return new PrimalSunflower();
            case "puffshroom": return new PuffShroom();
            case "pumpkin": return new Pumpkin();
            case "reinforcemint": return new ReinforceMint();
            case "repeater": return new Repeater();
            case "rotobaga": return new Rotobaga();
            case "seashroom": return new SeaShroom();
            case "snowpea": return new SnowPea();
            case "splitpea": return new SplitPea();
            case "squash": return new Squash();
            case "starfruit": return new Starfruit();
            case "sunbean": return new SunBean();
            case "sunflower": return new Sunflower();
            case "sunshroom": return new SunShroom();
            case "sweetpotato": return new SweetPotato();
            case "tallnut": return new TallNut();
            case "tanglekelp": return new TangleKelp();
            case "threepeater": return new Threepeater();
            case "torchwood": return new Torchwood();
            case "twinsunflower": return new TwinSunflower();
            case "wallnut": return new WallNut();
            case "wasabiwhip": return new WasabiWhip();
            case "wintermelon": return new WinterMelon();
            default:
                throw new IllegalArgumentException("گیاه ناشناخته در کارخانه: " + plantName);
        }
    }
}