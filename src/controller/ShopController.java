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
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a

    /** پیشنهاد روزانه را در صورت نیاز تازه‌سازی می‌کند (طبق تاریخ روز جاری). */
    public void refreshDailyOffer(User user) {
        Shop.updateDailyOffer(user);
    }

    /** خرید پیشنهاد روزانه؛ فقط یک‌بار در روز قابل خرید است. */
    public String purchaseDailyOffer(User user) {
        if (user.getDailyOfferPlant() == null) {
            return "ERR_NO_OFFER";
        }
        if (user.isDailyOfferPurchased()) {
            return "ERR_ALREADY_PURCHASED";
        }
        // پیشنهاد روزانه رایگان (یا با تخفیف ویژه) یک بسته بذر از گیاه پیشنهادی می‌دهد.
        user.addSeedPackets(user.getDailyOfferPlant(), 1);
        user.setDailyOfferPurchased(true);
        userManager.save();
        return "SUCCESS";
    }
<<<<<<< HEAD
=======
=======
>>>>>>> 68d6cdba585587d383ae1535892381be1eff1432
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
}