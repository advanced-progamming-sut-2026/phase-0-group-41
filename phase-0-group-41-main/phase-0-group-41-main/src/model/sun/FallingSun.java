package model.sun;

public class FallingSun {

    public enum Kind {
        NORMAL(25), SPECIAL(100), RADIOACTIVE(25);

        private final int value;

        Kind(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private final int id;
    private final int row;
    private final int col;
    private final Kind kind;
    private double ticksRemainingToLand = 50; // ۵ ثانیه = ۵۰ تیک
    private boolean landed = false;

    public FallingSun(int id, int row, int col, Kind kind) {
        this.id = id;
        this.row = row;
        this.col = col;
        this.kind = kind;
    }

    public int getId() {
        return id;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Kind getKind() {
        return kind;
    }

    public boolean isLanded() {
        return landed;
    }

    public void tick() {
        if (!landed) {
            ticksRemainingToLand--;
            if (ticksRemainingToLand <= 0) {
                landed = true;
            }
        }
    }
}
