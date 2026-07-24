package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class MelonPult extends Plant implements IShooter {
    private int damage = 80;
    private int aoeDamageBonus = 0;
    private double actionInterval = 29.0;
    private double tickCounter = 0;
    private int currentSunCost = 325;
    private int currentCooldown = 5;
    private int level = 1;

    public MelonPult() {
        super("melonpult", PlantType.LOBBER, 325, 5, 300, PlantTag.AOE);
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
            System.out.println("Plant Food: پرتاب هندوانه غول‌پیکر به زامبی‌های تصادفی!");
            decayFeedEffect();
            return;
        }
        tickCounter += 1.0;
        if (tickCounter >= actionInterval) {
            shoot(session);
            tickCounter -= actionInterval;
        }
    }

    @Override
    public void shoot(GameSession session) {
        int totalAoe = (damage / 2) + aoeDamageBonus;
        System.out.println(getName() + " هندوانه سنگین پرتاب کرد! (دمیج: " + damage + " | اسپلش: " + totalAoe + ")");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentSunCost -= 25;
        if (level >= 3) this.aoeDamageBonus += 15;
        if (level >= 4) this.damage += 30;
    }
    @Override
    public int getSunCost() { return currentSunCost; }
    @Override
    public int getCooldownTicks() { return currentCooldown; }
}