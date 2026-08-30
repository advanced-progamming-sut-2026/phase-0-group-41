package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
<<<<<<< HEAD
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
=======
<<<<<<< HEAD
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
=======
>>>>>>> 68d6cdba585587d383ae1535892381be1eff1432
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.HudBar;
import gdx.util.ImageUtils;
import model.shop.Shop;
import model.user.User;

import java.util.Map;

public class ShopScreen extends BaseMenuScreen {

    private final HudBar hudBar;
<<<<<<< HEAD
    private final Table dailyOfferSection = new Table();
    private TextButton dailyOfferButton;
=======
<<<<<<< HEAD
    private final Table dailyOfferSection = new Table();
    private TextButton dailyOfferButton;
=======
>>>>>>> 68d6cdba585587d383ae1535892381be1eff1432
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a

    public ShopScreen(PvZGame game) {
        super(game);
        User user = game.getLoggedInUser();

<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
        // پیشنهاد روزانه در صورت نیاز (روز جدید) تازه‌سازی می‌شود.
        if (user != null) {
            game.getShopController().refreshDailyOffer(user);
        }

<<<<<<< HEAD
=======
=======
>>>>>>> 68d6cdba585587d383ae1535892381be1eff1432
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
        Table top = new Table();
        top.setFillParent(true);
        top.top();
        hudBar = new HudBar(skin, user);
        top.add(hudBar).expandX().fillX().top();
        stage.addActor(top);

<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
        rootTable.add(title("Shop")).padBottom(10f).row();

        buildDailyOfferSection(user);
        rootTable.add(dailyOfferSection).padBottom(16f).row();
<<<<<<< HEAD
=======
=======
        rootTable.add(title("Shop")).padBottom(16f).row();
>>>>>>> 68d6cdba585587d383ae1535892381be1eff1432
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a

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
<<<<<<< HEAD
        rootTable.add(scrollPane).width(760f).height(340f).padBottom(16f).row();
=======
<<<<<<< HEAD
        rootTable.add(scrollPane).width(760f).height(340f).padBottom(16f).row();
=======
        rootTable.add(scrollPane).width(760f).height(380f).padBottom(16f).row();
>>>>>>> 68d6cdba585587d383ae1535892381be1eff1432
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a

        rootTable.add(errorLabel).width(600f).padBottom(10f).row();
        addButton(rootTable, "Back to Main Menu", game::goToMainMenu);
    }

<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
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
<<<<<<< HEAD
        iconStack.add(new Image(ImageUtils.loadRegion(AssetPaths.plantSeedPacket(user.getDailyOfferPlant()))));
=======
        iconStack.add(new Image(ImageUtils.loadRegion(AssetPaths.plantIcon(user.getDailyOfferPlant()))));
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
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

<<<<<<< HEAD
=======
=======
>>>>>>> 68d6cdba585587d383ae1535892381be1eff1432
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
    private void addItemRow(Table table, String itemName, String priceText, boolean useDiamonds) {
        Stack iconStack = new Stack();
        if (!AssetPaths.CARD_BACKGROUND.isEmpty()) {
            iconStack.add(new Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND)));
        }
<<<<<<< HEAD
        iconStack.add(new Image(ImageUtils.loadRegion(AssetPaths.plantSeedPacket(itemName))));
=======
        iconStack.add(new Image(ImageUtils.loadRegion(AssetPaths.plantIcon(itemName))));
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
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
