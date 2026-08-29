package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.HudBar;
import gdx.util.ImageUtils;
import model.shop.Shop;
import model.user.User;

import java.util.Map;

public class ShopScreen extends BaseMenuScreen {

    private final HudBar hudBar;

    public ShopScreen(PvZGame game) {
        super(game);
        User user = game.getLoggedInUser();

        Table top = new Table();
        top.setFillParent(true);
        top.top();
        hudBar = new HudBar(skin, user);
        top.add(hudBar).expandX().fillX().top();
        stage.addActor(top);

        rootTable.add(title("Shop")).padBottom(16f).row();

        Table itemsTable = new Table();
        itemsTable.top();

        for (Map.Entry<String, Integer> entry : Shop.PERMANENT_ITEM_COIN_PRICES.entrySet()) {
            addItemRow(itemsTable, entry.getKey(), entry.getValue() + " coins", false);
        }
        for (Map.Entry<String, Integer> entry : Shop.PERMANENT_ITEM_DIAMOND_PRICES.entrySet()) {
            addItemRow(itemsTable, entry.getKey(), entry.getValue() + " diamonds", true);
        }

        ScrollPane scrollPane = new ScrollPane(itemsTable, skin);
        scrollPane.setFadeScrollBars(false);
        rootTable.add(scrollPane).width(760f).height(380f).padBottom(16f).row();

        rootTable.add(errorLabel).width(600f).padBottom(10f).row();
        addButton(rootTable, "Back to Main Menu", game::goToMainMenu);
    }

    private void addItemRow(Table table, String itemName, String priceText, boolean useDiamonds) {
        Stack iconStack = new Stack();
        if (!AssetPaths.CARD_BACKGROUND.isEmpty()) {
            iconStack.add(new Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND)));
        }
        iconStack.add(new Image(ImageUtils.loadRegion(AssetPaths.plantIcon(itemName))));
        table.add(iconStack).size(48f).padRight(10f);
        table.add(new Label(itemName, skin)).width(220f).left();
        table.add(new Label(priceText, skin)).width(120f);
        addButton(table, "Buy", () -> doPurchase(itemName, useDiamonds));
        table.row();
    }

    private void doPurchase(String itemName, boolean useDiamonds) {
        clearError();
        User user = game.getLoggedInUser();
        boolean success = game.getShopController().processPurchase(user, itemName, 1, null);
        if (success) {
            hudBar.refresh(user);
        } else {
            showError("Not enough balance or purchase not possible.");
        }
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_SHOP;
    }
}
