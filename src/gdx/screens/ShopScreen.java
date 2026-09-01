package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.HudBar;
import gdx.util.ImageUtils;
import model.shop.Shop;
import model.user.User;

import java.util.ArrayList;
import java.util.Map;

public class ShopScreen extends BaseMenuScreen {

    private final HudBar hudBar;
    private final Table dailyOfferSection = new Table();
    private TextButton dailyOfferButton;

    // === پنل تأیید خرید (طبق سند: «از کاربر باید به طریقی تأیید گرفته شده و
    // سپس خرید انجام شود»). برای «بسته بذر انتخابی» یک SelectBox گیاه هم نشان
    // داده می‌شود چون این کالا نیاز به انتخاب گیاه دارد. ===
    private final Table confirmPanel = new Table();
    private SelectBox<String> plantPickerBox;

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
            addItemRow(itemsTable, entry.getKey(), entry.getValue(), "coins", false);
        }
        for (Map.Entry<String, Integer> entry : Shop.PERMANENT_ITEM_DIAMOND_PRICES.entrySet()) {
            addItemRow(itemsTable, entry.getKey(), entry.getValue(), "diamonds", true);
        }

        ScrollPane scrollPane = new ScrollPane(itemsTable, skin);
        scrollPane.setFadeScrollBars(false);
        rootTable.add(scrollPane).width(760f).height(300f).padBottom(10f).row();

        confirmPanel.top().left();
        rootTable.add(confirmPanel).padBottom(6f).row();

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
        iconStack.add(new Image(ImageUtils.loadRegion(AssetPaths.plantSeedPacket(user.getDailyOfferPlant()))));
        dailyOfferSection.add(iconStack).size(56f).padRight(10f);

        dailyOfferSection.add(new Label(user.getDailyOfferPlant(), skin)).width(220f).left();

        dailyOfferButton = new TextButton(
                user.isDailyOfferPurchased() ? "Claimed Today" : "Claim (1 per day)", skin);
        dailyOfferButton.setDisabled(user.isDailyOfferPurchased());
        dailyOfferButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
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

    private void addItemRow(Table table, String itemName, int price, String unit, boolean useDiamonds) {
        Stack iconStack = new Stack();
        if (!AssetPaths.CARD_BACKGROUND.isEmpty()) {
            iconStack.add(new Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND)));
        }
        iconStack.add(new Image(ImageUtils.loadRegion(AssetPaths.shopItemIcon(itemName))));
        table.add(iconStack).size(48f).padRight(10f);
        table.add(new Label(itemName, skin)).width(220f).left();
        table.add(new Label(price + " " + unit, skin)).width(120f);
        addButton(table, "Buy", () -> askConfirmation(itemName, price, unit, useDiamonds));
        table.row();
    }

    /** طبق سند: قبل از خرید باید از کاربر تأیید گرفته شود. */
    private void askConfirmation(String itemName, int price, String unit, boolean useDiamonds) {
        clearError();
        confirmPanel.clear();
        boolean needsPlantChoice = "chosen-seed-packet".equals(itemName);

        Label question = new Label("Buy " + itemName + " for " + price + " " + unit + "?", skin);
        confirmPanel.add(question).colspan(3).padBottom(6f).row();

        if (needsPlantChoice) {
            User user = game.getLoggedInUser();
            plantPickerBox = new SelectBox<>(skin);
            java.util.List<String> unlocked = new ArrayList<>(user.getUnlockedPlants());
            if (unlocked.isEmpty()) {
                confirmPanel.add(new Label("You have no unlocked plants to choose from.", skin)).colspan(3).row();
                return;
            }
            plantPickerBox.setItems(unlocked.toArray(new String[0]));
            confirmPanel.add(new Label("Plant:", skin)).padRight(6f);
            confirmPanel.add(plantPickerBox).width(200f).padRight(10f);
            confirmPanel.row();
        }

        Table buttons = new Table();
        addButton(buttons, "Confirm", () -> {
            String plantType = needsPlantChoice ? plantPickerBox.getSelected() : null;
            doPurchase(itemName, useDiamonds, plantType);
        });
        addButton(buttons, "Cancel", confirmPanel::clear);
        confirmPanel.add(buttons).colspan(3).row();
    }

    private void doPurchase(String itemName, boolean useDiamonds, String plantType) {
        clearError();
        User user = game.getLoggedInUser();
        boolean success = game.getShopController().processPurchase(user, itemName, 1, plantType);
        confirmPanel.clear();
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
