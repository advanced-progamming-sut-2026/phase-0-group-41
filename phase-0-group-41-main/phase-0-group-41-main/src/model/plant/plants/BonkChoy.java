package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IMeleeAttacker;

public class BonkChoy extends Plant implements IMeleeAttacker {

    private int damage = 15;
    private double actionInterval = 2.5; // 0.25 ثانیه
    private double tickCounter = 0;
    private int currentCooldown = 50;
    private int level = 1;

    public BonkChoy() {
        super("bonkchoy", PlantType.MELEE_ATTACKER, 150, 50, 300);
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
        model.zombie.Zombie target = null;
        double closestDist = 1.5; // برد مشت زدن (یک و نیم خانه)

        // پیدا کردن نزدیک‌ترین زامبی در ردیف خودش (جلو یا عقب)
        for (model.zombie.Zombie z : session.getAliveZombies()) {
            if (z.getRow() == getRow() && !z.isDead()) {
                double dist = Math.abs(z.getXPosition() - getCol());
                if (dist <= closestDist) {
                    closestDist = dist;
                    target = z;
                }
            }
        }

        if (target != null) {
            target.takeDamage(damage, model.zombie.DamageType.NORMAL);
            System.out.println(getName() + " به زامبی مشت زد! (دمیج: " + damage + ")");
        }
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}