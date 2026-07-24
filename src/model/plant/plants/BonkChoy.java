package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IMeleeAttacker;

public class BonkChoy extends Plant implements IMeleeAttacker {

    private int damage = 15;
    private double actionInterval = 2.5; // 0.25 ثانیه
    private double tickCounter = 0;
    private int currentCooldown = 5;
    private int level = 1;

    public BonkChoy() {
        super("bonkchoy", PlantType.MELEE_ATTACKER, 150, 5, 300);
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
            System.out.println("Plant Food: مشت زدن رگباری به شعاع ۳x۳ اطراف خود!");
            // اجرای منطق ضربه مساحتی در GameSession
            decayFeedEffect();
            return;
        }

        tickCounter += 1.0;
        if (tickCounter >= actionInterval) {
            attackMelee(session);
            tickCounter -= actionInterval;
        }
    }

    @Override
    public void attackMelee(GameSession session) {
        System.out.println(getName() + " ضربه فیزیکی به خانه‌های جلو و عقب زد! (دمیج: " + damage + ")");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.damage += 5;
        if (level >= 3) this.actionInterval *= 0.9;
        if (level >= 4) {
            this.setMaxHealth(this.getMaxHealth() + 200);
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}