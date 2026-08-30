package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
<<<<<<< HEAD
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
=======
<<<<<<< HEAD
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
=======
>>>>>>> 68d6cdba585587d383ae1535892381be1eff1432
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import model.user.User;

public class SettingsScreen extends BaseMenuScreen {

    private final SelectBox<Integer> difficultyBox;
    private final Slider speedSlider;
    private final CheckBox hitboxBox;
    private final CheckBox debugBox;
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
    private final CheckBox networkGridBox;

    // بخش تقلب (Cheat) - فقط وقتی حالت دیباگ فعال است نمایش داده می‌شود.
    private final Table cheatSection = new Table();
    private final TextField cheatAmountField;
    private final Label cheatResultLabel;
<<<<<<< HEAD
=======
=======
>>>>>>> 68d6cdba585587d383ae1535892381be1eff1432
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a

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
<<<<<<< HEAD
        speedSlider.setValue(user != null ? user.getGameSpeed() : 1f);
=======
<<<<<<< HEAD
        speedSlider.setValue(user != null ? user.getGameSpeed() : 1f);
=======
        speedSlider.setValue(1f);
>>>>>>> 68d6cdba585587d383ae1535892381be1eff1432
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
        form.add(new Label("Game Speed (1 to 3):", skin)).right().padRight(10f).padBottom(10f);
        form.add(speedSlider).width(200f).padBottom(10f).row();

        hitboxBox = new CheckBox(" Show grid / hitboxes", skin);
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
        hitboxBox.setChecked(user != null && user.isShowHitboxes());
        form.add().padRight(10f);
        form.add(hitboxBox).left().padBottom(10f).row();

        // تنظیمات شبکه‌بندی زمین: در صورت فعال بودن، در حین بازی خطوط قرمز شبکه‌بندی زمین نمایش داده می‌شود.
        networkGridBox = new CheckBox(" Show field grid lines (network layout)", skin);
        networkGridBox.setChecked(user != null && user.isShowNetworkGrid());
        form.add().padRight(10f);
        form.add(networkGridBox).left().padBottom(10f).row();

        debugBox = new CheckBox(" Debug mode", skin);
        debugBox.setChecked(user != null && user.isDebugMode());
        form.add().padRight(10f);
        form.add(debugBox).left().padBottom(10f).row();

        rootTable.add(form).padBottom(10f).row();

        // بخش تقلب: فقط وقتی چک‌باکس دیباگ تیک‌خورده باشد نمایش داده می‌شود.
        cheatAmountField = new TextField("100", skin);
        cheatResultLabel = new Label("", skin);
        buildCheatSection();
        rootTable.add(cheatSection).padBottom(16f).row();
        cheatSection.setVisible(debugBox.isChecked());

        debugBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                cheatSection.setVisible(debugBox.isChecked());
            }
        });

<<<<<<< HEAD
=======
=======
        form.add().padRight(10f);
        form.add(hitboxBox).left().padBottom(10f).row();

        debugBox = new CheckBox(" Debug mode", skin);
        form.add().padRight(10f);
        form.add(debugBox).left().padBottom(10f).row();

        rootTable.add(form).padBottom(16f).row();
>>>>>>> 68d6cdba585587d383ae1535892381be1eff1432
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
        rootTable.add(errorLabel).width(500f).padBottom(10f).row();

        addButton(rootTable, "Save Settings", this::doSave);
        addButton(rootTable, "Back to Main Menu", game::goToMainMenu);
    }

<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
    private void buildCheatSection() {
        cheatSection.add(new Label("Cheats (debug mode):", skin)).colspan(3).padBottom(8f).row();

        cheatSection.add(new Label("Amount:", skin)).right().padRight(10f);
        cheatSection.add(cheatAmountField).width(120f).height(40f).padRight(10f);
        cheatSection.row();

        Table buttons = new Table();
        addButton(buttons, "Add Coins", () -> doCheat("coin"));
        addButton(buttons, "Add Diamonds", () -> doCheat("diamond"));
        cheatSection.add(buttons).colspan(2).padTop(6f).row();

        cheatSection.add(cheatResultLabel).colspan(2).width(400f).padTop(6f).row();
    }

    private void doCheat(String type) {
        clearError();
        User user = game.getLoggedInUser();
        int amount;
        try {
            amount = Integer.parseInt(cheatAmountField.getText().trim());
        } catch (NumberFormatException e) {
            cheatResultLabel.setText("Amount must be a whole number.");
            return;
        }

        String result = game.getSettingsController().applyCheat(user, amount, type);
        switch (result) {
            case "SUCCESS_COIN":
                cheatResultLabel.setText(amount + " coins added. Current balance: " + user.getCoins());
                break;
            case "SUCCESS_DIAMOND":
                cheatResultLabel.setText(amount + " diamonds added. Current balance: " + user.getDiamonds());
                break;
            case "ERR_INVALID_AMOUNT":
                cheatResultLabel.setText("Amount must be greater than zero.");
                break;
            case "ERR_DEBUG_MODE_DISABLED":
                cheatResultLabel.setText("Debug mode must be enabled and saved first.");
                break;
            default:
                cheatResultLabel.setText("Error: " + result);
        }
    }

<<<<<<< HEAD
=======
=======
>>>>>>> 68d6cdba585587d383ae1535892381be1eff1432
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
    private void doSave() {
        clearError();
        User user = game.getLoggedInUser();
        String result = game.getSettingsController().changeDifficulty(user, difficultyBox.getSelected());
        if (!"SUCCESS".equals(result)) {
            showError("Invalid difficulty level.");
            return;
        }
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
        String speedResult = game.getSettingsController().changeGameSettings(
                user, speedSlider.getValue(), hitboxBox.isChecked(), debugBox.isChecked());
        if (!"SUCCESS".equals(speedResult)) {
            showError("Invalid game speed.");
            return;
        }
        game.getSettingsController().changeNetworkGridVisibility(user, networkGridBox.isChecked());
<<<<<<< HEAD
=======
=======
        // TODO: game speed, grid display and debug mode should be applied in the GameSession/gameplay controller.
>>>>>>> 68d6cdba585587d383ae1535892381be1eff1432
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
        game.goToMainMenu();
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_SETTINGS;
    }
}
