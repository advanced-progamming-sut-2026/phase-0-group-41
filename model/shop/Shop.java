package model.shop;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TODO: پیاده‌سازی کامل فروشگاه طبق داک (کالاهای دائمی: گلدان، غذای گیاه،
 * بسته بذر تصادفی/انتخابی، تبدیل ارز؛ و پیشنهاد روزانه با تخفیف ۲۰٪ که هر روز ساعت ۰۰:۰۰ رفرش می‌شود).
 */
public class Shop {

    public static final Map<String, Integer> PERMANENT_ITEM_COIN_PRICES = new LinkedHashMap<>();
    public static final Map<String, Integer> PERMANENT_ITEM_DIAMOND_PRICES = new LinkedHashMap<>();

    static {
        PERMANENT_ITEM_COIN_PRICES.put("pot", 2000);
        PERMANENT_ITEM_COIN_PRICES.put("random-seed-packet", 1000);
        PERMANENT_ITEM_DIAMOND_PRICES.put("plant-food", 3);
        PERMANENT_ITEM_DIAMOND_PRICES.put("chosen-seed-packet", 5);
        PERMANENT_ITEM_DIAMOND_PRICES.put("currency-exchange", 5);
    }
}
