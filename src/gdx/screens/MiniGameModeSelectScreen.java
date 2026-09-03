package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gdx.PvZGame;
import gdx.assets.AssetPaths;

/**
 * صفحه‌ی انتخاب حالت بازی برای «من، زامبی» بعد از کلیک روی یک سطح باز در
 * MiniGamesScreen. طبق درخواست: کلیک روی «Level n» دیگر مستقیماً بازی را شروع
 * نمی‌کند؛ به‌جای آن این منوی میانی با سه گزینه باز می‌شود:
 * ۱) آفلاین تک‌نفره (همان مینی‌گیم استاندارد؛ کاربر فقط نقش زامبی‌ها را دارد)
 * ۲) Couch Play (دو بازیکن روی یک دستگاه؛ یکی گیاه با ماوس، دیگری زامبی با کیبورد)
 * ۳) آنلاین (مسابقه‌ی دونفره‌ی تحت شبکه، با انتخاب حریف مشخص یا تصادفی)
 */
public class MiniGameModeSelectScreen extends BaseMenuScreen {

    public MiniGameModeSelectScreen(PvZGame game, String displayName, int level) {
        super(game);

        rootTable.add(title(displayName)).padBottom(6f).row();
        rootTable.add(new Label("Level " + level, skin)).padBottom(28f).row();

        Table buttons = new Table();
        addButton(buttons, "Offline (Single Player)", () -> game.startMiniGame("izombie", level));
        buttons.row();
        addButton(buttons, "Couch Play (2 players, 1 device)", () -> game.setScreen(new IZombieCouchScreen(game, level)));
        buttons.row();
        addButton(buttons, "Online (vs another player)", () -> game.setScreen(new IZombieOpponentSelectScreen(game, level)));
        buttons.row();
        rootTable.add(buttons).padBottom(20f).row();

        addButton(rootTable, "Back", game::goToMiniGames);
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_QUEST;
    }
}
