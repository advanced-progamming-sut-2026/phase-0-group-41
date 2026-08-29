package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import model.user.SecurityQuestions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** After successful registration, the user must pick a security question and answer it. */
public class SecurityQuestionScreen extends BaseMenuScreen {

    private final SelectBox<String> questionBox;
    private final List<Integer> questionIds = new ArrayList<>();
    private final TextField answerField;
    private final TextField confirmField;

    public SecurityQuestionScreen(PvZGame game) {
        super(game);

        rootTable.add(title("Choose Security Question")).padBottom(20f).row();

        questionBox = new SelectBox<>(skin);
        List<String> questionTexts = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : SecurityQuestions.all().entrySet()) {
            questionIds.add(entry.getKey());
            questionTexts.add(entry.getValue());
        }
        questionBox.setItems(questionTexts.toArray(new String[0]));

        answerField = new TextField("", skin);
        confirmField = new TextField("", skin);

        Table form = new Table();
        form.add(new Label("Question:", skin)).right().padRight(10f).padBottom(8f);
        form.add(questionBox).width(400f).height(40f).padBottom(8f).row();
        form.add(new Label("Answer:", skin)).right().padRight(10f).padBottom(8f);
        form.add(answerField).width(320f).height(40f).padBottom(8f).row();
        form.add(new Label("Confirm Answer:", skin)).right().padRight(10f).padBottom(8f);
        form.add(confirmField).width(320f).height(40f).padBottom(8f).row();

        rootTable.add(form).padBottom(16f).row();
        rootTable.add(errorLabel).width(500f).padBottom(10f).row();
        addButton(rootTable, "Confirm and go to Login", this::doSubmit);
    }

    private void doSubmit() {
        clearError();
        int qId = questionIds.get(questionBox.getSelectedIndex());
        String result = game.getRegisterController().pickQuestion(qId, answerField.getText(), confirmField.getText());
        switch (result) {
            case "SUCCESS":
                game.goToLogin();
                break;
            case "ERR_ANSWER_MISMATCH":
                showError("Answer and confirmation do not match.");
                break;
            case "ERR_INVALID_QUESTION_ID":
                showError("Selected question is invalid.");
                break;
            case "ERR_NO_PENDING_USER":
                showError("No pending user found. Please register again.");
                game.goToRegister();
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
