package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.HudBar;
import gdx.util.ImageUtils;
import gdx.util.SoundManager;
import model.user.User;

public class MainMenuScreen extends BaseMenuScreen {

    public MainMenuScreen(PvZGame game) {
        super(game);

        SoundManager.playMusic(AssetPaths.MUSIC_MENU);

        User user = game.getLoggedInUser();

        Table top = new Table();
        top.setFillParent(true);
        top.top();
        top.add(new HudBar(skin, user)).expandX().fillX().top();
        stage.addActor(top);

        // Official game logo; nothing is shown while AssetPaths.LOGO_PVZ2 is empty.
        if (!AssetPaths.LOGO_PVZ2.isEmpty()) {
            Image logo = new Image(ImageUtils.loadRegion(AssetPaths.LOGO_PVZ2));
            rootTable.add(logo).size(360f, 110f).padBottom(10f).row();
        }

        rootTable.add(title("Main Menu")).padBottom(10f).row();
        if (user != null) {
            rootTable.add(new Label("Welcome, " + user.getNickname(), skin)).padBottom(20f).row();
        }

        Table grid = new Table();
        addButton(grid, "Play", () -> game.goToChapterLevelSelect());
        grid.row();
        addButton(grid, "Profile", game::goToProfile);
        grid.row();
        String newsLabel = "News" + (user != null && user.hasUnreadNews() ? "  \u2757" : "");
        addButton(grid, newsLabel, game::goToNews);
        grid.row();
        addButton(grid, "Settings", game::goToSettings);
        grid.row();
        addButton(grid, "Greenhouse", game::goToGreenhouse);
        grid.row();
        addButton(grid, "Collection", game::goToCollection);
        grid.row();
        addButton(grid, "Quests", game::goToQuests);
        grid.row();
        addButton(grid, "Mini Games", game::goToMiniGames);
        grid.row();
        addButton(grid, "Leaderboard", game::goToLeaderboard);
        grid.row();
        addButton(grid, "Logout", game::logout);
        grid.row();

        rootTable.add(grid).row();
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_MAIN_MENU;
    }
}
