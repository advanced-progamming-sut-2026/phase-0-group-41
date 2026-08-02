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

    private int actionInterval = 20; // 2 ثانیه
    private int tickCounter = 0;

    private int currentSunCost = 175;
    private int currentCooldown = 50;
    private int level = 1;

    public Kiwibeast() {
        super("kiwibeast", PlantType.MELEE_ATTACKER, 175, 50, 300, PlantTag.AOE, PlantTag.WRAMP_UP);
    }

    @Override
    public void onTick(GameSession session) {
        // === تغییرات اینجاست ===
        if (isFrozenSolid()) {
            handleIceMelting(session);
            return;
        }
        if (isTransformedToCat() || isOctopused()) return;
        // =======================

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

        tickCounter += 1;
        if (tickCounter >= actionInterval) {
            attackMelee(session);
            tickCounter -= actionInterval;
        }
    }

    @Override
    public void attackMelee(GameSession session) {
        boolean hit = false;
        // اسکن تمام زامبی‌های بازی برای پیدا کردن آن‌هایی که در شعاع ۳x۳ هستند
        for (model.zombie.Zombie z : session.getAliveZombies()) {
            if (!z.isDead() && Math.abs(z.getRow() - getRow()) <= 1 && Math.abs(z.getXPosition() - getCol()) <= 1.5) {
                z.takeDamage(currentDamage, model.zombie.DamageType.NORMAL);
                hit = true;
            }
        }
        if (hit) System.out.println(getName() + " ضربه صوتی مساحتی وارد کرد! (دمیج: " + currentDamage + ")");
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