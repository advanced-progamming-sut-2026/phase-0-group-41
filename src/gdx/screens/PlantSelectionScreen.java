package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import controller.PlantSelectionController;
import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.HudBar;
import gdx.util.ImageUtils;
import model.user.User;

import java.util.List;
import java.util.Set;

/**
 * Plant selection screen shown before starting a level.
 */
public class PlantSelectionScreen extends BaseMenuScreen {

    private final Table unlockedTable = new Table();
    private final Table selectedTable = new Table();
    private final HudBar hudBar;
    private final PlantSelectionController controller;

    public PlantSelectionScreen(PvZGame game) {
        super(game);
        User user = game.getLoggedInUser();
        controller = game.getPlantSelectionController();
        controller.resetSelection();

        Table top = new Table();
        top.setFillParent(true);
        top.top();
        hudBar = new HudBar(skin, user);
        top.add(hudBar).expandX().fillX().top();
        stage.addActor(top);

        rootTable.add(title("Choose Plants")).padBottom(10f).row();

        rootTable.add(new Label("Available plants (click to add):", skin)).left().padBottom(6f).row();
        unlockedTable.top().left();
        ScrollPane unlockedScroll = new ScrollPane(unlockedTable, skin);
        unlockedScroll.setFadeScrollBars(false);
        rootTable.add(unlockedScroll).width(780f).height(180f).padBottom(14f).row();

        rootTable.add(new Label("Selected plants (max 8, click to remove):", skin)).left().padBottom(6f).row();
        selectedTable.top().left();
        ScrollPane selectedScroll = new ScrollPane(selectedTable, skin);
        selectedScroll.setFadeScrollBars(false);
        rootTable.add(selectedScroll).width(780f).height(120f).padBottom(14f).row();

        rootTable.add(errorLabel).width(700f).padBottom(10f).row();

        Table buttons = new Table();
        addButton(buttons, "Start Game", this::doStartGame);
        addButton(buttons, "Back to Main Menu", game::goToMainMenu);
        rootTable.add(buttons).row();

        refreshUnlocked();
        refreshSelected();
    }

    private void refreshUnlocked() {
        unlockedTable.clear();
        User user = game.getLoggedInUser();
        if (user == null) {
            return;
        }
        Set<String> unlocked = user.getUnlockedPlants();
        int col = 0;
        for (String plantName : unlocked) {
            unlockedTable.add(buildPlantCell(plantName, true)).size(80f).pad(4f);
            col++;
            if (col % 8 == 0) {
                unlockedTable.row();
            }
        }
    }

    private void refreshSelected() {
        selectedTable.clear();
        List<String> selected = controller.getSelectedPlants();
        int col = 0;
        for (String plantName : selected) {
            selectedTable.add(buildPlantCell(plantName, false)).size(80f).pad(4f);
            col++;
            if (col % 8 == 0) {
                selectedTable.row();
            }
        }
    }

    private Stack buildPlantCell(String plantName, boolean isFromUnlockedList) {
        Image icon = new Image(ImageUtils.loadRegion(AssetPaths.plantSeedPacket(plantName)));
        Label label = new Label(plantName, skin);
        label.setFontScale(0.6f);

        Stack stack = new Stack();
        if (!AssetPaths.CARD_BACKGROUND.isEmpty()) {
            stack.add(new Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND)));
        }
        stack.add(icon);
        Table overlay = new Table();
        overlay.bottom();
        overlay.add(label).width(76f);
        stack.add(overlay);

        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clearError();
                User user = game.getLoggedInUser();
                if (isFromUnlockedList) {
                    String result = controller.addPlant(user, plantName);
                    if (!"SUCCESS".equals(result)) {
                        showError(translateAdd(result));
                    }
                } else {
                    controller.removePlant(plantName);
                }
                refreshSelected();
            }
        });
        return stack;
    }

    private String translateAdd(String code) {
        switch (code) {
            case "ERR_ALREADY_SELECTED": return "This plant is already selected.";
            case "ERR_FULL": return "You can select at most 8 plants.";
            case "ERR_LOCKED_OR_NOT_FOUND": return "This plant is locked.";
            default: return "Error: " + code;
        }
    }

    private void doStartGame() {
        clearError();
        if (controller.getSelectedPlants().isEmpty()) {
            showError("Select at least one plant.");
            return;
        }
        User user = game.getLoggedInUser();
        // TODO: chapter/level باید از یک صفحه انتخاب فصل/مرحله (Chapter/Level Select) بیاید؛
        int chapter = Math.max(1, user != null ? user.getLastCompletedChapter() + 1 : 1);
        int level = user != null ? Math.max(1, user.getLastCompletedLevel() + 1) : 1;

        // TODO: لیست واقعی مأموریت‌های این مرحله باید از یک LevelRules/Quest controller بیاید؛
        java.util.List<String> objectives = java.util.Collections.emptyList();
        game.setScreen(new LevelObjectivesScreen(game, chapter, level, objectives));
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_PLANT_SELECTION;
    }
}