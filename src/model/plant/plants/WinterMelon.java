package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class WinterMelon extends Plant implements IShooter {

    private int damage = 80;
    private int aoeDamageBonus = 0;
    private int shootInterval = 29;
    private int tickCounter = 0;
    private int currentSunCost = 500;
    private int level = 1;

    public WinterMelon() {
        super("wintermelon", PlantType.LOBBER, 500, 5, 300, PlantTag.ICE, PlantTag.AOE);
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
            System.out.println("Plant Food: پرتاب هندوانه یخی سنگین به زامبی‌های تصادفی!");
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
        int totalAoe = (damage / 2) + aoeDamageBonus; // فرض بر اینکه دمیج مساحتی نصف دمیج اصلی است
        System.out.println(getName() + " هندوانه یخی پرتاب کرد! (دمیج: " + damage + " | اسپلش دمیج: " + totalAoe + " + کندکننده)");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentSunCost -= 50;
        if (level >= 3) this.aoeDamageBonus += 15; // AoE Dmg +15
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() { return currentSunCost; }
}