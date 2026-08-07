package controller;

import model.shop.Shop;
import model.user.User;
import model.user.UserManager;

public class ShopController {

    private final UserManager userManager;

    public ShopController(UserManager userManager) {
        this.userManager = userManager;
    }

    public boolean processPurchase(User user, String itemName, int count, String plantType) {
        boolean useDiamonds = Shop.PERMANENT_ITEM_DIAMOND_PRICES.containsKey(itemName);
        boolean success = Shop.CanBuy(user, itemName, count, plantType, useDiamonds);

        if (success) {
            userManager.save();
        }
        return success;
    }
}