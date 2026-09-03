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
    }

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