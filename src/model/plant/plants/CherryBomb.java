package model.plant.plants;

import model.game.Board;
import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;

/** بلافاصله پس از کاشته شدن، در محدوده ۳x۳ منفجر می‌شود. */
public class CherryBomb extends Plant {

    private static final int DAMAGE = 1800;
    private boolean exploded = false;

    public CherryBomb() {
        super("cherrybomb", PlantType.EXPLOSIVE, 150, 500, 999);
    }

    @Override
    public void onTick(GameSession session) {
        if (exploded) {
            return;
        }
        exploded = true;
        List<Zombie> toDamage = new ArrayList<>();
        for (Zombie z : session.getAliveZombies()) {
            if (Math.abs(z.getRow() - getRow()) <= 1 && Math.abs(z.getXPosition() - getCol()) <= 1) {
                toDamage.add(z);
            }
        }
        for (Zombie z : toDamage) {
            z.takeDamage(DAMAGE);
        }
        takeDamage(getMaxHealth()); // بمب پس از انفجار از بین می‌رود
        System.out.println("plant cherrybomb exploded at (" + getCol() + ", " + getRow() + ")");
    }
}
