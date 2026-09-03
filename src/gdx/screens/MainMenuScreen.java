package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

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
        grid.add(buildNewsButton(user)).width(280f).height(56f).pad(6f).row();
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

    /**
     * دکمه‌ی News با یک نشانگر واقعی (بج تصویری، نه کاراکتر یونیکد) در گوشه‌ی
     * بالا-راست، وقتی کاربر خبر خوانده‌نشده داشته باشد.
     */
    private Stack buildNewsButton(User user) {
        Stack stack = new Stack();
        TextButton button = new TextButton("News", skin);
        button.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                SoundManager.playSound(AssetPaths.SFX_CLICK);
                game.goToNews();
            }
        });
        stack.add(button);

        if (user != null && user.hasUnreadNews()) {
            Table badgeWrapper = new Table();
            badgeWrapper.top().right();
            Image badge = new Image(ImageUtils.loadRegion(AssetPaths.ICON_NOTIFICATION_BADGE));
            badgeWrapper.add(badge).size(22f).padTop(-6f).padRight(-6f);
            stack.add(badgeWrapper);
        }
        return stack;
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_MAIN_MENU;
    }
}
