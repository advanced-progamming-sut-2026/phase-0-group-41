package gdx.render;

import java.util.HashMap;
import java.util.Map;

public final class ZombieAnimationIds {
    private ZombieAnimationIds() {}

    private static final Map<String, String> PAM_BY_TYPE = new HashMap<>();
    static {
        PAM_BY_TYPE.put("normal",     "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM");
        PAM_BY_TYPE.put("explorer",   "768/INITIAL/ZOMBIE/ZOMBIE_EXPLORER/ZOMBIE_EXPLORER.PAM");
        PAM_BY_TYPE.put("imp",        "768/INITIAL/ZOMBIE/ZOMBIE_IMP_BARE/ZOMBIE_IMP_BARE.PAM");
        PAM_BY_TYPE.put("ra",         "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_RA/ZOMBIE_EGYPT_RA.PAM");
        PAM_BY_TYPE.put("tombraiser", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_TOMBRAISER/ZOMBIE_EGYPT_TOMBRAISER.PAM");
        PAM_BY_TYPE.put("conehead",   PAM_BY_TYPE.get("normal"));
        PAM_BY_TYPE.put("buckethead", PAM_BY_TYPE.get("normal"));
        PAM_BY_TYPE.put("block",      PAM_BY_TYPE.get("normal"));
        PAM_BY_TYPE.put("knight",     PAM_BY_TYPE.get("normal"));
        // TODO: بقیه تایپ‌ها رو با IDهایی که از Asset Browser می‌گیری اینجا اضافه کن
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