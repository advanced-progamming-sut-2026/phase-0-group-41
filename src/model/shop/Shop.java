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

    public static boolean CanBuy(User user, String itemName, Boolean useDiamonds) {
        double cost = 0;
        boolean isDailyOffer = user.getDailyOfferPlant() != null && user.getDailyOfferPlant().equalsIgnoreCase(itemName);

        // --- بخش خرید با الماس ---
        if (useDiamonds) {
            if (!PERMANENT_ITEM_DIAMOND_PRICES.containsKey(itemName)) {
                return false;
            }
            cost = PERMANENT_ITEM_DIAMOND_PRICES.get(itemName);

            if (user.getDiamonds() < cost) {
                return false;
            } else {
                switch (itemName) {
                    case "plant-food":
                        user.addStoredPlantFood(1);
                        break;
                    case "chosen-seed-packet":
                        user.addSeedPackets("peashooter", 1);
                        break;
                    case "currency-exchange":
                        user.addCoins(500);
                        break;
                }
                user.spendDiamonds((int) cost);
                return true;
            }
        }
        // --- بخش خرید با سکه ---
        else {
            if (isDailyOffer) {
                if (user.isDailyOfferPurchased()) {
                    return false;
                }
                cost = 2000 * 0.8; // قیمت تخفیف‌خورده پیشنهاد روزانه (۱۶۰۰ سکه)
            } else {
                if (!PERMANENT_ITEM_COIN_PRICES.containsKey(itemName)) {
                    return false;
                }
                cost = PERMANENT_ITEM_COIN_PRICES.get(itemName);
            }

            if (user.getCoins() < cost) {
                return false;
            } else {
                if (isDailyOffer) {
                    user.getUnlockedPlants().add(user.getDailyOfferPlant());
                    user.setDailyOfferPurchased(true);
                } else {
                    switch (itemName) {
                        case "pot":
                            user.addPendingGreenhousePots(1);
                            break;
                        case "random-seed-packet":
                            Random rand = new Random();
                            String randomPlantName = ALL_PLANTS[rand.nextInt(ALL_PLANTS.length)];
                            user.addSeedPackets(randomPlantName, 1);
                            break;
                    }
                }
                user.spendCoins((int) cost);
                return true;
            }
        }
    }

    public static void updateDailyOffer(User user) {
        String today = LocalDate.now().toString();
        if (user.getDailyOfferDate() == null || !user.getDailyOfferDate().equals(today)) {
            user.setDailyOfferDate(today);
            user.setDailyOfferPurchased(false);

            Random random = new Random();
            int randomIndex = random.nextInt(ALL_PLANTS.length);
            String randomPlant = ALL_PLANTS[randomIndex];
            user.setDailyOfferPlant(randomPlant);
        }
    }
}
