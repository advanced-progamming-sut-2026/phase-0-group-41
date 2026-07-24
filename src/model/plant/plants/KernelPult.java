package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class KernelPult extends Plant implements IShooter {

    private int damageCorn = 20;
    private int damageButter = 40;
    private double butterChance = 0.25; // شانس پایه 25 درصد
    private int shootInterval = 29; // 2.9 ثانیه
    private int tickCounter = 0;
    private int level = 1;

    public KernelPult() {
        super("kernelpult", PlantType.LOBBER, 100, 5, 300);
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
            System.out.println("Plant Food: پرتاب کره به سر تمامی زامبی‌های زمین!");
            decayFeedEffect();
            return;
        }

        tickCounter++;
        if (tickCounter >= shootInterval) {
            shoot(session);
            tickCounter = 0;
        }
    }

    @Override
    public void shoot(GameSession session) {
        if (Math.random() < butterChance) {
            System.out.println(getName() + " کَره پرتاب کرد! (دمیج " + damageButter + " + توقف زامبی)");
        } else {
            System.out.println(getName() + " دانه ذرت پرتاب کرد. (دمیج " + damageCorn + ")");
        }
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.butterChance += 0.05; // Butter +5%
        if (level >= 3) {
            this.damageCorn += 10;
            this.damageButter += 10;
        }
        if (level >= 4) {
            this.setMaxHealth(this.getMaxHealth() + 150);
            this.setHealth(this.getMaxHealth());
        }
    }
}