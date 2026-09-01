package network.izombie;

import java.io.Serializable;

/**
 * یک واکنش (پیام متنی آماده / ایموجی / استیکر متحرک) که یک بازیکن در حین
 * مسابقه‌ی دونفره برای حریف ارسال می‌کند (طبق بخش «سیستم ارسال واکنش در حین
 * بازی» سند فاز ۳). فیلد kind مشخص می‌کند این واکنش باید در کلاینت حریف به
 * چه شکلی نمایش داده شود.
 */
public class ReactionMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String KIND_TEXT = "TEXT";
    public static final String KIND_EMOJI = "EMOJI";
    public static final String KIND_STICKER = "STICKER"; // بخش امتیازی

    public String fromUsername;
    public String kind;
    public String content; // متن پیام، خود ایموجی، یا شناسه‌ی استیکر
}
