package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IMeleeAttacker;

public class Kiwibeast extends Plant implements IMeleeAttacker {

    private int currentDamage = 15;
    private int damageStg2 = 30;
    private int damageStg3 = 45;

    private int ticksAlive = 0;
    private int stage = 1;
    private int maxStage = 3;

    private double actionInterval = 20.0; // 2 ثانیه
    private double tickCounter = 0;

    private int currentSunCost = 175;
    private int currentCooldown = 5;
    private int level = 1;

    public Kiwibeast() {
        super("kiwibeast", PlantType.MELEE_ATTACKER, 175, 5, 300, PlantTag.AOE, PlantTag.WRAMP_UP);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (isFeedActive()) {
            System.out.println("Plant Food: پرش و کوبیدن محکم به زمین با دمیج مساحتی شدید!");
            // اجرای دمیج مساحتی توسط GameSession
            decayFeedEffect();
            return;
        }

        // سیستم رشد و افزایش استیج
        ticksAlive++;
        if (stage == 1 && ticksAlive >= 240) { // To Stg2: 24s
            stage = 2;
            currentDamage = damageStg2;
            System.out.println(getName() + " بزرگتر شد! (Stage 2)");
        } else if (stage == 2 && ticksAlive >= 720) { // To Stg3: 72s
            stage = 3;
            currentDamage = damageStg3;
            System.out.println(getName() + " به حداکثر اندازه رسید! (Stage 3)");
        }

        tickCounter += 1.0;
        if (tickCounter >= actionInterval) {
            attackMelee(session);
            tickCounter -= actionInterval;
        }
    }

    @Override
    public void attackMelee(GameSession session) {
        System.out.println(getName() + " موج صوتی مساحتی ایجاد کرد! (دمیج: " + currentDamage + " | استیج: " + stage + ")");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) {
            this.setMaxHealth(this.getMaxHealth() + 200);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 3) {
            this.currentDamage += 15;
            this.damageStg2 += 15;
            this.damageStg3 += 15;
        }
        if (level >= 4) this.maxStage += 1; // Max Size +1 (نیاز به پیاده سازی استیج 4 در آینده)
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}