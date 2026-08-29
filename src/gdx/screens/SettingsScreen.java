package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import model.user.User;

public class SettingsScreen extends BaseMenuScreen {

    private final SelectBox<Integer> difficultyBox;
    private final Slider speedSlider;
    private final CheckBox hitboxBox;
    private final CheckBox debugBox;

    public SettingsScreen(PvZGame game) {
        super(game);
        User user = game.getLoggedInUser();

        rootTable.add(title("Settings")).padBottom(20f).row();

        Table form = new Table();

        difficultyBox = new SelectBox<>(skin);
        difficultyBox.setItems(1, 2, 3, 4, 5);
        if (user != null) {
            difficultyBox.setSelected(user.getDifficultyLevel());
        }
        form.add(new Label("Difficulty (1 to 5):", skin)).right().padRight(10f).padBottom(10f);
        form.add(difficultyBox).width(120f).padBottom(10f).row();

        speedSlider = new Slider(1f, 3f, 1f, false, skin);
        speedSlider.setValue(1f);
        form.add(new Label("Game Speed (1 to 3):", skin)).right().padRight(10f).padBottom(10f);
        form.add(speedSlider).width(200f).padBottom(10f).row();

        hitboxBox = new CheckBox(" Show grid / hitboxes", skin);
        form.add().padRight(10f);
        form.add(hitboxBox).left().padBottom(10f).row();

        debugBox = new CheckBox(" Debug mode", skin);
        form.add().padRight(10f);
        form.add(debugBox).left().padBottom(10f).row();

        rootTable.add(form).padBottom(16f).row();
        rootTable.add(errorLabel).width(500f).padBottom(10f).row();

        addButton(rootTable, "Save Settings", this::doSave);
        addButton(rootTable, "Back to Main Menu", game::goToMainMenu);
    }

    private void doSave() {
        clearError();
        User user = game.getLoggedInUser();
        String result = game.getSettingsController().changeDifficulty(user, difficultyBox.getSelected());
        if (!"SUCCESS".equals(result)) {
            showError("Invalid difficulty level.");
            return;
        }
        // TODO: game speed, grid display and debug mode should be applied in the GameSession/gameplay controller.
        game.goToMainMenu();
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_SETTINGS;
    }
}
