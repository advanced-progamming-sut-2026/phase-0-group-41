package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.SoundManager;
import model.user.User;

/**
 * منوی برد/باخت. بعد از برد یا باخت کاربر، وضعیت به او اعلام می‌شود.
 * گزینه‌ی خروج همیشه هست؛ در صورت باخت، گزینه‌ی "تلاش مجدد" هم نمایش
 * داده می‌شود (طبق سند).
 */
public class WinLossScreen extends BaseMenuScreen {

    public WinLossScreen(PvZGame game, boolean won, int chapter, int level, int scoreEarned, Runnable onRetry) {
        super(game);

        User user = game.getLoggedInUser();

        SoundManager.playMusic(won ? AssetPaths.MUSIC_WIN : AssetPaths.MUSIC_LOSE);

        String headline = won ? "You Win!" : "You Lose!";
        rootTable.add(title(headline)).padBottom(16f).row();
        rootTable.add(new Label("Chapter " + chapter + " - Level " + level, skin)).padBottom(10f).row();
        rootTable.add(new Label("Score earned: " + scoreEarned, skin)).padBottom(30f).row();

        if (won && user != null) {
            user.incrementLevelsCompleted();
            if (level >= user.getLastCompletedLevel() || chapter > user.getLastCompletedChapter()) {
                user.setLastCompletedChapter(chapter);
                user.setLastCompletedLevel(level);
            }
            if (scoreEarned > user.getHighScore()) {
                user.setHighScore(scoreEarned);
            }
            game.getUserManager().save();
        }

        Table buttons = new Table();
        if (!won) {
            addButton(buttons, "Retry", () -> {
                if (onRetry != null) {
                    onRetry.run();
                } else {
                    game.goToPlantSelection();
                }
            });
        }
        addButton(buttons, "Exit to Main Menu", game::goToMainMenu);
        rootTable.add(buttons).row();
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_LEVEL_SELECT;
    }
}
