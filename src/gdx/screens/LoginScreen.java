package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import model.user.User;

public class LoginScreen extends BaseMenuScreen {

    private final TextField usernameField;
    private final TextField passwordField;
    private final CheckBox stayLoggedInBox;

    public LoginScreen(PvZGame game) {
        super(game);

        rootTable.add(title("Log In")).padBottom(20f).row();

        usernameField = new TextField("", skin);
        passwordField = new TextField("", skin);
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        stayLoggedInBox = new CheckBox(" Remember me", skin);

        Table form = new Table();
        form.add(new Label("Username:", skin)).right().padRight(10f).padBottom(8f);
        form.add(usernameField).width(320f).height(40f).padBottom(8f).row();
        form.add(new Label("Password:", skin)).right().padRight(10f).padBottom(8f);
        form.add(passwordField).width(320f).height(40f).padBottom(8f).row();
        form.add().padBottom(8f);
        form.add(stayLoggedInBox).left().padBottom(8f).row();

        rootTable.add(form).padBottom(16f).row();
        rootTable.add(errorLabel).width(500f).padBottom(10f).row();

        Table buttons = new Table();
        addButton(buttons, "Log In", this::doLogin);
        addButton(buttons, "Forgot Password", () -> game.setScreen(new ForgotPasswordScreen(game)));
        addButton(buttons, "Back to Register", game::goToRegister);
        rootTable.add(buttons).row();
    }

    private void doLogin() {
        clearError();
        String username = usernameField.getText();
        String result = game.getLoginController().authenticate(username, passwordField.getText(), stayLoggedInBox.isChecked());
        if ("SUCCESS".equals(result)) {
            User user = game.getLoginController().getAuthenticatedUser(username);
            game.setLoggedInUser(user);
            game.goToMainMenu();
        } else {
            showError("Incorrect username or password.");
        }
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_LOGIN;
    }
}
