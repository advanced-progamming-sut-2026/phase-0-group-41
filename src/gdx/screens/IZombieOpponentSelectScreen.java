package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import network.izombie.IZombieNetworkClient;

/**
 * سیستم انتخاب رقیب برای مینی‌گیم دونفره‌ی «من، زامبی» (طبق سند فاز ۳):
 * پیش از شروع بازی، از کاربر پرسیده می‌شود که می‌خواهد با فرد مشخصی رقابت
 * کند یا به‌صورت تصادفی با فرد دیگری بازی کند.
 */
public class IZombieOpponentSelectScreen extends BaseMenuScreen {

    private final TextField opponentUsernameField;
    private final int level;

    public IZombieOpponentSelectScreen(PvZGame game, int level) {
        super(game);
        this.level = level;

        rootTable.add(title("I, Zombie - Multiplayer")).padBottom(10f).row();
        rootTable.add(new Label("Level " + level, skin)).padBottom(24f).row();

        Table specificRow = new Table();
        opponentUsernameField = new TextField("", skin);
        specificRow.add(new Label("Opponent username:", skin)).padRight(10f);
        specificRow.add(opponentUsernameField).width(260f).height(40f);
        rootTable.add(specificRow).padBottom(10f).row();
        rootTable.add(errorLabel).width(520f).padBottom(10f).row();

        Table buttons = new Table();
        addButton(buttons, "Challenge This Player", this::challengeSpecificPlayer);
        rootTable.add(buttons).padBottom(6f).row();

        Table buttons2 = new Table();
        addButton(buttons2, "Play Random Opponent", this::playRandom);
        addButton(buttons2, "Back", game::goToMiniGames);
        rootTable.add(buttons2).row();
    }

    private void challengeSpecificPlayer() {
        clearError();
        String username = game.getLoggedInUser().getUsername();
        String target = opponentUsernameField.getText() == null ? "" : opponentUsernameField.getText().trim();
        if (target.isEmpty()) {
            showError("Enter the username of the player you want to challenge.");
            return;
        }
        if (target.equalsIgnoreCase(username)) {
            showError("You can't challenge yourself.");
            return;
        }

        IZombieNetworkClient.ChallengeResult result = IZombieNetworkClient.challengeUser(username, target, level);
        if (!result.sent) {
            showError(describeError(result.errorCode, target));
            return;
        }
        game.setScreen(new IZombieWaitingScreen(game, level, IZombieWaitingScreen.Mode.WAITING_FOR_CHALLENGE_RESPONSE, target));
    }

    private void playRandom() {
        clearError();
        game.setScreen(new IZombieWaitingScreen(game, level, IZombieWaitingScreen.Mode.RANDOM_QUEUE, null));
    }

    private String describeError(String code, String target) {
        if (code == null) {
            return "Could not send challenge.";
        }
        switch (code) {
            case "ERR_USER_NOT_FOUND":
                return "No user named \"" + target + "\" was found.";
            case "ERR_USER_OFFLINE":
                return "\"" + target + "\" is currently offline.";
            case "ERR_USER_BUSY":
                return "\"" + target + "\" is already in a match.";
            case "ERR_SELF":
                return "You can't challenge yourself.";
            default:
                return "Could not send challenge (" + code + ").";
        }
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_QUEST;
    }
}
