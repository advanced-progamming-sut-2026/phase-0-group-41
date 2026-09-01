package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.HudBar;
import gdx.util.ImageUtils;
import model.greenhouse.Greenhouse;
import model.user.User;

/**
 * Greenhouse: a grid of pots. Clicking a pot triggers the action matching its state
 * (locked/empty/growing/ready): unlock, plant, harvest, or accelerate.
 */
public class GreenhouseScreen extends BaseMenuScreen {

    private final Table gridTable = new Table();
    private final HudBar hudBar;

    public GreenhouseScreen(PvZGame game) {
        super(game);
        User user = game.getLoggedInUser();

        Table top = new Table();
        top.setFillParent(true);
        top.top();
        hudBar = new HudBar(skin, user);
        top.add(hudBar).expandX().fillX().top();
        stage.addActor(top);

        rootTable.add(title("Greenhouse")).padBottom(10f).row();
        if (user != null) {
            rootTable.add(new Label("Pots waiting to be unlocked: " + user.getPendingGreenhousePots(), skin)).padBottom(10f).row();
        }

        rootTable.add(gridTable).padBottom(16f).row();
        rootTable.add(errorLabel).width(600f).padBottom(10f).row();

        Table buttons = new Table();
        addButton(buttons, "Shop", game::goToShop);
        addButton(buttons, "Back to Main Menu", game::goToMainMenu);
        rootTable.add(buttons).row();

        refreshGrid();
    }

    private void refreshGrid() {
        gridTable.clear();
        User user = game.getLoggedInUser();
        if (user == null) {
            return;
        }
        Greenhouse gh = user.getGreenhouse();

        for (int row = 0; row < Greenhouse.ROWS; row++) {
            for (int col = 0; col < Greenhouse.COLS; col++) {
                gridTable.add(buildPotCell(user, gh, row, col)).size(90f).pad(4f);
            }
            gridTable.row();
        }
    }

    private Stack buildPotCell(User user, Greenhouse gh, int row, int col) {
        String texturePath;
        String statusText;

        if (!gh.isUnlocked(row, col)) {
            texturePath = AssetPaths.GREENHOUSE_POT_LOCKED;
            statusText = "Locked";
        } else if (gh.isEmpty(row, col)) {
            texturePath = AssetPaths.GREENHOUSE_POT_EMPTY;
            statusText = "Empty";
        } else if (gh.isReady(row, col)) {
            texturePath = AssetPaths.GREENHOUSE_POT_READY;
            statusText = gh.getPlantName(row, col) + " (Ready)";
        } else {
            texturePath = AssetPaths.GREENHOUSE_POT_GROWING;
            statusText = Greenhouse.formatDuration(gh.getRemainingMillis(row, col));
        }

        Image image = new Image(ImageUtils.loadRegion(texturePath));
        Label label = new Label(statusText, skin);
        label.setFontScale(0.7f);
        label.setWrap(true);

        Stack stack = new Stack();
        stack.add(image);
        Table overlay = new Table();
        overlay.bottom();
        overlay.add(label).width(84f);
        stack.add(overlay);

        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onPotClicked(user, gh, row, col);
            }
        });
        return stack;
    }

    private void onPotClicked(User user, Greenhouse gh, int row, int col) {
        clearError();
        String result;
        if (!gh.isUnlocked(row, col)) {
            result = game.getGreenhouseController().unlockPot(user, row, col);
        } else if (gh.isEmpty(row, col)) {
            result = game.getGreenhouseController().plant(user, row, col);
        } else if (gh.isReady(row, col)) {
            result = game.getGreenhouseController().harvest(user, row, col);
        } else {
            result = game.getGreenhouseController().accelerate(user, row, col);
        }

        if (result != null && result.startsWith("ERR")) {
            showError(translateError(result));
        }
        hudBar.refresh(user);
        refreshGrid();
    }

    private String translateError(String code) {
        switch (code) {
            case "ERR_NO_POTS": return "You have no pots to unlock. Buy some from the shop.";
            case "ERR_ALREADY_UNLOCKED": return "This pot is already unlocked.";
            case "ERR_LOCKED": return "This pot is still locked.";
            case "ERR_NOT_EMPTY": return "This pot is not empty.";
            case "ERR_EMPTY": return "This pot is empty.";
            case "ERR_NOT_READY": return "The plant is not ready yet.";
            case "ERR_ALREADY_READY": return "The plant is already ready.";
            case "ERR_NOT_ENOUGH_DIAMONDS": return "Not enough diamonds to speed up growth.";
            default: return "Error: " + code;
        }
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_GREENHOUSE;
    }
}
