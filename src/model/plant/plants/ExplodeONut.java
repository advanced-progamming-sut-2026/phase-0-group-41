package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class ExplodeONut extends Plant implements IExplosive {

    private int explodeDamage = 1800;
    private int armorHP = 0;
    private int currentSunCost = 50;
    private int currentCooldown = 20;
    private int level = 1;

    public ExplodeONut() {
        super("explodeonut", PlantType.WALL_NUT, 50, 20, 4000, PlantTag.EXPLOSIVE);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (isFeedActive()) {
            System.out.println("Plant Food: دریافت زره فلزی! (هنگام نابودی زره نیز انفجار رخ می‌دهد)");
            this.armorHP = 3000;
            decayFeedEffect();
            return;
        }

        if (this.getHealth() <= 0) {
            explode(session);
        }
    }

    @Override
    public void takeDamage(int amount) {
        if (armorHP > 0) {
            armorHP -= amount;
            if (armorHP <= 0) {
                System.out.println(getName() + " زرهش نابود شد و انفجار زرهی رخ داد!");
                // اجرای یک انفجار مجزا برای نابودی زره
                armorHP = 0;
            }
        } else {
            super.takeDamage(amount);
        }
    }

    @Override
    public void explode(GameSession session) {
        System.out.println(getName() + " کاملاً نابود شد و انفجار مساحتی عظیمی رخ داد! (دمیج: " + explodeDamage + ")");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) {
            this.setMaxHealth(this.getMaxHealth() + 1000);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 3) this.explodeDamage += 200;
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}