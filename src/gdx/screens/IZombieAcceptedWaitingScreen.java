package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import network.izombie.IZombieNetworkClient;

/**
 * پس از پذیرفتن یک چالش، سرور بلافاصله مسابقه را می‌سازد اما پاسخ
 * RESPOND_CHALLENGE مستقیماً matchId را برنمی‌گرداند (چون همان فرمان از سمت
 * چالش‌دهنده هم صدا زده نمی‌شود). این صفحه‌ی خیلی کوتاه، فوراً IZOMBIE_MY_MATCH
 * را صدا می‌زند و به محض پیدا شدن matchId وارد بازی می‌شود.
 */
public class IZombieAcceptedWaitingScreen extends BaseMenuScreen {

    private static final float POLL_INTERVAL = 0.5f;

    private final int level;
    private float accumulator = POLL_INTERVAL; // اولین چک بلافاصله انجام شود
    private boolean resolved = false;

    public IZombieAcceptedWaitingScreen(PvZGame game, int level) {
        super(game);
        this.level = level;
        rootTable.add(title("I, Zombie")).padBottom(16f).row();
        rootTable.add(new Label("Joining match...", skin)).padBottom(20f).row();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (resolved) return;
        accumulator += delta;
        if (accumulator < POLL_INTERVAL) return;
        accumulator = 0f;

        String username = game.getLoggedInUser().getUsername();
        String matchId = IZombieNetworkClient.pollMyCurrentMatch(username);
        if (matchId != null) {
            resolved = true;
            game.setScreen(new IZombieMultiplayerScreen(game, matchId, level));
        }
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_QUEST;
    }
}
