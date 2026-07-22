package model.zombie.zombies;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class NormalZombie extends Zombie {

    private static final double SPEED_PER_TICK = 0.01;
    private static final int DAMAGE = 10;

    public NormalZombie() {
        super("normal", 200, SPEED_PER_TICK * 10, 100, DAMAGE);
    }

    protected NormalZombie(String typeName, int health, int waveCost, int damage) {
        super(typeName, health, SPEED_PER_TICK * 10, waveCost, damage);
    }

    @Override
    public void onTick(GameSession session) {
        Board board = session.getBoard();
        int col = (int) Math.floor(getXPosition());
        Tile tile = board.getTile(getRow(), Math.max(col, 0));
        Plant plant = (tile != null) ? tile.getPlant() : null;
        if (plant != null && !plant.isDead() && Math.abs(getXPosition() - col) < 0.5) {
            setEating(true);
            plant.takeDamage(DAMAGE);
            if (plant.isDead()) {
                System.out.println("Plant " + plant.getName() + " at (" + plant.getCol() + ", " + plant.getRow() + ") is destroyed.");
                tile.setPlant(null);
                setEating(false);
            }
        } else {
            setEating(false);
            setXPosition(getXPosition() - SPEED_PER_TICK);
        }
    }
}
