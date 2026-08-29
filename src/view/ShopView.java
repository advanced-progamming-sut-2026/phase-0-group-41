package view;

import controller.ShopController;
import model.shop.Shop;
import model.user.User;
import util.CommandLine;
import java.util.List;

public class ShopView {

    private ShopController controller;
    private ConsoleView consoleView;

    public ShopView(ShopController controller, ConsoleView consoleView) {
        this.controller = controller;
        this.consoleView = consoleView;
    }

    public boolean checkCommand(User user, CommandLine cmd) {
        List<String> t = cmd.getTokens();
        if (t.isEmpty()) return false;

        String first = t.get(0);
        if (first.equals("shop")) {
            Shop.updateDailyOffer(user);
        }
        if (first.equals("shop") && t.size() >= 2 && t.get(1).equals("list")) {
            showShopList(user);
            return true;
        }

        if (first.equals("shop") && t.size() >= 2 && t.get(1).equals("daily")) {
            showDailyOffer(user);
            return true;
        }

        if (first.equals("shop") && t.size() >= 2 && t.get(1).equals("buy")) {
            String itemId = cmd.get("i");
            String countStr = cmd.get("n");
            String plantType = cmd.get("t"); // اختیاری

            if (itemId == null || countStr == null) {
                consoleView.printError("فرمت دستور اشتباه است. الگو: shop buy -i <item_id> -n <count> [-t <plant_type>]");
                return true;
            }

            try {
                int count = Integer.parseInt(countStr);
                boolean success = controller.processPurchase(user, itemId, count, plantType);

                if (success) {
                    consoleView.printMessage("خرید " + itemId + " با موفقیت انجام شد!");
                } else {
                    consoleView.printError("خرید ناموفق بود. یا موجودی کافی نیست یا نام کالا اشتباه است.");
                }
            } catch (NumberFormatException e) {
                consoleView.printError("تعداد باید یک عدد صحیح باشد.");
            }
            return true;
        }

        return false;
    }

    private void showShopList(User user) {
        consoleView.printMessage("=== فروشگاه (کالاهای دائمی) ===");
        consoleView.printMessage("-- با سکه --");
        for (String item : Shop.PERMANENT_ITEM_COIN_PRICES.keySet()) {
            consoleView.printMessage("- " + item + " : " + Shop.PERMANENT_ITEM_COIN_PRICES.get(item));
        }
        consoleView.printMessage("-- با الماس --");
        for (String item : Shop.PERMANENT_ITEM_DIAMOND_PRICES.keySet()) {
            consoleView.printMessage("- " + item + " : " + Shop.PERMANENT_ITEM_DIAMOND_PRICES.get(item));
        }
    }

    private void showDailyOffer(User user) {
        consoleView.printMessage("=== پیشنهاد روزانه (۲۰٪ تخفیف) ===");
        if (user.isDailyOfferPurchased()) {
            consoleView.printMessage("پیشنهاد امروز خریداری شده است!");
        } else {
            consoleView.printMessage("- " + user.getDailyOfferPlant() + " : 1600 سکه");
        }
    }
}