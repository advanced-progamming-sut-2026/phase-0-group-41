package gdx.util;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gdx.assets.AssetPaths;
import model.user.User;

/**
 * نوار بالای صفحه که تعداد سکه و الماس کاربر را نشان می‌دهد.
 * طبق سند فاز دو، این اطلاعات باید در تمامی منوها (حتی در حین بازی) قابل مشاهده باشد.
 */
public class HudBar extends Table {

    private final Label coinsLabel;
    private final Label diamondsLabel;

    public HudBar(Skin skin, User user) {
        super(skin);
        setBackground(skin.getDrawable("default"));
        pad(8f);

        Image coinIcon = new Image(ImageUtils.loadRegion(AssetPaths.ICON_COIN));
        coinsLabel = new Label(user != null ? String.valueOf(user.getCoins()) : "0", skin);

        Image diamondIcon = new Image(ImageUtils.loadRegion(AssetPaths.ICON_DIAMOND));
        diamondsLabel = new Label(user != null ? String.valueOf(user.getDiamonds()) : "0", skin);

        add(coinIcon).size(32f).padRight(6f);
        add(coinsLabel).padRight(24f);
        add(diamondIcon).size(32f).padRight(6f);
        add(diamondsLabel).padRight(24f);
    }

    public void refresh(User user) {
        if (user == null) {
            return;
        }
        coinsLabel.setText(String.valueOf(user.getCoins()));
        diamondsLabel.setText(String.valueOf(user.getDiamonds()));
    }
}
