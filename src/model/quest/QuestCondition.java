package model.quest;

/**
 * شرط تکمیل یک کوئست. این یک اینترفیس functional است تا بتوان
 * منطق‌های مختلف تکمیل کوئست (کشتن N زامبی، جمع‌آوری سکه، باز کردن گیاه و ...)
 * را به صورت لامبدا یا کلاس جداگانه پیاده‌سازی کرد.
 */
@FunctionalInterface
public interface QuestCondition {
    /**
     * @param context اطلاعات لازم برای بررسی شرط (وضعیت کاربر، بازی و ...)
     * @return true اگر شرط تکمیل کوئست برآورده شده باشد
     */
    boolean isSatisfied(QuestContext context);
}