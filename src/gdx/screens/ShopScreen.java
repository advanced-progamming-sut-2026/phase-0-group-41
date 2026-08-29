package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.HudBar;
import gdx.util.ImageUtils;
import model.shop.Shop;
import model.user.User;

import java.util.Map;

public class ShopScreen extends BaseMenuScreen {

    private final HudBar hudBar;
    private final Table dailyOfferSection = new Table();
    private TextButton dailyOfferButton;

    public ShopScreen(PvZGame game) {
        super(game);
        User user = game.getLoggedInUser();

        // پیشنهاد روزانه در صورت نیاز (روز جدید) تازه‌سازی می‌شود.
        if (user != null) {
            game.getShopController().refreshDailyOffer(user);
        }

        Table top = new Table();
        top.setFillParent(true);
        top.top();
        hudBar = new HudBar(skin, user);
        top.add(hudBar).expandX().fillX().top();
        stage.addActor(top);

        rootTable.add(title("Shop")).padBottom(10f).row();

        buildDailyOfferSection(user);
        rootTable.add(dailyOfferSection).padBottom(16f).row();

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
        rootTable.add(scrollPane).width(760f).height(340f).padBottom(16f).row();

        rootTable.add(errorLabel).width(600f).padBottom(10f).row();
        addButton(rootTable, "Back to Main Menu", game::goToMainMenu);
    }

    /** بخش پیشنهاد روزانه: یک گیاه تصادفی که هر روز عوض می‌شود و فقط یک‌بار در روز قابل خرید است. */
    private void buildDailyOfferSection(User user) {
        dailyOfferSection.clear();
        if (user == null || user.getDailyOfferPlant() == null) {
            return;
        }

        dailyOfferSection.add(new Label("Daily Offer", skin, "title")).colspan(3).padBottom(6f).row();

        Stack iconStack = new Stack();
        if (!AssetPaths.CARD_BACKGROUND.isEmpty()) {
            iconStack.add(new Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND)));
        }
        iconStack.add(new Image(ImageUtils.loadRegion(AssetPaths.plantIcon(user.getDailyOfferPlant()))));
        dailyOfferSection.add(iconStack).size(56f).padRight(10f);

        dailyOfferSection.add(new Label(user.getDailyOfferPlant(), skin)).width(220f).left();

        dailyOfferButton = new TextButton(
                user.isDailyOfferPurchased() ? "Claimed Today" : "Claim (1 per day)", skin);
        dailyOfferButton.setDisabled(user.isDailyOfferPurchased());
        dailyOfferButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                doClaimDailyOffer();
            }
        });
        dailyOfferSection.add(dailyOfferButton).width(180f).height(48f).row();
    }

    private void doClaimDailyOffer() {
        clearError();
        User user = game.getLoggedInUser();
        String result = game.getShopController().purchaseDailyOffer(user);
        switch (result) {
            case "SUCCESS":
                buildDailyOfferSection(user);
                break;
            case "ERR_ALREADY_PURCHASED":
                showError("You already claimed today's offer.");
                break;
            case "ERR_NO_OFFER":
                showError("No daily offer available.");
                break;
            default:
                showError("Error: " + result);
        }
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
