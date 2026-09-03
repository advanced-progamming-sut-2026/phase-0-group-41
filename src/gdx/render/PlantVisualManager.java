package gdx.render;

import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import model.game.Board;
import model.plant.Plant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlantVisualManager {

    public interface TileMapper {
        float x(int col);
        float y(int row);
    }

    private final Map<Plant, AnimatedEntity> entities = new HashMap<>();

    public void sync(Board board, TileMapper mapper) {
        Set<Plant> onBoard = new HashSet<>();

        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Plant p = board.getTile(r, c).getPlant();
                if (p == null || p.isDead()) continue;
                onBoard.add(p);

                AnimatedEntity entity = entities.get(p);
                if (entity == null) {
                    entity = new AnimatedEntity(
                            PlantAnimationIds.getPamPath(p.getName()),
                            PlantAnimationIds.IDLE_CLIP, 0, 0);
                    entities.put(p, entity);
                }
                entity.setPosition(mapper.x(c), mapper.y(r));

                boolean frozen = p.isFrozenSolid();
                entity.setTint(frozen ? new Color(0.5f, 0.8f, 1f, 1f) : Color.WHITE);
            }
        }
        entities.keySet().retainAll(onBoard);
    }

    public void update(float delta) {
        for (AnimatedEntity e : entities.values()) e.update(delta);
    }

    public void draw(SpriteBatch batch) {
        for (AnimatedEntity e : entities.values()) e.draw(batch);
    }
}