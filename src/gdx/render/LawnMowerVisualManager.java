package gdx.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import model.game.Board;

public class LawnMowerVisualManager {

    public interface TileMapper {
        float x(int col);
        float y(int row);
    }

    private static final String MOWER_PAM = "768/INITIAL/MOWERS/MOWER_TUTORIAL/MOWER_TUTORIAL.PAM";
    private static final float SPEED = 250f;           // پیکسل بر ثانیه، وقتی شروع به حرکت کرد
    private static final float ATTACK_DURATION = 0.4f; // چند ثانیه گاز می‌گیره قبل از شروع حرکت

    private final AnimatedEntity[] mowers = new AnimatedEntity[Board.ROWS];
    private final boolean[] moving = new boolean[Board.ROWS];
    private final float[] attackTimer = new float[Board.ROWS];
    private boolean initialized = false;

    /**
     * چون موقعیت اولیه به TileMapper نیاز داره (که فقط داخل GameScreen در
     * دسترسه)، ساخت واقعیِ AnimatedEntity ها به تعویق افتاده تا اولین sync.
     */
    public void sync(Board board, TileMapper mapper) {
        if (!initialized) {
            for (int r = 0; r < Board.ROWS; r++) {
                float startX = mapper.x(0) - 90f; // یه‌کم قبل از ستون صفر، منتظر می‌ایسته
                mowers[r] = new AnimatedEntity(MOWER_PAM, ZombieAnimationIds.IDLE_CLIP, startX, mapper.y(r));
            }
            initialized = true;
        }

        for (int r = 0; r < Board.ROWS; r++) {
            if (board.isLawnMowerUsed(r) && !moving[r] && attackTimer[r] == 0f) {
                attackTimer[r] = ATTACK_DURATION;
                mowers[r].setClipName("attack");
            }
        }
    }

    public void update(float delta) {
        for (int r = 0; r < Board.ROWS; r++) {
            if (mowers[r] == null) continue;
            mowers[r].update(delta);

            if (attackTimer[r] > 0f) {
                attackTimer[r] -= delta;
                if (attackTimer[r] <= 0f) {
                    attackTimer[r] = 0f;
                    moving[r] = true;
                    mowers[r].setClipName("transition");
                }
            } else if (moving[r]) {
                mowers[r].setPosition(mowers[r].getX() + SPEED * delta, mowers[r].getY());
            }
        }
    }

    public void draw(SpriteBatch batch) {
        for (AnimatedEntity mower : mowers) {
            if (mower != null) mower.draw(batch);
        }
    }
}