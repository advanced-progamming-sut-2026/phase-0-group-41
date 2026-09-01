package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.SoundManager;
import model.user.User;

public class IZombieMultiplayerResultScreen extends BaseMenuScreen {

    public IZombieMultiplayerResultScreen(PvZGame game, int level, boolean won) {
        super(game);

        SoundManager.playMusic(won ? AssetPaths.MUSIC_WIN : AssetPaths.MUSIC_LOSE);

        String headline = won ? "You Win!" : "You Lose!";
        rootTable.add(title(headline)).padBottom(16f).row();
        rootTable.add(new Label("I, Zombie Online - Level " + level, skin)).padBottom(30f).row();

        User user = game.getLoggedInUser();
        if (won && user != null) {
            user.setMiniGamesCompleted(user.getMiniGamesCompleted() + 1);
            game.getUserManager().save();
        }

        Table buttons = new Table();
        addButton(buttons, "Play Again", () -> game.setScreen(new IZombieOpponentSelectScreen(game, level)));
        addButton(buttons, "Mini Games Menu", game::goToMiniGames);
        addButton(buttons, "Main Menu", game::goToMainMenu);
        rootTable.add(buttons).row();
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_LEVEL_SELECT;
    }
}
