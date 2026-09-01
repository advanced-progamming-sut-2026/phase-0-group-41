package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import model.user.User;

public class ProfileScreen extends BaseMenuScreen {

    private final TextField usernameField;
    private final TextField nicknameField;
    private final TextField emailField;
    private final TextField oldPasswordField;
    private final TextField newPasswordField;
    private final Label statsLabel;

    public ProfileScreen(PvZGame game) {
        super(game);
        User user = game.getLoggedInUser();

        rootTable.add(title("Profile")).padBottom(16f).row();

        usernameField = new TextField(user != null ? user.getUsername() : "", skin);
        nicknameField = new TextField(user != null ? user.getNickname() : "", skin);
        emailField = new TextField(user != null ? user.getEmail() : "", skin);
        oldPasswordField = new TextField("", skin);
        oldPasswordField.setPasswordMode(true);
        oldPasswordField.setPasswordCharacter('*');
        newPasswordField = new TextField("", skin);
        newPasswordField.setPasswordMode(true);
        newPasswordField.setPasswordCharacter('*');

        Table form = new Table();
        addRow(form, "Username:", usernameField, "Save", this::doChangeUsername);
        addRow(form, "Nickname:", nicknameField, "Save", this::doChangeNickname);
        addRow(form, "Email:", emailField, "Save", this::doChangeEmail);
        addRow(form, "Current Password:", oldPasswordField, null, null);
        addRow(form, "New Password:", newPasswordField, "Change Password", this::doChangePassword);

        rootTable.add(form).padBottom(10f).row();
        rootTable.add(errorLabel).width(600f).padBottom(10f).row();

        String stats = user == null ? "" :
                "Games played: " + user.getGamesPlayed()
                        + "   |   Levels completed: " + user.getLevelsCompleted()
                        + "   |   Best MeowPoint: " + user.getMaxMowPoints()
                        + "   |   Coins: " + user.getCoins()
                        + "   |   Diamonds: " + user.getDiamonds();
        statsLabel = new Label(stats, skin);
        statsLabel.setWrap(true);
        rootTable.add(statsLabel).width(760f).padBottom(20f).row();

        addButton(rootTable, "Back to Main Menu", game::goToMainMenu);
    }

    private void addRow(Table form, String labelText, TextField field, String buttonText, Runnable onClick) {
        form.add(new Label(labelText, skin)).right().padRight(10f).padBottom(8f);
        form.add(field).width(320f).height(40f).padBottom(8f);
        if (buttonText != null) {
            addButton(form, buttonText, onClick);
        } else {
            form.add();
        }
        form.row();
    }

    private void doChangeUsername() {
        clearError();
        User user = game.getLoggedInUser();
        String result = game.getProfileController().changeUsername(user, usernameField.getText());
        reportResult(result);
    }

    private void doChangeNickname() {
        clearError();
        User user = game.getLoggedInUser();
        String result = game.getProfileController().changeNickname(user, nicknameField.getText());
        reportResult(result);
    }

    private void doChangeEmail() {
        clearError();
        User user = game.getLoggedInUser();
        String result = game.getProfileController().changeEmail(user, emailField.getText());
        reportResult(result);
    }

    private void doChangePassword() {
        clearError();
        User user = game.getLoggedInUser();
        String result = game.getProfileController().changePassword(user, oldPasswordField.getText(), newPasswordField.getText());
        reportResult(result);
    }

    private void reportResult(String result) {
        if ("SUCCESS".equals(result)) {
            game.setScreen(new ProfileScreen(game));
        } else {
            showError("Error: " + result);
        }
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_PROFILE;
    }
}
