package gdx.render;

import java.util.HashMap;
import java.util.Map;

public final class ZombieAnimationIds {
    private ZombieAnimationIds() {}

    private static final String FALLBACK_PAM_SOURCE_NORMAL = "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM";
    private static final Map<String, String> PAM_BY_TYPE = new HashMap<>();
    static {
        PAM_BY_TYPE.put("normal",     "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM");
        PAM_BY_TYPE.put("explorer",   "768/INITIAL/ZOMBIE/ZOMBIE_EXPLORER/ZOMBIE_EXPLORER.PAM");
        PAM_BY_TYPE.put("imp",        "768/INITIAL/ZOMBIE/ZOMBIE_IMP_BARE/ZOMBIE_IMP_BARE.PAM");
        PAM_BY_TYPE.put("ra",         "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_RA/ZOMBIE_EGYPT_RA.PAM");
        PAM_BY_TYPE.put("tombraiser", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_TOMBRAISER/ZOMBIE_EGYPT_TOMBRAISER.PAM");
        PAM_BY_TYPE.put("conehead",   PAM_BY_TYPE.get("normal"));
        PAM_BY_TYPE.put("buckethead", PAM_BY_TYPE.get("normal"));
        // نکته‌ی مهم: نام واقعی این نوع در مدل «blockhead» است، نه «block»
        // (به model.zombie.zombies.ArmorDecorator نگاه کنید)؛ کلید غلط باعث
        // می‌شد این زامبی هیچ‌وقت به این نگاشت نخورَد و همیشه با FALLBACK_PAM
        // (یعنی شکل زامبی معمولی) نمایش داده شود.
        PAM_BY_TYPE.put("blockhead",  PAM_BY_TYPE.get("normal"));
        PAM_BY_TYPE.put("knight",     PAM_BY_TYPE.get("normal"));
        // این‌ها فعلاً فایل PAM/اسکلت انیمیشن اختصاصی ندارند؛ رندر اصلی بدنِ
        // زامبی در GameScreen.drawZombies از AssetPaths.zombieIcon (که برای
        // همه‌ی این تایپ‌ها نگاشت درست دارد) استفاده می‌کند، نه از این کلاس؛
        // این نگاشت فقط برای مسیر انیمیشن اسکلتیِ PAM (در صورت اضافه‌شدن
        // فایل‌های .PAM واقعی در آینده) نگه داشته شده.
        PAM_BY_TYPE.put("allstar",      FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("arcade",       FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("barrelroller", FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("dodorider",    FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("fisherman",    FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("gargantuar",   FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("hunter",       FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("imp_dragon",   FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("jester",       FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("king",         FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("newspaper",    FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("octopus",      FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("parasol",      FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("pianist",      FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("prospector",   FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("snorkel",      FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("troglobite",   FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("turquoise",    FALLBACK_PAM_SOURCE_NORMAL);
        PAM_BY_TYPE.put("wizard",       FALLBACK_PAM_SOURCE_NORMAL);
        // TODO: وقتی فایل‌های .PAM اختصاصی این تایپ‌ها از Asset Browser به دست
        // آمد، مقدارشان را از FALLBACK_PAM_SOURCE_NORMAL به مسیر واقعی خودشان تغییر بده.
    }
    private static final String FALLBACK_PAM = PAM_BY_TYPE.get("normal");

    public static String getPamPath(String zombieType) {
        return PAM_BY_TYPE.getOrDefault(zombieType.toLowerCase(), FALLBACK_PAM);
    }

    public static final String IDLE_CLIP = "idle";
    public static final String WALK_CLIP = "walk";
    public static final String EAT_CLIP  = "eat";
    public static final String DIE_CLIP  = "die";
}