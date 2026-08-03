package model.zombie.zombies;

import model.game.GameSession;
import model.zombie.Zombie;

public class ArmorDecorator extends Zombie {

    private final Zombie wrappedZombie;
    private final String armorName;
    private int armorHealth;

    public ArmorDecorator(Zombie wrappedZombie, String armorName, int armorHealth, int waveCost) {
        // پاس دادن مقادیر اولیه به کلاس پدر زامبی
        super(wrappedZombie.getTypeName(), wrappedZombie.getHealth(), wrappedZombie.getSpeed(), waveCost, wrappedZombie.getDamagePerTick());
        this.wrappedZombie = wrappedZombie;
        this.armorName = armorName;
        this.armorHealth = armorHealth;
    }

    public Zombie getWrappedZombie() {
        return wrappedZombie;
    }

    @Override
    public void spawn(int row, double xPosition) {
        // هماهنگ کردن موقعیت اولیه هر دو لایه
        super.spawn(row, xPosition);
        wrappedZombie.spawn(row, xPosition);
    }

    @Override
    public void onTick(GameSession session) {
        // اجرای منطق حرکت و حمله زامبی درونی
        wrappedZombie.onTick(session);
        // همگام‌سازی موقعیت لایه بیرونی با لایه درونی که حرکت کرده است
        super.setXPosition(wrappedZombie.getXPosition());
    }

    @Override
    public void takeDamage(int amount) {
        if (armorHealth > 0) {
            armorHealth -= amount;
            if (armorHealth < 0) {
                // اگر دمیج بیشتر از جان زره بود، مابقی به خود زامبی وارد می‌شود
                wrappedZombie.takeDamage(-armorHealth);
                armorHealth = 0;
            }
        } else {
            wrappedZombie.takeDamage(amount);
        }
        // همگام‌سازی جان لایه بیرونی با لایه درونی
        super.setHealth(wrappedZombie.getHealth());
    }

    // 🌟 فوروارد کردن تمام متدهای حیاتی به شیء درونی برای جلوگیری از باگ‌های رندر و فیزیک
    @Override
    public boolean isDead() { return wrappedZombie.isDead(); }

    @Override
    public double getXPosition() { return wrappedZombie.getXPosition(); }

    @Override
    public void setXPosition(double xPosition) {
        super.setXPosition(xPosition);
        wrappedZombie.setXPosition(xPosition);
    }

    @Override
    public int getRow() { return wrappedZombie.getRow(); }

    @Override
    public int getHealth() { return wrappedZombie.getHealth(); }

    @Override
    public void setEating(boolean eating) {
        super.setEating(eating);
        wrappedZombie.setEating(eating);
    }

    public String getArmorName() { return armorName; }
    public int getArmorHealth() { return armorHealth; }
}