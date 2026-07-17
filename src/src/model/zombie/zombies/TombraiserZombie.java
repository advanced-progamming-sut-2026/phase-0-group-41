package model.zombie.zombies;

import model.game.Board;
import model.game.GameSession;
import model.game.TerrainType;
import model.game.Tile;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TombraiserZombie extends Zombie {

    private static final int THROW_COOLDOWN_TICKS = 50; // مثلا هر ۵ ثانیه
    private int ticksSinceLastThrow = 0;
    private final Random random = new Random();

    public TombraiserZombie() {
        super("tombraiser", 300, 0.01, 150, 10);
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        ticksSinceLastThrow++;
        if (ticksSinceLastThrow >= THROW_COOLDOWN_TICKS) {
            throwBone(session);
            ticksSinceLastThrow = 0; // ریست کردن تایمر
        }

        // حرکت و حمله مثل یک زامبی معمولی (می‌توانید متدهای NormalZombie را اینجا کپی یا از آن ارث ببرید)
        moveOrAttack(session);
    }

    private void throwBone(GameSession session) {
        Board board = session.getBoard();
        List<Tile> emptyTiles = new ArrayList<>();

        // پیدا کردن تمام خانه‌های خالی زمین برای پرتاب استخوان
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Tile tile = board.getTile(r, c);
                if (tile.isEmpty() && tile.getTerrainType() == TerrainType.NORMAL) {
                    emptyTiles.add(tile);
                }
            }
        }

        // اگر خانه خالی بود، یک قبر رندوم بساز
        if (!emptyTiles.isEmpty()) {
            Tile targetTile = emptyTiles.get(random.nextInt(emptyTiles.size()));
            targetTile.setTerrainType(TerrainType.GRAVE);
        }
    }

    private void moveOrAttack(GameSession session) {
        int col = (int) Math.floor(getXPosition());
        Tile tile = session.getBoard().getTile(getRow(), col);
        if (tile != null && tile.getPlant() != null) {
            tile.getPlant().takeDamage(getDamagePerTick());
            setEating(true);
        } else {
            setXPosition(getXPosition() - getSpeed());
            setEating(false);
        }
    }
}