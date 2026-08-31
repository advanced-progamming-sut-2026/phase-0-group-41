package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gdx.PvZGame;
import gdx.assets.AssetPaths;

/**
 * صفحه‌ی انتخاب مینی‌گیم: هر یک از چهار مینی‌گیم (کوزه‌شکنی، بولینگ گردویی،
 * من زامبی، ترکیب سه‌تایی) با سه دکمه‌ی سطح ۱، ۲ و ۳ نمایش داده می‌شود.
 * هر مرحله از قبلی سخت‌تر است (طبق سند فاز یک).
 */
public class MiniGamesScreen extends BaseMenuScreen {

    private static final String[][] GAMES = {
            {"vasebreaker", "Vasebreaker (Vase Breaking)"},
            {"wallnutbowling", "Wall-nut Bowling"},
            {"izombie", "I, Zombie"},
            {"beghouled", "Beghouled"}
    };

    public MiniGamesScreen(PvZGame game) {
        super(game);

        rootTable.add(title("Mini Games")).padBottom(16f).row();

        Table listTable = new Table();
        for (String[] entry : GAMES) {
            String id = entry[0];
            String displayName = entry[1];

            Table row = new Table();
            Label nameLabel = new Label(displayName, skin);
            nameLabel.setFontScale(1.05f);
            row.add(nameLabel).width(300f).left().padRight(20f);

            for (int level = 1; level <= 3; level++) {
                final int lvl = level;
                addButton(row, "Level " + level, () -> game.startMiniGame(id, lvl));
            }

            listTable.add(row).padBottom(14f).row();
        }

        ScrollPane scrollPane = new ScrollPane(listTable, skin);
        scrollPane.setFadeScrollBars(false);
        rootTable.add(scrollPane).width(900f).height(360f).padBottom(20f).row();

        addButton(rootTable, "Back to Main Menu", game::goToMainMenu);
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_QUEST;
    }
}
