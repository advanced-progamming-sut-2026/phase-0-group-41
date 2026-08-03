package model.zombie.zombies;

import model.game.GameSession;
import model.zombie.Zombie;

public class ArmorDecorator extends Zombie {

    private final Zombie wrappedZombie;
    private final String armorName;
    private int armorHealth;
    private final String fullZombieName; // نام هویتی اصلی زامبی

    public ArmorDecorator(Zombie wrappedZombie, String armorName, int armorHealth, int waveCost) {
        super(wrappedZombie.getTypeName(), wrappedZombie.getHealth(), wrappedZombie.getSpeed(), waveCost, wrappedZombie.getDamagePerTick());
        this.wrappedZombie = wrappedZombie;
        this.armorName = armorName;
        this.armorHealth = armorHealth;

        // نگاشت نام زره به نام کامل زامبی برای رفع تداخل هویتی
        if (armorName.equals("cone")) {
            this.fullZombieName = "conehead";
        } else if (armorName.equals("bucket")) {
            this.fullZombieName = "buckethead";
        } else if (armorName.equals("block")) {
            this.fullZombieName = "blockhead";
        } else {
            this.fullZombieName = armorName; // برای knight
        }
    }

    @Override
    public String getTypeName() {
        // همیشه نام کامل را برمی‌گرداند تا به عنوان زامبی normal یا نام‌های ناقص شناخته نشود
        return fullZombieName;
    }

    @Override
    public void applyDifficultyModifiers(int dl) {
        // اعمال ضریب سختی روی جان زره (Armor) که قبلاً فراموش شده بود!
        double increaseMultiplier = dl / 3.0;
        this.armorHealth = (int) (this.armorHealth * increaseMultiplier);

        // اعمال ضریب سختی روی زامبی درونی
        wrappedZombie.applyDifficultyModifiers(dl);

        // همگام‌سازی جان لایه بیرونی
        super.setHealth(wrappedZombie.getHealth());
        super.setMaxHealth(wrappedZombie.getMaxHealth());
    }

    public Zombie getWrappedZombie() {
        return wrappedZombie;
    }

    @Override
    public void spawn(int row, double xPosition) {
        super.spawn(row, xPosition);
        wrappedZombie.spawn(row, xPosition);
    }

    @Override
    public void onTick(GameSession session) {
        wrappedZombie.onTick(session);
        super.setXPosition(wrappedZombie.getXPosition());
    }

    @Override
    public void takeDamage(int amount) {
        if (armorHealth > 0) {
            armorHealth -= amount;
            if (armorHealth < 0) {
                wrappedZombie.takeDamage(-armorHealth);
                armorHealth = 0;
            }
        } else {
            wrappedZombie.takeDamage(amount);
        }
        super.setHealth(wrappedZombie.getHealth());
    }

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