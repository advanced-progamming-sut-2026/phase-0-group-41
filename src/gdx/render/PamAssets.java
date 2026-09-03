package gdx.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public final class PamAssets {

    private static PamAssets instance;

    private final TextureBank textureBank;
    private final PamPlayer pamPlayer;

    private PamAssets() {
        // TODO: این مسیر رو با مسیر واقعی پوشه‌ی assets روی سیستم خودت عوض کن
        FileHandle assetsFolder = Gdx.files.absolute("D:/APterm2/projectAP/project/assets");
        this.textureBank = new TextureBank("768", assetsFolder);
        this.pamPlayer = new PamPlayer(textureBank, assetsFolder);
    }

    public static PamAssets get() {
        if (instance == null) instance = new PamAssets();
        return instance;
    }

    public void update() { textureBank.update(); }

    public TextureRegion region(String imageId) { return textureBank.region(imageId); }

    public PamPlayer getPamPlayer() { return pamPlayer; }
}