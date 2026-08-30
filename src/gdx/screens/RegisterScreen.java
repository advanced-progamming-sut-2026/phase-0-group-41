package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import model.user.User;

public class RegisterScreen extends BaseMenuScreen {

    private final TextField usernameField;
    private final TextField passwordField;
    private final TextField passwordConfirmField;
    private final TextField nicknameField;
    private final TextField emailField;
    private final SelectBox<String> genderBox;

    public RegisterScreen(PvZGame game) {
        super(game);

        rootTable.add(title("Register")).padBottom(20f).row();

        usernameField = new TextField("", skin);
        passwordField = new TextField("", skin);
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        passwordConfirmField = new TextField("", skin);
        passwordConfirmField.setPasswordMode(true);
        passwordConfirmField.setPasswordCharacter('*');
        nicknameField = new TextField("", skin);
        emailField = new TextField("", skin);
        genderBox = new SelectBox<>(skin);
        genderBox.setItems("male", "female");

        Table form = new Table();
        addFieldRow(form, "Username:", usernameField);
        addFieldRow(form, "Password:", passwordField);
        addFieldRow(form, "Confirm Password:", passwordConfirmField);
        addFieldRow(form, "Nickname:", nicknameField);
        addFieldRow(form, "Email:", emailField);
        addFieldRow(form, "Gender:", genderBox);

        rootTable.add(form).padBottom(16f).row();
        rootTable.add(errorLabel).width(500f).padBottom(10f).row();

        Table buttons = new Table();
        addButton(buttons, "Register", this::doRegister);
        addButton(buttons, "Log in to existing account", game::goToLogin);
        rootTable.add(buttons).row();
    }

    private void addFieldRow(Table form, String labelText, com.badlogic.gdx.scenes.scene2d.Actor field) {
        form.add(new Label(labelText, skin)).right().padRight(10f).padBottom(8f);
        form.add(field).width(320f).height(40f).padBottom(8f).row();
    }

    private void doRegister() {
        clearError();
        String result = game.getRegisterController().registerUser(
                usernameField.getText(),
                passwordField.getText(),
                passwordConfirmField.getText(),
                nicknameField.getText(),
                emailField.getText(),
                genderBox.getSelected()
        );

        switch (result) {
            case "SUCCESS":
                game.setScreen(new SecurityQuestionScreen(game));
                break;
            case "ERR_INVALID_USERNAME":
                showError("Invalid username.");
                break;
            case "ERR_DUPLICATE_USERNAME":
                showError("This username is already taken.");
                break;
            case "ERR_PASSWORD_MISMATCH":
                showError("Password and confirmation do not match.");
                break;
            case "ERR_WEAK_PASSWORD":
                showError("Password is too weak (min 8 chars, upper/lower case, number, special char).");
                break;
            case "ERR_INVALID_NICKNAME":
                showError("Nickname must be between 3 and 30 characters.");
                break;
            case "ERR_INVALID_EMAIL":
                showError("Invalid email address.");
                break;
            case "ERR_INVALID_GENDER":
                showError("Gender must be male or female.");
                break;
            default:
                showError("Unknown error: " + result);
        }
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_REGISTER;
    }
}
