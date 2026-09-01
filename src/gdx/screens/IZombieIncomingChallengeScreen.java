package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import network.izombie.IZombieNetworkClient;

/**
 * پاپ‌آپ تایید/رد چالش «من، زامبی» (طبق سند فاز ۳: «یک پنجره Pop-up برای او
 * نمایش داده شود تا بتواند درخواست ورود به بازی را تایید یا رد کند»). این
 * صفحه وقتی نشان داده می‌شود که MiniGamesScreen حین Poll دوره‌ای متوجه یک
 * چالش ورودی شود.
 */
public class IZombieIncomingChallengeScreen extends BaseMenuScreen {

    private final String fromUsername;
    private final int level;
    private boolean responded = false;

    public IZombieIncomingChallengeScreen(PvZGame game, String fromUsername, int level) {
        super(game);
        this.fromUsername = fromUsername;
        this.level = level;

        rootTable.add(title("Challenge!")).padBottom(16f).row();
        Label msg = new Label(fromUsername + " wants to play I, Zombie (Level " + level + ") with you.", skin);
        msg.setWrap(true);
        rootTable.add(msg).width(520f).padBottom(30f).row();

        addButton(rootTable, "Accept", this::accept);
        addButton(rootTable, "Decline", this::decline);
    }

    private void accept() {
        if (responded) return;
        responded = true;
        String username = game.getLoggedInUser().getUsername();
        IZombieNetworkClient.respondToChallenge(username, true);

        // پس از پذیرفتن، سرور بلافاصله مسابقه را ساخته؛ اطلاعات آن را از
        // طریق match info کاربر مقصد پیدا می‌کنیم. چون matchId مستقیماً به
        // پاسخ RESPOND_CHALLENGE برنمی‌گردد، منتظر می‌مانیم و با
        // MATCH_INFO/POLL همان مکانیزم چالش‌دهنده را برای مقصد هم اجرا می‌کنیم:
        // ساده‌ترین راه، رفتن به یک Waiting که بی‌درنگ وضعیت مسابقه‌ی فعلی
        // کاربر را از سرور می‌پرسد.
        game.setScreen(new IZombieAcceptedWaitingScreen(game, level));
    }

    private void decline() {
        if (responded) return;
        responded = true;
        String username = game.getLoggedInUser().getUsername();
        IZombieNetworkClient.respondToChallenge(username, false);
        game.goToMiniGames();
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_QUEST;
    }
}
