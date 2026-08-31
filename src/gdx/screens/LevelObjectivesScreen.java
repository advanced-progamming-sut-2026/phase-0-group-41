package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gdx.PvZGame;
import gdx.assets.AssetPaths;

import java.util.List;

/**
 * منوی "آغاز مرحله": در ابتدای هر مرحله، مأموریت‌ها/محدودیت‌های آن مرحله
 * (مثلاً "بیش از ۳۰۰۰ خورشید خرج نکنید") به کاربر نمایش داده می‌شود.
 * طبق سند فاز دو نیازی نیست حتماً پنجره‌ی شناور (Modal) باشد؛ اینجا به‌صورت
 * یک صفحه‌ی کامل با دکمه‌ی "Continue" پیاده شده که پس از تایید کاربر،
 * وارد صفحه‌ی گرافیکی گیم‌پلی (GameScreen) می‌شود.
 */
public class LevelObjectivesScreen extends BaseMenuScreen {

    public LevelObjectivesScreen(PvZGame game, int chapter, int level, List<String> objectives) {
        super(game);

        rootTable.add(title("Level Objectives")).padBottom(10f).row();
        rootTable.add(new Label("Chapter " + chapter + " - Level " + level, skin)).padBottom(20f).row();

        Table list = new Table();
        if (objectives == null || objectives.isEmpty()) {
            list.add(new Label("No special objectives for this level.", skin)).padBottom(8f).row();
        } else {
            for (String objective : objectives) {
                Label item = new Label("\u2022 " + objective, skin);
                item.setWrap(true);
                list.add(item).width(600f).left().padBottom(10f).row();
            }
        }
        rootTable.add(list).width(600f).padBottom(24f).row();

        addButton(rootTable, "Continue", () -> game.goToGameScreen(chapter, level));
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_LEVEL_SELECT;
    }
}
