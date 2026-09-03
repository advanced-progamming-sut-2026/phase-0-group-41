package gdx.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import gdx.render.PamAssets;
import gdx.render.ZombieVisualManager;
public class AnimatedEntity {

    private final String pamPath;
    private String clipName;
    private float stateTime;
    private float x, y;
    private boolean flipped;
    private final Color tint = new Color(Color.WHITE);

    public AnimatedEntity(String pamPath, String clipName, float x, float y) {
        this.pamPath = pamPath;
        this.clipName = clipName;
        this.x = x;
        this.y = y;
    }

    public void update(float delta) { stateTime += delta; }

    public void draw(SpriteBatch batch) {
        batch.setColor(tint);
        PamAssets.get().getPamPlayer().draw(batch, pamPath, clipName, stateTime, x, y, flipped);
        batch.setColor(Color.WHITE);
    }

    public void setPosition(float x, float y) { this.x = x; this.y = y; }
    public float getX() { return x; }
    public float getY() { return y; }

    public String getClipName() { return clipName; }

    public void setClipName(String clipName) {
        if (!this.clipName.equals(clipName)) {
            this.clipName = clipName;
            this.stateTime = 0f;
        }
    }

    public void setFlipped(boolean flipped) { this.flipped = flipped; }
    public void setTint(Color color) { this.tint.set(color); }
    public float getStateTime() { return stateTime; }
}