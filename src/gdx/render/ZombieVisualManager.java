package gdx.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import model.zombie.Zombie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZombieVisualManager {

    public interface TileMapper {
        float x(double columnPosition);
        float y(int row);
        // عرض/ارتفاع یک کاشی (بر حسب واحد جهانِ بازی)، برای مقیاس‌دهیِ درستِ
        // اسپرایتِ زامبی نسبت به گیاه‌ها و شبکه‌ی زمین (رفع مشکل «زامبی‌ها
        // خیلی بزرگ‌تر از زمین/گیاه‌ها بودند»).
        float tileWidth();
        float tileHeight();
    }

    // حداکثر تعداد کاشی (ارتفاع) که یک زامبی اجازه دارد رویش بلندتر دیده شود؛
    // با همان مقدار GameScreen.ZOMBIE_MAX_HEIGHT_TILES هماهنگ نگه داشته شود.
    private static final float ZOMBIE_MAX_HEIGHT_TILES = 1.6f;

    // اندازه‌ی طبیعیِ تقریبیِ خودِ اسپرایتِ PAM؛ چون این کتابخانه اندازه‌ی
    // واقعیِ هر فریم را در زمانِ اجرا برنمی‌گرداند (متدی برای آن در دسترس
    // نیست)، از یک مبنای ثابت و معقول (اندازه‌ی معمولِ کاراکترهای این اطلس،
    // طبق مستندِ AssetPaths مثلاً ۱۲۵×۱۴۳) استفاده می‌شود تا حداقل نسبت مقیاس
    // به‌جای «بدون مقیاس‌دهی» درست باشد.
    private static final float ASSUMED_NATIVE_SIZE = 130f;

    private final Map<Zombie, AnimatedEntity> entities = new HashMap<>();

    public void sync(List<Zombie> aliveZombies, TileMapper mapper) {
        entities.keySet().retainAll(aliveZombies);

        for (Zombie z : aliveZombies) {
            AnimatedEntity entity = entities.get(z);
            if (entity == null) {
                entity = new AnimatedEntity(
                        ZombieAnimationIds.getPamPath(z.getTypeName()),
                        ZombieAnimationIds.WALK_CLIP,
                        0, 0
                );
                entities.put(z, entity);
            }

            entity.setPosition(mapper.x(z.getXPosition()), mapper.y(z.getRow()));
            entity.setClipName(z.isEating() ? ZombieAnimationIds.EAT_CLIP : ZombieAnimationIds.WALK_CLIP);
            entity.setTargetSize(mapper.tileWidth(), mapper.tileHeight() * ZOMBIE_MAX_HEIGHT_TILES,
                    ASSUMED_NATIVE_SIZE, ASSUMED_NATIVE_SIZE);

            boolean isChilled = z.getFrozenTicks() > 0 || z.getChilledTicks() > 0;
            entity.setTint(isChilled ? new Color(0.6f, 0.8f, 1f, 1f) : Color.WHITE);
        }
    }

    public void update(float delta) {
        for (AnimatedEntity e : entities.values()) e.update(delta);
    }

    public void draw(SpriteBatch batch) {
        for (AnimatedEntity e : entities.values()) e.draw(batch);
    }
}