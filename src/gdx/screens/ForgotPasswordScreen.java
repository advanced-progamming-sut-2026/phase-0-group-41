package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import gdx.PvZGame;
import gdx.assets.AssetPaths;

/** Three-step forgot-password flow; each step is a separate Table shown/hidden in place. */
public class ForgotPasswordScreen extends BaseMenuScreen {

    private final Table step1 = new Table();
    private final Table step2 = new Table();
    private final Table step3 = new Table();

    private final TextField usernameField = new TextField("", skin);
    private final TextField emailField = new TextField("", skin);

    private final Label questionLabel = new Label("", skin);
    private final TextField answerField = new TextField("", skin);

    private final TextField newPasswordField = new TextField("", skin);
    private final TextField confirmPasswordField = new TextField("", skin);

    public ForgotPasswordScreen(PvZGame game) {
        super(game);

        rootTable.add(title("Recover Password")).padBottom(20f).row();

        buildStep1();
        buildStep2();
        buildStep3();

        Stack stack = new Stack();
        stack.add(step1);
        stack.add(step2);
        stack.add(step3);
        rootTable.add(stack).padBottom(16f).row();

        rootTable.add(errorLabel).width(500f).padBottom(10f).row();
        addButton(rootTable, "Back to Login", game::goToLogin);

        showOnly(step1);
    }

    private void buildStep1() {
        step1.add(new Label("Username:", skin)).right().padRight(10f).padBottom(8f);
        step1.add(usernameField).width(320f).height(40f).padBottom(8f).row();
        step1.add(new Label("Email:", skin)).right().padRight(10f).padBottom(8f);
        step1.add(emailField).width(320f).height(40f).padBottom(8f).row();
        Table btnRow = new Table();
        addButton(btnRow, "Next", this::doStep1);
        step1.add(btnRow).colspan(2).row();
    }

    private void buildStep2() {
        step2.add(questionLabel).width(400f).padBottom(10f).row();
        step2.add(new Label("Answer:", skin)).right().padRight(10f).padBottom(8f);
        step2.add(answerField).width(320f).height(40f).padBottom(8f).row();
        Table btnRow = new Table();
        addButton(btnRow, "Check Answer", this::doStep2);
        step2.add(btnRow).colspan(2).row();
    }

    private void buildStep3() {
        step3.add(new Label("New Password:", skin)).right().padRight(10f).padBottom(8f);
        newPasswordField.setPasswordMode(true);
        newPasswordField.setPasswordCharacter('*');
        step3.add(newPasswordField).width(320f).height(40f).padBottom(8f).row();
        step3.add(new Label("Confirm Password:", skin)).right().padRight(10f).padBottom(8f);
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setPasswordCharacter('*');
        step3.add(confirmPasswordField).width(320f).height(40f).padBottom(8f).row();
        Table btnRow = new Table();
        addButton(btnRow, "Change Password", this::doStep3);
        step3.add(btnRow).colspan(2).row();
    }

    private void showOnly(Table visible) {
        step1.setVisible(step1 == visible);
        step2.setVisible(step2 == visible);
        step3.setVisible(step3 == visible);
    }

    private void doStep1() {
        clearError();
        String result = game.getLoginController().initiateForgetPassword(usernameField.getText(), emailField.getText());
        if ("SUCCESS".equals(result)) {
            questionLabel.setText(game.getLoginController().getPendingQuestion());
            showOnly(step2);
        } else {
            showError("Username or email does not match.");
        }
    }

    private void doStep2() {
        clearError();
        String result = game.getLoginController().answerSecurityQuestion(answerField.getText());
        if ("SUCCESS".equals(result)) {
            showOnly(step3);
        } else {
            showError("Incorrect answer.");
            showOnly(step1);
        }
    }

    private void doStep3() {
        clearError();
        String result = game.getLoginController().resetPassword(newPasswordField.getText(), confirmPasswordField.getText());
        switch (result) {
            case "SUCCESS":
                game.goToLogin();
                break;
            case "ERR_PASSWORD_MISMATCH":
                showError("Password and confirmation do not match.");
                break;
            case "ERR_WEAK_PASSWORD":
                showError("Password is too weak.");
                break;
            default:
                showError("Unknown error, please start over.");
                showOnly(step1);
        }
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_LOGIN;
    }
}
