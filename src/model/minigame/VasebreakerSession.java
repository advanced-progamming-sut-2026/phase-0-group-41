package model.minigame;

import model.game.Board;
import model.game.Tile;
import model.plant.Plant;
import model.plant.PlantFactory;
import model.user.User;
import model.zombie.Zombie;
import model.zombie.ZombieFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class VasebreakerSession extends MiniGameSession {

    public enum VaseType { NORMAL, GREEN, PURPLE }

    public static class DroppedSeedPacket {
        public String plantType;
        public int row;
        public int col;
        public int decayTicks;

        public DroppedSeedPacket(String type, int r, int c, int ticks) {
            this.plantType = type;
            this.row = r;
            this.col = c;
            this.decayTicks = ticks;
        }
    }

    // مقادیر بازگشتی برای ارتباط مدل با کنترلر
    public enum BreakResultStatus {
        INVALID_LOCATION, NO_VASE, GREEN_SEED, PURPLE_GARGANTUAR, NORMAL_ZOMBIE, NORMAL_SEED, NORMAL_EMPTY
    }

    public static class VaseBreakResult {
        public BreakResultStatus status;
        public String contentName; // نام گیاه یا زامبی خارج شده از کوزه

        public VaseBreakResult(BreakResultStatus status, String contentName) {
            this.status = status;
            this.contentName = contentName;
        }
    }

    public enum PlantSeedResult {
        SUCCESS, NO_SEED, INVALID_TARGET
    }

    private final VaseType[][] vases = new VaseType[Board.ROWS][Board.COLS];
    private final List<DroppedSeedPacket> droppedSeeds = new ArrayList<>();
    
    // لیستی برای اطلاع دادن به کنترلر درباره بذرهایی که همین الان به دلیل گذر زمان ناپدید شدند
    private final List<DroppedSeedPacket> recentlyDecayedSeeds = new ArrayList<>();
    private final Random random = new Random();

    public VasebreakerSession(User user) {
        super(user, 1);
        initializeVases();
    }

    private void initializeVases() {
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 4; c < Board.COLS; c++) {
                int chance = random.nextInt(100);
                if (chance < 5) vases[r][c] = VaseType.PURPLE;
                else if (chance < 25) vases[r][c] = VaseType.GREEN;
                else vases[r][c] = VaseType.NORMAL;
            }
        }
    }

    public VaseBreakResult breakVase(int row, int col) {
        if (row < 0 || row >= Board.ROWS || col < 0 || col >= Board.COLS) {
            return new VaseBreakResult(BreakResultStatus.INVALID_LOCATION, null);
        }
        if (vases[row][col] == null) {
            return new VaseBreakResult(BreakResultStatus.NO_VASE, null);
        }

        VaseType type = vases[row][col];
        vases[row][col] = null; // کوزه شکسته شد

        if (type == VaseType.GREEN) {
            String plant = getRandomPlant();
            dropSeedPacket(plant, row, col);
            return new VaseBreakResult(BreakResultStatus.GREEN_SEED, plant);
        } else if (type == VaseType.PURPLE) {
            spawnZombie("gargantuar", row, col);
            return new VaseBreakResult(BreakResultStatus.PURPLE_GARGANTUAR, "gargantuar");
        } else {
            int outcome = random.nextInt(100);
            if (outcome < 40) {
                String z = getRandomBasicZombie();
                spawnZombie(z, row, col);
                return new VaseBreakResult(BreakResultStatus.NORMAL_ZOMBIE, z);
            } else if (outcome < 80) {
                String p = getRandomPlant();
                dropSeedPacket(p, row, col);
                return new VaseBreakResult(BreakResultStatus.NORMAL_SEED, p);
            } else {
                return new VaseBreakResult(BreakResultStatus.NORMAL_EMPTY, null);
            }
        }
    }

    public PlantSeedResult plantDroppedSeed(int seedRow, int seedCol, int targetRow, int targetCol) {
        DroppedSeedPacket packet = null;
        for (DroppedSeedPacket ds : droppedSeeds) {
            if (ds.row == seedRow && ds.col == seedCol) {
                packet = ds;
                break;
            }
        }

        if (packet == null) {
            return PlantSeedResult.NO_SEED;
        }

        Tile targetTile = getBoard().getTile(targetRow, targetCol);
        if (targetTile == null || !targetTile.canPlantDirectly() || !targetTile.isEmpty()) {
            return PlantSeedResult.INVALID_TARGET;
        }

        Plant plant = PlantFactory.create(packet.plantType);
        plant.place(targetRow, targetCol);
        targetTile.setPlant(plant);
        droppedSeeds.remove(packet);
        
        return PlantSeedResult.SUCCESS;
    }

    private void dropSeedPacket(String plantType, int row, int col) {
        droppedSeeds.add(new DroppedSeedPacket(plantType, row, col, 100)); // ۱۰۰ تیک مهلت کاشت
    }

    private void spawnZombie(String type, int row, int col) {
        Zombie z = ZombieFactory.create(type);
        z.spawn(row, col);
        getAliveZombies().add(z);
    }

    // متدی برای کنترلر تا بذرهای ناپدید شده را بررسی و چاپ کند
    public List<DroppedSeedPacket> pollDecayedSeeds() {
        List<DroppedSeedPacket> copy = new ArrayList<>(recentlyDecayedSeeds);
        recentlyDecayedSeeds.clear();
        return copy;
    }

    @Override
    protected void customMiniGameTick() {
        getFallingSuns().clear();

        Iterator<DroppedSeedPacket> it = droppedSeeds.iterator();
        while (it.hasNext()) {
            DroppedSeedPacket ds = it.next();
            ds.decayTicks--;
            if (ds.decayTicks <= 0) {
                recentlyDecayedSeeds.add(ds); // اضافه کردن به لیست اتفاقات برای کنترلر
                it.remove();
            }
        }
    }

    private String getRandomPlant() {
        String[] plants = {"peashooter", "snowpea", "squash", "potatomine", "repeater"};
        return plants[random.nextInt(plants.length)];
    }

    private String getRandomBasicZombie() {
        String[] zombies = {"normal", "conehead", "buckethead", "jester"};
        return zombies[random.nextInt(zombies.length)];
    }
}