package model.shop;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.time.LocalDate;
import model.user.User;

public class Shop {

    public static final Map<String, Integer> PERMANENT_ITEM_COIN_PRICES = new LinkedHashMap<>();
    public static final Map<String, Integer> PERMANENT_ITEM_DIAMOND_PRICES = new LinkedHashMap<>();

    private static final String[] ALL_PLANTS = {
            "Sunflower", "Twin Sunflower", "Sun-shroom", "Primal Sunflower", "Gold Bloom",
            "Peashooter", "Repeater", "Threepeater", "Snow Pea", "Rotobaga", "Pea Pod",
            "Split Pea", "Citron", "Caulipower", "Electric Blueberry", "Bowling Bulb",
            "Cactus", "Fire Peashooter", "Starfruit", "Goo Peashooter", "Mega Gatling Pea",
            "Sea-shroom", "Puff-shroom", "Fume-shroom", "Cabbage-pult", "Kernel-pult",
            "Melon-pult", "Winter Melon", "Pepper-pult", "Potato Mine", "Primal Potato Mine",
            "Cherry Bomb", "Squash", "Grapeshot", "Jalapeno", "Doom-shroom", "Tangle Kelp",
            "Iceberg Lettuce", "Bonk Choy", "Phat Beet", "Chomper", "Wasabi Whip", "Kiwibeast",
            "Wall-nut", "Tall-nut", "Endurian", "Garlic", "Sweet Potato", "Explode-o-nut",
            "Pumpkin", "Sun Bean", "Torchwood", "Magnet-shroom", "Hypno-shroom", "Cat-tail",
            "Imitater", "Ice-shroom", "Lily Pad", "Hot Potato", "Grave Buster", "Enlighten-mint",
            "Appease-mint", "Arma-mint", "Bombard-mint", "Enforce-mint", "Reinforce-mint",
            "Enchant-mint", "Pierce-mint", "catTail-mint"
    };

    static {
        PERMANENT_ITEM_COIN_PRICES.put("pot", 2000);
        PERMANENT_ITEM_COIN_PRICES.put("random-seed-packet", 1000);
        PERMANENT_ITEM_DIAMOND_PRICES.put("plant-food", 3);
        PERMANENT_ITEM_DIAMOND_PRICES.put("chosen-seed-packet", 5);
        PERMANENT_ITEM_DIAMOND_PRICES.put("currency-exchange", 5);
    }

    public static boolean CanBuy(User user, String itemName, int count, String plantType, boolean useDiamonds) {
        Integer unitPrice = useDiamonds
                ? PERMANENT_ITEM_DIAMOND_PRICES.get(itemName)
                : PERMANENT_ITEM_COIN_PRICES.get(itemName);

        if (unitPrice == null) {
            return false; // کالای نامعتبر
        }

        // سقف ذخیره‌ی هم‌زمان غذای گیاه (۳ عدد)؛ رسیدن به سقف یعنی خرید ممکن نیست
        if ("plant-food".equals(itemName) && user.getPendingPlantFood() + count > 3) {
            return false;
        }

        int totalPrice = unitPrice * count;

        if (useDiamonds) {
            if (user.getDiamonds() < totalPrice) return false;
            user.setDiamonds(user.getDiamonds() - totalPrice);
        } else {
            if (user.getCoins() < totalPrice) return false;
            user.setCoins(user.getCoins() - totalPrice);
        }

        // اعمال اثر خرید بر اساس itemName
        applyPurchaseEffect(user, itemName, count, plantType);

        return true;
    }

    private static void applyPurchaseEffect(User user, String itemName, int count, String plantType) {
        switch (itemName) {
            case "pot":
                // باز کردن یک اسلات گلخانه (تا سقف ۲۰ عدد؛ سقف داخل GreenhouseController اعمال می‌شود)
                user.addPendingGreenhousePots(count);
                break;
            case "plant-food":
                // اضافه کردن غذای گیاه به انبار کاربر (در ابتدای مرحله‌ی بعد در اختیارش قرار می‌گیرد)
                user.addPendingPlantFood(count);
                break;
            case "random-seed-packet":
                java.util.List<String> unlocked = new java.util.ArrayList<>(user.getUnlockedPlants());                java.util.Random random = new java.util.Random();
                for (int i = 0; i < count; i++) {
                    if (!unlocked.isEmpty()) {
                        String randomPlant = unlocked.get(random.nextInt(unlocked.size()));
                        user.addSeedPackets(randomPlant, 1);
                    }
                }
                break;
            case "chosen-seed-packet":
                if (plantType == null) {
                    return;
                }
                user.addSeedPackets(plantType, count);
                break;
            case "currency-exchange":
                // تبدیل الماس به سکه
                break;
            default:
                break;
        }
    }
    public static void updateDailyOffer(User user) {
        String today = java.time.LocalDate.now().toString();
        if (user.getDailyOfferDate() == null || !user.getDailyOfferDate().equals(today)) {
            user.setDailyOfferDate(today);
            user.setDailyOfferPurchased(false);

            // انتخاب رندوم از بین گیاهانی که کاربر باز کرده است
            java.util.List<String> unlocked = new java.util.ArrayList<>(user.getUnlockedPlants());
            if (!unlocked.isEmpty()) {
                java.util.Random random = new java.util.Random();
                String randomPlant = unlocked.get(random.nextInt(unlocked.size()));
                user.setDailyOfferPlant(randomPlant);
            }
        }
    }
}