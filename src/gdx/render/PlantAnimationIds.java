package gdx.render;

import java.util.HashMap;
import java.util.Map;

public final class PlantAnimationIds {
    private PlantAnimationIds() {}

    private static final Map<String, String> PAM_BY_TYPE = new HashMap<>();
    static {
        PAM_BY_TYPE.put("peashooter", "768/FULL/PLANT/PEASHOOTER/PEASHOOTER.PAM");
        // TODO: بقیه گیاهان رو با IDهای واقعی از Asset Browser اضافه کن
    }
    private static final String FALLBACK_PAM = PAM_BY_TYPE.get("peashooter");

    public static String getPamPath(String plantName) {
        return PAM_BY_TYPE.getOrDefault(plantName.toLowerCase(), FALLBACK_PAM);
    }

    public static final String IDLE_CLIP = "idle";
    public static final String ATTACK_CLIP = "attack"; // اسم واقعیش رو با Asset Browser چک کن، ممکنه فرق کنه
}