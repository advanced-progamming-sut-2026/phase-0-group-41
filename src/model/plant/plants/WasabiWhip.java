package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IMeleeAttacker;

public class WasabiWhip extends Plant implements IMeleeAttacker {

    private int damage = 40;
    private int actionInterval = 20; // 2 ثانیه
    private int tickCounter = 0;
    private int currentCooldown = 5;
    private int rangeBonus = 0;
    private int level = 1;

    public WasabiWhip() {
        super("wasabiwhip", PlantType.MELEE_ATTACKER, 150, 5, 300, PlantTag.FIRE);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (isFeedActive()) {
            System.out.println("Plant Food: چرخش شلاق در مساحت ۳x۳!");
            decayFeedEffect();
            return;
        }

        tickCounter++;
        if (tickCounter >= actionInterval) {
            attackMelee(session);
            tickCounter = 0;
        }
    }

    @Override
    public void attackMelee(GameSession session) {
        System.out.println(getName() + " ضربه شلاق به خانه‌های جلو و عقب (+ " + rangeBonus + " تایل) زد! دمیج: " + damage + " (گرم‌کننده محیط)");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.damage += 10;
        if (level >= 3) this.rangeBonus += 1;
        if (level >= 4) {
            this.setMaxHealth(this.getMaxHealth() + 200);
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}