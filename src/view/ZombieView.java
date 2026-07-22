package view;

import model.zombie.Zombie;
import model.zombie.zombies.ArmorDecorator;

public class ZombieView {

    public void displayInfo(Zombie z) {
        if (z == null) return;

        // ۱. چاپ نام زامبی (حرف اول بزرگ) به همراه سپر دفاعی برای نام‌های خالی
        String typeName = z.getTypeName();
        String name = (typeName != null && !typeName.isEmpty())
                ? typeName.substring(0, 1).toUpperCase() + typeName.substring(1)
                : "Unknown Zombie";
        System.out.println(name + ":");

        // ۲. چاپ موقعیت و جان پایه زامبی اصلی
        System.out.println("  position: " + (int) z.getXPosition() + ", " + z.getRow());
        System.out.println("  health: " + z.getBaseHealth());

        // ۳. چاپ زره‌ها (باز کردن و پیمایش دقیق لایه‌های دکوراتور)
        System.out.println("  armor:");
        Zombie current = z;
        boolean hasArmor = false;

        while (current instanceof ArmorDecorator) {
            ArmorDecorator armor = (ArmorDecorator) current;
            if (armor.getArmorHealth() > 0) {
                System.out.println("    " + armor.getArmorName() + ": " + armor.getArmorHealth());
                hasArmor = true;
            }
            // رفتن به لایه عمیق‌تر و بررسی زره بعدی (مثلاً شوالیه که کلاه‌خود و شانه‌بند دارد)
            current = armor.getWrappedZombie();
        }

        if (!hasArmor) {
            System.out.println("    none");
        }

        // ۴. چاپ افکت‌ها (تبدیل شده به دو if مجزا برای نمایش هم‌زمان اثرات)
        System.out.println("  effects:");
        boolean hasEffects = false;

        if (z.getFrozenTicks() > 0) {
            System.out.println("    frozen: " + (z.getFrozenTicks() / 10.0) + "s");
            hasEffects = true;
        }
        if (z.getChilledTicks() > 0) {
            System.out.println("    chilled: " + (z.getChilledTicks() / 10.0) + "s");
            hasEffects = true;
        }

        if (!hasEffects) {
            System.out.println("    none");
        }

        System.out.println(); // یک خط فاصله متمایزکننده بین زامبی‌ها
    }
}