package view;

import controller.ShopController;
import model.shop.Shop;
import model.user.User;
import util.CommandLine;
import java.util.List;

public class ShopView {

    private final ShopController controller;
    private final ConsoleView consoleView;

    public ShopView(ShopController controller, ConsoleView consoleView) {
        this.controller = controller;
        this.consoleView = consoleView;
    }

    public boolean checkCommand(User user, CommandLine cmd) {
        List<String> t = cmd.getTokens();
        if (t.isEmpty()) return false;

        String first = t.get(0);

        // دستور show shop
        if (first.equals("show") && t.size() >= 2 && t.get(1).equals("shop")) {
            showShop(user);
            return true;
        }

        // دستور خرید
        if (first.equals("buy") && t.size() >= 2) {
            String itemName = t.get(1);
            boolean success = controller.processPurchase(user, itemName);
            
            if (success) {
                consoleView.printMessage("خرید " + itemName + " با موفقیت انجام شد!");
            } else {
                consoleView.printError("خرید ناموفق بود. یا موجودی کافی نیست یا نام کالا اشتباه است.");
            }
            return true;
        }

        return false;
    }

    private void showShop(User user) {
        consoleView.printMessage("=== فروشگاه ===");
        consoleView.printMessage("-- با سکه --");
        for (String item : Shop.PERMANENT_ITEM_COIN_PRICES.keySet()) {
            consoleView.printMessage("- " + item + " : " + Shop.PERMANENT_ITEM_COIN_PRICES.get(item));
        }
        consoleView.printMessage("-- با الماس --");
        for (String item : Shop.PERMANENT_ITEM_DIAMOND_PRICES.keySet()) {
            consoleView.printMessage("- " + item + " : " + Shop.PERMANENT_ITEM_DIAMOND_PRICES.get(item));
        }
        consoleView.printMessage("-- پیشنهاد روزانه (۲۰٪ تخفیف) --");
        if (user.isDailyOfferPurchased()) {
            consoleView.printMessage("پیشنهاد امروز خریداری شده است!");
        } else {
            consoleView.printMessage("- " + user.getDailyOfferPlant() + " : 1600 سکه");
        }
    }
}