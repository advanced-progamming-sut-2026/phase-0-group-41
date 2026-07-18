package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.zombie.Zombie;

/** تا ۱۵ تیک (۱.۵ ثانیه) پس از کاشت غیرفعال است (Arm)؛ سپس با نزدیک شدن زامبی منفجر می‌شود. */
public class PotatoMine extends Plant {

    private static final int ARM_TICKS = 15;
    private static final int DAMAGE = 1800;
    private int ticksAlive = 0;
    private boolean armed = false;
    private boolean exploded = false;

    public PotatoMine() {
        super("potatomine", PlantType.EXPLOSIVE, 25, 300, 300);
    }

    @Override
    public void onTick(GameSession session) {
        if (exploded) {
            return;
        }
        ticksAlive++;
        if (!armed && ticksAlive >= ARM_TICKS) {
            armed = true;
        }
        if (!armed) {
            return;
        }
        for (Zombie z : session.getAliveZombies()) {
            if (z.getRow() == getRow() && Math.abs(z.getXPosition() - getCol()) < 1) {
                z.takeDamage(DAMAGE);
                exploded = true;
                takeDamage(getMaxHealth());
                System.out.println("plant potatomine exploded at (" + getCol() + ", " + getRow() + ")");
                break;
            }
        }
    }
}
