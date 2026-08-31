package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gdx.PvZGame;
import gdx.assets.AssetPaths;

import java.util.List;

/**
 * Level objectives screen.
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

        // Conflict resolved: Merged version using functional navigation
        addButton(rootTable, "Continue", () -> game.goToGameScreen(chapter, level, 10));
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_LEVEL_SELECT;
    }
}