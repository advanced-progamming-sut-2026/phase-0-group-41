package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.SoundManager;
import model.user.User;

/**
 * صفحه‌ی برد/باخت مخصوص مینی‌گیم‌ها. برخلاف WinLossScreen (که برای مراحل
 * adventure است و فصل/مرحله را جلو می‌برد)، این صفحه هیچ پیشرفت adventure
 * را دستکاری نمی‌کند؛ فقط در صورت برد، شمارنده‌ی miniGamesCompleted کاربر
 * (که در جدول امتیازات هم استفاده می‌شود) را افزایش می‌دهد.
 */
public class MiniGameResultScreen extends BaseMenuScreen {

    public MiniGameResultScreen(PvZGame game, String gameName, int level, boolean won) {
        super(game);

        User user = game.getLoggedInUser();
        SoundManager.playMusic(won ? AssetPaths.MUSIC_WIN : AssetPaths.MUSIC_LOSE);

        String headline = won ? "You Win!" : "You Lose!";
        rootTable.add(title(headline)).padBottom(16f).row();
        rootTable.add(new Label(gameName + " - Level " + level, skin)).padBottom(30f).row();

        if (won && user != null) {
            user.setMiniGamesCompleted(user.getMiniGamesCompleted() + 1);
            user.recordMiniGameLevelWon(gameNameToId(gameName), level);
            game.getUserManager().save();
        }

        Table buttons = new Table();
        if (!won) {
            addButton(buttons, "Retry", () -> game.startMiniGame(gameNameToId(gameName), level));
        } else if (level < 3) {
            final int nextLevel = level + 1;
            addButton(buttons, "Next Level", () -> game.startMiniGame(gameNameToId(gameName), nextLevel));
        }
        addButton(buttons, "Mini Games Menu", game::goToMiniGames);
        addButton(buttons, "Main Menu", game::goToMainMenu);
        rootTable.add(buttons).row();
    }

    private String gameNameToId(String gameName) {
        return gameName.toLowerCase();
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_LEVEL_SELECT;
    }
}
