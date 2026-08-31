package gdx.render;

public final class ZombieAnimationIds {
    private ZombieAnimationIds() {}

    private static final String BASE_ZOMBIE_PAM =
            "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM";

    // فعلاً همه از همین اسکلت پایه استفاده می‌کنن (زره بعداً جدا رسم می‌شه)
    public static String getPamPath(String zombieType) { return BASE_ZOMBIE_PAM; }

    public static final String WALK_CLIP = "walk";
    public static final String EAT_CLIP = "walk"; // اگه اسم جدای "eat" پیدا کردیم عوضش می‌کنیم
}