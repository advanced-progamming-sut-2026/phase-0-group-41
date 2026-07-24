package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IMeleeAttacker;

public class Chomper extends Plant implements IMeleeAttacker {

    private int digestTimeTicks = 400; // 40 ثانیه
    private int digestCounter = 0;
    private boolean isDigesting = false;
    private int currentCooldown = 5;
    private int level = 1;

    public Chomper() {
        super("chomper", PlantType.MELEE_ATTACKER, 150, 5, 300);
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
            System.out.println("Plant Food: بلعیدن همزمان ۳ زامبی از فاصله دور!");
            // List<Zombie> targets = session.getRandomZombies(3);
            // for (Zombie z : targets) { z.takeDamage(9999); }
            this.isDigesting = true;
            this.digestCounter = 0;
            decayFeedEffect();
            return;
        }

        if (isDigesting) {
            digestCounter++;
            if (digestCounter >= digestTimeTicks) {
                isDigesting = false;
                digestCounter = 0;
                System.out.println(getName() + " هضم زامبی را تمام کرد و دوباره آماده حمله است!");
            }
        }
    }

    @Override
    public void attackMelee(GameSession session) {
        if (!isDigesting) {
            System.out.println(getName() + " بلعیدن آنی؛ نابودی یک زامبی درجا و شروع هضم!");
            isDigesting = true;
            digestCounter = 0;
        }
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.digestTimeTicks -= 20;
        if (level >= 3) {
            this.setMaxHealth(this.getMaxHealth() + 200);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 4) this.digestTimeTicks -= 30;
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}