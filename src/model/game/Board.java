package model.game;

public class Board {

    public static final int ROWS = 5;
    public static final int COLS = 9;

    private final Tile[][] tiles = new Tile[ROWS][COLS];
    private final boolean[] lawnMowerUsed = new boolean[ROWS];
    private final boolean[] lawnMowerAvailable = new boolean[ROWS];

    public Board() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                tiles[r][c] = new Tile(r, c);
            }
            lawnMowerAvailable[r] = true;
        }
    }

    public Tile getTile(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            return null;
        }
        return tiles[row][col];
    }

    public boolean isLawnMowerUsed(int row) {
        return lawnMowerUsed[row];
    }

    public boolean isLawnMowerAvailable(int row) {
        return lawnMowerAvailable[row];
    }

    /** @return true اگر ماشین چمن‌زنی این ردیف بار اول فعال شود (زامبی‌ها کشته می‌شوند)، false اگر باخت رخ دهد */
    public boolean triggerLawnMower(int row) {
        if (!lawnMowerUsed[row]) {
            lawnMowerUsed[row] = true;
            return true;
        }
        return false;
    }
}
